package com.example.test;

import lombok.extern.slf4j.Slf4j;
import org.newsclub.net.unix.AFSocketAddress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

@Slf4j
abstract class UdsSocketBase {

    private Socket socket;

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
    }

    public void connect(SocketAddress endpoint) throws IOException {
        log.info("Connect {} to {}", this, endpoint);

        if (endpoint instanceof AFSocketAddress) {
            socket = ((AFSocketAddress) endpoint).getAddressFamily().newSocket();
        } else {
            socket = new Socket();
        }

        socket.connect(endpoint);
    }

    @SuppressWarnings("hinding")
    protected abstract void handleSocket(Socket socket) throws IOException;
}
