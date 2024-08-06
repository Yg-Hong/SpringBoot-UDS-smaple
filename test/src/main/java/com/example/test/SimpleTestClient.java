package com.example.test;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class SimpleTestClient {
    private static final String SOCKET_PATH = "/tmp/my_unix_socket1.sock";
    private static final Logger log = LoggerFactory.getLogger(SimpleTestClient.class);
    private UdsSocketBase client;

    public void test() throws IOException {
        final File socketFile = new File(new File(SOCKET_PATH), System.currentTimeMillis() + ".sock");

        boolean connected = false;
        try (AFUNIXSocket sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(socketFile));
             InputStream in = sock.getInputStream();
             OutputStream out = sock.getOutputStream();
             DataInputStream dis = new DataInputStream(in);
             DataOutputStream dos = new DataOutputStream(out);
        ) {
            log.info("Connected");
            connected = true;

            byte[] buf = new byte[1024];
            int read = in.read(buf);

            log.info("Server said: {}", new String(buf, 0, read, StandardCharsets.UTF_8));
            log.info("Replying to server...");
            out.write("Hello Server".getBytes(StandardCharsets.UTF_8));
            out.flush();

            log.info("Now reading numbers from the server...");
            while (!Thread.interrupted()) {
                int number = dis.readInt();
                if (number == -123) {
                    break;
                }

                log.info("Server sent... : {}", number);

                int ourNumber = number * 2;
                log.info("Sending back : {}", ourNumber);
                dos.writeInt(ourNumber);
            }
        } catch (SocketException e) {
            if(!connected) {
                log.info("Cannot Connect to server. Have you started it now?");
                log.info("...");
            }
            throw e;
        }

        log.info("End of communication");
    }

}
