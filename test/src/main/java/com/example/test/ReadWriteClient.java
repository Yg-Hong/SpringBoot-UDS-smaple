package com.example.test;

import org.newsclub.net.unix.AFUNIXSocket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ReadWriteClient extends UdsSocketBase {
    @Override
    protected void handleSocket(Socket socket) throws IOException {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            final byte[] readBuffer = new byte[socket.getReceiveBufferSize()];
            final byte[] writeBuffer = new byte[socket.getSendBufferSize()];
            final CountDownLatch cdl = new CountDownLatch(2);

            new Thread() {
                public void run() {
                    int bytes;
                    try {
                        while ((bytes = in.read(readBuffer)) != -1) {
                            out.write(writeBuffer, 0, bytes);
                            out.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    cdl.countDown();
                }
            }.start();

            new Thread() {
                public void run() {
                    int bytes;
                    try {
                        while ((bytes = System.in.read(writeBuffer)) != -1) {
                            out.write(writeBuffer, 0, bytes);
                            out.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    cdl.countDown();
                }
            }.start();

            cdl.await();
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }
}
