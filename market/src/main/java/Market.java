import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Market {

    public static void main(String[] args) {
        String id;
        Instruments instruments = new Instruments();
        Checksum checkSum = new Checksum();

        try {
            SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5001));
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
            String data = new String(buffer.array()).trim();
            id = data;
            System.out.println("Started marketClient!");

            while (true) {
                instruments.printInstruments();
                String status = "Rejected";
                buffer = ByteBuffer.allocate(1024);
                client.read(buffer);
                String messageReceived = new String(buffer.array()).trim();
                Message message = new Message(messageReceived);
                System.out.println("Received message: " + messageReceived);
                if (messageReceived.equals("[Router] Checksum incorrect. Get it together mate!")) {
                    buffer = ByteBuffer.allocate(1024);
                    String exit = "109="+id+"|exit|10=";
                    String finalExit = exit + checkSum.convert(exit) + "|";
                    buffer.put(finalExit.getBytes());
                    buffer.flip();
                    client.write(buffer);
                    break;
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
                String returnMessage = "8=FIX.4.2|109="+id+"|100="+message.getClientId()+"|"+status+"|";
                String finalReturnMessage = returnMessage + "10=" + checkSum.convert(returnMessage) + "|";
                buffer = ByteBuffer.allocate(1024);
                buffer.put(finalReturnMessage.getBytes());
                buffer.flip();
                client.write(buffer);
            }

            client.close();
            System.out.println("Client connection closed!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}