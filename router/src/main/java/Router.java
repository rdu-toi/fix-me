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
            System.out.println("Accepting a new connection...");
            try {
                clientChannel = socketFuture.get();
                Callable<String> worker = new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        String host = clientChannel.getRemoteAddress().toString();
                        System.out.println("Incoming connection from: " + host);

                        final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                        while (clientChannel.read(buffer).get() != -1) {
                            System.out.println("Incoming message from: " + host);

                            buffer.flip();
                            System.out.println("1");

//                            String message = new String(buffer.array()).trim();
//                            System.out.println("2");
//                            if (message.equals("bye")) {
//                                break;
//                            }
                            System.out.println("3");

                            clientChannel.write(buffer).get();
                            System.out.println("4");

                            if (buffer.hasRemaining()) {
                                buffer.compact();
                            } else {
                                buffer.clear();
                            }
                            System.out.println("5");
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