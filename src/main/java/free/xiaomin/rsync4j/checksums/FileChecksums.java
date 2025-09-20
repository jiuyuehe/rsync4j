package free.xiaomin.rsync4j.checksums;

import free.xiaomin.rsync4j.util.Rsync4jConstants;
import free.xiaomin.rsync4j.util.Rsync4jException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * File checksums calculation and management.
 * This class calculates checksums for entire files and their individual blocks
 * for use in the rsync algorithm.
 * 
 * @author jiuyuehe
 */
public class FileChecksums {

	private static final Logger logger = LoggerFactory.getLogger(FileChecksums.class);

	private String name;
	private byte[] checksum;
	private List<BlockChecksums> blockChecksums = new ArrayList<BlockChecksums>();

	/**
	 * Creates file checksums for the specified file.
	 * 
	 * @param file the file to calculate checksums for
	 */
	public FileChecksums(File file) {
		this.name = file.getName();
		this.checksum = generateFileDigest(file);
		this.blockChecksums = generateBlockChecksums(file);
		
		logger.debug("Generated checksums for file: {} ({} blocks)", 
					file.getName(), blockChecksums.size());
	}

	/**
	 * Generates checksums for individual blocks of the file.
	 * 
	 * @param file the file to process
	 * @return list of block checksums
	 */
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
				list.add(new BlockChecksums(index, buf, offset, bytesRead));
				offset += bytesRead;
				index++;
			}
		} catch (FileNotFoundException e) {
			logger.error("File not found: {}", file.getAbsolutePath(), e);
			throw new Rsync4jException(e);
		} catch (IOException e) {
			logger.error("IO error reading file: {}", file.getAbsolutePath(), e);
			throw new Rsync4jException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.warn("Failed to close file input stream", e);
				}
			}
		}
		return list;
	}
	
	/**
	 * Generates MD5 checksum for the entire file.
	 * 
	 * @param file the file to process
	 * @return MD5 checksum bytes
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
			logger.error("IO error generating file digest: {}", file.getAbsolutePath(), e);
			throw new Rsync4jException(e);
		} catch (NoSuchAlgorithmException e) {
			logger.error("MD5 algorithm not available", e);
			throw new Rsync4jException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.warn("Failed to close file input stream", e);
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
