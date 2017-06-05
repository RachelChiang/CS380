import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FileClient
{
   private Socket socket;
   private SecretKey sessionKey;
   private byte[] encryptedKey;
   private Scanner sc;
   private ObjectOutputStream oos;
   private ObjectInputStream ois;
   
   private boolean hasMore;
   private int chunkSize;
   private File transferFile;
   private double chunksToSend;
   
   // TODO: comment
   // TODO: FIX. If successfully transmitted a file once to a server, cannot start a new
   // java FileTransfer client to the same server without closing the server.
   
   public FileClient(String publicKeyFile, String serverHost, String serverListenPort)
   {
      hasMore = true;
      transferFile = null;
      chunkSize = 0;
      chunksToSend = 0;
      
      // Connect to server, generate AES session key, encrypt session key using public key
      if (init(publicKeyFile, serverHost, serverListenPort))
      {
         run();
      }
      else
      {
         System.out.println("Please make sure your command line arguments are correct.");
      }
   }
   
   private void run()
   {
      while (hasMore)
      {
         try {
            // Set up file transfer with StartMessage
            oos.writeObject(makeStartMessage());
            
            // Get server acknowledgement of start
            Message serverResponse = (Message) ois.readObject();
            boolean serverReady = false;
            if (serverResponse.getType() == MessageType.ACK)
            {
               System.out.println("Read message of type " + serverResponse.getType());
               if (((AckMessage) serverResponse).getSeq() == 0)
               {
                  // server properly acknowledged
                  serverReady = true;
                  System.out.println("Transfer will begin.");
               }
               else
               {
                  // server did not respond with the right ack
                  serverReady = false;
                  System.out.println("Transfer cannot proceed.");
               }
            }
                        
            if (serverReady)
            {
               sendChunks();
            }
            
            // Client can disconnect or begin a new transfer
            hasMore = askRestart();
            
         } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
         } catch (IOException e) {
            e.printStackTrace(System.err);
         } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
         }
      }
      
      try
      {
         ois.close();
         oos.close();
         socket.close();
      } catch (IOException ioe) {
         ioe.printStackTrace(System.err);
      }
   }
   
   private boolean askRestart()
   {
      System.out.println("Disconnect or transfer another file? [d/t]");
      sc.nextLine();
      String userIn = sc.nextLine();
      while (true)
      {
         if (userIn.equalsIgnoreCase("d"))
         {
            try
            {
               oos.writeObject(new DisconnectMessage());

               boolean wait = true;
               while (wait)
               {
                  Message serverResponse = (Message) ois.readObject();
                  if (serverResponse.getType() == MessageType.ACK)
                  {
                     if (((AckMessage) serverResponse).getSeq() == -1)
                     {
                        wait = false;
                     }
                  }
               }
            } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace(System.err);
            }
            System.out.println("Terminating program.");
            return false;
         }
         else if (userIn.equalsIgnoreCase("t"))
         {
            System.out.println("Beginning a new file transfer.");
            return true;
         }
         else
         {
            System.out.println("Bad input. Try again.");
         }
      }
   }
   
   private boolean init(String publicKeyFile, String serverHost, String serverListenPort)
   {
      try
      {
         // Connecting to server
         socket = new Socket(serverHost, Integer.parseInt(serverListenPort));
         System.out.println("Connected to server.");
         
         // Generate AES session key
         KeyGenerator keyGen = KeyGenerator.getInstance("AES");
         SecureRandom random = new SecureRandom();
         keyGen.init(random);
         sessionKey = keyGen.generateKey();
         
         // Encrypt the session key using the public key
         ObjectInputStream fois = new ObjectInputStream(new FileInputStream(new File(publicKeyFile)));
         Key publicKey = (Key) fois.readObject();
         fois.close();
         Cipher cipher = Cipher.getInstance("RSA");
         cipher.init(Cipher.WRAP_MODE, publicKey);
         encryptedKey = cipher.wrap(sessionKey);
         
         oos = new ObjectOutputStream(socket.getOutputStream());
         ois = new ObjectInputStream(socket.getInputStream());
         
         sc = new Scanner(System.in);
      } catch (NumberFormatException | IOException e) {
         e.printStackTrace(System.err);
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace(System.err);
      } catch (ClassNotFoundException e) {
         e.printStackTrace(System.err);
      } catch (NoSuchPaddingException e) {
         e.printStackTrace(System.err);
      } catch (InvalidKeyException e) {
         e.printStackTrace(System.err);
      } catch (IllegalBlockSizeException e) {
         e.printStackTrace(System.err);
      }
      
      return true;
   }
   
   private StartMessage makeStartMessage()
   {
      boolean validInput = false;
      String filePath = "";
      
      while (!validInput)
      {
         // Prompt the user to enter the path for a file to transfer
         System.out.println("Enter path:");
         filePath = sc.nextLine();
         
         transferFile = new File(filePath);
         if (transferFile.exists())
         {
            validInput = true;
            System.out.println("Enter chunk size [1024]:");
            chunkSize = sc.nextInt();
         }
         else
         {
            System.out.println("File not found. Please try again.");
         }
      }
      
      System.out.printf("Sending: %s.  File size: %d\n", filePath, transferFile.length());
      chunksToSend = Math.ceil(((double) transferFile.length())/chunkSize);
      System.out.println("Sending " + chunksToSend + " chunks.");
            
      // Make StartMessage based off of user input
      if (chunkSize != 0)
      {
         return new StartMessage(transferFile.getName(), encryptedKey, chunkSize);
      }
      else
      {
         return new StartMessage(transferFile.getName(), encryptedKey);
      }
   }
   
   private void sendChunks()
   {
      // Client sends each chunk of the file in order
      // After each chunk, wait for server to respond with appropriate AckMessage
      // seqNum in Ack should be the number for the next expected chunk
      try
      {
         int currSeqNum = 0;
         FileInputStream fin = new FileInputStream(transferFile);
         double totalChunks = Math.ceil(((double) transferFile.length())/chunkSize);
         System.out.println("Beginning transmission of " + totalChunks + " chunks...");
         long fileSizeCounter = transferFile.length();
         boolean wantContinue = true;
         
         while (wantContinue)
         {
            // make sure the chunk is less than or equal to the next the remaining contents
            // of the file. Otherwise server may assume there are more bytes in the file
            // than the actual file contains
            byte[] chunk;
            if (fileSizeCounter - chunkSize > 0)
            {
               chunk = new byte[chunkSize];
               fileSizeCounter -= chunkSize;
            }
            else
            {
               chunk = new byte[(int) fileSizeCounter];
            }
            
            // check if end of file reached
            if (totalChunks == currSeqNum)
            {
               // if so, then we want to stop
               if (fin.read(chunk) == -1)
               {
                  System.out.println("End of file.");
               }
               wantContinue = false;
            }
            else
            {
               // prepare the next chunk
               fin.read(chunk);
               
               Checksum crc32 = new CRC32();
               crc32.update(chunk, 0, chunk.length);
               long errorCode = crc32.getValue();
               System.out.println("\nGenerated CRC32: "
                                 + String.format("%08X", errorCode));
               try
               {
                  Cipher cipher = Cipher.getInstance("AES");
                  cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
                  try
                  {
                     byte[] encryptedChunk = cipher.doFinal(chunk);
                     Message msg = new Chunk(currSeqNum, encryptedChunk, (int) errorCode);
                     boolean acknowledged = false;
                     while (!acknowledged)
                     {
                        oos.writeObject(msg);
                        Message serverResponse = (Message) ois.readObject();
                        if (serverResponse.getType() == MessageType.ACK)
                        {
                           if (((AckMessage) serverResponse).getSeq() == currSeqNum + 1)
                           {
                              System.out.println("Chunks completed [" + (currSeqNum + 1) + "/" + chunksToSend + "]");
                              acknowledged = true;
                              ++currSeqNum;
                           }
                        }
                     }
                  } catch (BadPaddingException e) {
                     e.printStackTrace(System.err);
                  } catch (IllegalBlockSizeException e) {
                     e.printStackTrace(System.err);
                  } catch (ClassNotFoundException e) {
                     e.printStackTrace(System.err);
                  }
               } catch (NoSuchAlgorithmException e1) {
                  e1.printStackTrace(System.err);
               } catch (NoSuchPaddingException e1) {
                  e1.printStackTrace(System.err);
               } catch (InvalidKeyException e1) {
                  e1.printStackTrace(System.err);
               }
               
            }
         }
         fin.close();
      } catch (FileNotFoundException fnfe) {
         fnfe.printStackTrace(System.err);
      } catch (IOException ioe) {
         ioe.printStackTrace(System.err);
      }
   }
}