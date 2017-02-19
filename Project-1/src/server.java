import java.io.*;
import java.net.*;

public class server {
    public static void main(String[] args) throws IOException{
        if (args.length != 1){
            System.err.println("Usage: java server <port #>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        boolean acceptingConnections = true;

        while (acceptingConnections) { //Await successive connections after initialization or client disconnect
            try (
                    //Socket and communication initialization
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                //Successfully opened a connection
                String inLine, outLine;
                serverProtocol serverProtocol = new serverProtocol();
                System.out.println("get connection from " + clientSocket.getRemoteSocketAddress().toString());
                toClient.println("Hello!");

                //Read input until client disconnects or asks server to terminate
                while ((inLine = fromClient.readLine()) != null) {
                    outLine = serverProtocol.processInput(inLine);
                    toClient.println(outLine);
                    System.out.println("get: " + inLine + ", return: " + outLine);
                    if (inLine.equals("terminate")) {
                        acceptingConnections = false;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Exception while listening for connection on port " + port);
                System.out.println(ex.getMessage());
            }
        }
    }
}
