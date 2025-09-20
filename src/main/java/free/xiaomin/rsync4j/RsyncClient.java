package free.xiaomin.rsync4j;

import free.xiaomin.rsync4j.checksums.DiffCheckItem;
import free.xiaomin.rsync4j.checksums.FileChecksums;
import free.xiaomin.rsync4j.checksums.RollingChecksum;
import free.xiaomin.rsync4j.util.Rsync4jConstants;
import free.xiaomin.rsync4j.util.RsyncFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level client for rsync operations.
 * This class provides a simple API for third-party developers to perform
 * file synchronization using the rsync algorithm.
 * 
 * @author jiuyuehe
 */
public class RsyncClient {
    
    private static final Logger logger = LoggerFactory.getLogger(RsyncClient.class);
    
    private int blockSize;
    
    /**
     * Creates a new RsyncClient with default block size.
     */
    public RsyncClient() {
        this(Rsync4jConstants.getBLOCK_SIZE());
    }
    
    /**
     * Creates a new RsyncClient with specified block size.
     * 
     * @param blockSize the block size to use for checksums
     */
    public RsyncClient(int blockSize) {
        this.blockSize = blockSize;
        Rsync4jConstants.setBLOCK_SIZE(blockSize);
    }
    
    /**
     * Synchronizes two files and creates a new merged file.
     * 
     * @param sourceFile the original file
     * @param modifiedFile the modified file
     * @param targetFile the output file to create
     * @throws IOException if an I/O error occurs
     */
    public void synchronize(File sourceFile, File modifiedFile, File targetFile) throws IOException {
        validateFiles(sourceFile, modifiedFile);
        validateOutputFile(targetFile);
        
        logger.info("Starting synchronization: {} + {} -> {}", 
                   sourceFile.getName(), modifiedFile.getName(), targetFile.getName());
        
        long startTime = System.currentTimeMillis();
        
        // Generate checksums for source file
        FileChecksums sourceChecksums = new FileChecksums(sourceFile);
        
        // Calculate differences
        List<DiffCheckItem> differences = calculateDifferences(sourceChecksums, modifiedFile);
        
        // Create temporary diff file
        File tempDiffFile = createTempDiffFile(differences);
        
        try {
            // Combine files to create synchronized result
            RsyncFileUtils.combineRsyncFile(sourceFile, targetFile, tempDiffFile);
            
            long endTime = System.currentTimeMillis();
            logger.info("Synchronization completed in {} ms", (endTime - startTime));
            
        } finally {
            // Clean up temporary file
            if (tempDiffFile.exists() && !tempDiffFile.delete()) {
                logger.warn("Failed to delete temporary file: {}", tempDiffFile.getAbsolutePath());
            }
        }
    }
    
    /**
     * Calculates the differences between a source file (represented by checksums)
     * and a modified file.
     * 
     * @param sourceChecksums checksums of the source file
     * @param modifiedFile the modified file
     * @return list of differences
     */
    public List<DiffCheckItem> calculateDifferences(FileChecksums sourceChecksums, File modifiedFile) {
        validateFile(modifiedFile, "Modified file");
        
        logger.debug("Calculating differences for file: {}", modifiedFile.getName());
        
        List<DiffCheckItem> differences = new ArrayList<>();
        RollingChecksum rollingChecksum = new RollingChecksum(sourceChecksums, modifiedFile, differences);
        
        rollingChecksum.rolling();
        
        logger.debug("Found {} differences", differences.size());
        return differences;
    }
    
    /**
     * Generates checksums for a file.
     * 
     * @param file the file to generate checksums for
     * @return file checksums
     */
    public FileChecksums generateChecksums(File file) {
        validateFile(file, "File");
        
        logger.debug("Generating checksums for file: {}", file.getName());
        return new FileChecksums(file);
    }
    
    /**
     * Creates a builder for more advanced configuration.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for RsyncClient configuration.
     */
    public static class Builder {
        private int blockSize = Rsync4jConstants.getBLOCK_SIZE();
        
        /**
         * Sets the block size for checksums.
         * 
         * @param blockSize the block size
         * @return this builder
         */
        public Builder blockSize(int blockSize) {
            if (blockSize <= 0) {
                throw new IllegalArgumentException("Block size must be positive");
            }
            this.blockSize = blockSize;
            return this;
        }
        
        /**
         * Builds the RsyncClient.
         * 
         * @return a new RsyncClient instance
         */
        public RsyncClient build() {
            return new RsyncClient(blockSize);
        }
    }
    
    private void validateFiles(File... files) {
        for (File file : files) {
            validateFile(file, "File");
        }
    }
    
    private void validateFile(File file, String description) {
        if (file == null) {
            throw new IllegalArgumentException(description + " cannot be null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException(description + " does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException(description + " is not a regular file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException(description + " is not readable: " + file.getAbsolutePath());
        }
    }
    
    private void validateOutputFile(File outputFile) {
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }
        
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IllegalArgumentException("Cannot create parent directories for: " + outputFile.getAbsolutePath());
            }
        }
    }
    
    private File createTempDiffFile(List<DiffCheckItem> differences) throws IOException {
        File tempFile = File.createTempFile("rsync4j_diff_", ".tmp");
        RsyncFileUtils.saveDiffToFile(differences, blockSize, tempFile);
        return tempFile;
    }
    
    /**
     * Gets the current block size.
     * 
     * @return the block size
     */
    public int getBlockSize() {
        return blockSize;
    }
}