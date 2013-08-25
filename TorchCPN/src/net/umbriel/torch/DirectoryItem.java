package net.umbriel.torch;


import java.util.ArrayList;
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
		for (int i=0; i<Constants._WORD_SIZE; i++) {
			rawData[i]=0;
		}
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
		return (fileName.trim()+"."+extension.trim());
	}

	public Integer[] getRawData() {
		return rawData;
	}
	
	public ArrayList<Integer> getDataArrayList() {
		ArrayList<Integer> data = new ArrayList<Integer>();
		for (int i=0; i< rawData.length; i++) {
			data.add(rawData[i]);
		}
		return data;
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
		updateRaw();
	}
	public boolean isL2Block() {
		return l2Block;
	}

	public void setL2Block(boolean b) {
		this.l2Block=b;
		updateRaw();
	}

	public int getHighRecordNumber() {
		return highRecordNumber;
	}
	public void setHighRecordNumber(int highRecordNumber) {
		this.highRecordNumber = highRecordNumber;
		updateRaw();
	}
	public int getUserNumber() {
		return userNumber;
	}
	public void setUserNumber(int userNumber) {
		this.userNumber = userNumber;
		updateRaw();
	}
	public String getRawFileName() {
		return fileName;
	}
	public void setRawFileName(String fileName) {
		while (fileName.length()<8) {
			fileName = fileName+" ";
		}
		this.fileName = fileName;
		updateRaw();
	}
	public String getRawExtension() {
		return extension;
	}
	public void setRawExtension(String extension) {
		while (extension.length()<3) {
			extension = extension+" ";
		}
		this.extension = extension;
		updateRaw();
	}
	
	public boolean[] getAttributes() {
		return attributes;
	}
	public void setAttributes(boolean[] attributes) {
		this.attributes = attributes;
		updateRaw();
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		updateRaw();
	}
	public boolean isSystemFile() {
		return systemFile;
	}
	public void setSystemFile(boolean systemFile) {
		this.systemFile = systemFile;
		updateRaw();
	}
	public boolean isArchived() {
		return archived;
	}
	public void setArchived(boolean archived) {
		this.archived = archived;
		updateRaw();
	}

	private void updateRaw() {
		int msb=0;
		int lsb=0;
		//Block address, little endian
		lsb=blockAddress & Constants._0xFF_MASK;
		msb=(blockAddress >>8) & Constants._0xFF_MASK;
		rawData[Constants._BLOCKADDR_LSB_INDEX]=lsb;
		rawData[Constants._BLOCKADDR_MSB_INDEX]=msb;
		
		//bit 14=0
		BitUtils.unSetBit(rawData[Constants._BLOCKADDR_MSB_INDEX],6);
		//bit 15=L2/L3
		if (l2Block) {
			msb=rawData[Constants._BLOCKADDR_MSB_INDEX];
			msb=BitUtils.setHighBit(msb);
			rawData[Constants._BLOCKADDR_MSB_INDEX]=msb;
		} else {
			msb=rawData[Constants._BLOCKADDR_MSB_INDEX];
			msb=BitUtils.unsetHighBit(msb);
			rawData[Constants._BLOCKADDR_MSB_INDEX]=msb;
		}

		//High record number, little endian
		lsb=highRecordNumber & Constants._0xFF_MASK;
		msb=(highRecordNumber >>8) & Constants._0xFF_MASK;
		rawData[Constants._HIGHRECORD_LSB_INDEX]=lsb;
		rawData[Constants._HIGHRECORD_MSB_INDEX]=msb;
		//User Number
		rawData[Constants._USERNUMBER_INDEX]=userNumber;

		//Filename (8chars)
		for (int i=0; i<fileName.length(); i++) {
			rawData[Constants._FILENAME_INDEX+i]=(int)fileName.charAt(i);
		}
		
		//Attributes
		for (int i=0; i<attributes.length; i++) {
			if (attributes[i]) {
				BitUtils.setHighBit(rawData[Constants._FILENAME_INDEX]+i);
			} else {
				BitUtils.unsetHighBit(rawData[Constants._FILENAME_INDEX]+i);
			}
		}

		//Extension (3chars)
		
		for (int i=0; i<extension.length(); i++) {
			rawData[Constants._EXTENSION_INDEX+i]=(int)extension.charAt(i);
		}
		
		
		//RW-USR-ARCHIVE bits

		if (systemFile) {
			BitUtils.setHighBit(rawData[Constants._SYSTEM_BIT_INDEX]);
		} 
		else BitUtils.unsetHighBit(rawData[Constants._SYSTEM_BIT_INDEX]);

		if (readOnly) {
			BitUtils.setHighBit(rawData[Constants._READONLY_BIT_INDEX]);
		}
		else BitUtils.unsetHighBit(rawData[Constants._READONLY_BIT_INDEX]);
		if (archived) {
			BitUtils.setHighBit(rawData[Constants._ARCHIVE_BIT_INDEX]);
		}
		else BitUtils.unsetHighBit(rawData[Constants._ARCHIVE_BIT_INDEX]);
	}





}
