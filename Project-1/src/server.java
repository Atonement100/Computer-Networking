import java.io.*;
import java.net.*;

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
            serverProtocol serverProtocol = new serverProtocol();

            while ((inLine = in.readLine()) != null) {
                outLine = serverProtocol.processInput(inLine);
                out.println(outLine);
                if (inLine.equals("terminate")){
                    break;
                }
            }
        } catch (IOException ex){
            System.out.println("Exception while listening for connection on port " + port);
            System.out.println(ex.getMessage());
        }
    }
}
