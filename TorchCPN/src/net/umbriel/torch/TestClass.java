package net.umbriel.torch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;



public class TestClass {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		ArrayList<Sector> diskImage = new ArrayList<Sector>();

		//Open an interleaved DSD image and read it into a series of sectors...
		try {
			FileInputStream fis = new FileInputStream("disk.dsd");
			File test = new File("../");
			
			System.out.println(test.isDirectory()+" "+test.getCanonicalPath());
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
						diskImage.add(currentSector);
					}
				}
			}
			System.out.println("Read "+diskImage.size()+" sectors.");
			Hashtable<Integer,Sector> diskHash = new Hashtable<Integer,Sector>();
			Iterator<Sector> iter = diskImage.iterator();
			while (iter.hasNext()) {
				Sector currentSector = iter.next();
				diskHash.put(currentSector.getBlockNumber(), currentSector);
			}
			System.out.println("Disk Image in Hash");
			//Output files...
			ArrayList<DirectoryItem> directory = new ArrayList<DirectoryItem>();
			for (int i=0; i<16; i++) {
				Sector currentSector = diskImage.get(i);
				//Directory entries are 16 bytes...
				for (int j=0; j<256; j+=16) {
					directory.add(new DirectoryItem((currentSector.getData().subList(j, j+16))));
				}

			}
			System.out.println("Directory size: "+directory.size());
			Iterator<DirectoryItem> iter2 = directory.iterator();
			while (iter2.hasNext()) {
				iter2.next().printInfo();
			}

			//Allocation map is two sectors of 256 bytes.
			ArrayList<Integer> tempData = new ArrayList<Integer>();
			for (int i=16; i<18; i++) {
				tempData.addAll(diskImage.get(i).getData());
			}

			AllocationMap map = new AllocationMap(tempData.toArray(new Integer[0]));
			map.displayMap();

			System.out.println("isBitSet 255,7 "+BitUtils.isBitSet(255, 7));
			//Let's try and extract all the files...
			
			iter2 = directory.iterator();
			while (iter2.hasNext()) {
				//get start block for starters.
				DirectoryItem temp = iter2.next();
				if (temp.getBlockAddress()!=0) {
					if (temp.isL2Block()) {
						System.out.println(temp.getRawFileName()+"."+temp.getRawExtension()+" L2:");
						Sector tempSector = diskHash.get(temp.getBlockAddress());
						for (int i=0; i< tempSector.getData().size(); i+=2) {
							int lsb=tempSector.getData().get(i);
							int msb=tempSector.getData().get(i+1);
							int l3 = (msb<<8)+lsb;
							if (l3!=0) {
								System.out.println("L3 at:"+Integer.toHexString(l3));
							}
						}
					} else {
						System.out.println(temp.getRawFileName()+"."+temp.getRawExtension()+" L3:");
						Sector tempSector = diskHash.get(temp.getBlockAddress());
						for (int i=0; i< tempSector.getData().size();i+=2) {
							int lsb=tempSector.getData().get(i);
							int msb=tempSector.getData().get(i+1);
							int data = ((msb & Constants._0x3F_MASK)<<Byte.SIZE)+lsb;
							int flags = ((msb & Constants._0xC0_MASK)>>Constants._6_BITS);
							if (data!=0) {
								System.out.println("Data at:"+Integer.toHexString(data)+" "+
										Integer.toBinaryString(flags));
							}
						}
					}
				}
	
			}
			System.out.println("for 44672 bytes you need "+DiskImage.requiredSectors(44672)); 
			DiskImage image = new DiskImage(new File("test.dsd"));
			//ArrayList<Integer> data = image.getSector(0, 1, 8).getData();
			//for (Integer d: data) {
			//	System.out.print(Integer.toHexString(d)+" ");
			//}
			image.extractFile("SNAKE.COM", new File("."));
			/**
			DiskImage image = new DiskImage();
			image.addFile(new File("SNAKE.COM"), 0);
			image.addFile(new File("SNAKCHAR.DAT"), 0);
			byte[] image2 = image.getBytes();
			image.printMap();
			FileOutputStream fos = new FileOutputStream("test.dsd");
			fos.write(image2);**/
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
