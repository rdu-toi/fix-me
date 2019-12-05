package broker;

import broker.Checksum;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class BrokerHandler implements Runnable {

    public SocketChannel client = null;
    public String id = null;
    public Checksum checkSum = new Checksum();
    public Thread t;

    public BrokerHandler() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 5000));
        } catch (IOException e) {
            System.out.println("The router is not available right now, please try again later.");
            System.exit(0);
        }
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String data = new String(buffer.array()).trim();
            id = data;
            System.out.println("Started brokerClient!");
            System.out.println("BROKER ID: " + String.format("%06d", Integer.parseInt(id)));

            String message = getMessage(id); // Still need to calculate and add checksum
            String finalMessage = message + "10=" + checkSum.convert(message) + "|";
            System.out.println("Prepared message: " + finalMessage);
            System.out.println("Message Sent!");
            buffer = ByteBuffer.allocate(1024);
            buffer.put(finalMessage.getBytes());
            buffer.flip();
            client.write(buffer);
            buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String messageReceived = new String(buffer.array()).trim();
            System.out.println("Received message: " + messageReceived);
            System.out.println("Type 'exit' to quit.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage(String id) throws IOException {
        List<String> lines = Collections.emptyList(); 
        try
        {
          lines = Files.readAllLines(Paths.get(System.getProperty("user.dir") + "/broker/src/main/java/", "messages.txt"), StandardCharsets.UTF_8); 
        }

        catch (IOException e) 
        { 
          e.printStackTrace(); 
        }

        int choice = Integer.parseInt(lines.get(0));

        String rawInstruments = lines.get(choice);

        String message = null;
        try {
            message = "109=" + String.format("%06d", Integer.parseInt(id)) + "|" + rawInstruments + "110=" + System.currentTimeMillis() + "|";
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Incorrect input dummy!");
            exit();
            System.exit(0);
        }
        return message;
    }

	public void exit() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String exit = "109=" + String.format("%06d", Integer.parseInt(id)) + "|exit|110=" + System.currentTimeMillis() + "|10=";
        String finalExit = exit + checkSum.convert(exit) + "|";
        buffer.put(finalExit.getBytes());
        buffer.flip();
        client.write(buffer);

        client.close();
        System.out.println("Client connection closed!");
	}

}