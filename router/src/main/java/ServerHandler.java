import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerHandler implements Runnable {

    private Selector selector = null;
    private int serverFlag;

    public ServerHandler(int serverFlag){
        this.serverFlag = serverFlag;
        try {
            selector = Selector.open();
            if (serverFlag == 1) {
                int ops = Router.marketChannel.validOps();
                Router.marketChannel.register(selector, ops, null);
            } else {
                int ops = Router.brokerChannel.validOps();
                Router.brokerChannel.register(selector, ops, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (serverFlag == 1)
                    System.out.println("[ROUTER] Waiting for market connection...");
                else
                    System.out.println("[ROUTER] Waiting for broker connection...");
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> i = selectedKeys.iterator();

                while (i.hasNext()) {
                    SelectionKey key = i.next();

                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                    i.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAccept(SelectionKey key) {
        if (serverFlag == 1) {
            System.out.println("[ROUTER] Connected to market client!");
            SocketChannel client;
            try {
                client = Router.marketChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                // String portNum = String.valueOf(client.getLocalAddress());
                // System.out.println(portNum.substring(portNum.indexOf(":") + 1));
                Router.markets.put(++Router.marketId, client);
                System.out.println("Number of connected markets: " + Router.markets.size());
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(String.valueOf(Router.marketId).getBytes());
                buffer.flip();
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[ROUTER] Connected to broker client!");
            SocketChannel client;
            try {
                client = Router.brokerChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                Router.brokers.put(++Router.brokerId, client);
                System.out.println("Number of connected brokers: " + Router.brokers.size());
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(String.valueOf(Router.brokerId).getBytes());
                buffer.flip();
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        if (serverFlag == 1)
            System.out.println("Reading from market...");
        else
            System.out.println("Reading from client...");
        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);
        String data = new String(buffer.array()).trim();
        if (data.length() > 0) {
            String[] messageArray = data.split("\\|");
            if (messageArray[1].equalsIgnoreCase("exit")) {
                client.close();
                // Remove client from either market or broker hashmaps based on client Id
                if (serverFlag == 1)
                    Router.markets.remove(Integer.parseInt(messageArray[0]));
                else
                    Router.brokers.remove(Integer.parseInt(messageArray[0]));
                System.out.println("Connection closed...");
            }
            for (String message: messageArray) {
                if (message.contains("100=")) {
                    if (serverFlag == 1) {
                        int brokerId = Integer.parseInt(message.substring(message.indexOf("100=") + 4));
                        System.out.println("Market looking to connect to broker of ID: " + brokerId);
                        SocketChannel brokerClient = Router.brokers.get(brokerId);
                        buffer = ByteBuffer.allocate(1024);
                        buffer.put(data.getBytes());
                        buffer.flip();
                        brokerClient.write(buffer);
                    }
                    else {
                        int marketId = Integer.parseInt(message.substring(message.indexOf("100=") + 4));
                        System.out.println("Broker looking to connect to market of ID: " + marketId);
                        SocketChannel marketClient = Router.markets.get(marketId);
                        buffer = ByteBuffer.allocate(1024);
                        buffer.put(data.getBytes());
                        buffer.flip();
                        marketClient.write(buffer);
                        }
                }
            }
        }
    }
}
