import java.net.*;
import java.io.*;

public class server {
    public static void main(String[] args) throws IOException{
        if (args.length != 1){
            System.err.println("Usage: java server <port #>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ){
            String inLine, outLine;

        } catch (IOException ex){
            System.out.println("Exception while listening for connection on port " + port);
            System.out.println(ex.getMessage());
        }
    }
}
