/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 7: File Transfer (Encryption)
 */
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

/*
 * This class contains the client processes. The steps for file transfer is as
 * follows:
 *  1. Generate the AES session key.
 *  2. Encrypt the session key using the server's public key. Encrypt the key
 *     using Cipher.WRAP_MODE.
 *  3. Prompt the user to enter the path for a file to transfer.
 *  4. If the path is valid, ask the user to enter the desired chunk size in
 *     bytes, where the default of the bytes is 1024.
 *  5. Send the server a StartMessage, to which the server will respond with an
 *     AckMessage with a sequence number of 0 if the transfer can proceed (or -1
 *     if it cannot).
 *  6. Send each chunk of the file in order. After each chunk, wait for the
 *     server to respond with an AckMessage such that the sequence number
 *     matches that of the next expected chunk. With each chunk byte array,
 *     there should be a CRC32 value on the plaintext of the chunk. After cal-
 *     culating the CRC32 value, encrypt the chunk data using the session key.
 *  7. After all chunks are sent and the final Ack was received, transfer is
 *     complete. The client can either begin a new file transfer or disconnect.
 */

/**
 * This class represents the client that communicates with the server. It will
 * initialize by first connecting to the server, generating the AES session key,
 * and encrypting the session key using the public key in the {@link
 * #init(String, String, String)}. The method also initializes a few other
 * fields. If the initialization was successful it will {@link #run()}, but if
 * not, then it will signal the end of the program with a small error message.
 * Once this is done, the client will send a StartMessage using {@link
 * #makeStartMessage()} and if the client is given the okay to proceed, it will
 * begin sending the chunks of the file in order with {@link #sendChunks()}.
 * Finally, it will prompt the user for further instructions (to terminate or
 * to transfer a new file) using {@link #askRestart()}.
 * @author rchiang
 */
public class FileClient
{
   // communication fields
   /**
    * The socket, which is initialized in {@link #init(String, String, String)},
    * using the parameters from {@link #FileClient(String, String, String)}.
    */
   private Socket socket;
   /**
    * The session key, which is generated in {@link
    * #init(String, String, String)} and wrapped into the {@link #encryptedKey}
    * and is used to encrypt the chunk's data in {@link #sendChunks()}.
    */
   private SecretKey sessionKey;
   /**
    * The wrapped {@link #sessionKey}, which will be sent in the {@link
    * #StartMessage.java}.
    */
   private byte[] encryptedKey;
   /**
    * A scanner.
    */
   private Scanner sc;
   /**
    * The socket's object output stream. 
    */
   private ObjectOutputStream oos;
   /**
    * The socket's object input stream.
    */
   private ObjectInputStream ois;
   
   // control and file-related fields
   /**
    * This field correlates to the size of the chunk that will be sent to the
    * server. It is set by the user or is defaulted as 1024.
    */
   private int chunkSize;
   /**
    * This is the file that is being transfered.
    */
   private File transferFile;
   /**
    * This is the number of chunks to send.
    */
   private double chunksToSend;
   
   /**
    * The constructor initializes fields and executes the method {@link #run()}.
    * If the {@link #init(String, String, String)} call returns false, the
    * program will end.
    * @param publicKeyFile - the file name of the public key
    * @param serverHost - the name of the host
    * @param serverListenPort - the port for the host
    */
   public FileClient(String publicKeyFile, String serverHost,
         String serverListenPort)
   {
      // initializations
      transferFile = null;
      chunkSize = 0;
      chunksToSend = 0;
      
      // Connect to server, generate AES session key,
      // encrypt session key using public key
      if (init(publicKeyFile, serverHost, serverListenPort))
      {
         run();
      }
      else
      {
         System.out.println("Please make sure your command line "
               + "arguments are correct.");
      }
   }
   
   /**
    * This method initializes connection with the server. It connects to the
    * server via the {@link #socket}, will generate a {@link #sessionKey}, and
    * encrypt the {@link #sessionKey} into the {@link #encryptedKey} using the
    * public key.
    * @param publicKeyFile - the file name of the public key
    * @param serverHost - the name of the host
    * @param serverListenPort - the port for the host
    * @return - If it was successful it will return true. Otherwise it will
    *       return false
    */
   private boolean init(String publicKeyFile, String serverHost,
         String serverListenPort)
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
         ObjectInputStream fois = new ObjectInputStream(
               new FileInputStream(new File(publicKeyFile)));
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
         return false;
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace(System.err);
         return false;
      } catch (ClassNotFoundException e) {
         e.printStackTrace(System.err);
         return false;
      } catch (NoSuchPaddingException e) {
         e.printStackTrace(System.err);
         return false;
      } catch (InvalidKeyException e) {
         e.printStackTrace(System.err);
         return false;
      } catch (IllegalBlockSizeException e) {
         e.printStackTrace(System.err);
         return false;
      }
      
      return true;
   }

   /**
    * This method loops, allowing the client to send multiple files in one
    * session. First, the client must send a StartMessage to the client, which
    * is handled in {@link #makeStartMessage()}. This sets up the file transfer
    * for the server. If the server gives a positive acknowledgement (a value of
    * 0), then the server is ready to receive the file, so the client begins to
    * {@link #sendChunks()}. After all the chunks have been sent, the client
    * asks the user if he would like to send another file or terminate via
    * {@link #askRestart()}.
    */
   private void run()
   {
      boolean hasMore = true;
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
   
   /**
    * This method interfaces with the user and creates a {@link
    * #StartMessage.java} to send to the server. It asks for a file, checks the
    * existence of the file, asks for the chunk size, and then returns with a
    * new start message depending on the user's input.
    * @return - a new {@link #StartMessage.java} depending on user input
    */
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
      
      System.out.printf("Sending: %s.  File size: %d\n",
               filePath, transferFile.length());
      chunksToSend = Math.ceil(((double) transferFile.length())/chunkSize);
      System.out.println("Sending " + chunksToSend + " chunks.");
            
      // Make StartMessage based off of user input
      if (chunkSize != 0)
      {
         return new StartMessage(
               transferFile.getName(), encryptedKey, chunkSize);
      }
      else
      {
         return new StartMessage(transferFile.getName(), encryptedKey);
      }
   }
   
   /**
    * This method handles sequentially sending the chunks of the file. The
    * client sends each chunk of the file in order, and after each chunk, the
    * client must wait for the server to respond with the correct {@link
    * #AckMessage.java}.
    */
   private void sendChunks()
   {
      // seqNum in Ack should be the number for the next expected chunk
      try
      {
         // current sequence number
         int currSeqNum = 0;
         // file stream and information
         FileInputStream fin = new FileInputStream(transferFile);
         double totalChunks = Math.ceil(
               ((double) transferFile.length())/chunkSize);
         System.out.println("Beginning transmission of " +
               totalChunks + " chunks...");
         // this counter is for the byte array, chunk.
         long fileSizeCounter = transferFile.length();
         // this controls the loop so it keeps going until the file is done
         boolean wantContinue = true;
         
         while (wantContinue)
         {
            // make sure the chunk is less than or equal to the next the
            // remaining contents of the file. Otherwise server may assume there
            // are more bytes in the file than the actual file contains
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
               wantContinue = false;
            }
            else
            {
               // prepare the next chunk for sending to the server
               fin.read(chunk);
               
               // generate the CRC32 to attach to the Chunk message
               Checksum crc32 = new CRC32();
               crc32.update(chunk, 0, chunk.length);
               long errorCode = crc32.getValue();
               
               try
               {
                  // encrypt the chunk
                  Cipher cipher = Cipher.getInstance("AES");
                  cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
                  try
                  {
                     byte[] encryptedChunk = cipher.doFinal(chunk);
                     // create the Chunk message
                     Message msg = new Chunk(
                           currSeqNum, encryptedChunk, (int) errorCode);
                     // wait for the proper acknowledgement
                     boolean acknowledged = false;
                     while (!acknowledged)
                     {
                        // send to the server
                        oos.writeObject(msg);
                        Message serverResponse = (Message) ois.readObject();
                        // ack received
                        if (serverResponse.getType() == MessageType.ACK)
                        {
                           // verify the sequence number
                           if (((AckMessage) serverResponse).getSeq()
                                 == currSeqNum + 1)
                           {
                              // increment the sequence number and stop
                              // trying to send/receive for this specific chunk
                              // because it was successful
                              System.out.println("Chunks completed ["
                                    + (currSeqNum + 1)
                                    + "/" + ((int) chunksToSend) + "]");
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

   /**
    * This method simply prompts for the user's input toward the end of the
    * {@link #run()} loop. The user can disconnect or transfer another file
    * using the letter responses "d" or "t".
    * @return - true if the user wants to transfer more files, false if not
    */
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
   
               Message serverResponse = (Message) ois.readObject();
               if (serverResponse.getType() == MessageType.ACK)
               {
                  if (((AckMessage) serverResponse).getSeq() == -1)
                  {
                     System.out.println("Terminating program.");
                     return false;
                  }
               }
            } catch (IOException | ClassNotFoundException e) {
               e.printStackTrace(System.err);
            }
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
}