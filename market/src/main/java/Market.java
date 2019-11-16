import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

public class Market {

    public static void main(String[] args) {
        String id;
        String brokerId;

        Instruments instruments = new Instruments();
        HashMap<String, String[]> instrumentsList = instruments.getInstruments();
        for (String i : instrumentsList.keySet()) {
            System.out.println("Instrument: " + i + ", Quantity: " + instrumentsList.get(i)[0] + ", Price: " + instrumentsList.get(i)[1]);
        }

        try {
            SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5001));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String data = new String(buffer.array()).trim();
            id = data;
            System.out.println("Starting marketClient...");

            while (true) {
                buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                String messageReceived = new String(buffer.array()).trim();
                System.out.println("Received message: " + messageReceived);
                if (messageReceived.equals("Just testing"))
                    break;
                buffer = ByteBuffer.allocate(1024);
                buffer.put(messageReceived.getBytes());
                buffer.flip();
                client.write(buffer);
            }

            client.close();
            System.out.println("Client connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}