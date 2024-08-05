// server.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#define SOCKET_PATH "/tmp/my_unix_socket"
#define BUFFER_SIZE 256

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

int main(void) {
    int server_socket, client_socket;
    struct sockaddr_un server_addr;

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

    listen(server_socket, 5);

    printf("Server is listening on %s\n", SOCKET_PATH);

    while(1) {
        client_socket = accept(server_socket, NULL, NULL);
        if(client_socket < 0) {
            perror("ERROR on accept");
            exit(1);
        }

        handle_client(client_socket);
    }

    close(server_socket);
    unlink(SOCKET_PATH);
    return 0;
}

