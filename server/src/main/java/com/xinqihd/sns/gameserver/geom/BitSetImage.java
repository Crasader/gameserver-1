package com.xinqihd.sns.gameserver.geom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the BitSet to store an image 's data.
 * @author wangqi
 *
 */
public class BitSetImage {
	
	private static final Logger logger = LoggerFactory.getLogger(BitSetImage.class);
	
	private BitSet bitSet;
	
	private int width;
	
	private int height;

	public BitSetImage(BitSet bitSet, int width, int height) {
		this.bitSet = bitSet;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the bitSet
	 */
	public BitSet getBitSet() {
		return bitSet;
	}

	/**
	 * @param bitSet the bitSet to set
	 */
	public void setBitSet(BitSet bitSet) {
		this.bitSet = bitSet;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	
	/**
	 * Check if the pixel is transparent at given (x,y) location.
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isBitSet(int x, int y) {
		int index = y*width+x;
		if ( index < 0 || index > bitSet.size() ) {
			return false;
		}
		boolean set = bitSet.get(index);
		return set;
	}
	
	/**
	 * Set the given pixel's value at position (x,y)
	 * @param x
	 * @param y
	 * @param value
	 */
	public void setBitAt(int x, int y, boolean value) {
		int index = y*width+x;
		if ( index >= 0 && index < bitSet.size() ) {
			bitSet.set(index, value);
		}
	}
	
	/**
	 * Subtract the given BitSetImage from this one, i.e. set those true bit
	 * int the given bitSetImage to false in this one.
	 * @param bitSetImage
	 * @param centerX
	 * @param centerY
	 */
	public void substract(BitSetImage bitSetImage, int centerX, int centerY, 
			int scaleRatio){
		int leftTopX = centerX/scaleRatio - bitSetImage.width/2;
		int leftTopY = centerY/scaleRatio - bitSetImage.height/2;
		int rightBottomX = leftTopX + bitSetImage.width;
		int rightBottomY = leftTopY + bitSetImage.height;
		for ( int y=leftTopY; y<rightBottomY; y++ ) {
			for ( int x=leftTopX; x<rightBottomX; x++ ) {
				if ( bitSetImage.isBitSet(x-leftTopX, y-leftTopY) ) {
					this.setBitAt(x, y, false);
				}
			}
		}
	}
		
	/**
	 * Clone this object
	 */
	@Override
	public BitSetImage clone() {
		BitSet newBitSet = (BitSet)this.bitSet.clone();
		BitSetImage newOne = new BitSetImage(newBitSet, this.width, this.height);
		return newOne;
	}
	
	/**
	 * Output this class to data file format.
	 * @param outFile
	 */
	public void toFile(File outFile) {
		try {
			FileOutputStream fos = new FileOutputStream(outFile);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(width);
			dos.writeInt(height);
			int size = bitSet.size()/8;
			dos.writeInt(size);
			for ( int i=0; i<size; i++ ) {
				byte b = 0;
				for ( int j=0; j<8; j++ ) {
					if ( bitSet.get(i*8+j) ) {
						b |= 1<<j;
					}
				}
				dos.writeByte(b);
			}
			dos.close();
		} catch (Exception e) {
			logger.debug("Failed to output bitsetImage to file: {}. Exception",
					outFile, e.getMessage());
		}
	}
	
	/**
	 * Read the BitSetImage back from output file.
	 * @param inFile
	 * @return
	 */
	public static BitSetImage fromFile(File inFile) {
		try {
			FileInputStream fis = new FileInputStream(inFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			int width = dis.readInt();
			int height = dis.readInt();
			int size = dis.readInt();
			BitSet bitSet = new BitSet(size*8);
			for ( int i=0; i<size; i++ ) {
				byte b = dis.readByte();
				for ( int j=0; j<8; j++ ) {
					if ( ((b>>j) & 0x1) == 1 ) {
						bitSet.set(i*8+j, true);
					}
				}
			}
			BitSetImage bitSetImage = new BitSetImage(bitSet, width, height);
			
			dis.close();
			return bitSetImage;
		} catch (Exception e) {
			logger.debug("Failed to read bitsetImage from file: {}. Exception",
					inFile, e.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitSet == null) ? 0 : bitSet.hashCode());
		result = prime * result + height;
		result = prime * result + width;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitSetImage other = (BitSetImage) obj;
		if (bitSet == null) {
			if (other.bitSet != null)
				return false;
		} else if (!bitSet.equals(other.bitSet))
			return false;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
	
}
