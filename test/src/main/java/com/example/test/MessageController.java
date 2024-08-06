package com.example.test;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api")
public class MessageController {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket1.sock";
    private AFUNIXSocket sock;

    @PostMapping("/connect")
    public String connect() throws IOException {
        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        log.info("UDS Connected Completely to {}", endpoint);
        return "UDS Connected Completely to " + endpoint;
    }

    @PostMapping("/disconnect")
    public String disconnect() throws IOException {
        sock.close();
        log.info("UDS Disconnected Completely");
        return "UDS Disconnected Completely";
    }

    @PostMapping("/write")
    public String write(@RequestBody MessageRequest request) throws IOException {

        if (!sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            return "UDS Not Connected";
        }

        try (OutputStream out = sock.getOutputStream()) {
            out.write(request.getMessage().getBytes(StandardCharsets.UTF_8));
            out.flush();

            log.info("Now reading numbers from the server...");
        } catch (IOException e) {
            log.info("ERROR...");
            throw e;
        }

        log.info("End of communication");
        return "End of communication";
    }

    @GetMapping("/read")
    public String read() throws IOException {
        if (!sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            return "UDS Not Connected";
        }

        String response = "Initial String";

        try (InputStream in = sock.getInputStream();
             DataInputStream dis = new DataInputStream(in)
        ) {
            byte[] buf = new byte[2048];
            int read = in.read(buf);

            response = new String(buf, 0, read, StandardCharsets.UTF_8);

            log.info("Server said: {}", response);
            log.info("Replying to server...");
            while (!Thread.interrupted()) {
                int number = dis.read();
                if (number == -123) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private SocketAddress getSocketAddress(String socketName) throws IOException {
        if (socketName.startsWith("file:")) {
            // demo only: assume file: URLs are always handled by AFUNIXSocketAddress
            return AFUNIXSocketAddress.of(URI.create(socketName));
        } else if (socketName.contains(":/")) {
            // assume URI, e.g., unix:// or tipc://
            return AFSocketAddress.of(URI.create(socketName));
        }

        int colon = socketName.lastIndexOf(':');
        int slashOrBackslash = Math.max(socketName.lastIndexOf('/'), socketName.lastIndexOf('\\'));

        if (socketName.startsWith("@")) {
            // abstract namespace (Linux only!)
            return AFUNIXSocketAddress.inAbstractNamespace(socketName.substring(1));
        } else if (colon > 0 && slashOrBackslash < colon && !socketName.startsWith("/")) {
            // assume TCP socket
            String hostname = socketName.substring(0, colon);
            int port = Integer.parseInt(socketName.substring(colon + 1));
            return new InetSocketAddress(hostname, port);
        } else {
            // assume unix socket file name
            return AFUNIXSocketAddress.of(new File(socketName));
        }
    }
}
