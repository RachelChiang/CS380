import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public final class EchoServer
{
    private static class ServerThread implements Runnable
    {
      // socket for the client side of the connection
        private Socket socket;
        public ServerThread(Socket socket)
        {
            this.socket = socket;
        }

       // Override for runnable. The thread will run this.
        @Override
        public void run()
        {
            try {
               // Retrieves the address the client is using
                String address = socket.getInetAddress().getHostAddress();
                System.out.printf("Client connected: %s%n", address);
               // Retrieves the output stream of the socket
               // and instantiates a PrintStream with it
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os, true, "UTF-8");

               // Gets the input stream for the socket
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
               // Used to read from the char input stream and buffers the chars
                BufferedReader br = new BufferedReader(isr);

               // The loop runs until the client enters "exit"
               // br.readLine() returns a String containing the contents of the line,
               // not including any line-termination characters
               // It will return null if the end of the stream has been reached
                String msg = br.readLine();
                while (msg != null)
                {
                    out.printf("%s%n", msg);
                    msg = br.readLine();
                }
               // Client exited the program
                System.out.printf("Client disconnected: %s%n", address);
            } catch (IOException ioe) {
                System.out.printf("Unexpected error occurred.");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(22222)) {
            try {
                while (true)
                {
                   // Starts a new thread whenever a client connects
                    (new Thread(new ServerThread(serverSocket.accept()))).start();
                }
            } finally {
                serverSocket.close();
            }
        }
    }
}
