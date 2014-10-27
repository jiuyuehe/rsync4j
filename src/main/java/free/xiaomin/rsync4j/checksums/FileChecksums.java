package free.xiaomin.rsync4j.checksums;

import free.xiaomin.rsync4j.util.Rsync4jConstants;
import free.xiaomin.rsync4j.util.Rsync4jException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

/**
 * 文件对比
 * @author jiuyuehe
 *
 */
public class FileChecksums {

	private String name;
	private byte[] checksum;
	private List<BlockChecksums> blockChecksums = new ArrayList<BlockChecksums>();

	public FileChecksums(File file) {
		this.name = file.getName();
		this.checksum = generateFileDigest(file);
		this.blockChecksums = generateBlockChecksums(file);
	}

	private List<BlockChecksums> generateBlockChecksums(File file) {
		List<BlockChecksums> list = new ArrayList<BlockChecksums>();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buf = new byte[Rsync4jConstants.BLOCK_SIZE];
			int bytesRead = 0;
			long offset = 0;
			int index = 0;
			while ((bytesRead = fis.read(buf)) > 0) {
				list.add(new BlockChecksums(index ,buf, offset, bytesRead));
				offset += bytesRead;
				index ++;
			}
		} catch (FileNotFoundException e) {
			throw new Rsync4jException(e);
		} catch (IOException e) {
			throw new Rsync4jException(e);
		}
		return list;
	}
	
	/**
	 * 获取整个文件的MD5
	 * @param file
	 * @return
	 */
	private byte[] generateFileDigest(File file) {
		FileInputStream fis = null;
		try {
			MessageDigest sha = MessageDigest.getInstance(Rsync4jConstants.MD5);
			fis = new FileInputStream(file);
			byte[] buf = new byte[Rsync4jConstants.BLOCK_SIZE];
			int read = 0;
			while ((read = fis.read(buf)) > 0) {
				sha.update(buf, 0, read);
			}
			return sha.digest();
		} catch (IOException e) {
			throw new Rsync4jException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new Rsync4jException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new Rsync4jException(e);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public byte[] getChecksum() {
		return checksum;
	}

	public List<BlockChecksums> getBlockChecksums() {
		return blockChecksums;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("fileChecksums for: ");
		builder.append(getName());
		builder.append("file checksum: ");
		builder.append(getHexChecksum());
		return builder.toString();
	}

	public String getHexChecksum() {
		return new String(Hex.encodeHex(getChecksum()));
	}

}
