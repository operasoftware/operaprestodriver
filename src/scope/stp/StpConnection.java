/* Copyright (C) 2009 Opera Software ASA.  All rights reserved. */
package com.opera.core.systems.scope.stp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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

public class StpConnection implements Runnable {

	private ServerSocketChannel serverChannel;
	private Selector selector;
	private String response = "";
	private Response stpResponse;

	private final AtomicBoolean countKnown = new AtomicBoolean();
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final ArrayBlockingQueue<ByteBuffer> requests;

	protected boolean running = false;
	private boolean waitForKey = true;
	private StringBuilder builder;
	private SocketChannel socketChannel;
	private boolean isStp1;
	private AbstractEventHandler eventHandler;
	private UmsEventParser stp1EventHandler;
	private XmlEventParser stp0EventHandler;
	
	private IConnectionHandler connectionHandler;

	public enum State {
		HANDSHAKE, EMPTY, STP, STP_SIZE, STP_DATA;
	}

	public Response getStpResponse() {
		return stpResponse;
	}

	byte[] prefix = { 'S', 'T', 'P', 1 };
	
	private int messageSize = 0;

	private int messageType = 0;

	private ByteString stpPrefix = ByteString.copyFrom(prefix);
	
	private State state = State.HANDSHAKE;
	
	private byte[] remainingBytes = new byte[0];
	
	/**
	 * Sets the event handler and creates the event parsers
	 * @param eventHandler
	 */
	public void setEventHandler(final AbstractEventHandler eventHandler) {
		this.eventHandler = eventHandler;
		if(isStp1)
			stp1EventHandler = new UmsEventParser(eventHandler);
		else
			stp0EventHandler = new XmlEventParser(eventHandler);
	}

	//FIXME
	public String getAndEmptyResponse() {
		//synchronized(this) {
			String result = response;
			setResponse("");
			return result;
		//}
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	public boolean isConnected() {
		if(socketChannel == null)
			return false;
		
		return socketChannel.isConnected();
	}
	
	public ArrayBlockingQueue<ByteBuffer> getRequests() {
		return requests;
	}
	
	public AbstractEventHandler getEventHandler() {
		return eventHandler;
	}

	public StpConnection(final int port, IConnectionHandler handler) {
		this(null, port, handler);
	}

	public static void sleep(final long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (InterruptedException e) {
			//ignore
		}
	}

	/**
	 * Initializes variables in object scope, sets 'count known' to false to
	 * read byte count (STP/0).
	 * 
	 * @param hostAddress Adress to listen on, localhost if null
	 * @param port Port to bind to
	 */
	public StpConnection(final InetAddress hostAddress, final int port, IConnectionHandler handler) {
		connectionHandler = handler;
		try {
			selector = initSelector(hostAddress, port);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error on initialize", e.getMessage());
			throw new WebDriverException("Cant initialize on port " + port);
		}
		
		countKnown.set(false);
		builder = new StringBuilder();
		requests = new ArrayBlockingQueue<ByteBuffer>(1024);
	}

	/**
	 * Initializes the selector and binds it to the specified port
	 * 
	 * @param hostAddress
	 * @param port
	 * @return initialized selector
	 * @throws IOException if binding fails
	 */
	private Selector initSelector(InetAddress hostAddress, int port) throws IOException {
		final Selector selector = SelectorProvider.provider().openSelector();

		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		InetSocketAddress address = new InetSocketAddress(hostAddress, port);
		serverChannel.socket().bind(address);

		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		logger.log(Level.INFO, "Ready to accept connection on port " + port);
		return selector;
	}

	/**
	 * Accepts the key, creates the socket channel, sets IO to non blocking
	 * switches to read state and attaches buffer to key for read operations
	 * 
	 * @param key SelectionKey to get channel from
	 */
	private void accept(SelectionKey key) {
		// BUG windows sends more than one accept key?
		if (!serverChannel.isOpen()) {
			return;
		}

		ByteBuffer buffer = ByteBuffer.allocate(4096);
		final ServerSocketChannel channel = (ServerSocketChannel) key.channel();

		try {
			socketChannel = channel.accept();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ, buffer);
			running = true;

		} catch (IOException e) {
			logger.log(Level.WARNING, "Error on accepting key", e);
		}
	}
	
	public void switchToStp0() {
		isStp1 = false;
		stp0EventHandler = new XmlEventParser(eventHandler);
	}
	
	
	public void switchToStp1() {
		isStp1 = true;
		stp1EventHandler = new UmsEventParser(eventHandler);
	}

	/**
	 * If there are requests in queue, registers them to selector this way we
	 * access the selector keyset only from this thread avoiding any
	 * synchronization. Processes the key for read/write events. If connection
	 * close is requested, cleanup is called to close the selector and channel.
	 */
	public void run() {
		while (waitForKey) {
			try {
				if (!requests.isEmpty()) {
					SelectionKey key = socketChannel.keyFor(this.selector);
					key.interestOps(SelectionKey.OP_WRITE);
				}
				selector.select();
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

				while (iterator.hasNext()) {

					SelectionKey key = iterator.next();
					iterator.remove();
					
					if (!key.isValid()) {
						continue;
					}
					if (key.isAcceptable()) {
						this.accept(key);
					}
					else if (key.isReadable()) {
						this.read(key);
						//can it be both readable and writable?
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (NullPointerException ex) {
				logger.log(Level.WARNING, "Unexpected error: ", ex.getMessage());
				logger.log(Level.FINE, "Stack trace: " , ex);
				close();

			} catch (IOException ex) {
				logger.log(Level.WARNING, "IO Error: ", ex.getMessage());
				logger.log(Level.FINE, "Stack trace: " , ex);
			}
		}
		cleanUp();
	}
	

	
	/**
	 * Queues up an STP/1 message sent from another thread and wakes up selector to
	 * register it to the key
	 * 
	 * @param message to add to the request queue
	 * @throws IOException 
	 */
	public void send(Command command) {
		if (running) {
			byte[] payload = command.toByteArray();
			int totalSize = payload.length + 1; //increment 1 for message type
	
			ByteBuffer messageSize = encodeMessageSize(totalSize);
			
			// create the message
			ByteBuffer buffer = ByteBuffer.allocateDirect(prefix.length + messageSize.position() + 1 + payload.length);
			buffer.put(prefix, 0, prefix.length);
			messageSize.flip();
			buffer.put(messageSize);
			buffer.put((byte) 1);
			buffer.put(payload);
			requests.add(buffer);
		}
		selector.wakeup();
	}
	

	/**
	 * Queues up an STP/0 message sent from another thread and wakes up selector to
	 * register it to the key
	 * 
	 * @param message to add to the request queue
	 */
	public void send(String message) {
		if (running) {
			String scopeMessage = message.length() + " " + message;
			logger.log(Level.FINE, "WRITE : {0}", scopeMessage);
			byte[] bytes;
			try {
				bytes = scopeMessage.getBytes("UTF-16BE");
			} catch (UnsupportedEncodingException e) {
				throw new WebDriverException("Unsupported encoding : " + e.getMessage());
			}
			ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
			buffer.put(bytes);
			requests.add(buffer);
		}
		selector.wakeup();
	}

	/**
	 * Closes the connection and the key's channel, to avoid queueing up new
	 * messages sets connection state
	 * 
	 * @param key
	 */
	private void cleanUp(SelectionKey key) {
		close();
		try {
			key.channel().close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Exception while closing channel : {0}", e.getMessage());
		}
		running = false;
	}

	/**
	 * Reads an incoming message into a buffer, cleans up on exception (host
	 * abort connection) or disconnect. A message is an UTF-16 string, each
	 * byte is used in {@link CharBuffer} and read into {@link StringBuilder},
	 * space is the seperator in STP/0
	 * 
	 * @param key
	 */
	private void read(SelectionKey key) {
		ByteBuffer buffer = (ByteBuffer) key.attachment();
		int numRead = 0;
		// do...while (numRead!=0)
		try {
			numRead = socketChannel.read(buffer);
		} catch (Exception e) {
			cleanUp(key);
			logger.log(Level.WARNING, "Channel closed", e.getMessage());
			connectionHandler.onSocketException();
			return;
		}
		if (numRead < 0) {
			cleanUp(key);
			logger.log(Level.INFO, "Channel closed : {0}", socketChannel.socket().getInetAddress().getHostName());
			connectionHandler.onDisconnect();
			return;
		}

		if(isStp1) {
			readMessage(buffer, numRead);
		} else {
			buffer.flip();
			builder.append(buffer.asCharBuffer());
			buffer.clear();
	
			if (!countKnown.get() && !builder.toString().contains(" ")) {
				//FIXME should not be needed
				selector.wakeup();
				return;
			}
	
			this.readXmlMessage();
		}
	}

	/**
	 * Processes an incoming message/response
	 * TODO There are multiple selector.wakeup calls.
	 * Is that something we should avoid?
	 */
	private void readXmlMessage() {
		String response = builder.toString();
		int countEnd = response.indexOf(' ');
		String numberOfChars = response.substring(0, countEnd);
		String messageBody = response.substring(countEnd + 1);

		int messageLength = Integer.valueOf(numberOfChars);
		int bodyLength = messageBody.length();
		countKnown.set(true);
		if (bodyLength == messageLength) {

			processXmlMessage(messageBody);
			logger.fine(messageBody);
			logger.log(Level.FINEST, "READ: {0}", messageBody);
			builder = new StringBuilder();
			countKnown.set(false);
		}

		else if (bodyLength < messageLength) {
			selector.wakeup();
		}

		else if (bodyLength > messageLength) {
			int cutPoint = numberOfChars.length() + 1 + messageLength;
			String firstMessage = response.substring(countEnd + 1, cutPoint);
			logger.fine(firstMessage);

			processXmlMessage(firstMessage);

			logger.log(Level.FINEST, "READ: {0}", firstMessage);
			String remaining = response.substring(cutPoint);
			builder = new StringBuilder();
			builder.append(remaining);
			if (remaining.contains(" ")) {
				countKnown.set(true);
				readXmlMessage();
			} else {
				countKnown.set(false);
				selector.wakeup();
			}
		}
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
	private void write(SelectionKey key) throws IOException {
		do {
			ByteBuffer buffer = requests.poll();
			buffer.flip();
			int written = 0;
			
			do
			{
			 written = socketChannel.write(buffer);
			 if(written == 0)
			   sleep(1);
			} while(buffer.hasRemaining());
			
		} while(!requests.isEmpty());
		
		
		if(!requests.isEmpty())
			selector.wakeup();
		else 
			key.interestOps(SelectionKey.OP_READ);
	}


	/**
	 * Switches the wait state and wakes up the selector to process
	 */
	public void close() {
		this.waitForKey = false;
		selector.wakeup();
	}

	/**
	 * Closes the socket channel and selector
	 */
	private void cleanUp() {
		try {
			if(socketChannel != null && socketChannel.isOpen())
				socketChannel.close();
			if(selector != null && selector.isOpen())
				selector.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Exception while closing connection : {0}", e.getMessage());
		}
	}

	/**
	 * Closes the listener to avoid new incoming connections In our current
	 * scenario, there can only be 1 connection (in future however we might
	 * consider running multiple connections and use this codebase as proxy)
	 */
	public void closeListener() {
		try {
			this.serverChannel.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Error while closing server channel : {0}", e.getMessage());
		}
	}
	
	/**
	 * Processes an incoming message and passes it to event handler if needed,
	 * the following events are to our interest: 
	 * Runtime-Started : ecmascript runtime starts in Opera (that we can inject to) 
	 * Runtime-Stopped : ecmascript runtime stops (not used, buggy) 
	 * Message: fired from console log events 
	 * Updated-Window: a window is updated OR created (opener-id=0)
	 * Active-Window: window focus changed
	 * Window-Closed: self explanatory 
	 * If message matches none it is added to the response queue
	 * (probably response to command)
	 * 
	 * @param message
	 */
	public void processXmlMessage(String message) {	
		logger.log(Level.FINEST, "READ: {0}", message);
		
		if (stp0EventHandler == null || !stp0EventHandler.handleIfEvent(message)) {
			setResponse(message);
			connectionHandler.onResponseReceived();
		}
	}
	
	private void readMessage(ByteBuffer buffer, int bytesRead) {
		
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
		case HANDSHAKE:
			if(bytesRead >= 6) {
				byte[] dst = new byte[6];
				buffer.get(dst);
				String handShake = new String(dst);
				if(!handShake.equals("STP/1\n"))
					throw new WebDriverException("Scope Transport Protocol Error : Handshake");
				buffer.clear();
				state = State.EMPTY;
				connectionHandler.onResponseReceived();
				break;
			}
			bufferRemaining(buffer, bytesRead);
			break;
		case EMPTY:
			if(bytesRead >= 4) {
				byte[] prefix = new byte[4];
				buffer.get(prefix);
				ByteString incomingPrefix = ByteString.copyFrom(prefix);
				if(stpPrefix.equals(incomingPrefix)){
					state = State.STP;
					if(buffer.hasRemaining()){
						buffer.compact();
						readMessage(buffer, buffer.position());
					} else {
						buffer.clear();
					}
				} else {
					throw new WebDriverException("Scope Transport Protocol Error : Header");
				}
				break;
			}
			// we got less than 4
			bufferRemaining(buffer, bytesRead);
			break;
		case STP:
			//this one needs more error handling
			messageSize = readRawVarint32(buffer);
			state = State.STP_DATA;
			if (buffer.hasRemaining()) {
				buffer.compact();
				readMessage(buffer, buffer.position());
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
				state = State.EMPTY;
				try {
					processMessage(messageType, payload);
				} catch (IOException e) {
					throw new WebDriverException("Error while processing the message: "  + e.getMessage());
				}
				if(buffer.hasRemaining()){
					buffer.compact();
					readMessage(buffer, buffer.position());
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
			remainingBytes = new byte[read];
			buffer.get(remainingBytes);
			buffer.clear();
		}
	}

	private void processMessage(int stpType, byte[] payload) throws IOException {

		switch (stpType) {
		/*
		 * case 1://command //commands are not supposed to be received throw new
		 * WebDriverException("Received command from host?");
		 */
		case 2:// response
			stpResponse = Response.parseFrom(payload);
			connectionHandler.onResponseReceived();
			break;
		case 3:// event
			Event event = Event.parseFrom(payload);
			stp1EventHandler.handleEvent(event);
			break;
		case 4:// error
			Error error = Error.parseFrom(payload);
			if (error.getService().equals("ecmascript-debugger") && error.getStatus() == Status.INTERNAL_ERROR.getCode()) {
				stpResponse = null;
				connectionHandler.onResponseReceived();
			} else {
				// FIXME we should not discard this one
				logger.log(Level.SEVERE, "Error : {0}", error.toString());
				stpResponse = null;
				connectionHandler.onResponseReceived();
				// throw new WebDriverException("Error on " + error.getService()
				// + " " + Status.get(error.getStatus()) + " - " +
				// error.toString());
			}
			break;
		default:
			throw new WebDriverException("Unhandled STP type: " + stpType);
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
	            throw new WebDriverException("Error while reading raw int");
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
