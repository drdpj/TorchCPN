package net.umbriel.torch;


import java.util.List;

public class DirectoryItem {

	private Integer[] rawData = new Integer[Constants._WORD_SIZE];
	private int blockAddress = 0;
	private boolean l2Block = false;
	private int highRecordNumber = 0;
	private int userNumber = 0;
	private String fileName = "";
	private String extension = "";
	private boolean[] attributes = new boolean[Constants._CPN_FILENAME_LENGTH]; //attribute bits
	private boolean readOnly = false;
	private boolean systemFile = false;
	private boolean archived = false;


	public DirectoryItem() {
		
	}
	
	public DirectoryItem(List<Integer> data) {
		this.setRawData(data.toArray(new Integer[0]));
	}



	public void printInfo() {
		if (getBlockAddress()!=0) System.out.println(fileName+"."+extension+" SB:"+
				Integer.toHexString(blockAddress)+ " size(bytes):"+
				(highRecordNumber*Constants._CPN_BLOCK_SIZE) +" l2:"+l2Block+" "
				+Integer.toBinaryString(rawData[Constants._BLOCKADDR_MSB_INDEX]));
	}

	/**
	 * Process the raw data into the relevant pieces..
	 */
	private void processBytes() {
		//block address:
		int lsb= rawData[Constants._BLOCKADDR_LSB_INDEX];
		int msb = BitUtils.stripHighBit(rawData[Constants._BLOCKADDR_MSB_INDEX]);
		blockAddress= (msb<< Byte.SIZE) + lsb;

		//L2 or L3?
		l2Block=BitUtils.isHighBitSet(rawData[Constants._L2_BIT_INDEX]);

		//high record
		lsb=rawData[Constants._HIGHRECORD_LSB_INDEX];
		msb=rawData[Constants._HIGHRECORD_MSB_INDEX];
		highRecordNumber=(msb<<Byte.SIZE) +lsb;

		//user number
		userNumber=this.rawData[Constants._USERNUMBER_INDEX];

		//Filename...
		StringBuilder tempName = new StringBuilder();
		for (int i=0; i<Constants._CPN_FILENAME_LENGTH; i++) {
			tempName.append((char)BitUtils.stripHighBit(rawData[i+Constants._FILENAME_INDEX]));
			attributes[i] = BitUtils.isHighBitSet(rawData[i+Constants._FILENAME_INDEX]); //set the attributes
		}
		fileName=(tempName.toString());

		//Extension...
		tempName = new StringBuilder();
		for (int i=0; i<Constants._CPN_EXTENSION_LENGTH; i++) {
			tempName.append((char)BitUtils.stripHighBit(rawData[i+Constants._EXTENSION_INDEX]));
		}
		extension=tempName.toString();
		readOnly=BitUtils.isHighBitSet(rawData[Constants._READONLY_BIT_INDEX]);
		systemFile=BitUtils.isHighBitSet(rawData[Constants._SYSTEM_BIT_INDEX]);
		archived=BitUtils.isHighBitSet(rawData[Constants._ARCHIVE_BIT_INDEX]);
	}

	public String getFileName() {
		return (fileName.trim()+extension.trim());
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
		return l2Block;
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
	public String getRawFileName() {
		return fileName;
	}
	public void setRawFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getRawExtension() {
		return extension;
	}
	public void setRawExtension(String extension) {
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
