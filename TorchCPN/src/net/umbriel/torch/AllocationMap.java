package net.umbriel.torch;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Allocation map...
 * @author daniel
 *
 */

public class AllocationMap {

	private Integer[] rawMap;
	private Hashtable<Integer, Integer> lookup;
	private ArrayList<Boolean> sectorAllocation;
	
	public AllocationMap(Integer[] rawData) {
		rawMap = rawData;
		sectorAllocation = new ArrayList<Boolean>();
		lookup = new Hashtable<Integer, Integer>(); //That's "index, blocknumber"
		//Set up blocks for hashtable...
		int counter=0;
		for (int t=0; t<80; t++) {
			for (int s=0; s<2; s++) {
				for (int sec=0; sec<16; sec++) {
					int block=(t*32)+(s*16)+sec;
					lookup.put(counter, block);
					counter++;
				}
			}
		}
		processData();
	}
	
	
	/**
	 * 
	 */
	private void processData() {
		for (int i=0; i<rawMap.length; i++) {
			int currentByte = rawMap[i].intValue(); //is this implicit? probably...
			//Bits..
			for (int j=0; j<8; j++) {
				sectorAllocation.add((currentByte & 0x1)==1);
				currentByte = currentByte >> 1;
			}
		}
	}
	
	public void displayMap() {
		System.out.println("      |  Side 0 Sectors  |  Side 1 Sectors ");
		System.out.println("Track | 0123456789ABCDEF | 0123456789ABCDEF");
		System.out.println("======|==================|=================");
		int counter=0;
		for (int t=0; t<80; t++) {
			System.out.print(" "+String.format("%02d", t)+"  ");
			for (int s = 0; s<2; s++) {
				System.out.print(" | ");
				for (int sec=0; sec<16; sec++) {
					if (sectorAllocation.get(counter)) {
						System.out.print("1");
					} else {
						System.out.print("0");
					}
					counter++;
				}
			}
			System.out.println();
		}
		
	}



}
