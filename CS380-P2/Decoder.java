/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 2: 4B/5B NRZI
 */
import java.util.HashMap;

/**
 * This class handles decoding a received stream of bytes that are represented
 * as an array of integers and that themselves represent a 32-Byte signal of
 * bits.
 */
public class Decoder
{
   /**
    * The {@link #table} is a simple lookup table for the 5b->4b mapping. It is
    * initialized in the constructor {@link #Decoder(int, double)} and is filled
    * in {@link #populateTable()}. A HashMap data structure was selected for
    * quick access, and the 5b value is the key because this is being translated
    * from a 5b to the 4b.
    */
   private HashMap<Integer, Byte> table;
   /**
    * The {@link #new5B} array contains only the values 0 and 1. It is filled in
    * {@link #findTrue5BSignal(int[])}, where the received encoded signal is
    * translated from NRZI to the server's original 5b signal.
    */
   private int[] new5B;
   /**
    * This is the baseline from the PhysLayerClient and the server's preamble.
    * It is used in {@link #findTrue5BSignal(int[])} to determine whether a
    * received Byte represents a 0 or 1.
    */
   private double baseline;
   
   /**
    * Constructor.
    */
   public Decoder(double baseline)
   {
      table = new HashMap<Integer, Byte>(16);
      populateTable();
      this.baseline = baseline;
   }
   
   /**
    * This method simply populates the {@link #table} for looking up the 4b
    * mapping that corresponds with a 5b signal.
    */
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

   /**
    * This method is called outside of the Decoder class. It begins the
    * procedure for decoding a received signal. It calls on a handful of helper
    * functions: {@link #findTrue5BSignal(int[])} to fill {@link #new5B}; {@link
    * #accumulateBits(int)} to handle the repetitive bit-shifting; and {@link
    * #printDecodedSignal(byte[])} to print the decoded signal.
    * 
    * @param received - the signal to decode, which are Bytes represented as
    *          integers, which represent a 1 or 0 bit value.
    * @return This returns the true 4b signal, decoded from the NRZI 4B/5B
    *          signal.
    */
   public byte[] decode(int[] received)
   {
      // fills new5B
      findTrue5BSignal(received);
      byte[] true4BSignal = new byte[32];
      // i traverses the new5B array, j traverses the resulting array
      int i = 0;
      int j = 0;
      
      while (i < new5B.length)
      {
         // Bit shift the left and right half of the Bytes separately
         int left = accumulateBits(i);
         i += 5;
         int right = accumulateBits(i);
         i += 5;
         // So that we can look them up in the table separately
         byte leftByte = table.get(left);
         byte rightByte = table.get(right);
         // And finally combine them to form 1 Byte
         true4BSignal[j++] = (byte) ((leftByte << 4) + rightByte);
      }
      
      // A simple printout of the signal.
      printDecodedSignal(true4BSignal);
      
      return true4BSignal;
   }

   /**
    * This method finds the true 5b signal from the original stream of Bytes and
    * saves it in {@link #new5B}. It is not a one-to-one translation from the
    * original5B to the new5B because it is encoded in NRZI.
    * NRZI: If the PRevious signal was LOW, and the CUrrent signal is LOW, then
    *   the ORiginal signal is LOW because we only switch the signal on HIGH. To
    *   clarify, here is a cute little table:
    *     PR  CU | OR 
    *   ---------+-----
    *     LO  LO | LO
    *     LO  HI | HI
    *     HI  LO | HI
    *     HI  HI | LO
    * This method is called in {@link #decode(int[])}.
    * 
    * @param original5B - the original stream of Bytes which are represented as
    *          integers, which represent bits.
    */
   private void findTrue5BSignal(int[] original5B)
   {
      new5B = new int[original5B.length];
      // Given 320 bits,
      // convert bits into original 5b signal

      int previous = 0;
      // This "previous" value is an educated guess. I checked the preamble many
      // times to see how the signal leaves off (HI/LO?), and it seemed to leave
      // off at low every time.
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
         
         previous = original5B[i];
      }
   }
   
   /**
    * This method simply shifts bits around so that they can eventually be
    * looked up. It is called in {@link #decode(int[])} twice, since it only
    * pulls five bits together at a time.
    * @param index - This corresponds to the values to pull together in the
    *          array, {@link #new5B}.
    * @return - returns an integer representing the group of 5 bits.
    */
   private int accumulateBits(int index)
   {
      int b = 0;
      for (int bitShift = 4; bitShift >= 0; --bitShift)
      {
         b += new5B[index++] << bitShift;
      }
      return b;
   }

   /**
    * This simply prints the decoded signal.
    * @param bytes
    */
   private void printDecodedSignal(byte[] bytes)
   {
      System.out.print("Received 32 bytes: ");
      for (int i = 0; i < bytes.length; ++i)
      {
         System.out.print(String.format("%02X", bytes[i]));
      }
      System.out.println();
   }
}
