import java.util.HashMap;

public class MagicNumberLookup
{
   private HashMap<Integer, String> table;
   
   public MagicNumberLookup()
   {
      table = new HashMap<Integer, String>(8);
      populateTable();
   }
   
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