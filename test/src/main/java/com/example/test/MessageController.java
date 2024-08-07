package com.example.test;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@RequestMapping("/api")
public class MessageController {

    private final SocketProvider socketProvider;

    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) throws IOException {
        // Send a message and read the response
        log.info("Client(Spring Boot) request \"{}\" to Server(c)", request.getMessage());
        String response = socketProvider.sendMessage(request.getMessage());

        log.info("Server(c) response \"{}\" to Client(Spring Boot)", response);
        return response;
    }
}
