// server.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>

#define SOCKET_PATH "/tmp/my_unix_socket.sock"
#define BUFFER_SIZE 1024
#define BACKLOG 5

char last_message[BUFFER_SIZE] = {0};

void daemonize() {
    pid_t pid, sid;

    pid = fork();
    if (pid < 0) {
        exit(EXIT_FAILURE);
    }
    if (pid > 0) {
        exit(EXIT_SUCCESS);
    }

    umask(0);

    sid = setsid();
    if (sid < 0) {
        exit(EXIT_FAILURE);
    }

    if ((chdir("/")) < 0) {
        exit(EXIT_FAILURE);
    }

    close(STDIN_FILENO);
    close(STDOUT_FILENO);
    close(STDERR_FILENO);
}

int main(int argc, char *argv[]) {
    int server_socket, client_socket;
    struct sockaddr_un server_addr;
    ssize_t numRead;
    char buf[BUFFER_SIZE];
    FILE *log_file;

    // log file config
    log_file = fopen("~/test/Server.log", "a+");
    if(log_file == NULL) {
        exit(EXIT_FAILURE);
    }

    // If there are socket file which has same name already
    if(access(SOCKET_PATH, F_OK) == 0) {
        unlink(SOCKET_PATH);
    }

    daemonize();

    server_socket = socket(AF_UNIX, SOCK_STREAM, 0);
    if(server_socket < 0) {
        perror("ERROR opening socket");
        exit(1);
    }

    memset(&server_addr, 0, sizeof(struct sockaddr_un));
    server_addr.sun_family = AF_UNIX;
    strncpy(server_addr.sun_path, SOCKET_PATH, sizeof(server_addr.sun_path) - 1);

    if(bind(server_socket, (struct sockaddr *) &server_addr, sizeof(struct sockaddr_un)) < 0) {
        fprintf(log_file, "ERROR on binding\n");
        fclose(log_file);
        exit(1);
    }

    if(listen(server_socket, BACKLOG) < 0) {
        fprintf(log_file, "ERROR on listening\n");
        fclose(log_file);
        exit(1);
    }

    fprintf(log_file, "Server is listening on %s\n", SOCKET_PATH);
    fflush(log_file);

    while(1) {
        client_socket = accept(server_socket, NULL, NULL);
        if(client_socket < 0) {
            fprintf(log_file, "ERROR on accept\n");
            fclose(log_file);
            exit(1);
        }

        while((numRead= read(client_socket, buf, BUFFER_SIZE)) > 0) {
            buf[numRead] = '\n';
            fprintf(log_file, "Received: %s\n", buf);
            fflush(log_file);

            // save the last message
            strncpy(last_message, buf, BUFFER_SIZE);

            if(write(STDOUT_FILENO, buf, numRead) != numRead) {
                fprintf(log_file, "ERROR on writing\n");
                fflush(log_file);
                break;
            }
        }

        if(numRead < 0) {
            fprintf(log_file, "ERROR on reading\n");
            fflush(log_file);
        }

        close(client_socket);
    }

    fprintf(log_file, "Server is disconnected on %s\n", SOCKET_PATH);
    fflush(log_file);

    close(server_socket);
    unlink(SOCKET_PATH);
    fclose(log_file);
    return 0;
}
