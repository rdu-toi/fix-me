import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

// FIX MESSAGE FORMAT:
// BeginString(FIX Version) -	8=String [FIX.4.2]
// ClientId				    -	109=Int
// OrdType				    -	40=Int [1=Market]
// Side(Buy or Sell)		-	54=Int [Buy=1, Sell=2]
// SecurityID(Instrument)	-	48=String
// NumShares			    -	53=Int
// ExDestination			-	100=Int
// Price				    -	44=Int
// CheckSum			        -	10=Int

public class Market {

    public static void main(String[] args) {
        String id;
        Instruments instruments = new Instruments();

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
                if (messageReceived.equals("Just testing"))
                    break;
                String returnMessage = "8=FIX.4.2|109="+id+"|100="+message.getClientId()+"|"+status;
                buffer = ByteBuffer.allocate(1024);
                buffer.put(returnMessage.getBytes());
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