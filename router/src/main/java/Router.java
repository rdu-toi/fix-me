import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.StandardSocketOptions;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.*;

public class Router {

    public static ExecutorService taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
    public static HashMap<Integer, SocketChannel> brokers = new HashMap<Integer, SocketChannel>();
    public static HashMap<Integer, SocketChannel> markets = new HashMap<Integer, SocketChannel>();
    public static ServerSocketChannel brokerChannel;
    public static ServerSocketChannel marketChannel;
    public static int marketId = 000000;
    public static int brokerId = 000000;

    public Router() throws IOException {
        setUpServers(5001, 1);
        setUpServers(5000, 2);
    }

    public void setUpServers(int port, int serverFlag) throws IOException  {
        if (serverFlag == 1) {
            try {
                marketChannel = ServerSocketChannel.open();
                ServerSocket serverSocket = marketChannel.socket();
                marketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
                marketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                InetSocketAddress hostAddress = new InetSocketAddress("localhost", port);
                serverSocket.bind(hostAddress);
                marketChannel.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ServerHandler serverThread = new ServerHandler(serverFlag);
            taskExecutor.execute(serverThread);
        } else {
            try {
                brokerChannel = ServerSocketChannel.open();
                ServerSocket serverSocket = brokerChannel.socket();
                brokerChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
                brokerChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                InetSocketAddress hostAddress = new InetSocketAddress("localhost", port);
                serverSocket.bind(hostAddress);
                brokerChannel.configureBlocking(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ServerHandler serverThread = new ServerHandler(serverFlag);
            taskExecutor.execute(serverThread);
        }
    }

    public static void main(String[] args) throws IOException {
        Router server = new Router();
    }

}