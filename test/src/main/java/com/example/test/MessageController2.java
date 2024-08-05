package com.example.test;

import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/api2")
public class MessageController2 {

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/receive")
    public void receiveMessage() {
        String response = "";

        Path socketPath = Path.of(System.getProperty("user.home"))
                .resolve("test.sock");
        System.out.println(socketPath.toString());
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try {

            ServerSocketChannel serverChannel = ServerSocketChannel
                    .open(StandardProtocolFamily.UNIX);

            serverChannel.bind(socketAddress);

            SocketChannel channel = serverChannel.accept();

            // Receiving
//            while(true) {
//                readSocketMessage(channel)
//                        .ifPresent(message -> System.out.printf("[Client message %s]", message));
//
//                Thread.sleep(100);
//            }

            // 버퍼가 끝에 도달할때까지 반복
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

    @GetMapping("/send")
    public void sendMessage() {

        Path socketPath = Path.of(System.getProperty("user.home"))
                .resolve("test.sock");
        UnixDomainSocketAddress socketAddress = UnixDomainSocketAddress.of(socketPath);

        try {

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
