Server is listening on /tmp/my_unix_socket1.sock
// client.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#define SOCKET_PATH "/tmp/my_unix_socket1.sock"
#define BUFFER_SIZE 256
#define BACKLOG

int main(int argc, char *argv[]) {
    struct sockaddr_un client_addr;
    int server_socket;
    ssize_t numRead;
    char buf[BUFFER_SIZE];

    server_socket = socket(AF_UNIX, SOCK_STREAM, 0);
    if(server_socket < 0) {
        perror("ERROR opening socket");
        exit(1);
    }

    memset(&client_addr, 0, sizeof(struct sockaddr_un));
    client_addr.sun_family = AF_UNIX;
    strncpy(client_addr.sun_path, SOCKET_PATH, sizeof(client_addr.sun_path) - 1);

    if(connect(server_socket, (struct sockaddr *) &client_addr, sizeof(struct sockaddr_un)) < 0) {
        perror("ERROR on connecting");
        exit(1);
    }

    while((numRead = read(STDIN_FILENO, buf, BUFFER_SIZE)) > 0) {
        if(write(server_socket, buf, numRead) != numRead) {
            break;
        }
    }

    if(numRead == -1) {
        perror("ERRORon reading");
        exit(1);
    }

    exit(EXIT_SUCCESS);
}// server.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#define SOCKET_PATH "/tmp/my_unix_socket1.sock"
#define BUFFER_SIZE 256
#define BACKLOG 5

void handle_client(int client_socket) {
    char buffer[BUFFER_SIZE];
    int n = read(client_socket, buffer, BUFFER_SIZE - 1);

    if(n < 0) {
        perror("ERROR reading from socket");
        exit(1);
    }
    buffer[n] = '\0';

    printf("Recieved message: %s\n", buffer);

    // Echo the message back to the client(Spring server)
    n = write(client_socket, buffer, strlen(buffer));
    if(n < 0) {
        perror("ERROR writing to socket");
        exit(1);
    }

    close(client_socket);
}

int main(int argc, char *argv[]) {
    int server_socket, client_socket;
    struct sockaddr_un server_addr;
    ssize_t numRead;
    char buf[BUFFER_SIZE];

    // If there are socket file which has same name already
    if(access(SOCKET_PATH, F_OK) == 0) {
        unlink(SOCKET_PATH);
    }

    server_socket = socket(AF_UNIX, SOCK_STREAM, 0);
    if(server_socket < 0) {
        perror("ERROR opening socket");
        exit(1);
    }

    memset(&server_addr, 0, sizeof(struct sockaddr_un));
    server_addr.sun_family = AF_UNIX;
    strncpy(server_addr.sun_path, SOCKET_PATH, sizeof(server_addr.sun_path) - 1);

    if(bind(server_socket, (struct sockaddr *) &server_addr, sizeof(struct sockaddr_un)) < 0) {
        perror("ERROR on binding");
        exit(1);
    }

    if(listen(server_socket, BACKLOG) < 0) {
        perror("ERROR on listening");
        exit(1);
    }

    printf("Server is listening on %s\n", SOCKET_PATH);

    while(1) {
        client_socket = accept(server_socket, NULL, NULL);
        if(client_socket < 0) {
            perror("ERROR on accept");
            exit(1);
        }

        while((numRead= read(client_socket, buf, BUFFER_SIZE)) > 0) {
            if(write(STDOUT_FILENO, buf, numRead) != numRead) {
                break;
            }
        }

        if(numRead < 0) {
            perror("ERROR on reading");
        }

        // handle_client(client_socket);
    }

    printf("Server is unconected on %s\n", SOCKET_PATH);

    close(server_socket);
    unlink(SOCKET_PATH);
    return 0;
}

