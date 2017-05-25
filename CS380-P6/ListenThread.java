import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import npMessage.BoardMessage;
import npMessage.ErrorMessage;
import npMessage.Message;
import npMessage.MessageType;

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
            msg = (Message) ois.readObject();
            //System.out.println("Received message of type " + msg.getType());
            // Server should respond with BoardMessage
            if (msg.getType() == MessageType.BOARD)
            {
               gameContinue = gd.gameContinue(((BoardMessage) msg).getStatus(), ((BoardMessage) msg).getTurn(), ((BoardMessage) msg).getBoard());
            }
            else if (msg.getType() == MessageType.ERROR)
            {
               if (!(gd.processErrorMsg(((ErrorMessage) msg).getError())))
               {
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
