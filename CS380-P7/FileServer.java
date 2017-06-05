import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/*
 *  0. Wait for client to connect. Once connected, client will send instances of
    sub-classes of Message. Based on the type of Message, the server will
    perform different actions:
     - Client sends DisconnectMessage: server should close connection and wait
       for a new one.
     - Client sends StartMessage: server should prepare for a file transfer
       based on the information in the message. It should then respond to the
       client with an AckMessage with sequence number 0. If the server is
       unable to begin the file transfer, it should respond with an AckMessage
       with seqNum -1
       File Transfer Preparation:
        - Decrypt client session key from StartMessage using the server's
          private key to an instance of Key. Use Cipher.UNWRAP_MODE to decrypt
          the key. Assume both sides use AES for the symmetric encryption
          algorithm
     - Client sends StopMessage: server should discard the file transfer and
       respond with an AckMessage with seqNum -1
     - Client sends a Chunk and File Transfer has been initiated, server must
       handle the Chunk with the following steps:
        1. Check Chunk's seqNum such that it is indeed the next expected
           seqNum by the server.
        2. If it is, the server should decrypt the data stored in the Chunk
           using the session key from the transfer initialization step
        3. Server should calculate the CRC32 value for the decrypted data and
           compare it with the CRC32 value included in the chunk
        4. If these values match and the seqNum of the chunk is the next
           expected seqNum, the server should accept the chunk by storing the
           data and incrementing the next expected seqNum
        5. Server should respond with an AckMessage with seqNum of the next
           expected chunk
        Example: client sends chunk 0 and the server expects chunk 0
                 If the server accepts chunk 0, server responds with ACK 1.
                 Otherwise, it responds to the client with ACK 0.
        Once the final chunk has been accepted, the transfer is complete.
        The client recognizes this when the server responds with ACK n
        (where n is the total number of chunks in the file).
 */

// TODO: comment
// TODO: FIX. Same server cannot receive from multiple separately, sequentially
// executed clients. Disconnect might not be working right
// TODO: ADJUST. filename output thing
public class FileServer
{
   private ServerSocket serverSocket;
   private Key privateKey;
   
   private boolean clientConnected;
   private Key clientKey;
   private int nextSeqNum;
   private ArrayList<Byte> allChunks;
   private String fileName;
   private int fileInChunks;
   
   public FileServer(String privateKeyFile, String listenPortNumber)
   {
      if(init(privateKeyFile, listenPortNumber))
      {
         reset();
         run();
      }
      else
      {
         System.out.println("Could not start server. Please make sure your command line arguments are correct.");
      }
   }
   
   private boolean init(String privateKeyFile, String listenPortNumber)
   {
      try {
         serverSocket = new ServerSocket(Integer.parseInt(listenPortNumber));
         
         ObjectInputStream fois = new ObjectInputStream(new FileInputStream(new File(privateKeyFile)));
         privateKey = (Key) fois.readObject();
         fois.close();
      } catch (NumberFormatException e1) {
         e1.printStackTrace();
         return false;
      } catch (IOException e1) {
         e1.printStackTrace();
         return false;
      } catch (ClassNotFoundException cfe) {
         cfe.printStackTrace(System.err);
         return false;
      }
      return true;
   }

   private void run()
   {
      while (true)
      {
         // Client connected
         try
         {
            Socket socket = serverSocket.accept();
            String address = socket.getInetAddress().getHostAddress();
            System.out.printf("Client connected: %s%n", address);
            // Client will send instances of sub-classes of Message
            // Based on type of Message, server performs different actions
            ObjectInputStream ois = new ObjectInputStream(
                  socket.getInputStream());
            Message msg = null;
            System.out.println("Beginning communications...");
            
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            
            while (clientConnected)
            {
               msg = (Message) ois.readObject();
               System.out.println("Read message of type " + msg.getType());
               handleMessage(msg);
               oos.writeObject(new AckMessage(nextSeqNum));
            }
            
            ois.close();
            oos.close();
            socket.close();
         } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace(System.err);
         } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
         } finally {
            System.out.println("Client disconnected.");
         }
      }
   }

   private void reset()
   {
      clientConnected = true;
      clientKey = null;
      nextSeqNum = -1;
      allChunks = null;
      fileName = "";
      fileInChunks = 0;
   }

   private void acceptChunk(byte[] outputChunk)
   {
      for (int i = 0; i < outputChunk.length; ++i)
      {
         allChunks.add(outputChunk[i]);
      }
      System.out.println("Chunk received: [" + nextSeqNum + "/" + fileInChunks + "]");
   }
   
   private void outputFile()
   {
      // QOL to make sure the file name does not already exist.
      int copies = 1;
      String outFileName = fileName.substring(0, fileName.indexOf('.')) + "-copy" + copies + ".txt";
      File f = new File(outFileName);
      while (f.exists())
      {
         ++copies;
         outFileName = fileName.substring(0, fileName.indexOf('.')) + "-copy" + copies + ".txt";
         f = new File(outFileName);
      }
      
      System.out.println("Transfer complete.");
      System.out.println("Output path: " + outFileName);
      try {
         FileOutputStream fos = new FileOutputStream(new File(outFileName));
         
         byte[] fileArray = new byte[allChunks.size()];
         for (int i = 0; i < fileArray.length; ++i)
         {
            fileArray[i] = allChunks.get(i);
         }
         
         fos.write(fileArray);
         
         fos.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace(System.err);
      } catch (IOException e) {
         e.printStackTrace(System.err);
      }
   }
   
   private void handleMessage(Message clientMsg)
   {
      switch (clientMsg.getType())
      {
         case DISCONNECT:
            handleMessage(((DisconnectMessage) clientMsg));
            break;
         case START:
            handleMessage(((StartMessage) clientMsg));
            break;
         case STOP:
            handleMessage(((StopMessage) clientMsg));
            break;
         case CHUNK:
            handleMessage(((Chunk) clientMsg));
            break;
         default:
            System.out.println("PAUSE and RESUME Messages are not handled by this server.");
            break;
      }
   }
   
   private void handleMessage(DisconnectMessage clientMsg)
   {
      nextSeqNum = -1;
      clientConnected = false;
   }
   
   private void handleMessage(StartMessage clientMsg)
   {
      Cipher cipher;
      try {
         cipher = Cipher.getInstance("RSA");
         cipher.init(Cipher.UNWRAP_MODE, privateKey);
         clientKey = cipher.unwrap(clientMsg.getEncryptedKey(), "AES", Cipher.SECRET_KEY);
         nextSeqNum = 0;
         allChunks = new ArrayList<Byte>();
         fileName = clientMsg.getFile();
         fileInChunks = (int) Math.ceil((double) clientMsg.getSize()/clientMsg.getChunkSize());
         System.out.println("Receiving file of size " + clientMsg.getSize());
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace(System.err);
      } catch (NoSuchPaddingException e) {
         e.printStackTrace(System.err);
      } catch (InvalidKeyException e) {
         e.printStackTrace(System.err);
      }
   }
   
   private void handleMessage(StopMessage clientMsg)
   {
      reset();
   }
   
   private void handleMessage(Chunk clientMsg)
   {
      if (clientKey != null)
      {
         // File Transfer has been initiated
         // Check Chunk.seqNum such that it is indeed the next expected
         // seqNum by the server
         if (clientMsg.getSeq() == nextSeqNum)
         {
            // It is, so server should decrypt data stored
            // in the Chunk using session key
            try {
               Cipher cipher = Cipher.getInstance("AES");
               cipher.init(Cipher.DECRYPT_MODE, clientKey);
               byte[] outputChunk = cipher.doFinal(clientMsg.getData());
               // Calculate CRC32 value for decrypted data
               Checksum crc32 = new CRC32();
               crc32.update(outputChunk, 0, outputChunk.length);
               long errorCode = crc32.getValue();
               //System.out.println((int) errorCode + " ? " + ((Chunk) msg).getCrc());
               // compare it with the CRC32 value included in the chunk
               if (clientMsg.getCrc() == ((int) errorCode))
               {
                  // If values match, server should accept the chunk by storing
                  // the data and incrementing the next expected seqNum
                  acceptChunk(outputChunk);
                  ++nextSeqNum;
               }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
               e.printStackTrace(System.err);
            } catch (InvalidKeyException e) {
               e.printStackTrace(System.err);
            } catch (IllegalBlockSizeException e) {
               e.printStackTrace(System.err);
            } catch (BadPaddingException e) {
               e.printStackTrace(System.err);
            }
         }
         
         // after the client has finished sending chunks, client can begin a new file
         // transfer or disconnect
         if (nextSeqNum == fileInChunks)
         {
            outputFile();
         }
      }
   }
}