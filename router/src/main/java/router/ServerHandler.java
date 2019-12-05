package router;

import router.*;

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
    private Checksum checkSum = new Checksum();

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
                    System.out.println("[ROUTER]" + "\u001B[36m" + " Waiting for market connection..." + "\u001B[0m");
                else
                    System.out.println("[ROUTER]" + "\u001B[36m" + " Waiting for broker connection..." + "\u001B[0m");
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
            System.out.println("[ROUTER]" + "\u001B[32m" + " Connected to market client!" + "\u001B[0m");
            SocketChannel client;
            try {
                client = Router.marketChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                Router.markets.put(++Router.marketId, client);
                System.out.println("[ROUTER]" + "\u001B[33m" + " Number of connected markets: " + Router.markets.size() + "\u001B[0m");
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(String.valueOf(Router.marketId).getBytes());
                buffer.flip();
                client.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[ROUTER]" + "\u001B[32m" + " Connected to broker client!" + "\u001B[0m");
            SocketChannel client;
            try {
                client = Router.brokerChannel.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
                Router.brokers.put(++Router.brokerId, client);
                System.out.println("[ROUTER]" + "\u001B[33m" + " Number of connected brokers: " + Router.brokers.size() + "\u001B[0m");
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
            System.out.println("[ROUTER]" + "\u001B[33m" + " Reading from market..." + "\u001B[0m");
        else
            System.out.println("[ROUTER]" + "\u001B[33m" + " Reading from client..." + "\u001B[0m");
        SocketChannel client = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        client.read(buffer);
        String data = new String(buffer.array()).trim();
        if (data.length() > 0) {
            String[] messageArray = data.split("\\|");
            if (messageArray[1].equalsIgnoreCase("exit")) {
                client.close();
                int clientToRemove = Integer.parseInt(messageArray[0].substring(messageArray[0].indexOf("109=") + 4));
                if (serverFlag == 1) {
                    Router.markets.remove(clientToRemove);
                    System.out.println("[ROUTER]" + "\u001B[91m" + " MarketClient[id=" + clientToRemove + "] disconnected" + "\u001B[0m");
                }
                else {
                    Router.brokers.remove(clientToRemove);
                    System.out.println("[ROUTER]" + "\u001B[91m" + " BrokerClient[id=" + clientToRemove + "] disconnected" + "\u001B[0m");
                }
                return;
            }
            for (String message: messageArray) {
                if (message.contains("100=")) {
                    if (serverFlag == 1) {
                        int brokerId = Integer.parseInt(message.substring(message.indexOf("100=") + 4));
                        System.out.println("[ROUTER]" + "\u001B[33m" + " Market looking to connect to broker of ID: " + brokerId + "\u001B[0m");
                        if (!Router.brokers.containsKey(brokerId))
                            return ;
                        SocketChannel brokerClient = Router.brokers.get(brokerId);
                        if (!checkSum.compare(data)) {
                            buffer = ByteBuffer.allocate(1024);
                            buffer.put("[Router] Checksum incorrect. Get it together mate!".getBytes());
                            buffer.flip();
                            client.write(buffer);
                            buffer = ByteBuffer.allocate(1024);
                            buffer.put("[Router] The market messed up. Order Rejected!".getBytes());
                            buffer.flip();
                            brokerClient.write(buffer);
                            return ;
                        }
                        buffer = ByteBuffer.allocate(1024);
                        buffer.put(data.getBytes());
                        buffer.flip();
                        brokerClient.write(buffer);
                    }
                    else {
                        int marketId = Integer.parseInt(message.substring(message.indexOf("100=") + 4));
                        System.out.println("[ROUTER]" + "\u001B[33m" + " Broker looking to connect to market of ID: " + marketId + "\u001B[0m");
                        if (!checkSum.compare(data) || !Router.markets.containsKey(marketId)) {
                            buffer = ByteBuffer.allocate(1024);
                            buffer.put("[ROUTER] Rejected".getBytes());
                            buffer.flip();
                            client.write(buffer);
                            return ;
                        }
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
