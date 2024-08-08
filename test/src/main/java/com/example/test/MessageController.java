package com.example.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
