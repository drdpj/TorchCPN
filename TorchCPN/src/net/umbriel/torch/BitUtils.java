package net.umbriel.torch;

final public class BitUtils {

	static public boolean isBitSet(int integer, int bit) {
		return (((integer & (int)(Math.pow(2, bit)))>>bit)==1);
		
	}
	
	static public int setBit(int integer, int bit) {
		return (integer | (1<<bit));
	}
	
	static public int unSetBit (int integer, int bit) {
		return (integer &~(1<<bit));	
	}
	
	static public int toggleBit (int integer, int bit) {
		return (integer ^(1<<bit));
	}
	
	static public int stripBit(int integer, int bit) {
		int mask = ~((int)Math.pow(2, bit))&0xFF;
		return integer & mask;
	}
	
	static public boolean isHighBitSet(int integer) {
		return isBitSet(integer,7);
	}
	static public int setHighBit (int integer) {
		return setBit(integer, 7);
	}
	static public int unsetHighBit (int integer) {
		return unSetBit(integer, 7);
	}
	static public int stripHighBit(int integer) {
		return stripBit(integer, 7);
	}
	
}
