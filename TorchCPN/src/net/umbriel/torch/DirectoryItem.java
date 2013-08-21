package net.umbriel.torch;


import java.util.List;

public class DirectoryItem {

	private Integer[] rawData = new Integer[Constants._WORD_SIZE];
	private int blockAddress = 0;
	private boolean L2Block = false;
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
		if (getBlockAddress()!=0) System.out.println(getFileName()+"."+getExtension()+" SB:"+
				Integer.toHexString(getBlockAddress())+ " size(bytes):"+(getHighRecordNumber()*Constants._CPN_BLOCK_SIZE) +" l2:"+isL2Block());
	}

	/**
	 * Process the raw data into the relevant pieces..
	 */
	private void processBytes() {
		//block address:
		int lsb= this.getRawData()[Constants._BLOCKADDR_LSB_INDEX] & Constants._0xFF_MASK;
		int msb= this.getRawData()[Constants._BLOCKADDR_MSB_INDEX] & Constants._0x7F_MASK;
		this.setBlockAddress( (msb<< Byte.SIZE) + lsb);

		//L2 or L3?
		setL2Block(((this.getRawData()[1] & Constants._0x80_MASK)>>7)==1);

		//high record
		lsb=this.getRawData()[2] & Constants._0xFF_MASK;
		msb=this.getRawData()[3] & Constants._0xFF_MASK;
		this.setHighRecordNumber((msb<<Byte.SIZE) +lsb);

		//user number
		this.setUserNumber(this.getRawData()[Constants._USERNUMBER_INDEX]);

		//Filename...
		StringBuilder tempName = new StringBuilder();
		for (int i=0; i<Constants._CPN_FILENAME_LENGTH; i++) {
			tempName.append((char)(this.getRawData()[i+Constants._FILENAME_INDEX] & Constants._0x7F_MASK));
			this.getAttributes()[i] = ((this.getRawData()[i+Constants._FILENAME_INDEX] & Constants._0x80_MASK)>>7)==1; //set the attributes
		}
		this.setFileName(tempName.toString().trim());

		//Extension...
		tempName = new StringBuilder();
		for (int i=0; i<Constants._CPN_EXTENSION_LENGTH; i++) {
			tempName.append((char)(this.getRawData()[i+Constants._EXTENSION_INDEX] & Constants._0x7F_MASK));
		}
		this.setExtension(tempName.toString());
		this.setReadOnly(((this.getRawData()[Constants._EXTENSION_INDEX]&Constants._0x7F_MASK)>>7)==1);
		this.setSystemFile(((this.getRawData()[Constants._EXTENSION_INDEX+1]&Constants._0x7F_MASK)>>7)==1);
		this.setArchived(((this.getRawData()[Constants._EXTENSION_INDEX+2]&Constants._0x7F_MASK)>>7)==1);
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
	
	private void updateRawData() {
		
	}





}
