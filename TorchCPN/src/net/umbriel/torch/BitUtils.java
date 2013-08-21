package net.umbriel.torch;

final public class BitUtils {

	static public boolean isBitSet(int integer, int bit) {
		System.out.println(Integer.toHexString((int)(Math.pow(2, bit))));
		//System.out.println(" "+Integer.toBinaryString(integer)+" "+integer);
		return (((integer & (int)(Math.pow(2, bit)))>>bit)==1);
		
	}
	
}
