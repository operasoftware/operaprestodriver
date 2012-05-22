package com.opera.core.systems.common.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;

public class CloseableServerSocket extends ServerSocket implements Closeable {

  public CloseableServerSocket(int port) throws IOException {
    super(port);
  }

}