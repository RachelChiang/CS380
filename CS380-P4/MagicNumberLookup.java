/*
 * Rachel Chiang
 * CS 380.01 Computer Networks
 * Project 4: IPv6
 */
import java.util.HashMap;

/**
 * This little class just helps with interpreting the magic numbers. It's
 * basically a lookup table.
 */
public class MagicNumberLookup
{
   /**
    * The lookup table for the magic number (the key) and the string (the mapped
    * values) that will describe what the number means.
    */
   private HashMap<Integer, String> table;
   
   /**
    * Constructor.
    */
   public MagicNumberLookup()
   {
      table = new HashMap<Integer, String>(8);
      populateTable();
   }
   
   /**
    * Filling the {@link #table}.
    */
   private void populateTable()
   {
      table.put(0xCAFEBABE, "Packet was correct!");
      table.put(0xCAFED00D, "Error: Version");
      table.put(0xDEADF00D, "Error: A \"Do not implement\" field");
      table.put(0xBBADBEEF, "Error: Payload length");
      table.put(0xFFEDFACE, "Error: Next header");
      table.put(0xFEE1DEAD, "Error: Hop limit");
      table.put(0xDEADC0DE, "Error: Source address");
      table.put(0xABADCAFE, "Error: Destination address");
   }
   
   /**
    * Access from outside is limited. All this does is give the result of the
    * lookup. It will print the corresponding interpretation only if the
    * packet was deemed incorrect.
    * @param magicNumber - The server's response.
    * @return - True if the packet was deemed correct (magicNumber should be
    *    0xCAFEBABE). False otherwise.
    */
   public boolean translate(int magicNumber)
   {
      if (magicNumber == 0xCAFEBABE)
      {
         return true;
      }
      else
      {
         System.out.println(table.get(magicNumber));
         return false;
      }
   }
}