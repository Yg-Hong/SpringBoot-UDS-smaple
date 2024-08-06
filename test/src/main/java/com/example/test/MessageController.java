package com.example.test;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api")
public class MessageController {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket1.sock";
    private UdsSocketBase client;

    @PostMapping("/connect")
    public String connect() throws IOException {
        client = new ReadWriteClient();
        SocketAddress endpoint = DemoHelper.socketAddress(SOCKET_PATH);
        try {
            client.connect(endpoint);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "Connect Success on " + SOCKET_PATH;
    }

    @PostMapping("/disconnect")
    public String disconnect() throws IOException {
        client.close();

        return "Disconnect Success";
    }
}
