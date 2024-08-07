package com.example.test;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class MessageController {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket.sock";
    private AFUNIXSocket sock;
    private OutputStream out;
    private InputStream in;

    @Autowired
    private SocketProvider socketProvider;

    @PostMapping("/connect")
    public String connect() throws IOException {
        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = sock.getOutputStream();
        in = sock.getInputStream();

        log.info("UDS Connected Completely to {}", endpoint);
        return "UDS Connected Completely to " + endpoint + "\n";
    }

    @PostMapping("/disconnect")
    public String disconnect() throws IOException {
        sock.close();
        log.info("UDS Disconnected Completely");
        return "UDS Disconnected Completely\n";
    }

    @PostMapping("/write")
    public String write(@RequestBody MessageRequest request) throws IOException {
        if (sock == null || !sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            return "UDS Not Connected";
        }

        try {
            log.info("Now writing string({}) to the server...", request.getMessage());

            out.write(request.getMessage().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            log.info("ERROR...{}", e.getMessage());
            throw e;
        }

        log.info("End of communication");
        return "End of communication\n";
    }

    @GetMapping("/read")
    public String read() throws IOException {
        if (sock == null || !sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            return "UDS Not Connected";
        }

        String response = "Initial String";

        try (DataInputStream dis = new DataInputStream(in)) {
            byte[] buf = new byte[2048];
            int read = dis.read(buf);

            if (read > 0) {
                response = new String(buf, 0, read, StandardCharsets.UTF_8);
                log.info("Server said: {}", response);
            } else {
                response = "No response from server";
            }
        } catch (IOException e) {
            log.error("Error reading from c server ... {}", e.getMessage());
        }

        return response;
    }


    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) throws IOException {
        // Send a message and read the response
        log.info("Client(Spring Boot) request \"{}\" to Server(c)", request.getMessage());
        String response = socketProvider.sendMessage(request.getMessage());

        log.info("Server(c) response \"{}\" to Client(Spring Boot)", response);
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
