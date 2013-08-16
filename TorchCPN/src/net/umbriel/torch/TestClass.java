package net.umbriel.torch;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
			for (int i=0; i<9; i++) {
				Sector currentSector = diskHash.get(i);
				//Directory entries are 16 bytes...
				for (int j=0; j<256; j+=16) {
					directory.add(new DirectoryItem((currentSector.getData().subList(j, j+16))));
				}
				
			}
			System.out.println("Directory size: "+directory.size());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
