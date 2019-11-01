import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Broker {

    private SocketChannel client;
    private static Broker instance;

    private Broker() {
        try {
            client = SocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5001);
            client.connect(hostAddress);
            client.configureBlocking(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Broker getInstance() {
        if (instance == null)
            instance = new Broker();
        return instance;
    }

    public String sendMessage(String message) throws IOException {
        byte[] byteMsg = message.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        client.write(buffer);
        buffer.flip();
        client.read(buffer);
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Broker client = Broker.getInstance();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        System.out.println("Message to server:");
        while ((line = br.readLine()) != null) {
            String response = client.sendMessage(line);
            System.out.println("response from server: " + response);
            if (response.equals("bye")) break;
            System.out.println("Message to server:");
        }
        client.stop();
    }

}