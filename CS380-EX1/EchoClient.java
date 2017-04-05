import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 22222)) {
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os, true, "UTF-8");

            Scanner sc = new Scanner(System.in);
            String msg = "";

            while (!(msg.equals("exit")))
            {
               // Prompt for client input
                System.out.printf("Client> ");
                msg = sc.nextLine();
                out.printf("%s%n", msg);
                msg = br.readLine();

                if(!(msg.equals("exit")))
                {
                   // The server will echo
                    System.out.println("Server> " + msg);
                }
            }
        }
    }
}
