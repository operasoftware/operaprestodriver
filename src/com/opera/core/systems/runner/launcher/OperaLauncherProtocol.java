package com.opera.core.systems.runner.launcher;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherHandshakeResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.TreeMap;
import java.util.EnumSet;
import java.util.logging.Logger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implements the Opera launcher protocol.
 *
 * @author Jan Vidar Krey (janv@opera.com)
 */
public class OperaLauncherProtocol {

    private static Logger logger = Logger.getLogger(OperaLauncherProtocol.class.getName());
    private Socket socket;

    public enum MessageType {
        MSG_HELLO((byte)0),
        MSG_START((byte)1),
        MSG_STATUS((byte)2),
        MSG_SCREENSHOT((byte)3),
        MSG_STOP((byte)4),
        MSG_SHUTDOWN((byte)5);
        MessageType(byte n)
        {
            code = n;
        }
        private static final Map<Byte, MessageType> lookup = new TreeMap<Byte, MessageType>();

        static {
            for (MessageType command : EnumSet.allOf(MessageType.class))
                lookup.put(command.getValue(), command);
        }

        private byte code;
        public byte getValue() {
            return code;
        }
        public static MessageType get(byte code) {
            MessageType type = lookup.get(code);
            return type;
        }
    }

    public class ResponseEncapsulation
    {
        private boolean success;
        private GeneratedMessage response;

        public ResponseEncapsulation(boolean success, GeneratedMessage response)
        {
            this.success = success;
            this.response = response;
        }

        public GeneratedMessage getResponse() { return response; }
        public boolean IsSuccess() { return success; }
    }    

    public OperaLauncherProtocol(Socket socket)
    {
        this.socket = socket;
        logger.fine("Got Opera launcher connection from " + socket.getRemoteSocketAddress().toString());
    }

    public void shutdown() throws IOException
    {
    	this.socket.close();
    }
    
    /**
     * Send the 8 byte header before a Opera Launcher message body (payload).
     *
     * @param type The payload type to be sent after
     * @param size Size of the payload following the header
     * @throws java.io.IOException if sending failed.
     */
    private void sendRequestHeader(MessageType type, int size) throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put((byte) 'L');
        buf.put((byte) '1');
        buf.put((byte) type.getValue());
        buf.put((byte) 0); // request
        buf.putInt(size);
        buf.flip();
        logger.finest("SEND: type=" + ((int) 0) + ", command=" + ((int) type.getValue()) + ", size=" + size);
        socket.getOutputStream().write(buf.array());
    }

    /**
     * Send a request and receive a result.
     *
     * @param type The request type to be sent
     * @param body The serialized request payload
     * @return The response
     * @throws java.io.IOException
     */
    public ResponseEncapsulation sendRequest(MessageType type, byte[] body) throws IOException
    {
        int size = (body != null) ? size = body.length : 0;
        sendRequestHeader(type, size);
        if (body  != null)
            socket.getOutputStream().write(body);
        return recvMessage();
    }

    /**
     * Send a request without a response. Used for shutdown.
     *
     * @param type The request type to be sent
     * @param body The serialized request payload
     * @throws java.io.IOException
     */
    public void sendRequestWithoutResponse(MessageType type, byte[] body) throws IOException
    {
        int size = (body != null) ? size = body.length : 0;
        sendRequestHeader(type, size);
        if (body  != null)
            socket.getOutputStream().write(body);
    }
    
    /**
     * Receive and block until *all* length bytes are placed in buffer.
     *
     * @param buffer Target buffer to fill
     * @param length Desired length
     * @throws java.io.IOException if socket receive error
     */
    private void recv(byte[] buffer, int length) throws IOException
    {
        int bytes = 0;
        while (bytes < length){
        	int res = socket.getInputStream().read(buffer, bytes, length - bytes);
        	if(res > 0){
        		bytes += res;
        	} else {
        		return;
        	}
        }
    }

    /**
     * Receive a message response.
     *
     * @return Response body and request status code
     * @throws java.io.IOException if socket read error or protocol parse error.
     */
    private ResponseEncapsulation recvMessage() throws IOException
    {
        GeneratedMessage msg = null;
        byte[] headers = new byte[8];
        recv(headers, headers.length);

        if (headers[0] != 'L' || headers[1] != '1')
            throw new IOException("Wrong launcher protocol header");

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(headers, 4, 4);
        buf.flip();
        
        int size = buf.getInt();
        
        logger.finest("RECV: type=" + ((int) headers[3]) + ", command=" + ((int) headers[2]) + ", size=" + size);
        
        byte[] data = new byte[size];
        recv(data, size);

        boolean success = (headers[3] == (byte) 1);
        MessageType type = MessageType.get(headers[2]);

        if (type == null)
            throw new IOException("Unable to determine message type");

        if ((headers[3] != (byte) 1) && (headers[3] != (byte) 2))
            throw new IOException("Unable to determine success or error");

        switch (type) {
            case MSG_HELLO:
            {
                LauncherHandshakeResponse.Builder response = LauncherHandshakeResponse.newBuilder();
                buildMessage(response, data);
                msg = response.build();
                break;
            }

            case MSG_START:
            case MSG_STATUS:
            case MSG_STOP:
            {
                LauncherStatusResponse.Builder response = LauncherStatusResponse.newBuilder();
                buildMessage(response, data);
                msg = response.build();
                break;
            }

            case MSG_SCREENSHOT:
            {
                LauncherScreenshotResponse.Builder response = LauncherScreenshotResponse.newBuilder();
                buildMessage(response, data);
                msg = response.build();
                break;
            }

        }

        return new ResponseEncapsulation(success, msg);
    }

    private final GeneratedMessage.Builder<?> buildMessage(GeneratedMessage.Builder<?> builder, byte[] message) throws IOException
    {
        try {
            return builder.mergeFrom(message);
        } catch (InvalidProtocolBufferException ex) {
            throw new IOException("Could not parse launcher message " + builder.getDescriptorForType().getFullName() +  " : " + ex.getMessage());
        }
    }

}
