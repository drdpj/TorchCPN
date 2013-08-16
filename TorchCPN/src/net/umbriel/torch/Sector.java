package net.umbriel.torch;

import java.util.ArrayList;

public class Sector {

	private int side=0;
	private int track=0;
	private int sectorNumber=0;
	private ArrayList<Integer> data;
	private int blockNumber=0;
	
	/**
	 * Create new sector based on block number
	 * @param blockNumber
	 */
	public Sector(int blockNumber) { // don't know if we want to do this
		
	}
	
	/**
	 * 
	 * @param track
	 * @param side
	 * @param sector
	 */
	public Sector(int track, int side, int sector) {
		setTrack(track);
		setSide(side);
		setSectorNumber(sector);
	}

	private void calculateBlock() {
		blockNumber = (getTrack()*32)+(getSide()*16)+getSectorNumber();
	}
	
	private void calculateSector() {// don't know if we want to do this
		
	}
	
	/**
	 * @return the side
	 */
	public int getSide() {
		return side;
	}

	/**
	 * @param side the side to set
	 */
	public void setSide(int side) {
		this.side = side;
		calculateBlock();
	}

	/**
	 * @return the track
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * @param track the track to set
	 */
	public void setTrack(int track) {
		this.track = track;
		calculateBlock();
	}

	/**
	 * @return the sectorNumber
	 */
	public int getSectorNumber() {
		return sectorNumber;
	}

	/**
	 * @param sectorNumber the sectorNumber to set
	 */
	public void setSectorNumber(int sectorNumber) {
		this.sectorNumber = sectorNumber;
		calculateBlock();
	}

	/**
	 * @return the data
	 */
	public ArrayList<Integer> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(ArrayList<Integer> data) {
		this.data = data;
	}

	/**
	 * @return the blockNumber
	 */
	public int getBlockNumber() {
		return blockNumber;
	}

	/**
	 * @param blockNumber the blockNumber to set
	 */
	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	
}
