package com.example.test;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;

@Slf4j
@Component
public class SocketProvider {

    private static final String SOCKET_PATH = "/tmp/my_unix_socket.sock";
    private static final int BUFFER_SIZE = 256;
    private AFUNIXSocket sock;
    private OutputStream out;
    private InputStream in;

    @PostConstruct
    public void connect() throws IOException {
        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = sock.getOutputStream();
        in = sock.getInputStream();

        log.info("UDS Connected Completely to {}", endpoint);
    }

    @PreDestroy
    public void disconnect() throws IOException {
        if (sock != null && !sock.isClosed()) {
            sock.close();
            log.info("Disconnected from server on " + SOCKET_PATH);
        }
    }

    // Need for custom return value for error handling
    public boolean isValid() {
        if (sock == null) {
            return false;
        } else if (!sock.isConnected()) {
            return false;
        }

        return true;
    }

    public void reconnect() throws IOException {
        if (this.isValid()) {
            return;
        }

        SocketAddress endpoint = getSocketAddress(SOCKET_PATH);
        sock = AFUNIXSocket.connectTo(AFUNIXSocketAddress.of(endpoint));
        out = sock.getOutputStream();
        in = sock.getInputStream();
    }

    public String sendMessage(String message) throws IOException {
        if (sock == null || !sock.isConnected()) {
            log.info("UDS NOT CONNECTED!!!");
            throw new IOException("UDS Not Connected");
        }
        log.info("Now writing string({}) to the server...", message);
        out.write(message.getBytes());
        out.flush();

        byte[] buffer = new byte[BUFFER_SIZE];
        int numRead = in.read(buffer);
        if (numRead > 0) {
            return new String(buffer, 0, numRead);
        } else {
            return null;
        }
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
