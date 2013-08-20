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
	private Hashtable<Integer, Integer> lookupBlockNumber;
	private Hashtable<Integer, Integer> lookupIndexNumber;
	private ArrayList<Boolean> sectorAllocation;

	public AllocationMap(Integer[] rawData) {
		rawMap = rawData;
		initialise();
	}
	
	private void initialise() {
		sectorAllocation = new ArrayList<Boolean>();
		lookupBlockNumber = new Hashtable<Integer, Integer>(); //That's "index, blocknumber"
		lookupIndexNumber = new Hashtable<Integer,Integer>();
		//Set up blocks for hashtable...
		int counter=0;
		for (int t=0; t<80; t++) {
			for (int s=0; s<2; s++) {
				for (int sec=0; sec<16; sec++) {
					int block=(t*32)+(s*16)+sec;
					lookupBlockNumber.put(counter, block);
					lookupIndexNumber.put(block, counter);
					counter++;
				}
			}
		}
		processData();
	}

	public void allocateSector(int block) {
		sectorAllocation.set(lookupIndexNumber.get(block), true);
	}

	public void deAllocateSector(int block) {
		sectorAllocation.set(lookupIndexNumber.get(block), false);
	}

	public Boolean isAllocated(int block) {
		return sectorAllocation.get(lookupIndexNumber.get(block));
	}

	public AllocationMap() {
		//Empty map...
		rawMap = new Integer[512];
		for (int i=0; i<512; i++) {
			rawMap[i]=0xFF;
		}
		for (int i=4; i<320; i+=2) {
			rawMap[i]=0;
			rawMap[i+1]=0xFC;
		}
		initialise();
	}


	private void generateRaw() {

	}

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
		for (int t=0; t<rawMap.length/4; t++) {
			System.out.print(" "+String.format("%03d", t)+"  ");
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
