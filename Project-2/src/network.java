import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class network {
    static ServerSocket network;
    static PrintWriter toReceiver, toSender;
    static BufferedReader fromReceiver, fromSender;

    public static void main(String[] args) throws IOException {
        if (args.length != 1){
            System.err.println("Usage: java network <port #>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            network = new ServerSocket(port);
        } catch (IOException ex){
            System.out.println("Exception while listening for connection on port " + port);
            System.out.println(ex.getMessage());
            System.exit(-1);
        }

        Thread receiverThread = new Thread(){
            public void run(){
                try (
                    Socket receiver = network.accept();
                    PrintWriter toReceiver = new PrintWriter(receiver.getOutputStream(), true);
                    BufferedReader fromReceiver = new BufferedReader(new InputStreamReader(receiver.getInputStream()));
                ){
                    toReceiver.println("a");
                    String inLine, outLine;
                    while ((inLine = fromReceiver.readLine()) != null) {
                        outLine = inLine;
                        toReceiver.println(outLine);
                        System.out.println("get: " + inLine + ", return: " + outLine);
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make receiver connection");
                }
            }
        };

        Thread senderThread = new Thread(){
            public void run(){
                try (
                    Socket sender = network.accept();
                    PrintWriter toSender = new PrintWriter(sender.getOutputStream(), true);
                    BufferedReader fromSender = new BufferedReader(new InputStreamReader(sender.getInputStream()));
                ){
                    toSender.println("A");
                    String inLine, outLine;
                    while ((inLine = fromSender.readLine()) != null) {
                        outLine = inLine;
                        toSender.println(outLine);
                        System.out.println("get: " + inLine + ", return: " + outLine);
                    }
                } catch (IOException ex){
                    System.out.println("Exception while attempting to make sender connection");
                }
            }
        };

        receiverThread.start();
        senderThread.start();

        System.out.println("threads started");

/*
        try (
                //Socket and communication initialization
                ServerSocket serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();
                PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            //Successfully opened a connection
            String inLine, outLine;
            //serverProtocol serverProtocol = new serverProtocol();
            System.out.println("get connection from " + clientSocket.getRemoteSocketAddress().toString());
            toClient.println("Hello!");

            //Read input until client disconnects or asks server to terminate
            while ((inLine = fromClient.readLine()) != null) {
                outLine = inLine;
                toClient.println(outLine);
                System.out.println("get: " + inLine + ", return: " + outLine);
            }
        } catch (IOException ex) {
            System.out.println("Exception while listening for connection on port " + port);
            System.out.println(ex.getMessage());
        }
*/
    }
}
