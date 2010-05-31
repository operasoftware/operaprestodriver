/* Copyright (C) 2009-2010 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.stp;

import com.opera.core.systems.util.SocketListener;
import com.opera.core.systems.util.SocketMonitor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriverException;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import com.opera.core.systems.scope.handlers.AbstractEventHandler;
import com.opera.core.systems.scope.handlers.IConnectionHandler;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Error;
import com.opera.core.systems.scope.protos.UmsProtos.Event;
import com.opera.core.systems.scope.protos.UmsProtos.Response;

public class StpConnection implements SocketListener {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private SocketChannel socketChannel;

    // Outgoing send queue
    private final ArrayBlockingQueue<ByteBuffer> requests;
    private ByteBuffer recvBuffer;
    private byte[] remainingBytes = new byte[0];

    // For STP1
    private Response stpResponse;
    byte[] prefix = { 'S', 'T', 'P', 1 };
    private ByteString stpPrefix = ByteString.copyFrom(prefix);
    private String serviceMessage;

    private AbstractEventHandler eventHandler;
    private UmsEventParser stp1EventHandler;
    private IConnectionHandler connectionHandler;

    public enum State {
            SERVICELIST, HANDSHAKE, EMPTY, STP, STP_SIZE, STP_DATA;
    }

    public Response getStpResponse() {
            return stpResponse;
    }

    private int messageSize = 0;
    private int messageType = 0;

    private State state = State.SERVICELIST;


    /**
     * Sets the event handler and creates the event parsers
     * @param eventHandler
     */
    
    private void setEventHandler(final AbstractEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        stp1EventHandler = new UmsEventParser(eventHandler);
    }

    private void setState(State state)
    {
        // logger.fine("Setting state: " + state.toString());
        this.state = state;
    }


    public boolean isConnected() {
            if (socketChannel == null)
                    return false;

            return true;
    }

    public ArrayBlockingQueue<ByteBuffer> getRequests() {
            return requests;
    }

    @Override
    public void finalize() throws Throwable {
        if (socketChannel != null)
            close();
        super.finalize();
    }

    /**
     * Initializes variables in object scope, sets 'count known' to false to
     * read byte count (STP/0).
     *
     * @param hostAddress Adress to listen on, localhost if null
     * @param port Port to bind to
     */
    public StpConnection(SocketChannel socket, IConnectionHandler handler, AbstractEventHandler eventHandler) throws IOException {
        connectionHandler = handler;
        socketChannel = socket;
        this.eventHandler = eventHandler;
        requests = new ArrayBlockingQueue<ByteBuffer>(1024);
        recvBuffer = ByteBuffer.allocateDirect(65536);

        socket.configureBlocking(false);

        SocketMonitor.instance().add(socket, this, SelectionKey.OP_READ);
        
        if (!handler.onConnected(this))
        {
            close();
            throw new IOException("Connection not allowed from IConnectionHandler (already connected)");
        }
        
        
    }

    private void switchToStp1() {
        stp1EventHandler = new UmsEventParser(eventHandler);
        sendEnableStp1();
        setState(State.HANDSHAKE);
    }

    /**
     * Queues up an STP/1 message sent from another thread and wakes up selector to
     * register it to the key
     *
     * @param message to add to the request queue
     * @throws IOException
     */
    public void send(Command command) {
        logger.log(Level.FINEST, command.toString());
        byte[] payload = command.toByteArray();
        int totalSize = payload.length + 1; //increment 1 for message type

        ByteBuffer outMessageSize = encodeMessageSize(totalSize);

        // create the message
        ByteBuffer buffer = ByteBuffer.allocateDirect(prefix.length + outMessageSize.position() + 1 + payload.length);
        buffer.put(prefix, 0, prefix.length);
        outMessageSize.flip();
        buffer.put(outMessageSize);
        buffer.put((byte) 1);
        buffer.put(payload);

        // log what is being sent.
        logger.fine("SEND: " + command.toString());

        requests.add(buffer);
        SocketMonitor.instance().modify(socketChannel, this, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void sendEnableStp1() {
        send("*enable-stp1");
    }

    public void sendQuit() {
        send("*quit");
    }

    /**
     * Queues up an STP/0 message sent from another thread and wakes up selector to
     * register it to the key
     *
     * @param message to add to the request queue
     */
    @Deprecated
    public void send(String message) {
        String scopeMessage = message.length() + " " + message;
        logger.log(Level.INFO, "WRITE : " + scopeMessage);
        byte[] bytes = null;
        try {
                bytes = scopeMessage.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException e) {
                close();
                connectionHandler.onException(e);
                return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        requests.add(buffer);
        SocketMonitor.instance().modify(socketChannel, this, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    /**
     * Reads an incoming message into a buffer, cleans up on exception (host
     * abort connection) or disconnect. A message is an UTF-16 string, each
     * byte is used in {@link CharBuffer} and read into {@link StringBuilder},
     * space is the seperator in STP/0
     *
     * @param key
     */
    public boolean canRead(SelectableChannel channel) throws IOException {
        logger.info("canRead");
        ByteBuffer buffer = recvBuffer; // FIXME
        int numRead = 0;

        try {
            numRead = socketChannel.read(buffer);
            logger.fine("Read " + numRead + " bytes, state=" + state.toString());

        } catch (IOException e) {
            logger.log(Level.WARNING, "Channel closed, causing exception", e.getMessage());
            numRead = -1;
        }

        if (numRead < 0) {
                logger.log(Level.INFO, "Channel closed : {0}", socketChannel.socket().getInetAddress().getHostName());
                connectionHandler.onDisconnect();
                SocketMonitor.instance().remove(socketChannel);
                return false;
        }

        readMessage(buffer, numRead, false);
        return true;
    }

    /**
     * Tries to write the message, the buffer is attached to the selection key
     * If the write fails (and there is a partial write) we compact the buffer
     * and attach it to key again (avoiding new write events). If there are
     * requests waiting in the queue, we wake up the selector so they can be
     * registered
     *
     * TODO Can't we register them at one go?
     *
     * @param key
     * @throws IOException
     */
    public boolean canWrite(SelectableChannel channel) throws IOException
    {
        logger.info("canWrite");
        int totalWritten = 0;
        while (!requests.isEmpty())
        {
            ByteBuffer buffer = requests.poll();
            buffer.flip();
            int written = 0;

            do
            {
                written = socketChannel.write(buffer);
                if(written <= 0)
                    break;
                if (written > 0)
                    totalWritten += written;
            } while(buffer.hasRemaining());
        }

        logger.fine("Wrote " + totalWritten + " bytes");

        return (!requests.isEmpty());
    }


    /**
     * Switches the wait state and wakes up the selector to process
     */
    public void close() {
        SocketMonitor.instance().remove(socketChannel);
        try {
            socketChannel.close();
        } catch (IOException ignored) {
            /* nothing to be done */
        } finally {
            socketChannel = null;
        }
    }


    /**
     * Processes an incoming message and passes it to event handler if needed,
     * the following events are to our interest:
     * Runtime-Started : ecmascript runtime starts in Opera (that we can inject to)
     * Runtime-Stopped : ecmascript runtime stops (not used, buggy)
     * Message: fired from console log event
     * Updated-Window: a window is updated OR created (opener-id=0)
     * Active-Window: window focus changed
     * Window-Closed: self explanatory
     * If message matches none it is added to the response queue
     * (probably response to command)
     *
     * @param message
     */
    public void parseServiceList(String message) {
        logger.fine("parseServiceList: \"" + message + "\"");

        int split = message.indexOf(" ");

        if (split < 0) {
           connectionHandler.onException(new WebDriverException("Invalid service list received."));
           return;
        }

        List<String> services  = Arrays.asList(message.substring(split+1).split(","));
        serviceMessage = message;
        connectionHandler.onServiceList(services);
        logger.info("Service List OK.");

        if (!services.contains("stp-1"))
        {
            connectionHandler.onException(new WebDriverException("Stp-1 is not supported!"));
            return;
        }

        switchToStp1();
    }
    
    public String getServiceMessage()
    {
        return serviceMessage;
    }
    

    private void signalResponse(boolean success, int tag)
    {
        connectionHandler.onResponseReceived(success, tag);
    }

    private void signalEvent(Event event)
    {
        logger.fine("EVENT " + event.toString());
        stp1EventHandler.handleEvent(event);
    }

    private void readMessage(ByteBuffer buffer, int bytesRead, boolean recurse) {

        logger.finest("readMessage: " + bytesRead + " bytes read, remaining=" + remainingBytes.length + ", state=" + state.toString() + ", recurse=" + recurse);

        if (remainingBytes.length > 0) {
            bytesRead = remainingBytes.length + bytesRead;
            buffer.flip();
            ByteBuffer swap = ByteBuffer.allocateDirect(bytesRead);
            swap.put(remainingBytes);
            swap.put(buffer);
            remainingBytes = new byte[0];
            buffer.clear();
            buffer = swap;
        }

        buffer.flip();


        switch (state) {
            case SERVICELIST:
                StringBuilder builder = new StringBuilder();
                builder.append(buffer.asCharBuffer());
                buffer.clear();
                parseServiceList(builder.toString());
                break;
                
            case HANDSHAKE:
                if(bytesRead >= 6) {
                    byte[] dst = new byte[6];
                    buffer.get(dst);
                    String handShake = new String(dst);
                    if(!handShake.equals("STP/1\n")) {
                        close();
                        connectionHandler.onException(new WebDriverException("Scope Transport Protocol Error : Handshake"));
                    }
                    buffer.clear();
                    setState(State.EMPTY);
                    connectionHandler.onHandshake(true);
                    break;
                }
                bufferRemaining(buffer, bytesRead);
                break;

            case EMPTY: /* read 4 byte header: STP\0 */
                if(bytesRead >= 4) {
                    byte[] headerPrefix = new byte[4];
                    buffer.get(headerPrefix);
                    ByteString incomingPrefix = ByteString.copyFrom(headerPrefix);
                    if(stpPrefix.equals(incomingPrefix)){
                        setState(State.STP);
                        if(buffer.hasRemaining()){
                            buffer.compact();
                            readMessage(buffer, buffer.position(), true);
                        } else {
                            buffer.clear();
                        }
                    } else {
                        close();
                        connectionHandler.onException(new WebDriverException("Scope Transport Protocol Error : Header"));
                    }
                    break;
                }
                // we got less than 4
                bufferRemaining(buffer, bytesRead);
                break;

            case STP:
                //this one needs more error handling
                messageSize = readRawVarint32(buffer);
                setState(State.STP_DATA);
                if (buffer.hasRemaining()) {
                        buffer.compact();
                        readMessage(buffer, buffer.position(), true);
                } else {
                        //we have no more bytes, clear the buffer so we can read more
                        buffer.clear();
                }
                break;

            case STP_DATA:
                if(bytesRead >= messageSize) {
                    messageType = buffer.get();
                    byte[] payload = new byte[--messageSize];
                    buffer.get(payload);
                    setState(State.EMPTY);
                    try {
                        processMessage(messageType, payload);
                    } catch (IOException e) {
                        close();
                        connectionHandler.onException(new WebDriverException("Error while processing the message: "  + e.getMessage()));
                    }
                    if(buffer.hasRemaining()){
                        buffer.compact();
                        readMessage(buffer, buffer.position(), true);
                        break;
                    } else {
                        buffer.clear();
                        break;
                    }
                }
                // if we got less than expected, lets keep reading
                bufferRemaining(buffer, bytesRead);
                break;
        }

    }

    private void bufferRemaining(ByteBuffer buffer, int read) {
            if(read > 0) {
                logger.warning("Partial read, might lose data here!");
                    remainingBytes = new byte[read];
                    buffer.get(remainingBytes);
                    buffer.clear();
            }
    }

    private void processMessage(int stpType, byte[] payload) throws IOException {

            logger.finest("processMessage: " + stpType);

            switch (stpType) {
            /*
             * case 1://command //commands are not supposed to be received
             * throw new WebDriverException("Received command from host?");
             */
            case 2:// response
                    stpResponse = Response.parseFrom(payload);
                    logger.log(Level.FINEST, stpResponse.toString());
                    signalResponse(true, stpResponse.getTag());
                    break;
            case 3:// event
                    Event event = Event.parseFrom(payload);
                    signalEvent(event);
                    break;
            case 4:// error
                    Error error = Error.parseFrom(payload);
                    if (error.getService().equals("ecmascript-debugger") && error.getStatus() == Status.INTERNAL_ERROR.getCode()) {
                            stpResponse = null;
                            // signalResponse(false, "ES Error - " + error.toString());
                            signalResponse(false, error.getTag());
                    } else {
                            logger.log(Level.SEVERE, "Error : {0}", error.toString());
                            stpResponse = null;
                            // signalResponse(false, "Error - " + error.toString());
                            connectionHandler.onException(new WebDriverException("Error on command : " + error.toString() ));
                    }
                    break;
            default:
                    connectionHandler.onException(new WebDriverException("Unhandled STP type: " + stpType));
            }
    }

    // protobuf methods, taken from the protobuf library by Google
    private int readRawVarint32(ByteBuffer bytes) {
        byte tmp = bytes.get();
        if (tmp >= 0) {
          return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = bytes.get()) >= 0) {
          result |= tmp << 7;
        } else {
          result |= (tmp & 0x7f) << 7;
          if ((tmp = bytes.get()) >= 0) {
            result |= tmp << 14;
          } else {
            result |= (tmp & 0x7f) << 14;
            if ((tmp = bytes.get()) >= 0) {
              result |= tmp << 21;
            } else {
              result |= (tmp & 0x7f) << 21;
              result |= (tmp = bytes.get()) << 28;
              if (tmp < 0) {
                // Discard upper 32 bits.
                for (int i = 0; i < 5; i++) {
                  if (bytes.get() >= 0) return result;
                }
                connectionHandler.onException(new WebDriverException("Error while reading raw int"));
              }
            }
          }
        }
        return result;
    }

    private ByteBuffer encodeMessageSize(int value) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(CodedOutputStream.computeRawVarint32Size(value));
        while (true) {
            if ((value & ~0x7F) == 0) {
                buffer.put((byte) (value));
                return buffer;
            } else {
                buffer.put((byte) ((value & 0x7F) | 0x80));
                value >>>= 7;
            }
        }
    }
}
