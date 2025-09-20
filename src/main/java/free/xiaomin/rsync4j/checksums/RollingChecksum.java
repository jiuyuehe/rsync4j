package free.xiaomin.rsync4j.checksums;

import free.xiaomin.rsync4j.util.Rsync4jConstants;
import free.xiaomin.rsync4j.util.Rsync4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rolling checksum implementation for rsync algorithm.
 * This class performs the core rolling checksum algorithm to find differences
 * between two files efficiently.
 * 
 * @author jiuyuehe
 */
public class RollingChecksum {

	private static final Logger logger = LoggerFactory.getLogger(RollingChecksum.class);

	/**
	 * Source file checksums
	 */
	private FileChecksums srcFile;

	/**
	 * Updated/modified file
	 */
	private File updateFile;

	/**
	 * List to store difference results
	 */
	private List<DiffCheckItem> diffList;

	/**
	 * Random access file for reading
	 */
	private RandomAccessFile raf;

	/**
	 * Random access file for diff operations
	 */
	private RandomAccessFile diffraf;

	public RollingChecksum() {
	}

	public RollingChecksum(FileChecksums srcFile, File updateFile,
			List<DiffCheckItem> diffList) {
		this.srcFile = srcFile;
		this.diffList = diffList;
		this.updateFile = updateFile;
	}

	@Override
	public String toString() {
		return "RollingChecksum [srcFile=" + srcFile + "]";
	}

	/**
	 * Converts file checksums to a map for quick lookup.
	 * 
	 * @return map of weak checksums to block checksums
	 */
	private Map<Long, BlockChecksums> converte2Map() {
		List<BlockChecksums> blist = srcFile.getBlockChecksums();
		Map<Long, BlockChecksums> map = new HashMap<Long, BlockChecksums>();

		for (BlockChecksums blockChecksums : blist) {
			map.put(blockChecksums.getWeakChecksum(), blockChecksums);
		}
		return map;
	}

	/**
	 * Rolling checksum algorithm implementation.
	 * This is the core algorithm that finds differences between files.
	 */
	public void rolling() {
		Map<Long, BlockChecksums> srcMap = converte2Map();

		logger.debug("Source file block count: {}", srcMap.size());

		if (diffList == null) {
			diffList = new ArrayList<DiffCheckItem>();
		}

		long fileLength = updateFile.length();
		int offset = 0;
		do {
			offset = checkBlk(srcMap, offset, diffList);
		} while (offset < fileLength);

		// Close resources properly
		closeResource(diffraf, "diff file");
		closeResource(raf, "source file");
		
		// Calculate statistics
		int totalDiffBytes = 0;
		for (DiffCheckItem item : diffList) {
			if (!item.isMatch()) {
				totalDiffBytes += item.getData().length;
			}
		}
		
		logger.debug("File total size: {} bytes; Different blocks to transfer: {} bytes", 
					fileLength, totalDiffBytes);
	}

	/**
	 * Rolling comparison algorithm.
	 * 
	 * @param srcMap source file blocks map
	 * @param offset current offset in the file
	 * @param difList list to store differences
	 * @return next offset to process
	 */
	private int checkBlk(Map<Long, BlockChecksums> srcMap, int offset,
			List<DiffCheckItem> difList) {
		int start = offset;
		BlockChecksums matchedBlock = null; // Matched block from source file
		BlockChecksums currentBlock = null; // Current block from new file
		
		for (; start < updateFile.length(); start++) {
			currentBlock = getNextBlock(start);
			if (srcMap.containsKey(currentBlock.getWeakChecksum())) {
				matchedBlock = srcMap.get(currentBlock.getWeakChecksum());
				if (matchedBlock.getHexStrongChecksum().equals(
						currentBlock.getHexStrongChecksum())) {
					break;
				}
			}
		}
		
		if (currentBlock != null) {
			int len = start - offset;
			
			if (len > 0) {
				try {
					if (diffraf == null) {
						diffraf = new RandomAccessFile(updateFile, "r");
					}
					byte[] by = new byte[len];
					diffraf.seek(offset);
					diffraf.read(by, 0, len);
					
					DiffCheckItem dl = new DiffCheckItem();
					dl.setMatch(false);
					dl.setData(by);
					difList.add(dl);
					
				} catch (FileNotFoundException e) {
					logger.error("File not found while reading diff block", e);
					throw new Rsync4jException(e);
				} catch (IOException e) {
					logger.error("IO error while reading diff block", e);
					throw new Rsync4jException(e);
				}
				
				if (matchedBlock != null) {
					DiffCheckItem dl = new DiffCheckItem();
					dl.setIndex(matchedBlock.getIndex());
					dl.setMatch(true);
					difList.add(dl);
				}
			} else {
				DiffCheckItem dl = new DiffCheckItem();
				dl.setIndex(matchedBlock.getIndex());
				dl.setMatch(true);
				difList.add(dl);
			}
			return start + Rsync4jConstants.BLOCK_SIZE;
		} else {
			logger.debug("Found diff block at offset: {}", (start - offset));
			return start;
		}
	}

	/**
	 * Helper method to safely close resources.
	 * 
	 * @param resource the resource to close
	 * @param description description for logging
	 */
	private void closeResource(RandomAccessFile resource, String description) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				logger.warn("Failed to close {}", description, e);
			}
		}
	}

	/**
	 * Gets the checksum for the next block at the specified offset.
	 * 
	 * @param offset the offset in the file
	 * @return BlockChecksums for the block at the offset
	 */
	private BlockChecksums getNextBlock(int offset) {
		byte[] buf = new byte[Rsync4jConstants.BLOCK_SIZE];
		try {
			if (raf == null) {
				raf = new RandomAccessFile(updateFile, "r");
			}
			raf.seek(offset);
			int bytesRead = raf.read(buf, 0, Rsync4jConstants.BLOCK_SIZE);
			BlockChecksums blk = new BlockChecksums(buf, offset, bytesRead);
			return blk;
		} catch (FileNotFoundException e) {
			logger.error("File not found while reading block at offset {}", offset, e);
			throw new Rsync4jException(e);
		} catch (IOException e) {
			logger.error("IO error while reading block at offset {}", offset, e);
			throw new Rsync4jException(e);
		}
	}

	// Getter and setter methods

	public FileChecksums getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(FileChecksums srcFile) {
		this.srcFile = srcFile;
	}

	public File getUpdateFile() {
		return updateFile;
	}

	public void setUpdateFile(File updateFile) {
		this.updateFile = updateFile;
	}

	public List<DiffCheckItem> getDiffList() {
		return diffList;
	}

	public void setDiffList(List<DiffCheckItem> diffList) {
		this.diffList = diffList;
	}
}
