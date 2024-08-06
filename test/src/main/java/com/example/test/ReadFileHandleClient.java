package com.example.test;

import org.newsclub.net.unix.AFUNIXSocket;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ReadFileHandleClient extends UdsSocketBase {
    @Override
    protected void handleSocket(Socket socket) throws IOException {
        if (!(socket instanceof AFUNIXSocket)) {
            throw new UnsupportedOperationException("FileHandleClient only supports AFUNIX sockets");
        }

        handleSocket((AFUNIXSocket) socket);
    }

    protected void handleSocket(AFUNIXSocket socket) throws IOException {
        // set to a reasonable size
        socket.setAncillaryReceiveBufferSize(1024);

        try (InputStream in = socket.getInputStream()) {
            byte[] buffer = new byte[socket.getReceiveBufferSize()];

            while (in.read(buffer) != -1) {
                FileDescriptor[] descriptors = socket.getReceivedFileDescriptors();
                if (descriptors != null) {
                    for (FileDescriptor fd : descriptors) {
                        handleFileDescriptor(fd);
                    }
                }
            }
        }
    }

    private void handleFileDescriptor(FileDescriptor fd) throws IOException {
        try (FileInputStream fin = new FileInputStream(fd)) {
            byte[] buf = new byte[4096];

            int read;
            while ((read = fin.read(buf)) != -1) {
                System.out.write(buf, 0, read);
            }
            System.out.flush();
        }
    }
}
