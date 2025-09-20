package free.xiaomin.rsync4j.demo;

import free.xiaomin.rsync4j.RsyncClient;
import free.xiaomin.rsync4j.checksums.DiffCheckItem;
import free.xiaomin.rsync4j.checksums.FileChecksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Demonstration of how to use the RsyncClient for file synchronization.
 * This class shows common usage patterns for third-party developers.
 * 
 * @author jiuyuehe
 */
public class RsyncDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(RsyncDemo.class);
    
    public static void main(String[] args) {
        RsyncDemo demo = new RsyncDemo();
        
        try {
            // Basic usage example
            demo.basicSynchronizationExample();
            
            // Advanced usage with custom configuration
            demo.advancedConfigurationExample();
            
            // Checksum generation example
            demo.checksumExample();
            
        } catch (Exception e) {
            logger.error("Demo failed", e);
        }
    }
    
    /**
     * Demonstrates basic file synchronization with default settings.
     */
    public void basicSynchronizationExample() throws IOException {
        logger.info("=== Basic Synchronization Example ===");
        
        // Create test files
        File sourceFile = createTestFile("source.txt", "Hello World!\nThis is the original content.\nLine 3\nLine 4");
        File modifiedFile = createTestFile("modified.txt", "Hello Universe!\nThis is the modified content.\nLine 3\nNew Line 4");
        File syncedFile = new File(System.getProperty("java.io.tmpdir"), "synced.txt");
        
        // Create RsyncClient with default settings
        RsyncClient client = new RsyncClient();
        
        // Synchronize files
        client.synchronize(sourceFile, modifiedFile, syncedFile);
        
        logger.info("Files synchronized successfully!");
        logger.info("Source: {}", sourceFile.getAbsolutePath());
        logger.info("Modified: {}", modifiedFile.getAbsolutePath());
        logger.info("Result: {}", syncedFile.getAbsolutePath());
        
        // Cleanup
        sourceFile.delete();
        modifiedFile.delete();
        syncedFile.delete();
    }
    
    /**
     * Demonstrates advanced configuration using the Builder pattern.
     */
    public void advancedConfigurationExample() throws IOException {
        logger.info("=== Advanced Configuration Example ===");
        
        // Create test files
        File sourceFile = createTestFile("source2.txt", generateLargeContent(1000));
        File modifiedFile = createTestFile("modified2.txt", generateLargeContent(1000) + "\nAdditional content");
        File syncedFile = new File(System.getProperty("java.io.tmpdir"), "synced2.txt");
        
        // Create RsyncClient with custom block size
        RsyncClient client = RsyncClient.builder()
                .blockSize(1024) // Use 1KB blocks
                .build();
        
        logger.info("Using block size: {} bytes", client.getBlockSize());
        
        // Synchronize files
        client.synchronize(sourceFile, modifiedFile, syncedFile);
        
        logger.info("Advanced synchronization completed!");
        
        // Cleanup
        sourceFile.delete();
        modifiedFile.delete();
        syncedFile.delete();
    }
    
    /**
     * Demonstrates manual checksum generation and difference calculation.
     */
    public void checksumExample() throws IOException {
        logger.info("=== Checksum and Differences Example ===");
        
        // Create test files
        File sourceFile = createTestFile("checksums_source.txt", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        File modifiedFile = createTestFile("checksums_modified.txt", "ABCDEFGHIJKLMNOP-MODIFIED-WXYZ");
        
        RsyncClient client = new RsyncClient(8); // Small block size for demo
        
        // Generate checksums for source file
        FileChecksums sourceChecksums = client.generateChecksums(sourceFile);
        logger.info("Generated checksums for source file: {}", sourceFile.getName());
        logger.info("File checksum: {}", sourceChecksums.getHexChecksum());
        logger.info("Number of blocks: {}", sourceChecksums.getBlockChecksums().size());
        
        // Calculate differences
        List<DiffCheckItem> differences = client.calculateDifferences(sourceChecksums, modifiedFile);
        logger.info("Found {} differences between files", differences.size());
        
        // Display differences
        for (int i = 0; i < differences.size(); i++) {
            DiffCheckItem diff = differences.get(i);
            if (diff.isMatch()) {
                logger.info("Block {}: MATCH (index {})", i, diff.getIndex());
            } else {
                logger.info("Block {}: DIFF ({} bytes)", i, diff.getData().length);
            }
        }
        
        // Cleanup
        sourceFile.delete();
        modifiedFile.delete();
    }
    
    /**
     * Helper method to create a test file with specified content.
     */
    private File createTestFile(String filename, String content) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
    
    /**
     * Helper method to generate large content for testing.
     */
    private String generateLargeContent(int lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append("This is line number ").append(i).append(" of the test content.\n");
        }
        return sb.toString();
    }
}