/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This project involves passing (Serializable) messages back and forth between
 * the server to play a game of Tic-Tac-Toe. The required steps are as follows:
 *   1. Establish a socket to codebank.xyz on port 38006.
 *   2. Send a ConnectMessage identifying yourself.
 *   3. Send a CommandMessage to start a new game with the server.
 *   4. The server will respond with a BoardMessage showing the starting board
 *      configuration.
 *   5. Send a MoveMessage indicating where you are making your move.
 *   6. The server will move and reply with another BoardMessage.
 *   (5 and 6 repeat until the game ends)
 */
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