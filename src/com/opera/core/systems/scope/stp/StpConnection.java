/*
Copyright 2008-2012 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems.scope.stp;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;

import com.opera.core.systems.scope.exceptions.ScopeException;
import com.opera.core.systems.scope.handlers.ConnectionHandler;
import com.opera.core.systems.scope.stp.services.messages.desktop.DesktopUtilsMessage;
import com.opera.core.systems.scope.exceptions.CommunicationException;
import com.opera.core.systems.scope.handlers.EventHandler;
import com.opera.core.systems.scope.protos.UmsProtos.Command;
import com.opera.core.systems.scope.protos.UmsProtos.Error;
import com.opera.core.systems.scope.protos.UmsProtos.Event;
import com.opera.core.systems.scope.protos.UmsProtos.Response;
import com.opera.core.systems.scope.protos.UmsProtos.Status;
import com.opera.core.systems.internal.SocketListener;
import com.opera.core.systems.internal.SocketMonitor;
import com.opera.core.systems.scope.internal.OperaIntervals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class StpConnection implements SocketListener {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private SocketChannel socketChannel;

  // Outgoing send queue
  private final ArrayBlockingQueue<ByteBuffer> requests;
  private ByteBuffer recvBuffer;

  // For STP1
  private final byte[] prefix = {'S', 'T', 'P', 1};
  private ByteString stpPrefix = ByteString.copyFrom(prefix);

  private EventHandler eventHandler;
  private UmsEventParser stp1EventHandler;
  private ConnectionHandler connectionHandler;

  public enum State {
    SERVICELIST, HANDSHAKE, EMPTY, STP
  }

  private State state = State.SERVICELIST;
  private SocketMonitor monitor;

  private void setState(State state) {
    this.state = state;
  }

  public boolean isConnected() {
    return (socketChannel != null);
  }

  /*
  @Override
  public void finalize() throws Throwable {
    logger.severe("STPConnection cleanup");
      if (socketChannel != null && socketChannel.isOpen()) {
          close();
      }
      super.finalize();
  }
  */

  /**
   * Initializes variables in object scope, sets 'count known' to false to read byte count (STP/0).
   */
  public StpConnection(SocketChannel socket, ConnectionHandler handler, EventHandler eventHandler,
                       SocketMonitor monitor)  throws IOException {
    connectionHandler = handler;
    socketChannel = socket;
    this.eventHandler = eventHandler;
    this.monitor = monitor;
    requests = new ArrayBlockingQueue<ByteBuffer>(1024);
    recvBuffer = ByteBuffer.allocateDirect(65536);
    recvBuffer.limit(0);

    socket.configureBlocking(false);

    monitor.add(socket, this, SelectionKey.OP_READ);

    if (!handler.onConnected(this)) {
      close();
      throw new IOException(
          "Connection not allowed from ConnectionHandler (already connected)");
    }
  }

  private void switchToStp1() {
    stp1EventHandler = new UmsEventParser(eventHandler);
    sendEnableStp1();
    setState(State.HANDSHAKE);
  }

  /**
   * Queues up an STP/1 message sent from another thread and wakes up selector to register it to the
   * key.
   *
   * @param command to add to the request queue
   */
  public void send(Command command) {
    logger.finest(command.toString());
    byte[] payload = command.toByteArray();
    int totalSize = payload.length + 1; // increment 1 for message type

    ByteBuffer outMessageSize = encodeMessageSize(totalSize);

    // create the message
    ByteBuffer buffer = ByteBuffer.allocateDirect(prefix.length
                                                  + outMessageSize.position() + 1 + payload.length);
    buffer.put(prefix, 0, prefix.length);
    outMessageSize.flip();
    buffer.put(outMessageSize);
    buffer.put((byte) 1);
    buffer.put(payload);

    // Log what is being sent.
    logger.finest("SEND: " + command);

    requests.add(buffer);
    monitor.modify(socketChannel, this, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
  }

  public void sendEnableStp1() {
    send("*enable stp-1");
  }

  public void sendQuit() {
    send("*quit");
  }

  /**
   * Queues up an STP/0 message sent from another thread and wakes up selector to register it to the
   * key.
   *
   * @param message to add to the request queue
   */
  private void send(String message) {
    String scopeMessage = message.length() + " " + message;
    logger.finest("WRITE : " + scopeMessage);
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
    monitor.modify(socketChannel, this, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
  }

  public boolean canRead(SelectableChannel channel) throws IOException {
    logger.finest("canRead");

    if (!channel.isOpen()) {
      return false;
    }

    if (socketChannel == null) {
      throw new IOException("Socket lost");
    }

    // Read as much data as possible into buffer.
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(1000);
    int readSize = 0;

    // Continue to read data and read messages until there are no messages
    boolean didNotFindAnyMessage = false;
    while (!didNotFindAnyMessage) {
      logger.finest("canReadLoop!");

      // Read as much data as possible (until recvBuffer is full OR we don't get
      // any more data)
      do {
        readBuffer.clear();
        try {
          // do we have a socket
          if (socketChannel == null) {
            readSize = -1;

            // do we have room in our buffer?
          } else {

            readSize = socketChannel.read(readBuffer);

            if (readSize == 0) {
                 try {
                     Thread.sleep(OperaIntervals.SOCKET_READ_RETRY_TIMEOUT.getMs());
                     readSize = socketChannel.read(readBuffer);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (readSize > 0) {
              readBuffer.limit(readSize);
              readBuffer.position(0);
            }

          }

        } catch (IOException e) {
          logger.warning("Channel closed, causing exception: " + e.getMessage());
          readSize = -1;  // same as error from socketChannel.read
        }

        if (readSize < 0) {
          logger.finer(String.format("Channel closed: %s", socketChannel != null ? socketChannel
              .socket().getInetAddress().getHostName() : "(unknown)"));

          connectionHandler.onDisconnect();
          monitor.remove(socketChannel);
          return false;
        } else if (readSize > 0) {

          // Double buffer size if needed!
          if (recvBuffer.limit() + readBuffer.limit() >= recvBuffer.capacity()) {
            logger.finest("Doubled the size of our recv buffer");
            ByteBuffer newRecvBuffer = ByteBuffer.allocate(recvBuffer.capacity() * 2);
            newRecvBuffer.clear();
            recvBuffer.position(0);
            newRecvBuffer.limit(recvBuffer.limit());
            newRecvBuffer.position(0);
            newRecvBuffer.put(recvBuffer);
            newRecvBuffer.position(0);
            recvBuffer = newRecvBuffer;
          }

          recvBuffer.position(recvBuffer.limit());
          recvBuffer.limit(recvBuffer.limit() + readSize);// increase limit!
          recvBuffer.put(readBuffer);
          logger.finest(String.format("Read %d bytes, new buffer size is %d",
                                      readSize, recvBuffer.limit()));
        }
      } while (readSize > 0);

      // Read as many messages as possible, and only
      didNotFindAnyMessage = true;
      while (readMessage(recvBuffer)) {
        didNotFindAnyMessage = false;
      }
    }
    return true;
  }

  public boolean canWrite(SelectableChannel channel) throws IOException {
    if (socketChannel == null) {
      throw new IOException("Socket lost");
    }

    int totalWritten = 0;
    while (!requests.isEmpty()) {
      ByteBuffer buffer = requests.poll();
      buffer.flip();
      int written = 0;

      do {
        written = socketChannel.write(buffer);
        //for now assume written equals 0 only when previous write has not finished yet so retry will be successful and thus no endless loop
        if (written < 0) {
          break;
        }
        if (written > 0) {
          totalWritten += written;
        }
      } while (buffer.hasRemaining());
    }

    logger.finest("Wrote " + totalWritten + " bytes");

    return (!requests.isEmpty());
  }

  /**
   * Switches the wait state and wakes up the selector to process.
   */
  public void close() {
    if (socketChannel == null) {
      return;
    }

    monitor.remove(socketChannel);
    try {
      socketChannel.close();
    } catch (IOException ignored) {
      /* nothing to be done */
    } finally {
      socketChannel = null;
    }
  }

  /**
   * Processes an incoming message and passes it to event handler if needed, the following events
   * are to our interest:
   *
   * Runtime-Started: ecmascript runtime starts in Opera (that we can inject to)
   *
   * Runtime-Stopped: ecmascript runtime stops (not used, buggy)
   *
   * Message: fired from console log event
   *
   * Updated-Window: a window is updated OR created (opener-id=0)
   *
   * Active-Window: window focus changed
   *
   * Window-Closed: self explanatory If message matches none it is added to the response queue
   * (probably response to command).
   */
  public void parseServiceList(String message) {
    // We expect the service list to be in this format:
    //   *245 service-list window-manager,core,ecmascript-service
    List<String> services;

    try {
      services = ImmutableList.copyOf(Splitter.on(',').split(message.split(" ")[2]));
    } catch (ArrayIndexOutOfBoundsException e) {
      connectionHandler.onException(
          new IllegalStateException(String.format("Invalid service list received: %s", message)));
      return;
    }

    logger.fine(String.format("Available services: %s", services));
    connectionHandler.onServiceList(services);

    if (!services.contains("stp-1")) {
      connectionHandler.onException(new IllegalStateException("STP/0 is not supported!"));
      return;
    }

    switchToStp1();
  }

  private void signalResponse(int tag, Response response) {
    connectionHandler.onResponseReceived(tag, response);
  }

  private void signalEvent(Event event) {
    logger.finest("EVENT " + event.toString());
    stp1EventHandler.handleEvent(event);
  }

  /**
   * Reads a message from the buffer, and pops the used data out of the buffer.
   *
   * @param buffer the buffer containing messages
   * @return true if we got a message from the buffer
   */
  private boolean readMessage(ByteBuffer buffer) {

    // logger.finest("readMessage: " + bytesRead + " bytes read, remaining=" +
    // remainingBytes.length + ", state=" + state.toString() + ", recurse=" +
    // recurse);

    buffer.position(0);
    int bytesWeHaveBeenReading = 0;

    switch (state) {
      case SERVICELIST:
        StringBuilder builder = new StringBuilder();
        builder.append(buffer.asCharBuffer());
        parseServiceList(builder.toString());
        buffer.position(0);// reset position!
        bytesWeHaveBeenReading = buffer.limit(); // bytesWeHaveBeenreading = all
        // bytes in buffer!
        break;

      case HANDSHAKE:
        if (buffer.limit() >= 6) {
          byte[] dst = new byte[6];
          buffer.position(0);// read from start!
          buffer.get(dst);
          buffer.position(0);// reset position!
          bytesWeHaveBeenReading = 6; // 6 bytes will be removed from buffer
          String handShake = new String(dst);
          if (!handShake.equals("STP/1\n")) {
            close();
            connectionHandler.onException(
                new CommunicationException("Expected STP/1, got: " + handShake));
          }
          setState(State.EMPTY);
          connectionHandler.onHandshake(true);
        }
        break;

      case EMPTY: // read 4 byte header: STP/0
        if (buffer.limit() >= 4) {
          byte[] headerPrefix = new byte[4];
          buffer.get(headerPrefix);
          buffer.position(0);
          bytesWeHaveBeenReading = 4;
          ByteString incomingPrefix = ByteString.copyFrom(headerPrefix);
          if (stpPrefix.equals(incomingPrefix)) {
            setState(State.STP);
            /*
            if(buffer.hasRemaining()){
                buffer.compact();
                readMessage(buffer, buffer.position(), true);
            } else {
                buffer.clear();
            }
            */
          } else {
            close();
            connectionHandler.onException(
                new CommunicationException("Expected empty header"));
          }
        }
        break;

      case STP:
        // Try to read size
        buffer.position(0);
        if (buffer.limit() <= 0) {
          logger.finest("STP: Empty buffer");
          break;
        }
        int messageSize = readRawVarint32(buffer);// read part of buffer
        bytesWeHaveBeenReading = buffer.position();
        buffer.position(0);

        // If we got size, read more, if not just leave it!
        if (buffer.limit() >= bytesWeHaveBeenReading + messageSize) {
          buffer.position(bytesWeHaveBeenReading);

          // Read type and Payload!
          int messageType = buffer.get();
          bytesWeHaveBeenReading += 1;

          byte[] payload = new byte[--messageSize];
          buffer.get(payload);
          buffer.position(0);

          bytesWeHaveBeenReading += messageSize; // 32 bits = 4 bytes :-)

          setState(State.EMPTY);

          try {
            processMessage(messageType, payload);
          } catch (IOException e) {
            close();
            connectionHandler.onException(new CommunicationException(
                "Error while processing the message: " + e.getMessage()));
          }
        } else {
          // 4 + messageSize because of the int at the beginning
          logger.finest(String.format("Tried to read a message and expected %d bytes, but got %d",
                                      (4 + messageSize), buffer.limit()));

          buffer.position(0);
          bytesWeHaveBeenReading = 0;
        }
        break;

    }

    // Pop number of read bytes from
    if (bytesWeHaveBeenReading > 0) {

      // Pop X bytes, and keep message for the rest
      int rest = buffer.limit() - bytesWeHaveBeenReading;
      if (rest <= 0) {
        buffer.clear();
        buffer.limit(0);
      } else {
        byte[] temp = new byte[rest];

        buffer.position(bytesWeHaveBeenReading);
        buffer.get(temp, 0, rest);

        buffer.clear();
        buffer.limit(rest);
        buffer.position(0);
        buffer.put(temp, 0, rest);
        buffer.position(0);// set position back to start!
      }

      logger.finest(String.format("Did read message of %d bytes, new buffer size is %d",
                                  bytesWeHaveBeenReading, buffer.limit()));

      return true; // We did read a message :-)
    } else {
      if (buffer.limit() > 0) {
        logger.finest("Did NOT read message from buffer of size " + buffer.limit());
      } else {
        logger.finest("No messages in empty buffer");
      }
      return false;
    }
  }

  private void processMessage(int stpType, byte[] payload) throws IOException {
    logger.finest("processMessage: " + stpType);

    switch (stpType) {
      //case 1: //command //commands are not supposed to be received
      //throw new WebDriverException("Received command from host?");
      case 2: // response
        // Log what is being sent.
        Response response = Response.parseFrom(payload);
        logger.finest("RECV RESPONSE: " + response.toString());
        signalResponse(response.getTag(), response);
        break;

      case 3: // event
        Event event = Event.parseFrom(payload);
        logger.finest("RECV EVENT: " + event.toString());
        signalEvent(event);
        break;
      case 4: // error
        Error error = Error.parseFrom(payload);
        logger.finest("RECV ERROR: " + error.toString());

        String service = error.getService();
        int status = error.getStatus();

        // We get exceptions when, in the ECMAScript services, we use a runtime that doesn't exist.
        // We can ignore these exceptions and carry on.
        if (((service.equals("ecmascript-debugger") || service.equals("ecmascript"))
             && status == Status.INTERNAL_ERROR.getNumber()) ||
            (service.equals("ecmascript") && status == Status.BAD_REQUEST.getNumber()) ||
            (service.equals("desktop-utils") && error.getCommandID() == DesktopUtilsMessage
                .GET_STRING.getID())) {
          signalResponse(error.getTag(), null);
        } else {
          connectionHandler.onException(
              new CommunicationException(String.format("Error on command: %s", error)));
        }

        break;

      default:
        connectionHandler.onException(new CommunicationException(
            String.format("Unhandled STP type: %d", stpType)));
    }
  }

  // protobuf methods, taken from the protobuf library by Google
  // explained: http://code.google.com/apis/protocolbuffers/docs/encoding.html#varints
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
              if (bytes.get() >= 0) {
                return result;
              }
            }
            connectionHandler.onException(new ScopeException("Error while reading raw int"));
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
