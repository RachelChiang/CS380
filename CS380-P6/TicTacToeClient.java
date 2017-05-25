/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TicTacToeClient
{
   private static class ListenThread implements Runnable
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
            Display d = new Display();
            boolean gameover = false;
            while (!gameover)
            {
               msg = (Message) ois.readObject();
               //System.out.println("Received message of type " + msg.getType());
               // Server should respond with BoardMessage
               if (msg.getType() == MessageType.BOARD)
               {
                  d.show(((BoardMessage) msg).getStatus(), ((BoardMessage) msg).getTurn(), ((BoardMessage) msg).getBoard());
                  if (((BoardMessage) msg).getStatus() != BoardMessage.Status.IN_PROGRESS)
                  {
                     System.out.println("Game over. Please terminate.");
                     gameover = true;
                  }
               }
               else if (msg.getType() == MessageType.ERROR)
               {
                  if (!(d.printErrorMsg(((ErrorMessage) msg).getError())))
                  {
                     gameover = true;
                  }
               }
            }
         } catch (IOException ioe) {
            System.out.println("[LT] Unexpected IO error occurred.");
            ioe.printStackTrace();
         } catch (ClassNotFoundException e) {
            System.out.println("[LT] Class not found for this message.");
            e.printStackTrace();
         }
      }
   }
   
   private static class WriteThread implements Runnable
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
            Scanner sc = new Scanner(System.in);
            Display d = new Display();
            
            System.out.print("Please enter a username: ");
            String userIn = sc.nextLine();
            
            // Send a ConnectMessage identifying yourself
            Message msg = new ConnectMessage(userIn);
            oos.writeObject(msg);
            
            // Send a CommandMessage to start a new game with the server
            msg = new CommandMessage(CommandMessage.Command.NEW_GAME);
            oos.writeObject(msg);
            
            // Send a MoveMessage indicating where you are making your move
            boolean wantContinue = true;
            boolean badInput = false;
            
            while (wantContinue)
            {
               try
               {
                  if (!badInput)
                  {
                     // QOL. Hopefully your connection isn't too slow.
                     Thread.sleep(800);
                  }
                  
                  System.out.println("Input row number, \'x\' to terminate, or \'o\' for another option.");
                  if (sc.hasNextByte())
                  {
                     byte row = sc.nextByte();
                     System.out.println("Input column number.");
                     byte col = sc.nextByte();
                     msg = new MoveMessage(row, col);
                     oos.writeObject(msg);

                     badInput = false;
                  }
                  else
                  {
                     String s = sc.next();
                     if (s.equalsIgnoreCase("x"))
                     {
                        System.out.println("Terminating...");
                        wantContinue = false;
                     }
                     else if (s.equalsIgnoreCase("o"))
                     {
                        d.printOtherOptions();
                        s = sc.next();
                        if (s.equalsIgnoreCase("p"))
                        {
                           //msg = new CommandMessage(CommandMessage.Command.LIST_PLAYERS);
                           //oos.writeObject(msg);
                           // I don't get why this command ends the game
                        }
                        else if (s.equalsIgnoreCase("s"))
                        {
                           msg = new CommandMessage(CommandMessage.Command.SURRENDER);
                           oos.writeObject(msg);
                           wantContinue = false;
                        }
                     }
                  }
               } catch (InputMismatchException ime) {
                  System.out.println("[WT] Bad input. Please try again.");
                  sc.nextLine();
                  badInput = true;
               }
            }
            
            msg = new CommandMessage(CommandMessage.Command.EXIT);
            oos.writeObject(msg);
            
         } catch (IOException ioe) {
            System.out.println("[WT] Unexpected IO error occurred.");
            ioe.printStackTrace();
         } catch (InterruptedException ie) {
            System.out.println("[WT] Thread interrupted.");
            ie.printStackTrace();
         }
      }
   }
   
   public static void main(String[] args)
   {
      try
      {
         Socket socket = new Socket("codebank.xyz", 38006);
         System.out.println("Connected to the server.");
         
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