package net.umbriel.torch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class DiskImage {

	/*
	 * Torch disk images are two sides of 80 tracks with 10 sectors of
	 * 256 bytes.  Each sector stores 2 128byte CPN records.
	 * 
	 * Tracks are addressed as: Cyl:Side:Sector
	 * 
	 * Sector numbers are described as 16bit words:
	 * 
	 * |__Cyl________________|S|_Sec___|
	 * |                     | |       |
	 * |0 0 0 0 1 0 0 1|1 1 1 1 1 0 0 1|
	 * |_____MSB_______|______LSB______|
	 * 
	 * First 11 bits gives Cylinder number (0 to 79), next bit is the side
	 * (0 or 1) and final 4 bits are the sector number (0-9).
	 * For my purposes the first 4 bits will always be 0.
	 * 
	 * The directory spans Cylinder 0 on both sides running from 
	 * 0:0:0 to 0:0:9 then 0:1:0 to 0:1:5 - a total of 16 sectors.
	 * 
	 * Directory entries are constructed as follows, words are Little-Endian:
	 * 
	 * Bytes 0-1	(Little Endian Word)
	 * 	Bits 00-13	: Address of L2/L3 block
	 * 	Bit 14		: 0
	 * 	Bit 15 		: 0 = L3, 1=L2
	 * 
	 * Bytes 2-3	: Highest Record Number in File
	 * 
	 * Byte 4		: User number of file
	 * 
	 * Bytes 5-12	
	 * 	Bits 0-6	: Filename (main part)
	 * 
	 * Bytes 5-8	
	 * 	Bit 7		: User set attributes
	 * 
	 * Bytes 9-12
	 * 	Bit 7		: Reserved
	 * 
	 * Bytes 12-15
	 * 	Bits 0-6	: Filename Extension
	 * 
	 * Byte 13
	 * 	Bit 7 		: 0 = r/w file, 1=r/o rile
	 * 
	 * Byte 14
	 * 	Bit 7		: 0 = user file, 1 = system file
	 * 
	 * Byte 15
	 * 	Bit 7		: 0 = touched, 1 = archived
	 * 
	 * The next sectors, 0:1:6 - 0:1:7 are the allocation map (256x16 bit words)
	 * 
	 * 
	 */

	private ArrayList<DirectoryItem> directory;
	private AllocationMap map;
	private ArrayList<Sector> sectors;
	private Hashtable<Integer, Sector> blockMap;

	/**
	 * Build the disk image from file F
	 * @param f
	 */
	public DiskImage(File f) {
		// Load the image, store sectors and put them in the hash
		try {
			FileInputStream fis = new FileInputStream(f);
			//80 tracks, 10 sectors/track
			for (int track = 0; track <80; track ++) {
				for (int side = 0; side <2; side ++) {
					for (int sector =0; sector <10; sector ++) {
						Sector currentSector = new Sector(track, side, sector);
						ArrayList<Integer> data = new ArrayList<Integer>();
						for (int bytecount=0; bytecount<256; bytecount++) {
							data.add(fis.read());
						}
						currentSector.setData(data);
						sectors.add(currentSector);
						blockMap.put(sectors.indexOf(currentSector),currentSector);
					}
				}
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Extract Directory
		directory = new ArrayList<DirectoryItem>();
		for (int i=0; i<16; i++) {
			Sector currentSector = sectors.get(i);
			//Directory entries are 16 bytes...
			for (int j=0; j<256; j+=16) {
				directory.add(new DirectoryItem((currentSector.getData().subList(j, j+16))));
			}

		}
		
		//Extract AllocationMap
		ArrayList<Integer> tempData = new ArrayList<Integer>();
		for (int i=16; i<18; i++) {
			tempData.addAll(sectors.get(i).getData());
		}
		map = new AllocationMap(tempData.toArray(new Integer[0]));
		
	}

	/**
	 * Empty constructor
	 */
	public DiskImage() {
		map = new AllocationMap();
		sectors = new ArrayList<Sector>();
		directory = new ArrayList<DirectoryItem>();
		blockMap = new Hashtable<Integer,Sector>();

		//Populate directory with empty items
		for (int i=0; i<256; i++) {
			directory.add(new DirectoryItem());
		}

		//ArrayList of sectors and them add them to the map...
		for (int t=0; t<80; t++) {
			for (int side=0; side<2; side++) {
				for (int s=0; s<10; s++) {
					Sector sec = new Sector(t,side,s);
					sectors.add(sec);
					blockMap.put(sectors.indexOf(sec),sec);
				}
			}
		}

	}

	public String[] getFileNames() {
		ArrayList<String> fileNames = new ArrayList<String>();
		for (DirectoryItem d: directory) {
			if (d.getBlockAddress()!=0) {
				fileNames.add(d.getFileName());
			}
		}
		return fileNames.toArray(new String[0]);
	}

	/**
	 * Return a specific sector based on track, side and sector
	 * @param track
	 * @param side
	 * @param sector
	 * @return
	 */
	public Sector getSector(int track, int side, int sector) {
		return blockMap.get(sectorToBlock(track,side,sector));
	}

	/**
	 * Return a specific sector indexed by blockNumber
	 * @param blockNumber
	 * @return
	 */
	public Sector getSector(int blockNumber) {
		return blockMap.get(blockNumber);
	}
	
	/**
	 * Extract a file "filename" to "location"
	 * @param filename
	 * @param location
	 */
	public void extractFile(String filename, File location) {
		//Get the right directory item...
		
	}

	private int sectorToBlock(int track, int side, int sector) {
		return (track*32)+(side*16)+sector;
	}

	/**
	 * Display sector to block mapping on stdOut
	 */
	public void dumpSectors() {
		for (int i=0; i<sectors.size(); i++) {
			System.out.println("Sector "+i+" block:"+Integer.toHexString(sectors.get(i).getBlockNumber()));
		}
	}
	
	/**
	 * Display map on stdout
	 */
	public void printMap() {
		map.displayMap();
	}


}
