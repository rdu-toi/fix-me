import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Router {
    private static ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    private ServerSocketChannel serverChannel;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public Router() {
        try {
            serverChannel = ServerSocketChannel.open();
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5001);
            serverChannel.bind(hostAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() throws IOException {
        while (true) {
            System.out.println("[ROUTER] Waiting for client connection...");
            SocketChannel client = serverChannel.accept();
            client.configureBlocking(false);
            System.out.println("[ROUTER] Connected to client!");
            ClientHandler clientThread = new ClientHandler(client);
            clients.add(clientThread);
            taskExecutor.execute(clientThread);
        }
    }

    public static void main(String[] args) throws IOException {
        Router server = new Router();
        server.runServer();
    }

}