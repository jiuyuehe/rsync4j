package free.xiaomin.rsync4j.checksums;

import free.xiaomin.rsync4j.util.Rsync4jException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

import org.apache.commons.codec.binary.Hex;

/**
 * 文件块校验
 * 
 * @author jiuyuehe
 *
 */
public class BlockChecksums {
	private int index;
	private long offset;
	private long size;
	private long weakChecksum;
	private byte[] strongChecksum;

	public BlockChecksums(byte[] buf, long offset, long size) {
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf);
		this.strongChecksum = generateStrongChecksum(buf);
	}
	
	public BlockChecksums(int index,byte[] buf, long offset, long size) {
		this.index = index;
		this.offset = offset;
		this.size = size;
		this.weakChecksum = generateWeakChecksum(buf);
		this.strongChecksum = generateStrongChecksum(buf);
	}
	
	/**
	 * md5 校验
	 * @param buf
	 * @return
	 */
	private byte[] generateStrongChecksum(byte[] buf) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.update(buf);
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new Rsync4jException(e);
		}
	}
	
	/**
	 * adler32 校验
	 * @param buf
	 * @return
	 */
	private long generateWeakChecksum(byte[] buf) {
		Adler32 adler32 = new Adler32();
		adler32.update(buf);
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
