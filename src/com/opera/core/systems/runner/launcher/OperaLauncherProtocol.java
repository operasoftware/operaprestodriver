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

package com.opera.core.systems.runner.launcher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherHandshakeResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherScreenshotResponse;
import com.opera.core.systems.runner.launcher.OperaLauncherProtos.LauncherStatusResponse;
import com.opera.core.systems.scope.internal.OperaIntervals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implements the launcher protocol.
 *
 * @author Jan Vidar Krey <janv@opera.com>
 */
public class OperaLauncherProtocol {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final Socket socket;
  private final OutputStream os;

  public enum MessageType {

    // TODO: Document
    MSG_HELLO((byte) 0),
    MSG_START((byte) 1),
    MSG_STATUS((byte) 2),
    MSG_SCREENSHOT((byte) 3),
    MSG_STOP((byte) 4),
    MSG_SHUTDOWN((byte) 5);

    private static final Map<Byte, MessageType> lookup = Maps.uniqueIndex(
        ImmutableList.copyOf(MessageType.values()), new Function<MessageType, Byte>() {
      public Byte apply(MessageType type) {
        return type.getValue();
      }
    });

    private final byte code;

    MessageType(byte n) {
      code = n;
    }

    public byte getValue() {
      return code;
    }

    public static MessageType get(byte code) {
      return lookup.get(code);
    }

  }

  public class ResponseEncapsulation {

    private boolean success;
    private GeneratedMessage response;

    public ResponseEncapsulation(boolean success, GeneratedMessage response) {
      this.success = success;
      this.response = response;
    }

    public GeneratedMessage getResponse() {
      return response;
    }

    public boolean isSuccess() {
      return success;
    }

  }

  public OperaLauncherProtocol(Socket client) throws IOException {
    socket = client;
    os = socket.getOutputStream();
    // Just to make sure we don't block forever if something goes wrong
    // TODO(andreastt): Unsafe int cast
    socket.setSoTimeout((int) OperaIntervals.LAUNCHER_RESPONSE_TIMEOUT.getMs());
    logger.finer("Setting launcher protocol timeout to " + socket.getSoTimeout() + " ms");
    logger.fine("Got launcher connection from " + socket.getRemoteSocketAddress().toString());
  }

  /**
   * Shuts down the connection to the launcher.
   *
   * @throws IOException if the socket is already closed
   */
  public void shutdown() throws IOException {
    os.close();
    socket.close();
  }

  /**
   * Send the 8 byte header before a Opera Launcher message body (payload).
   *
   * @param type the payload type to be sent after
   * @param size size of the payload following the header
   * @throws IOException if socket send error or protocol parse error
   */
  private void sendRequestHeader(MessageType type, int size) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(8);
    buf.order(ByteOrder.BIG_ENDIAN);
    buf.put((byte) 'L');
    buf.put((byte) '1');
    buf.put(type.getValue());
    buf.put((byte) 0);  // request
    buf.putInt(size);
    buf.flip();
    logger.finest("SEND: type=" + (0) + ", command=" + ((int) type.getValue()) + ", size=" + size);
    os.write(buf.array());
  }

  /**
   * Send a request and receive a result.
   *
   * @param type the request type to be sent
   * @param body the serialized request payload
   * @return the response
   * @throws IOException if socket read error or protocol parse error
   */
  public ResponseEncapsulation sendRequest(MessageType type, byte[] body) throws IOException {
    sendRequestHeader(type, (body != null) ? body.length : 0);
    if (body != null) {
      os.write(body);
    }
    return recvMessage();
  }

  /**
   * Send a request without a response. Used for shutdown.
   *
   * @param type the request type to be sent
   * @param body the serialized request payload
   * @throws IOException if socket read error or protocol parse error
   */
  public void sendRequestWithoutResponse(MessageType type, byte[] body) throws IOException {
    sendRequestHeader(type, (body != null) ? body.length : 0);
    if (body != null) {
      os.write(body);
    }
  }

  /**
   * Receive and block until *all* length bytes are placed in buffer.
   *
   * @param buffer Target buffer to fill
   * @param length Desired length
   * @throws IOException if socket read error or protocol parse error
   */
  private void recv(byte[] buffer, int length) throws IOException {
    int bytes = 0;

    while (bytes < length) {
      int res = socket.getInputStream().read(buffer, bytes, length - bytes);
      if (res > 0) {
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
   * @throws IOException if socket read error or protocol parse error
   */
  private ResponseEncapsulation recvMessage() throws IOException {
    GeneratedMessage msg = null;
    byte[] headers = new byte[8];
    recv(headers, headers.length);

    if (headers[0] != 'L' || headers[1] != '1') {
      throw new IOException("Wrong launcher protocol header");
    }

    ByteBuffer buf = ByteBuffer.allocate(4);
    buf.order(ByteOrder.BIG_ENDIAN);
    buf.put(headers, 4, 4);
    buf.flip();

    int size = buf.getInt();

    logger.finest("RECV: type=" + ((int) headers[3]) + ", command="
                  + ((int) headers[2]) + ", size=" + size);

    byte[] data = new byte[size];
    recv(data, size);

    boolean success = (headers[3] == (byte) 1);
    MessageType type = MessageType.get(headers[2]);

    if (type == null) {
      throw new IOException("Unable to determine message type");
    }

    if ((headers[3] != (byte) 1) && (headers[3] != (byte) 2)) {
      throw new IOException("Unable to determine success or error");
    }

    switch (type) {
      case MSG_HELLO: {
        LauncherHandshakeResponse.Builder response = LauncherHandshakeResponse.newBuilder();
        buildMessage(response, data);
        msg = response.build();
        break;
      }

      case MSG_START:
      case MSG_STATUS:
      case MSG_STOP: {
        LauncherStatusResponse.Builder response = LauncherStatusResponse.newBuilder();
        buildMessage(response, data);
        msg = response.build();
        break;
      }

      case MSG_SCREENSHOT: {
        LauncherScreenshotResponse.Builder response = LauncherScreenshotResponse.newBuilder();
        buildMessage(response, data);
        msg = response.build();
        break;
      }

    }

    return new ResponseEncapsulation(success, msg);
  }

  private GeneratedMessage.Builder<?> buildMessage(
      GeneratedMessage.Builder<?> builder, byte[] message) throws IOException {
    try {
      return builder.mergeFrom(message);
    } catch (InvalidProtocolBufferException e) {
      throw new IOException("Could not parse launcher message "
                            + builder.getDescriptorForType().getFullName() + ": "
                            + e.getMessage());
    }
  }

}