import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Broker {

    public static void main(String[] args) {
        String id;
        try {
            SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5000));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String data = new String(buffer.array()).trim();
            id = data;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Starting client...");

            while (true) {
                String message = br.readLine();
                String msg = id + "|" + message;
                System.out.println("Prepared message: " + msg);
                buffer = ByteBuffer.allocate(1024);
                buffer.put(msg.getBytes());
                buffer.flip();
                int bytesWritten = client.write(buffer);
                System.out.println(String.format("Sending Message: %s\nbufferBytes: %d", msg, bytesWritten));
                if (message.equals("exit"))
                    break;
            }

            br.close();
            client.close();
            System.out.println("Client connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}