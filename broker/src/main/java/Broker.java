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

            String message = "8=FIX.4.2|109="+id+"|40=1|54=1|48=ARWR|53=10|100=1|44=47"; // Still need to calculate and add checksum
            System.out.println("Prepared message: " + message);
            System.out.println("Message Sent!");
            buffer = ByteBuffer.allocate(1024);
            buffer.put(message.getBytes());
            buffer.flip();
            client.write(buffer);
            buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String messageReceived = new String(buffer.array()).trim();
            System.out.println("Received message: " + messageReceived);
            
            buffer = ByteBuffer.allocate(1024);
            String exit = "109="+id+"|exit";
            buffer.put(exit.getBytes());
            buffer.flip();
            client.write(buffer);

            client.close();
            System.out.println("Client connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}