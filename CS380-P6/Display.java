
public class Display
{
   public Display()
   {
      
   }
   
   public void show(BoardMessage.Status status, byte turn, byte[][] board)
   {
      printStatus(status, turn);
      
      System.out.println("   0  1  2");
      
      for (int row = 0; row < board.length; ++row)
      {
         System.out.print(row + " ");
         for (int col = 0; col < board.length; ++col)
         {
            System.out.print("[");
            if (board[row][col] == 0) // blank
            {
               System.out.print(" ");
            }
            else if (board[row][col] == 1) // p1's X
            {
               System.out.print("X");
            }
            else if (board[row][col] == 2) // p2's O
            {
               System.out.print("O");
            }
            System.out.print("]");
         }
         System.out.println();
      }
   }
   
   private void printStatus(BoardMessage.Status status, byte turn)
   {
      System.out.println("TURN: " + turn);
      System.out.print("STATUS: ");
      switch (status)
      {
         case PLAYER1_SURRENDER:
            System.out.println("Player 1 has surrendered!");
            break;
         case PLAYER2_SURRENDER:
            System.out.println("Player 2 has surrendered!");
            break;
         case PLAYER1_VICTORY:
            System.out.println("Player 1 has won!");
            break;
         case PLAYER2_VICTORY:
            System.out.println("Player 2 has won!");
            break;
         case STALEMATE:
            System.out.println("Stalemate!");
            break;
         case IN_PROGRESS:
            System.out.println("In progress, awaiting user input...");
            break;
         case ERROR:
            System.out.println("Error occurred!");
            break;
         default:
            System.out.println("You shouldn't be here.");
            break;
      }
   }
   
   public boolean printErrorMsg(String s)
   {
      if (s.equals("Game stopping."))
      {
         System.out.println("Ending communication.");
         return false;
      }
      System.out.println("ERROR: " + s);
      return true;
   }
   
   public void printOtherOptions()
   {
      String options = "OTHER OPTIONS: \n"
            + "  \'p\' to list the players\n"
            + "  \'s\' to surrender\n"
            + "To return to the previous menu, input \'r\'.";
      System.out.println(options);
   }
}
