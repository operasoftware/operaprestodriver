/*
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems.common.hash;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class MD5 {

  /**
   * Return the MD5 HEX sum of a hashed MD5 byte array.
   *
   * @param bytes the hashed MD5 byte array
   * @return HEX version of the byte array
   */
  public static String sum(byte[] bytes) {
    String result = "";
    for (byte b : bytes) {
      result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
    }
    return result;
  }

  /**
   * Get the MD5 hash of the given byte array.
   *
   * @param bytes a byte array
   * @return a byte array of the MD5 hash
   * @throws java.security.NoSuchAlgorithmException
   *                             if MD5 is not available
   * @throws java.io.IOException if an I/O error occurs
   */
  public static byte[] hash(byte[] bytes) throws NoSuchAlgorithmException, IOException {
    return ByteStreams.hash(ByteStreams.newInputStreamSupplier(bytes), Hashing.md5()).asBytes();
  }

  /**
   * Get the MD5 hash of the given file.
   *
   * @param file file to compute a hash on
   * @return a byte array of the MD5 hash
   * @throws IOException              if file cannot be found
   * @throws NoSuchAlgorithmException if MD5 is not available
   */
  public static byte[] hash(File file) throws NoSuchAlgorithmException, IOException {
    return Files.hash(file, Hashing.md5()).asBytes();
  }

  /**
   * Return the MD5 HEX sum of the given byte array.
   *
   * @param bytes a byte array
   * @return HEX version of the hash of the byte array
   * @throws NoSuchAlgorithmException if MD5 is not available
   * @throws IOException              if an I/O error occurs
   */
  public static String of(byte[] bytes) throws NoSuchAlgorithmException, IOException {
    return sum(hash(bytes));
  }

  /**
   * Return the MD5 HEX sum of the given file.
   *
   * @param file the file to get the MD5 sum of
   * @return HEX version of the hash of the byte array
   * @throws NoSuchAlgorithmException if MD5 is not available
   * @throws IOException              if an I/O error occurs
   */
  public static String of(File file) throws NoSuchAlgorithmException, IOException {
    return sum(hash(file));
  }

}