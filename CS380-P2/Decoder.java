import java.util.HashMap;

public class Decoder
{
   private HashMap<Integer, Byte> table;
   private int[] original5B;
   private int[] new5B;
   private double baseline;
   
   public Decoder(int[] received, double baseline)
   {
      table = new HashMap<Integer, Byte>(16);
      populateTable();
      original5B = received;
      this.baseline = baseline;
   }
   
   public byte[] decode()
   {
      findTrue5BSignal();
      printOG5B();
      byte[] true4BSignal = new byte[32];
      int j = 0;
      
      for (int i = 0; i < new5B.length; )
      {
         int left = 0;
         int right = 0;
         for (int bitShift = 4; bitShift >= 0; --bitShift)
         {
            left += new5B[i++] << bitShift;
         }
         for (int bitShift = 4; bitShift >= 0; --bitShift)
         {
            right += new5B[i++] << bitShift;
         }
         //System.out.print(Integer.toBinaryString(left) + " " + Integer.toBinaryString(right) + "; ");
         byte leftByte = table.get(left);
         byte rightByte = table.get(right);
         true4BSignal[j++] = (byte) ((leftByte << 4) + rightByte);
         //System.out.print(" " + String.format("%02X", true4BSignal[j-1]));
         
         /*
         // _XXXX
         // _XXXX
         left += new5B[i++] << 4;
         // X_XXX
         left += new5B[i++] << 3;
         // XX_XX
         left += new5B[i++] << 2;
         // XXX_X
         left += new5B[i++] << 1;
         // XXXX_
         left += new5B[i];*/
      }
      
      return true4BSignal;
   }
   
   private void populateTable()
   {
      int[] fiveBitCode = {
            30, 9, 20, 21, 10, 11, 14, 15,
            18, 19, 22, 23, 26, 27, 28, 29};
      for (int i = 0; i < 16; ++i)
      {
         table.put(fiveBitCode[i], (byte) i);
      }
   }
   
   private void findTrue5BSignal()
   {
      new5B = new int[original5B.length];
      // Given 320 bits,
      // convert bits into original 5b signal
      /*
       * Encoded in NRZI.
       * If the previous signal was low and the current signal is low,
       * this means that the original signal is low, since we only switch
       * the signal on high, see below.
       * Pr Cu Or
       * LO LO LO
       * LO HI HI
       * HI LO HI
       * HI HI LO
       */
      int previous = 0; // starts low
      for (int i = 0; i < original5B.length; ++i)
      {
         if (previous < baseline && original5B[i] < baseline)
         {
            // LO LO -> LO
            new5B[i] = 0;
         }
         else if (previous < baseline && original5B[i] > baseline)
         {
            // LO HI -> HI
            new5B[i] = 1;
         }
         else if (previous > baseline && original5B[i] < baseline)
         {
            // HI LO -> HI
            new5B[i] = 1;
         }
         else if (previous > baseline && original5B[i] > baseline)
         {
            // HI HI -> LO
            new5B[i] = 0;
         }
         else
         {
            System.out.println("Possible error in NRZI conversion");
         }
         
         previous = original5B[i];
      }
   }
   
   public void printOG5B()
   {
      for (int i = 0; i < new5B.length; ++i)
      {
         System.out.print(" " + new5B[i]);
      }
      System.out.println();
   }
   
   public void printTable()
   {
      System.out.println(table.toString());
   }
}
