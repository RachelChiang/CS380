import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KPairGenerator
{
   /*
    * generate a public/private RSA key pair, written in serialized forms to
    * files public.bin and private.bin, then exit
    */
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
