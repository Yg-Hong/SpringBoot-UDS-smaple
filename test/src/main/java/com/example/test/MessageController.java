package com.example.test;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RestController
@RequestMapping("/api")
public class MessageController {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket";

    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageRequest request) {
        String response = "";
        try (AFUNIXSocket socket = AFUNIXSocket.newInstance();
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            socket.connect(AFUNIXSocketAddress.of(new File(SOCKET_PATH)));

            writer.write(request.getMessage());
            writer.newLine();
            writer.flush();

            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            response = "Error: " + e.getMessage();
        }
        return response;
    }
}
