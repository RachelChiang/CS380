/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * This class is the listening thread, which receives information from the
 * server. It does not actually do a whole lot other than listen and call a
 * class to print the board and can end the game depending on what it receives.
 */
public class ListenThread implements Runnable
{
   private Socket socket;
   
   public ListenThread(Socket socket)
   {
      this.socket = socket;
   }

   @Override
   public void run()
   {
      try
      {
         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
         Message msg = null;
         GameDisplay gd = new GameDisplay();
         boolean gameContinue = true;
         
         while (gameContinue)
         {
            // receive a message from the server
            msg = (Message) ois.readObject();
            
            // Server should have responded with BoardMessage
            if (msg.getType() == MessageType.BOARD)
            {
               // this simply prints out the status, turn, and board
               // It returns true or false depending on whether or not the game
               // should continue
               gameContinue = gd.gameContinue(
                     ((BoardMessage) msg).getStatus(),
                     ((BoardMessage) msg).getTurn(),
                     ((BoardMessage) msg).getBoard());
            }
            // Server may also respond with an error message
            else if (msg.getType() == MessageType.ERROR)
            {
               if (!(gd.processErrorMsg(((ErrorMessage) msg).getError())))
               {
                  // Most error messages end the game, and half of the Commands
                  // I have found were actually deprecated and lead straight
                  // to an ErrorMessage...
                  gameContinue = false;
               }
            }
         }
         socket.close();
      } catch (IOException ioe) {
         System.out.println("[LT] Unexpected IO error occurred.");
         ioe.printStackTrace();
      } catch (ClassNotFoundException e) {
         System.out.println("[LT] Class not found for this message.");
         e.printStackTrace();
      }
   }
}
