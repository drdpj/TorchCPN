package net.umbriel.torch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;


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
	private Hashtable<String, DirectoryItem> directoryHash;
	private Integer numberOfFiles;
	private Integer firstFreeSector;


	/**
	 * Build the disk image from file F
	 * @param f
	 */
	public DiskImage(File f) {
		sectors = new ArrayList<Sector>();
		blockMap = new Hashtable<Integer,Sector>();
		// Load the image, store sectors and put them in the hash
		try {
			FileInputStream fis = new FileInputStream(f);
			//80 tracks, 10 sectors/track
			for (int track = 0; track <Constants._TRACKS; track ++) {
				for (int side = 0; side <Constants._SIDES; side ++) {
					for (int sector =0; sector <Constants._SECTORS; sector ++) {
						Sector currentSector = new Sector(track, side, sector);
						ArrayList<Integer> data = new ArrayList<Integer>();
						for (int bytecount=0; bytecount<Constants._SECTOR_SIZE; bytecount++) {
							data.add(fis.read());
						}
						currentSector.setData(data);
						sectors.add(currentSector);
						blockMap.put(currentSector.getBlockNumber(),currentSector);
					}
				}
			}
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Extract Directory
		directory = new ArrayList<DirectoryItem>();
		for (int i=0; i<Constants._NUMBER_OF_DIRECTORY_SECTORS; i++) {
			Sector currentSector = sectors.get(i);
			//Directory entries are 16 bytes..., there are two sectors of Directory entries..
			for (int j=0; j<Constants._SECTOR_SIZE; j+=Constants._WORD_SIZE) {
				directory.add(new DirectoryItem((currentSector.getData().subList(j, j+Constants._WORD_SIZE))));
			}

		}

		directoryHash = new Hashtable<String,DirectoryItem>();
		numberOfFiles = 0; //Also count the number of files here...
		updateNumberOfFiles();

		//Extract AllocationMap
		ArrayList<Integer> tempData = new ArrayList<Integer>();
		for (int i=Constants._ALLOCATION_MAP_SECTOR; 
				i<Constants._ALLOCATION_MAP_SECTOR+Constants._ALLOCATION_MAP_SIZE; i++) {
			tempData.addAll(sectors.get(i).getData());
		}
		map = new AllocationMap(tempData.toArray(new Integer[0]));
		firstFreeSector = map.getFirstFreeSector();
	}

	/**
	 * Empty constructor
	 */
	public DiskImage() {
		map = new AllocationMap();
		sectors = new ArrayList<Sector>();
		directory = new ArrayList<DirectoryItem>();
		blockMap = new Hashtable<Integer,Sector>();
		numberOfFiles=0;

		//Populate directory with empty items
		for (int i=0; i<Constants._DIRECTORY_SIZE; i++) {
			directory.add(new DirectoryItem());
		}

		//ArrayList of sectors and them add them to the map...
		for (int t=0; t<Constants._TRACKS; t++) {
			for (int side=0; side<Constants._SIDES; side++) {
				for (int s=0; s<Constants._SECTORS; s++) {
					Sector sec = new Sector(t,side,s);
					sectors.add(sec);
					blockMap.put(sec.getBlockNumber(),sec);
				}
			}
		}
		//Sector 0:1:9 byte 0xFE = 0x85
		getSector(0,1,9).getData().set(254, 0x85);

		//Sector 0:1:8 is full of something odd...
		ArrayList<Integer> testPattern = new ArrayList<Integer>();
		for (int i=0xd6; i<0x100; i++) {
			testPattern.add(i);
		}
		for (int i=0; i<0xd6; i++) {
			testPattern.add(i);
		}

		getSector(0,1,8).setData(testPattern);

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
		DirectoryItem d = directoryHash.get(filename);
		try {
			//Here's the outfile...
			File outFile = new File(location.getCanonicalPath()+"/"+filename);
			//Need to put this somewhere...
			FileOutputStream fos = new FileOutputStream(outFile);

			//Get the blocks...
			ArrayList<DataBlockInfo> blocks = new ArrayList<DataBlockInfo>();


			if (d.isL2Block()) { 			//Is this an L2 block?
				//Get the list of l3 blocks
				Integer[] l3list = sectorToWords(blockMap.get(d.getBlockAddress()).getData()); //

				//Add the l3 blocks to the list of what to get...
				for (int i=0; i<l3list.length;i++) {
					blocks.addAll(processL3Block(blockMap.get(l3list[i]).getData()));
				}
			} else {
				//Get the list of what to get
				blocks.addAll(processL3Block(blockMap.get(d.getBlockAddress()).getData()));;			
			}
			for (DataBlockInfo dbi: blocks) {
				Integer[] outData =blockMap.get(dbi.getBlock()).getData().toArray(new Integer[0]);
				// Depending on allocation for that sector write out either 128 or 256 bytes.
				for (int i=0; i<dbi.getAllocation()*Constants._BLOCK_SIZE;i++) { 
					fos.write(outData[i]);
				}
			}
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addFile (File f, Integer user) {
		int fileSize = (int) f.length();
		int sectorSize = requiredSectors(fileSize);
		//Do we have enough space?
		if (sectorSize>map.getFreeSectorCount()) {
			System.out.println("Not enough space for "+f.getName());
		} else {
			DirectoryItem d = directory.get(numberOfFiles); //get the next free directory item
			d.setRawFileName(f.getName().substring(0, f.getName().indexOf('.')).toUpperCase());
			d.setRawExtension(f.getName().substring(f.getName().indexOf('.')+1).toUpperCase());
			Integer[] allocatedSectors = map.getFreeSectors(sectorSize);
			int nextFreeBlockIndex=0;
			d.setHighRecordNumber(((fileSize+Constants._BLOCK_SIZE-1)/Constants._BLOCK_SIZE)-1); //High record is req blocks -1;
			d.setBlockAddress(allocatedSectors[nextFreeBlockIndex]); // Set the directory start block to the first allocated sector
			//First sector will be L2 or L3.


			if (((fileSize+Constants._SECTOR_SIZE-1)/Constants._SECTOR_SIZE)>Constants._BLOCK_SIZE) {
				d.setL2Block(true);				//First sector is L2...
				Sector l2Block = blockMap.get(allocatedSectors[nextFreeBlockIndex]);
				ArrayList<Integer> l2Data = new ArrayList<Integer>();

				//Now we need to generate a series of L3 blocks...
				try {
					FileInputStream fis = new FileInputStream(f);

					boolean moreData=true;
					while (moreData) {
						ArrayList<DataBlockInfo> blockInfo= new ArrayList<DataBlockInfo>();
						nextFreeBlockIndex++;

						Sector l3Block = blockMap.get(allocatedSectors[nextFreeBlockIndex]);
						int lsb= l3Block.getBlockNumber() & Constants._0xFF_MASK;
						int msb= (l3Block.getBlockNumber() >> 8) & Constants._0xFF_MASK;
						l2Data.add(lsb); // record the l3 in the l2..
						l2Data.add(msb);
						for (int i=0; i<Constants._BLOCK_SIZE; i++) { //We're going to have 128 sectors
							ArrayList<Integer> currentData = new ArrayList<Integer>();
							nextFreeBlockIndex++; // we shouldn't over-run as we've calculated this properly...
							Sector currentSector = blockMap.get(allocatedSectors[nextFreeBlockIndex]);
							for (int j=0; j<Constants._SECTOR_SIZE; j++) {
								int dataByte = fis.read();
								if (dataByte>-1) {
									currentData.add(dataByte);
								} else {
									moreData=false;
									System.out.println("pointer at "+fis.getChannel().position());
									break;
								}
							}
							currentSector.setData(currentData);
							int blocksUsed = DataBlockInfo.FIRST;
							if (currentData.size()>Constants._BLOCK_SIZE) {
								blocksUsed= DataBlockInfo.BOTH;
							}
							if (currentData.size() < Constants._SECTOR_SIZE) {
								for (int j=currentData.size(); j<Constants._SECTOR_SIZE; j++) {
									currentData.add(0);
								}
							}
							blockInfo.add(new DataBlockInfo(currentSector.getBlockNumber(),blocksUsed));
							if (moreData==false) break;
						}
						ArrayList<Integer> currentData = new ArrayList<Integer>();
						for (int i=0;i<blockInfo.size();i++) {

							int block = blockInfo.get(i).getBlock();
							msb = (block>>8) & Constants._0xFF_MASK;
							lsb = block & Constants._0xFF_MASK;
							if (blockInfo.get(i).getAllocation()==DataBlockInfo.BOTH) {
								msb = msb | Constants._0xC0_MASK;
							}
							currentData.add(lsb);
							currentData.add(msb);
						}
						while (currentData.size()<Constants._SECTOR_SIZE) { //Pad out sector if necessary...
							currentData.add(0);
						}
						l3Block.setData(currentData);
					}// End of while (moreData)
					while (l2Data.size()<Constants._SECTOR_SIZE) { //Pad the block.
						l2Data.add(0);
					}

					l2Block.setData(l2Data); //set the l2 data
					map.allocateSectors(allocatedSectors); //allocate the sectors


					
					fis.close();  // What a hideous load of duplication. This needs rationalising at some point....
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					//First sector is L3...

					Sector l3Block = blockMap.get(allocatedSectors[0]);
					ArrayList<DataBlockInfo> blockInfo= new ArrayList<DataBlockInfo>();

					FileInputStream fis = new FileInputStream(f);
					for (int i=1; i<allocatedSectors.length;i++) { //for each of my empty sectors
						Sector currentSector = blockMap.get(allocatedSectors[i]);
						ArrayList<Integer> currentData = new ArrayList<Integer>();
						for (int j=0; j<Constants._SECTOR_SIZE; j++) {
							int dataByte = fis.read();
							if (dataByte>-1) { //Whilst there's still data...
								currentData.add(dataByte);
							} else { //Argh! No data!
								break;
							}
						}
						int blocksUsed = DataBlockInfo.FIRST; //We've at least used one block
						if (currentData.size()>Constants._BLOCK_SIZE) {
							blocksUsed= DataBlockInfo.BOTH; //Have we used both blocks?
						}

						while (currentData.size()<Constants._SECTOR_SIZE) { //Pad out sector...
							currentData.add(0);

						}
						currentSector.setData(currentData);
						blockInfo.add(new DataBlockInfo(currentSector.getBlockNumber(),blocksUsed));
					}
					//Sort out the L3 block
					ArrayList<Integer> currentData = new ArrayList<Integer>();
					for (int i=0;i<blockInfo.size();i++) {

						int block = blockInfo.get(i).getBlock();
						int msb = (block>>8) & Constants._0xFF_MASK;
						int lsb = block & Constants._0xFF_MASK;
						if (blockInfo.get(i).getAllocation()==DataBlockInfo.BOTH) {
							msb = msb | Constants._0xC0_MASK;
						}
						currentData.add(lsb);
						currentData.add(msb);
					}
					for (int i=currentData.size();i<Constants._SECTOR_SIZE; i++) { //pad it
						currentData.add(0);
					}
					l3Block.setData(currentData);
					map.allocateSectors(allocatedSectors);
					fis.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		

			}

		}
		//re-write the directory sectors (16 sectors, 16 entries per sector...)
		for (int j=0; j<Constants._NUMBER_OF_DIRECTORY_SECTORS;j++) {
			Sector directorySector = sectors.get(j);
			ArrayList<Integer> directoryData=new ArrayList<Integer>();
			for (int k=(j*Constants._SIZE_OF_DIRECTORY_ENTRY);
					k<(j*Constants._SIZE_OF_DIRECTORY_ENTRY)+
					(Constants._SECTOR_SIZE/Constants._SIZE_OF_DIRECTORY_ENTRY); k++) {
				directoryData.addAll(directory.get(k).getDataArrayList());
			}
			directorySector.setData(directoryData);
		}
		//Write the Allocation Map Sectors... (0:1:6-7, or the two sectors after the 16 directory ones...)
		getSector(0,1,6).setData(Arrays.asList(map.getRawMap()).subList(0, Constants._SECTOR_SIZE).toArray(new Integer[0]));
		getSector(0,1,7).setData(Arrays.asList(map.getRawMap()).subList(Constants._SECTOR_SIZE,Constants._SECTOR_SIZE*2).toArray(new Integer[0]));
		numberOfFiles++;
	}

	/**
	 * Convert bytes to words
	 * @param d
	 * @return
	 */
	private Integer[] sectorToWords(ArrayList<Integer> d) {
		ArrayList<Integer> processed = new ArrayList<Integer>();
		for (int i=0; i<d.size();i+=2) {
			int msb=d.get(i+1);
			int lsb=d.get(i);

			int value=(msb<<Byte.SIZE)+lsb;
			if (value !=0) {
				processed.add(value);
			}
		}
		return processed.toArray(new Integer[0]);
	}

	private ArrayList<DataBlockInfo> processL3Block(ArrayList<Integer> l3) {
		ArrayList<DataBlockInfo> db = new ArrayList<DataBlockInfo>();
		for (int i=0; i<l3.size();i+=2) {
			int msb=l3.get(i+1);
			int lsb=l3.get(i);
			int allocation =((msb & Constants._0xC0_MASK)>>Constants._6_BITS);
			int value=((msb & Constants._0x3F_MASK)<<Byte.SIZE)+lsb;
			if (value !=0) {
				db.add(new DataBlockInfo(value,allocation));
			}
		}
		return db;

	}

	/**
	 * Returns number of sectors required for a particular file.
	 * @param fileSize in bytes
	 * @return
	 */
	public static int requiredSectors(Integer fileSize) {
		int required = (fileSize+Constants._SECTOR_SIZE-1)/Constants._SECTOR_SIZE;
		if (required<=128) {
			//we can use a single L3 block (that's required data+1)
			return required++;
		}  else {
			//Calc no. L2 blocks required:
			int l3 = (required+Constants._BLOCK_SIZE-1)/Constants._BLOCK_SIZE; //One L3 per 128 sectors...
			int l2 = (l3+Constants._BLOCK_SIZE-1)/Constants._BLOCK_SIZE; //One L2 per 128 L3s...
			return required+l3+l2;
		}
	}

	private int sectorToBlock(int track, int side, int sector) {
		return (track*32)+(side*16)+sector; //Can't be mithered to do the maths to de-magic these numbers...
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
	 * Get the raw interleaved disk image
	 * @return the raw interleaved disk image as byte array
	 */
	public byte[] getBytes() {
		ArrayList<Integer> finalData = new ArrayList<Integer>();
		for (Sector s: sectors) {
			finalData.addAll(s.getData());
		}
		byte[] bytes = new byte[finalData.size()];
		for (int i=0;i<bytes.length;i++) {
			bytes[i]=(byte)finalData.get(i).intValue();
		}
		return bytes;
	}
	/**
	 * 
	 */
	private void updateNumberOfFiles() {
		numberOfFiles=0;
		for (DirectoryItem d: directory) {
			if (d.getBlockAddress()!=0) {
				directoryHash.put(d.getFileName(), d);
				numberOfFiles++;
			}
		}
	}

	/**
	 * Display map on stdout
	 */
	public void printMap() {
		map.displayMap();
	}


}
