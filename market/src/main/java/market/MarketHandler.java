package market;

import market.Checksum;
import market.Instruments;
import market.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;

public class MarketHandler implements Runnable{

    public SocketChannel client = null;
    public String id = null;
    public Instruments instruments = new Instruments();
    public Checksum checkSum = new Checksum();
    public Thread t;

    public MarketHandler() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 5001));
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
            instruments = new Instruments();
            checkSum = new Checksum();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String data = new String(buffer.array()).trim();
            id = data;
            System.out.println("Started marketClient!");

            while (true) {
                System.out.println("MARKET ID: " + String.format("%06d", Integer.parseInt(id)));
                instruments.printInstruments();
                System.out.println("Type 'exit' to quit.");
                String status = "Rejected";
                buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                String messageReceived = new String(buffer.array()).trim();
                Message message = new Message(messageReceived);
                System.out.println("Received message: " + messageReceived);
                if (messageReceived.equals("[Router] Checksum incorrect. Get it together mate!")) {
                    exit();
                    return ;
                }
                if (message.checkValidity()) {
                    System.out.println("Message is valid!");
                    if (instruments.changeQuanity(message)) {
                        status = "Accepted";
                        System.out.println("Order Accepted!");
                    }
                    else
                        System.out.println("Order Rejected!");
                }
                else {
                    System.out.println("Message is not valid!");
                    System.out.println("Order Rejected!");
                }
                String returnMessage = "109=" + String.format("%06d", Integer.parseInt(id)) + "|100=" + String.format("%06d", message.getClientId()) + "|" + status + "|110=" + message.getValidationId() +  "|";
                String finalReturnMessage = returnMessage + "10=" + checkSum.convert(returnMessage) + "|";
                buffer = ByteBuffer.allocate(1024);
                buffer.put(finalReturnMessage.getBytes());
                buffer.flip();
                client.write(buffer);
                System.out.println("Type 'exit' to quit.");
            }
        } catch (SocketException e) {
            try {
				client.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            System.exit(0);
        } catch (AsynchronousCloseException e) {
            // System.out.println("Client connection closing...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        String exit = "109=" + String.format("%06d", Integer.parseInt(id)) + "|exit|10=";
        String finalExit = exit + checkSum.convert(exit) + "|";
        buffer.put(finalExit.getBytes());
        buffer.flip();
        client.write(buffer);
        client.close();
        return ;
    }
}