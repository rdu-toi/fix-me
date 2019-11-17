import java.io.IOException;
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
            System.out.println("Started brokerClient!");

            while (true) {
                String message = "8=FIX.4.2|109="+id+"40=1|54=1|48=ARWR|53=10|100=1|44=47.44"; // Still need to calculate and add checksum
                String msg = id + "|" + message;
                System.out.println("Prepared message: " + msg);
                buffer = ByteBuffer.allocate(1024);
                buffer.put(msg.getBytes());
                buffer.flip();
                int bytesWritten = client.write(buffer);
                System.out.println(String.format("Sending Message: %s\nbufferBytes: %d", msg, bytesWritten));
                if (message.equals("exit"))
                    break;
                buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                String messageReceived = new String(buffer.array()).trim();
                System.out.println("Received message: " + messageReceived);
            }

            client.close();
            System.out.println("Client connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}