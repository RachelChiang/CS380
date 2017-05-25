/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TicTacToeClient
{
   public static void main(String[] args)
   {
      try
      {
         Socket socket = new Socket("codebank.xyz", 38006);
         
         new Thread(new ListenThread(socket)).start();
         new Thread(new WriteThread(socket)).start();
      } catch (UnknownHostException uhe) {
         System.out.print("[M] Could not connect to server.");
         uhe.printStackTrace();
      } catch (IOException ioe) {
         System.out.println("[M] Unexpected IO error occurred.");
         ioe.printStackTrace();
      }
   }
}

/*
 * Establish socket via codebank.xyz on port 38006. Then, begin the game:
 *   1. Send a ConnectMessage identifying yourself
 *   2. Send a CommandMessage to start a new game with the server. You
 *      are always the first player and the server will be second.
 *   3. The server will respond with a BoardMessage showing the starting
 *      board configuration.
 *   4. Send a MoveMessage indicating where you are making your move.
 *   5. The server will move and reply with another BoardMessage.
 *   (3-5 continues until the game ends)
 * 
 *   If a problem occurs, the server will respond with an ErrorMessage.
 *   It will be useful to create a separate listening thread for this
 *   project.
 */