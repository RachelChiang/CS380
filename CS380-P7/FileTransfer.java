/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 7: File Transfer (Encryption)
 */
/**
 * This program accepts command line arguments which will cause it to operate
 * in three different ways: (1) it can generate a pair of keys, (2) it will
 * act as a server, or (3) it will act as a client. A client will be able to
 * transfer encrypted files to a server.
 * @author rchiang
 */
public class FileTransfer
{
   /**
    * @param args - makekeys/server/client
    *    makekeys
    *    server privateKeyFile serverListenPortNumber
    *    client publicKeyFile serverHost serverListenPortNumber
    */
   public static void main(String[] args)
   {
      String invalidInputResponse = "Invalid command line arguments.\n"
            + "Please use one of the following:\n"
            + " makekeys\n"
            + " server privateKeyFile serverListenPortNumber\n"
            + " client publicKeyFile serverHost serverListenPortNumber";
      try
      {
         if (args[0].equals("makekeys"))
         {
            // makekeys
            System.out.println("Creating a pair of RSA keys...");
            KPairGenerator keyGen = new KPairGenerator();
            keyGen.makeKeyPair();
            System.out.println("Success!");
         }
         else if (args[0].equals("server"))
         {
            // operate as a server
            FileServer server = new FileServer(args[1], args[2]);
         }
         else if (args[0].equals("client"))
         {
            // operate as a client
            FileClient client = new FileClient(args[1], args[2], args[3]);
         }
         else
         {
            // the input was wrong
            System.out.println(invalidInputResponse);
         }
      } catch (ArrayIndexOutOfBoundsException e) {
         // the input was wrong
         System.out.println(invalidInputResponse);
      }
   }
}
