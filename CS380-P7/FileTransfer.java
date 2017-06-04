/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 7: File Transfer (Encryption)
 */
public class FileTransfer
{
   public static void main(String[] args)
   {
      String invalidInputResponse = "Invalid command line arguments.\n"
            + "Please use one of the following:\n"
            + " makekeys\n"
            + " server privateKeyFile serverListenPortNumber\n"
            + " client publicKeyFile serverHost serverListenPortNumber";
      try
      {
         /*
          * commands: makekeys, server, client
          *   makekeys
          *   server <privateKeyFile> <serverListenPortNumber>
          *   client <publicKeyFile> <serverHost> <serverListenPortNumber>
          */
         if (args[0].equals("makekeys"))
         {
            System.out.println("Creating a pair of RSA keys...");
            KPairGenerator keyGen = new KPairGenerator();
            keyGen.makeKeyPair();
            System.out.println("Success!");
         }
         else if (args[0].equals("server"))
         {
            FileServer server = new FileServer(args[1], args[2]);
         }
         else if (args[0].equals("client"))
         {
            FileClient client = new FileClient(args[1], args[2], args[3]);
         }
         else
         {
            System.out.println(invalidInputResponse);
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         System.out.println(invalidInputResponse);
      }
   }
}
