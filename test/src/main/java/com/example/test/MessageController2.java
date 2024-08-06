package com.example.test;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFUNIXSocket;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.SocketAddress;

@Slf4j
@RestController
@RequestMapping("/api2")
public class MessageController2 {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket1.sock";
    private UdsSocketBase client;
    private AFUNIXSocket sock;

    @PostMapping("/connect")
    public String connect2() throws IOException {
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

    @PostMapping("/write")
    public String write(@RequestParam String message) throws IOException {
        /**
         * message를 RequestParam으로 받고 UDS를 통해 Server.c로 전송
         */
        return "";
    }

    @GetMapping("/read")
    public String read() throws IOException {
        String response = "";
        /**
         * UDS를 통해 Server.c 에서 response를 가져와 반환
         */
        return response;
    }
}
