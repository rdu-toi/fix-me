import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class ServerHandler implements Runnable {

    private static Selector selector = null;
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
        System.out.println("Reading...");
        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);
        String data = new String(buffer.array()).trim();
        if (data.length() > 0) {
            String[] checkExit = data.split("\\|");
            if (checkExit[1].equalsIgnoreCase("exit")) {
                client.close();
                // Remove client from either market or broker hashmaps based on client Id
                if (serverFlag == 1)
                    Router.markets.remove(Integer.parseInt(checkExit[0]));
                else
                    Router.brokers.remove(Integer.parseInt(checkExit[0]));
                System.out.println("Connection closed...");
            }
        }
    }
}
