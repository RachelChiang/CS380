/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class is the thread that handles communication between the user and the
 * ObjectOutputStream, which it uses to send Messages to the server. To begin
 * the game, the user must first give a username using a ConnectMessage, then
 * establish a new game using the CommandMessage. Once the game begins, the user
 * will be taken to a menu that lists the corresponding integers to make a move
 * or end the game. Unfortunately, the commands LIST_PLAYER and SURRENDER do not
 * appear to have been deprecated since they don't function in any way.
 */
public class WriteThread implements Runnable
{
   private Socket socket;
   
   public WriteThread(Socket socket)
   {
      this.socket = socket;
   }

   @Override
   public void run()
   {
      try
      {
         ObjectOutputStream oos =
               new ObjectOutputStream(socket.getOutputStream());
         UserInterface ui = new UserInterface();
         
         String userIn = ui.getUsername();
         
         // Send a ConnectMessage identifying yourself
         Message msg = new ConnectMessage(userIn);
         oos.writeObject(msg);
         
         // Send a CommandMessage to start a new game with the server
         msg = new CommandMessage(CommandMessage.Command.NEW_GAME);
         oos.writeObject(msg);
         
         // Send a MoveMessage indicating where you are making your move
         boolean wantContinue = true;
         int decision = 0;
         
         while (wantContinue)
         {
            // QOL, at least it is to me. Usually the receiving a response takes
            // a bit of time, so the Board doesn't get printed immediately, so
            // I decided to make this thread wait just a little bit. 800 ms is
            // what works on my computer though, so I realize this may not be so
            // nice on other computers if the connection is not similar enough
            Thread.sleep(800);
            
            // It might display an action menu. I think playing is smoother if
            // the main action menu is not printed every player turn. You can
            // display this menu by inputting -1 in one of the two position
            // requests. This seems unimportant now that I've removed all of
            // the deprecated commands though. :\
            decision = ui.askMove();
            
            if (decision == 0) // make move
            {
               // asks what position the user wants to move in
               byte row = ui.requestPosition("row");
               byte col = ui.requestPosition("col");
               if (row != -1 && col != -1)
               {
                  // sends the message as long as it wasn't a "return to main
                  // action menu" input (-1 for at least one of the two)
                  msg = new MoveMessage(row, col);
                  oos.writeObject(msg);
               }
            }
            else if (decision == 1) // end game
            {
               wantContinue = false;
            }
         }
         
         // The loop ends ONLY if there was an end game issued by the human
         msg = new CommandMessage(CommandMessage.Command.EXIT);
         oos.writeObject(msg);
      } catch (SocketException se) {
         // The connection was likely terminated by the server, so the Listen
         // Thread may have closed the socket, meaning this exception would be
         // thrown
         System.out.println("Communication with the server has ended.");
      } catch (IOException ioe) {
         System.out.println("[WT] Unexpected IO error occurred.");
         ioe.printStackTrace();
      } catch (InterruptedException ie) {
         System.out.println("[WT] Thread interrupted.");
         ie.printStackTrace();
      }
   }
}
