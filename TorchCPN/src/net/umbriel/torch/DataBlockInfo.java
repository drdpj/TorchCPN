package net.umbriel.torch;

public class DataBlockInfo {
	private Integer block;
	private Integer allocation;
	public static Integer FIRST=1;
	public static Integer BOTH=2;
	public static Integer NONE=0;

	public DataBlockInfo() {
		block = 0;
		allocation = DataBlockInfo.NONE;
	}
	
	public DataBlockInfo(Integer block, Integer allocation) {
		this.block=block;
		if (allocation==3) allocation=2; //Dirty fix for binary
		this.allocation=allocation;
	}

	public Integer getBlock() {
		return block;
	}
	
	public Integer getAllocation() {
		return allocation;
	}
}
