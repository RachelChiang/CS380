/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 6: Tic-Tac-Toe
 */
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This class deals with interaction with the human player and will print things
 * to console. It just does not look very neat having print and scanner state-
 * ments everywhere. This class is used only by the {@link #WriteThread.java}.
 */
public class UserInterface
{
   /**
    * A scanner.
    */
   private Scanner sc;
   
   /**
    * This field just toggles whether to show the action menu or not. The action
    * menu is shown in {@link #askMove()} calls when the menuToggle is true. It
    * is an unnecessary thing that is sort of a quality-of-life feature that I
    * am including because I prefer going straight into inputing the row/column
    * numbers while the game loops rather than having to go through multiple
    * menus or having too many input options.
    */
   private boolean menuToggle;
   
   /**
    * The constructor.
    */
   public UserInterface()
   {
      sc = new Scanner(System.in);
      menuToggle = true;
   }
   
   /**
    * This simply asks the user for a username.
    * @return - a string representing the username that will be passed to the
    *    server in the ConnectMessage
    */
   public String getUsername()
   {
      System.out.print("Successfully connected to server.\n"
            + "Please enter a username: ");
      String username = sc.nextLine();
      return username;
   }
   
   /**
    * This method prints the menu of actions, which are only to make a move or
    * to end the game, since the other commands appear to have been deprecated.
    * Notably, this will only prompt for user action if {@link #menuToggle} is
    * true. Otherwise, it will always return 0.
    * @return - 0 if the user wants to make a move, or 1 if the user wants to
    *       end the game
    */
   public int askMove()
   {
      if (menuToggle)
      {
         int response = 0;
         boolean validInput = false;
         while (!validInput)
         {
            try
            {
               // decision display
               String actions =
                     "ACTIONS: Select an action by entering the corresponding "
                     + "integer value.\n"
                     + "  0 Make a move\n"
                     + "  1 End game\n"
                     + "Input action value: ";
               System.out.print(actions);
               
               // handling the user's response
               response = sc.nextInt();
               if (response == 0 || response == 1)
               {
                  if (response == 0)
                  {
                     menuToggle = false;
                     skipMenuMsg();
                  }
                  validInput = true;
               }
               else
               {
                  System.out.println("Invalid integer response."
                        +" Please try again.");
               }
            } catch (InputMismatchException ime) {
               System.out.println("Please input an integer.");
               sc.nextLine();
            }
         }
         return response;
      }
      return 0;
   }
   
   /**
    * This simply notifies the player that if they want to return to the action
    * menu, they should input -1 in at least one of the requests for the row or
    * column.
    */
   private void skipMenuMsg()
   {
      System.out.println("NOTE! Action menu will no longer be displayed. "
            + "If you want it, input -1 during one of the row/col requests.");
   }
   
   /**
    * This method requests from the player which row and column numbers to fill.
    * @param type - it just fills in whether it's the row or column message
    * @return - the row or column for the board, or -1 to go back
    */
   public byte requestPosition(String type)
   {
      boolean validInput = false;
      byte response = 3;
      while (!validInput)
      {
         try
         {
            System.out.printf("Input %s number: ", type);
            response = sc.nextByte();
            if (response >= 0 && response <= 2)
            {
               validInput = true;
            }
            else if (response == -1)
            {
               menuToggle = true;
               validInput = true;
            }
            else
            {
               System.out.println("Tic-tac-toe uses a 3x3 board."
                     + " Please input an integer in [0,2].");
            }
         } catch (InputMismatchException ime) {
            System.out.println("Bad input. Please try again.");
            sc.nextLine();
         }
      }
      return response;
   }
}
