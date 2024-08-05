package com.example.test;

import org.apache.catalina.Server;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.BindException;
import java.net.Socket;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MessageController {

    @PostMapping("/receive")
    public void receiveMessage(@RequestBody MessageRequest request) {
        String response = "";

        Path socketPath = Path.of(System.getProperty("user.home"))
                .resolve("test.sock");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try {
            Files.deleteIfExists(socketPath);

            ServerSocketChannel serverChannel = ServerSocketChannel
                    .open(StandardProtocolFamily.UNIX);

            serverChannel.bind(socketAddress);

            SocketChannel channel = serverChannel.accept();

            // Receiving
            while(true) {
                readSocketMessage(channel)
                        .ifPresent(message -> System.out.printf("[Client message %s]", message));

                Thread.sleep(100);
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

//        return response;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody MessageRequest request) {

        Path socketPath = Path.of(System.getProperty("user.home"))
                .resolve("test.sock");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try {
            Files.deleteIfExists(socketPath);

            // Sending
            SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(socketAddress);

            String message = "Hello from TEST Unix Domain Socket";

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.clear();
            buffer.put(message.getBytes());
            buffer.flip();

            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Optional<String> readSocketMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);
        if (bytesRead < 0) {
            return Optional.empty();
        }

        byte[] bytes = new byte[bytesRead];
        buffer.flip();
        buffer.get(bytes);
        String message = new String(bytes);

        return Optional.of(message);
    }
}
