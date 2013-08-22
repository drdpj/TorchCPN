package net.umbriel.torch;

import java.util.ArrayList;
import java.util.BitSet;
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
	private Integer freeSectors;

	public AllocationMap(Integer[] rawData) {
		rawMap = rawData;
		initialise();
	}

	private void initialise() {
		freeSectors =0;
		sectorAllocation = new ArrayList<Boolean>();
		lookupBlockNumber = new Hashtable<Integer, Integer>(); //That's "index, blocknumber"
		lookupIndexNumber = new Hashtable<Integer,Integer>(); // or "blockNumber, Index"
		//Set up blocks for hashtable...
		int counter=0;
		for (int t=0; t<Constants._TRACKS; t++) {
			for (int s=0; s<Constants._SIDES; s++) {
				for (int sec=0; sec<Constants._WORD_SIZE; sec++) {
					int block=(t*32)+(s*16)+sec;
					lookupBlockNumber.put(counter, block);
					lookupIndexNumber.put(block, counter);
					counter++;
				}
			}
		}
		processData();
		updateFreeSpace();
	}

	public void allocateSector(int block) {
		sectorAllocation.set(lookupIndexNumber.get(block), true);
		updateRawMap();
		updateFreeSpace();
	}

	public void deAllocateSector(int block) {
		sectorAllocation.set(lookupIndexNumber.get(block), false);
		updateRawMap();
		updateFreeSpace();
	}

	public Boolean isAllocated(int block) {
		return sectorAllocation.get(lookupIndexNumber.get(block));
	}

	public Integer getFirstFreeSector() {
		int i=0;
		while (sectorAllocation.get(i)) {
			i++;
		}
		return lookupBlockNumber.get(i);
	}
	
	
	/**
	 * Allocate multiple sectors.
	 * @param sectors
	 */
	public void allocateSectors(Integer[] sectors) {
		for (int i=0; i<sectors.length;i++) {
			allocateSector(sectors[i]);
		}
	}
	
	/**
	 * get a list of n free sectors.
	 * @param n
	 * @return
	 */
	public Integer[] getFreeSectors(Integer n) {
		Integer[] sectorList = new Integer[n];
		int sectorCount =0;
		for (int i=0; i<sectorAllocation.size();i++) {
			if (sectorCount==n) break;
			if (!sectorAllocation.get(i)) {
				sectorList[sectorCount]=lookupBlockNumber.get(i);
				sectorCount++;
			}
		}
		return sectorList;
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

	private void updateRawMap() {
		for (int i=0; i<rawMap.length;i++) {
			BitSet set = new BitSet();
			for (int j=0; j<Byte.SIZE;j++) {
				set.set(j, sectorAllocation.get((i*Byte.SIZE)+j));
			}
			if (set.length()>0) {
				rawMap[i]=(int)set.toLongArray()[0];
			} else {
				rawMap[i]=0;
			}
		}
	}
	
	private void updateFreeSpace() {
		freeSectors=0;
		for (Boolean b: sectorAllocation) {

			if (!b) {
				freeSectors++;
			}
		}
	}
	
	public Integer[] getRawMap() {
		return rawMap;
	}
	
	public Integer getFreeSectorCount() {
		return freeSectors;
	}



}
