import java.util.InputMismatchException;
import java.util.Scanner;

public class UserInterface
{
   private Scanner sc;
   private boolean menuToggle;
   
   public UserInterface()
   {
      sc = new Scanner(System.in);
      menuToggle = true;
   }
   
   public String getUsername()
   {
      System.out.print("Successfully connected to server.\nPlease enter a username: ");
      String username = sc.nextLine();
      return username;
   }
   
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
               String actions = "ACTIONS: Select an action by inputting the corresponding integer value.\n"
                     + "  0 Make a move\n"
                     + "  1 List the players\n"
                     + "  2 Surrender\n"
                     + "  3 End game\n"
                     + "Input action value: ";
               System.out.print(actions);
               response = sc.nextInt();
               if (response >= 0 && response <= 3)
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
                  System.out.println("Invalid integer response, please try again.");
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
   
   private void skipMenuMsg()
   {
      System.out.println("NOTE! Action menu will no longer be displayed. If you want it, input -1 during one of the row/col requests.");
   }
   
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
               System.out.println("Tic-tac-toe uses a 3x3 board. Please input an integer in [0,2].");
            }
         } catch (InputMismatchException ime) {
            System.out.println("Bad input. Please try again.");
            sc.nextLine();
         }
      }
      return response;
   }
}
