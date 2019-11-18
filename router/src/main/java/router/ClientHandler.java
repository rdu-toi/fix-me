package router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ClientHandler implements Runnable {

    private ByteBuffer buffer;
    private SocketChannel client;

    public ClientHandler(SocketChannel client){
        this.client = client;
        this.buffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void run() {
        try {
            int num = client.read(buffer);
            if (num != -1) {
                buffer.flip();
                String message = new String(buffer.array()).trim();
                if (message.equals("bye")) {
                    return;
                }
                buffer = ByteBuffer.wrap(new String(message).getBytes());
                client.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
