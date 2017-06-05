/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 7: File Transfer (Encryption)
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * This class contains code written by Pantic. It generates a public/private RSA
 * key pair and writes them in serialized forms to public.bin and private.bin.
 */
public class KPairGenerator
{
   public void makeKeyPair()
   {
      try
      {
         KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
         gen.initialize(4096); // you can use 2048 for faster key gen
         KeyPair keyPair = gen.generateKeyPair();
         PrivateKey privateKey = keyPair.getPrivate();
         PublicKey publicKey = keyPair.getPublic();
         try (ObjectOutputStream oos = new ObjectOutputStream(
               new FileOutputStream(new File("public.bin"))))
         {
            oos.writeObject(publicKey);
         }
         try (ObjectOutputStream oos = new ObjectOutputStream(
               new FileOutputStream(new File("private.bin"))))
         {
            oos.writeObject(privateKey);
         }
      
      } catch (NoSuchAlgorithmException | IOException e) {
         e.printStackTrace(System.err);
      }
   }
}
