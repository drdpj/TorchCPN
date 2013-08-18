package net.umbriel.torch;

import java.util.ArrayList;
import java.util.List;

public class DirectoryItem {

	Integer[] rawData = new Integer[16];
	int blockAddress = 0;
	boolean L2Block = false;
	int highRecordNumber = 0;
	int userNumber = 0;
	String fileName = "";
	String extension = "";
	boolean[] attributes = new boolean[8]; //attribute bits
	boolean readOnly = false;
	boolean systemFile = false;
	boolean archived = false;
	int l2msb =0;
	int l2lsb =0;

	public DirectoryItem(List<Integer> data) {
		this.setRawData(data.toArray(new Integer[0]));
	}



	public void printInfo() {
		if (getBlockAddress()!=0) System.out.println(getFileName()+"."+getExtension()+" SB:"+
				Integer.toHexString(getBlockAddress())+ " size(bytes):"+(getHighRecordNumber()*128) +" l2:"+isL2Block());
	}

	/**
	 * Process the raw data into the relevant pieces..
	 */
	private void processBytes() {
		//block address:
		int lsb= this.getRawData()[0] & 0xFF;
		int msb= this.getRawData()[1] & 0x7F;
		this.setBlockAddress( (msb<< 8) + lsb);

		//L2 or L3?
		setL2Block(((this.getRawData()[1] & 0x80)>>7)==1);

		//high record
		lsb=this.getRawData()[2] & 0xFF;
		msb=this.getRawData()[3] & 0xFF;
		this.setHighRecordNumber((msb<<8) +lsb);

		//user number
		this.setUserNumber(this.getRawData()[4]);

		//Filename...
		StringBuilder tempName = new StringBuilder();
		for (int i=0; i<8; i++) {
			tempName.append((char)(this.getRawData()[i+5] & 0x7F));
			this.getAttributes()[i] = ((this.getRawData()[i+5] & 0x80)>>7)==1; //set the attributes
		}
		this.setFileName(tempName.toString());

		//Extension...
		tempName = new StringBuilder();
		for (int i=0; i<3; i++) {
			tempName.append((char)(this.getRawData()[i+0xD] & 0x7F));
		}
		this.setExtension(tempName.toString());
		this.setReadOnly(((this.getRawData()[0xD]&0x7F)>>7)==1);
		this.setSystemFile(((this.getRawData()[0xE]&0x7F)>>7)==1);
		this.setArchived(((this.getRawData()[0xF]&0x7F)>>7)==1);
	}

	public Integer[] getRawData() {
		return rawData;
	}
	public void setRawData(Integer[] data) {
		this.rawData = data;
		processBytes();

	}
	public int getBlockAddress() {
		return blockAddress;
	}
	public void setBlockAddress(int blockAddress) {
		this.blockAddress = blockAddress;
	}
	public boolean isL2Block() {
		return L2Block;
	}
	public void setL2Block(boolean l2Block) {
		L2Block = l2Block;
	}
	public int getHighRecordNumber() {
		return highRecordNumber;
	}
	public void setHighRecordNumber(int highRecordNumber) {
		this.highRecordNumber = highRecordNumber;
	}
	public int getUserNumber() {
		return userNumber;
	}
	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public boolean[] getAttributes() {
		return attributes;
	}
	public void setAttributes(boolean[] attributes) {
		this.attributes = attributes;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public boolean isSystemFile() {
		return systemFile;
	}
	public void setSystemFile(boolean systemFile) {
		this.systemFile = systemFile;
	}
	public boolean isArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
	}





}
