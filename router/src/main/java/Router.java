import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Router {

    public static ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    private static ArrayList<ClientHandler> brokerClients = new ArrayList<>();
    private static ArrayList<ClientHandler> marketClients = new ArrayList<>();
    private static ServerSocketChannel brokerChannel;
    private static ServerSocketChannel marketChannel;

    public Router() throws IOException {
        setUpServers(5001, marketChannel, marketClients);
        setUpServers(5000, brokerChannel, brokerClients);
    }

    public void setUpServers(int port, ServerSocketChannel server, ArrayList<ClientHandler> clients) throws IOException  {
        try {
            server = ServerSocketChannel.open();
//            server.configureBlocking(false);
            server.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", port);
            server.bind(hostAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        runServer(server, clients);
    }

    public void runServer(ServerSocketChannel server, ArrayList<ClientHandler> clients) throws IOException {
        ServerHandler serverThread = new ServerHandler(server, clients);
        taskExecutor.execute(serverThread);
//        while (true) {
//            System.out.println("[ROUTER] Waiting for client connection...");
//            SocketChannel client = server.accept();
//            client.configureBlocking(false);
//            System.out.println("[ROUTER] Connected to client!");
//            ClientHandler clientThread = new ClientHandler(client);
//            clients.add(clientThread);
//            taskExecutor.execute(clientThread);
//        }
    }

    public static void main(String[] args) throws IOException {
        Router server = new Router();
    }

}