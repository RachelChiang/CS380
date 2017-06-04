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
   
   // TODO: Organize.
   // TODO: FIX. If successfully transmitted a file once to a server, cannot start a new
   // java FileTransfer client to the same server without closing the server.
   // TODO: make sure client can choose to send another file or disconnect
   
   public FileClient(String publicKeyFile, String serverHost, String serverListenPort)
   {
      // Connect to server, generate AES session key, encrypt session key using public key
      if (init(publicKeyFile, serverHost, serverListenPort))
      {
         try {
            boolean validInput = false;
            File transferFile = null;
            int chunkSize = 0;
            long fileSize = 0;
            String filePath = "";
            
            while (!validInput)
            {
               // Prompt the user to enter the path for a file to transfer
               System.out.println("Enter path:");
               Scanner sc = new Scanner(System.in);
               filePath = sc.nextLine();
               
               transferFile = new File(filePath);
               if (transferFile.exists())
               {
                  validInput = true;
                  System.out.println("Enter chunk size [1024]:");
                  chunkSize = sc.nextInt();
                  fileSize = transferFile.length();
               }
               else
               {
                  System.out.println("File not found. Please try again.");
               }
            }
            
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            
            // Next, send the server a StartMessage
            Message msg;
            if (chunkSize != 0)
            {
               msg = new StartMessage(transferFile.getName(), encryptedKey, chunkSize);
            }
            else
            {
               msg = new StartMessage(transferFile.getName(), encryptedKey);
            }
            oos.writeObject(msg);
            
            System.out.printf("Sending: %s.  File size: %d\n", filePath, fileSize);
            double chunksToSend = Math.ceil(((double) fileSize)/chunkSize);
            System.out.println("Sending " + chunksToSend + " chunks.");
            
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message serverResponse = (Message) ois.readObject();
            boolean wantContinue = true;
            long fileSizeCounter = fileSize;
            
            if (serverResponse.getType() == MessageType.ACK)
            {
               System.out.println("Read message of type " + serverResponse.getType());
               if (((AckMessage) serverResponse).getSeq() == 0)
               {
                  //wantContinue = true;
                  System.out.println("Transfer will begin.");
               }
               else
               {
                  wantContinue = false;
                  System.out.println("Transfer cannot proceed.");
               }
            }
            
            // Client sends each chunk of the file in order
            // After each chunk, wait for server to respond with appropriate AckMessage
            // seqNum in Ack should be the number for the next expected chunk
            int currSeqNum = 0;
            FileInputStream fin = new FileInputStream(transferFile);
            double totalChunks = Math.ceil(((double) transferFile.length())/chunkSize);
            System.out.println("Beginning transmission of " + totalChunks + " chunks...");
            while (wantContinue)
            {
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
               
               if (totalChunks == currSeqNum)
               {
                  if (fin.read(chunk) == -1)
                  {
                     System.out.println("End of file.");
                  }
                  wantContinue = false;
               }
               else
               {
                  fin.read(chunk);
                  
                  Checksum crc32 = new CRC32();
                  crc32.update(chunk, 0, chunk.length);
                  long errorCode = crc32.getValue();
                  System.out.println("\nGenerated CRC32: "
                                    + String.format("%08X", errorCode));
                  Cipher cipher = Cipher.getInstance("AES");
                  cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
                  try {
                     byte[] encryptedChunk = cipher.doFinal(chunk);
                     msg = new Chunk(currSeqNum, encryptedChunk, (int) errorCode);
                     boolean acknowledged = false;
                     while (!acknowledged)
                     {
                        oos.writeObject(msg);
                        serverResponse = (Message) ois.readObject();
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
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
            }
            fin.close();
            
            // Disconnecting
            // TODO: Make sure to allow client to begin a new transfer or
            // select to disconnect
            msg = new DisconnectMessage();
            oos.writeObject(msg);
            
            //socket.close();
         } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      else
      {
         System.out.println("Please make sure your command line arguments are correct.");
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
}