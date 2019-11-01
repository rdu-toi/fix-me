import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ServerHandler implements Runnable {

    private ServerSocketChannel server;
    private ArrayList<ClientHandler> clients;

    public ServerHandler(ServerSocketChannel server, ArrayList<ClientHandler> clients){
            this.server = server;
            this.clients = clients;
    }

//    public void runServer(ServerSocketChannel server, ArrayList<ClientHandler> clients) throws IOException {
//        while (true) {
//            System.out.println("[ROUTER] Waiting for client connection...");
//            SocketChannel client = server.accept();
//            client.configureBlocking(false);
//            System.out.println("[ROUTER] Connected to client!");
//            ClientHandler clientThread = new ClientHandler(client);
//            clients.add(clientThread);
//            Router.taskExecutor.execute(clientThread);
//        }
//    }

    @Override
    public void run() {
        while (true) {
            System.out.println("[ROUTER] Waiting for client connection...");
            SocketChannel client = null;
            try {
                client = server.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                client.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[ROUTER] Connected to client!");
            ClientHandler clientThread = new ClientHandler(client);
            clients.add(clientThread);
            Router.taskExecutor.execute(clientThread);
        }
    }
}
