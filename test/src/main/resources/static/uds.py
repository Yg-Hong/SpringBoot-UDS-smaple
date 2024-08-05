import socket
import json

print("Python Activate!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

def send_request(request):
    client = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
    client.connect("/tmp/test.sock")
    
    client.sendall(request.encode('utf-8'))
    response = client.recv(4096)
    client.close()
    return response.decode('utf-8')

# GET request
get_request = (
    "GET /test HTTP/1.1\r\n"
    "Host: localhost\r\n"
    "Connection: close\r\n"
    "\r\n"
)

response = send_request(get_request)
print("GET response:", response)

# POST request
post_data = json.dumps({"message": "Hello, world!"})
post_request = (
    "POST /echo HTTP/1.1\r\n"
    "Host: localhost\r\n"
    "Content-Type: application/json\r\n"
    "Content-Length: {}\r\n"
    "Connection: close\r\n"
    "\r\n"
    "{}"
).format(len(post_data), post_data)

response = send_request(post_request)
print("POST response:", response)
