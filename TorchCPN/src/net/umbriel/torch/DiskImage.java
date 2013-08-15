package net.umbriel.torch;

public class DiskImage {

	/**
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
	
	
}
