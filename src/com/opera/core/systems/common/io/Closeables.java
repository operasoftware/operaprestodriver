package com.opera.core.systems.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Closeables {

  private static final Logger logger = Logger.getLogger(Closeables.class.getName());

  public static void close(Object klass, boolean swallowIOException) throws IOException {
    if (klass == null) {
      return;
    }

    if (klass instanceof Closeable) {
      com.google.common.io.Closeables.close((Closeable) klass, swallowIOException);
    }

    try {
      Method method = klass.getClass().getMethod("close");
      method.invoke(klass);
    } catch (Exception e) {
      // nothing we can do
    }
  }

  public static void closeQuietly(Object klass) {
    try {
      close(klass, true);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "IOException should not have been thrown", e);
    }
  }

}