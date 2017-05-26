/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */

/**
 * This class simply deals with the display. It is only used by the listening
 * thread, {@link #ListenThread.java}. It prints the status, turn, and board in
 * {@link #gameContinue(BoardMessage.Status, byte, byte[][])} and the
 * {@link #printStatus(BoardMessage.Status, byte)} methods. It also
 * can print an error message using {@link #processErrorMsg(String)}.
 */
public class GameDisplay
{
   /**
    * This method prints the status, turn, and game board. The status and turn
    * are actually printed by the private method
    * {@link #printStatus(BoardMessage.Status, byte)}. The board is
    * printed after that in a bracketed configuration with the row and column
    * numbers on the top and left sides. An empty board looks like this:
    *    0  1  2
    * 0 [ ][ ][ ]
    * 1 [ ][ ][ ]
    * 2 [ ][ ][ ]
    * @param status - the status from the BoardMessage
    * @param turn - the turn from the BoardMessage
    * @param board - the board from the BoardMessage
    * @return - true if the game should continue and false if the game is ending
    */
   public boolean gameContinue(BoardMessage.Status status,
                              byte turn,
                              byte[][] board)
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

      if (status == BoardMessage.Status.IN_PROGRESS)
      {
         return true;
      }
      
      // If it's not IN_PROGRESS, that means that a surrender, victory,
      // stalemate, or error occurred.
      System.out.println("The game has ended!");
      return false;
   }
   
   /**
    * This method simply prints the turn number and status.
    * @param status - the status from the BoardMessage
    * @param turn - the turn from the BoardMessage
    */
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
            break;
      }
   }
   
   /**
    * This method prints the error message. The server may end communication
    * depending on the error.
    * @param s - the string from the ErrorMessage
    * @return - true if the game can continue after this error or false if the
    *       game ends after the error
    */
   public boolean processErrorMsg(String s)
   {
      if (s.equals("Game stopping.") || s.equals("Name in use."))
      {
         // When the server sends these, the server kills communication
         System.out.println(s + " Communication with the server has ended.");
         return false;
      }
      System.out.println("ERROR: " + s);
      return true;
   }
}
