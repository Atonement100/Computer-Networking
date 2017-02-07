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

        while (acceptingConnections) {
            try (
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            ) {
                String inLine, outLine;
                serverProtocol serverProtocol = new serverProtocol();
                System.out.println("get connection from " + clientSocket.getRemoteSocketAddress().toString());
                toClient.println("Hello!");

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
