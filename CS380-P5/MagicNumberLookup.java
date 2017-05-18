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
      table.put(0xBAADF00D, "Error: IPv4 Packet!");
      table.put(0xCAFED00D, "Error: UDP Destination Port");
      table.put(0xDEADC0DE, "Error: UDP Checksum");
      table.put(0xBBADBEEF, "Error: UDP Data Length");
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