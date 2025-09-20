package free.xiaomin.rsync4j.checksums;

import free.xiaomin.rsync4j.util.Rsync4jException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

/**
 * Block checksum calculation using weak (Adler32) and strong (MD5) checksums.
 * This class represents checksums for a single block of data in the rsync algorithm.
 * 
 * @author jiuyuehe
 */
public class BlockChecksums {
	
	private static final Logger logger = LoggerFactory.getLogger(BlockChecksums.class);
	
	private int index;
	private long offset;
	private long size;
	private long weakChecksum;
	private byte[] strongChecksum;

	/**
	 * Creates block checksums without index.
	 * 
	 * @param buf the data buffer
	 * @param offset the offset in the file
	 * @param size the size of the data
	 */
	public BlockChecksums(byte[] buf, long offset, long size) {
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf, (int)size);
		this.strongChecksum = generateStrongChecksum(buf, (int)size);
	}
	
	/**
	 * Creates block checksums with index.
	 * 
	 * @param index the block index
	 * @param buf the data buffer
	 * @param offset the offset in the file
	 * @param size the size of the data
	 */
	public BlockChecksums(int index, byte[] buf, long offset, long size) {
		this.index = index;
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf, (int)size);
		this.strongChecksum = generateStrongChecksum(buf, (int)size);
	}
	
	/**
	 * Generates MD5 strong checksum for the data.
	 * 
	 * @param buf the data buffer
	 * @param length the length of valid data in buffer
	 * @return MD5 checksum bytes
	 */
	private byte[] generateStrongChecksum(byte[] buf, int length) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(buf, 0, length);
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			logger.error("MD5 algorithm not available", e);
			throw new Rsync4jException(e);
		}
	}
	
	/**
	 * Generates Adler32 weak checksum for the data.
	 * 
	 * @param buf the data buffer
	 * @param length the length of valid data in buffer
	 * @return Adler32 checksum value
	 */
	private long generateWeakChecksum(byte[] buf, int length) {
		Adler32 adler32 = new Adler32();
		adler32.update(buf, 0, length);
		return adler32.getValue();
	}
	
	/**
	 * adler32 校验
	 * @param buf
	 * @return
	 */
	private long generateNextWeakChecksum(byte[] buf, int offset , int length ) {
		Adler32 adler32 = new Adler32();
		adler32.update(buf,offset,length);
		return adler32.getValue();
	}
	
	
	

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	public long getWeakChecksum() {
		return weakChecksum;
	}

	public byte[] getStrongChecksum() {
		return strongChecksum;
	}

	public String getHexStrongChecksum() {
		return new String(Hex.encodeHex(strongChecksum));
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "offset: " + offset + " size: " + size + " weak sum: "
				+ weakChecksum + " strong sum: " + getHexStrongChecksum();
	}
}
