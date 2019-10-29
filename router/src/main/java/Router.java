import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class Router {
    private ExecutorService taskExecutor;
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;

    public Router() {
        try {
            taskExecutor = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 4 * 1024);
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
            serverChannel.bind(hostAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runServer() {
        while (true) {
            Future<AsynchronousSocketChannel> socketFuture = serverChannel.accept();
            try {
                clientChannel = socketFuture.get();
                Callable<String> worker = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String host = clientChannel.getRemoteAddress().toString();
                        while (true) {
                            System.out.println("Incoming connection from: " + host);
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            Future<Integer> readResult = clientChannel.read(buffer);
                            readResult.get();
                            buffer.flip();
                            String message = new String(buffer.array()).trim();
                            if (message.equals("bye")) {
                                break;
                            }
                            buffer = ByteBuffer.wrap(new String(message).getBytes());
                            Future<Integer> writeResult = clientChannel.write(buffer);
                            writeResult.get();
                            buffer.clear();
                        }

                        clientChannel.close();
                        System.out.println(host + " was successfully served!");

                        return host;
                    }
                };
                taskExecutor.submit(worker);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e);
                System.err.println("\n Server is shutting down ...");
                taskExecutor.shutdown();
                while (!taskExecutor.isTerminated()) {
                }
                break;
            }
        }
    }

    public static void main(String[] args) {
        Router server = new Router();
        server.runServer();
    }

}