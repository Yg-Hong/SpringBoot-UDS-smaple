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

    if(connect(server_socket, (struct sockaddr_un *) &client_addr, sizeof(struct sockaddr_un)) < 0) {
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
}