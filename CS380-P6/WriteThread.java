import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import npMessage.CommandMessage;
import npMessage.ConnectMessage;
import npMessage.Message;
import npMessage.MoveMessage;

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
         ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
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
            // QOL, at least it is to me.
            Thread.sleep(800);
            
            decision = ui.askMove();
            
            if (decision == 0) // make move
            {
               byte row = ui.requestPosition("row");
               byte col = ui.requestPosition("col");
               if (row != -1 && col != -1)
               {
                  msg = new MoveMessage(row, col);
                  oos.writeObject(msg);
               }
            }
            else if (decision == 1) // list player
            {
               //msg = new CommandMessage(CommandMessage.Command.LIST_PLAYERS);
               //oos.writeObject(msg);
               // I don't get why this command ends the game
            }
            else if (decision == 2) // surrender
            {
               msg = new CommandMessage(CommandMessage.Command.SURRENDER);
               oos.writeObject(msg);
               wantContinue = false;
            }
            else if (decision == 3) // end game
            {
               wantContinue = false;
            }
         }
         
         msg = new CommandMessage(CommandMessage.Command.EXIT);
         oos.writeObject(msg);
      } catch (SocketException se) {
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
