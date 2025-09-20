# Rsync4j - Java Implementation of the Rsync Algorithm

[![Java 8+](https://img.shields.io/badge/java-8%2B-blue.svg)](https://docs.oracle.com/javase/8/)
[![Maven Central](https://img.shields.io/badge/maven-compatible-blue.svg)](https://maven.apache.org/)

Rsync4j is a pure Java implementation of the rsync algorithm for efficient file synchronization. It provides a high-level API that third-party developers can easily integrate into their applications for incremental file transfers and synchronization.

## Table of Contents

- [Features](#features)
- [Algorithm Overview](#algorithm-overview)
- [Quick Start](#quick-start)
- [Usage Examples](#usage-examples)
- [API Documentation](#api-documentation)
- [Performance](#performance)
- [Requirements](#requirements)
- [Building](#building)
- [Contributing](#contributing)

## Features

- **Pure Java Implementation**: No external dependencies on rsync binary
- **High-level API**: Simple, intuitive interface for third-party integration
- **Efficient Algorithm**: Uses rolling checksums (Adler32) and strong checksums (MD5)
- **Configurable Block Size**: Optimize for different file types and network conditions
- **Comprehensive Logging**: Built-in logging with SLF4J
- **Thread-safe**: Safe for concurrent use
- **Memory Efficient**: Processes files in configurable block sizes

## Algorithm Overview

The rsync algorithm is designed to efficiently synchronize files by transferring only the differences between two versions. Here's how it works:

### 1. Block Division
Files are divided into fixed-size blocks (configurable, default 5KB). Each block gets:
- **Weak Checksum**: Fast Adler32 checksum for quick comparison
- **Strong Checksum**: MD5 hash for collision-resistant verification

### 2. Rolling Checksum
The algorithm uses a "rolling" approach to find matching blocks:
- Calculates checksums for overlapping windows in the modified file
- Compares against the original file's block checksums
- Identifies matching and differing regions

### 3. Delta Generation
Only the differences (deltas) need to be transferred:
- **Matched blocks**: Reference to original block index
- **New/Modified data**: Raw bytes for changed content

### 4. File Reconstruction
The receiving end reconstructs the file using:
- Original file blocks (for matched regions)
- Delta data (for changed regions)

This approach typically reduces transfer size by 80-95% for incremental changes.

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>info.theros</groupId>
    <artifactId>rsync4j</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage

```java
import free.xiaomin.rsync4j.RsyncClient;
import java.io.File;

// Create client with default settings
RsyncClient client = new RsyncClient();

// Synchronize files
File sourceFile = new File("original.txt");
File modifiedFile = new File("modified.txt");
File outputFile = new File("synchronized.txt");

client.synchronize(sourceFile, modifiedFile, outputFile);
```

## Usage Examples

### 1. Basic File Synchronization

```java
import free.xiaomin.rsync4j.RsyncClient;
import java.io.File;
import java.io.IOException;

public class BasicSync {
    public static void main(String[] args) throws IOException {
        // Create an RsyncClient with default settings
        RsyncClient client = new RsyncClient();
        
        File originalFile = new File("document.pdf");
        File modifiedFile = new File("document_v2.pdf");
        File syncedFile = new File("document_synced.pdf");
        
        // Synchronize the files
        client.synchronize(originalFile, modifiedFile, syncedFile);
        
        System.out.println("Files synchronized successfully!");
    }
}
```

### 2. Advanced Configuration

```java
import free.xiaomin.rsync4j.RsyncClient;

// Create client with custom configuration
RsyncClient client = RsyncClient.builder()
    .blockSize(1024)  // Use 1KB blocks for better granularity
    .build();

// Use the configured client
client.synchronize(sourceFile, modifiedFile, outputFile);
```

### 3. Manual Difference Calculation

```java
import free.xiaomin.rsync4j.RsyncClient;
import free.xiaomin.rsync4j.checksums.FileChecksums;
import free.xiaomin.rsync4j.checksums.DiffCheckItem;
import java.util.List;

RsyncClient client = new RsyncClient();

// Generate checksums for the source file
FileChecksums sourceChecksums = client.generateChecksums(sourceFile);
System.out.println("File checksum: " + sourceChecksums.getHexChecksum());
System.out.println("Number of blocks: " + sourceChecksums.getBlockChecksums().size());

// Calculate differences
List<DiffCheckItem> differences = client.calculateDifferences(sourceChecksums, modifiedFile);

// Analyze the differences
int matchedBlocks = 0;
int changedBytes = 0;

for (DiffCheckItem diff : differences) {
    if (diff.isMatch()) {
        matchedBlocks++;
    } else {
        changedBytes += diff.getData().length;
    }
}

System.out.println("Matched blocks: " + matchedBlocks);
System.out.println("Changed bytes: " + changedBytes);
```

### 4. File Comparison

```java
import free.xiaomin.rsync4j.util.RsyncFileUtils;

// Check if two files have identical content
boolean areIdentical = RsyncFileUtils.checkFileSame(file1, file2);
if (areIdentical) {
    System.out.println("Files are identical");
} else {
    System.out.println("Files differ");
}
```

## API Documentation

### RsyncClient

The main entry point for rsync operations.

#### Constructors

- `RsyncClient()` - Creates client with default block size (5KB)
- `RsyncClient(int blockSize)` - Creates client with specified block size

#### Methods

- `synchronize(File source, File modified, File target)` - Synchronizes two files
- `calculateDifferences(FileChecksums source, File modified)` - Calculates differences
- `generateChecksums(File file)` - Generates checksums for a file
- `getBlockSize()` - Returns current block size

#### Builder Pattern

```java
RsyncClient client = RsyncClient.builder()
    .blockSize(2048)  // Custom block size
    .build();
```

### Key Classes

- **FileChecksums**: Represents checksums for an entire file and its blocks
- **BlockChecksums**: Individual block checksum with weak (Adler32) and strong (MD5) checksums
- **DiffCheckItem**: Represents a difference (either matching block reference or raw data)
- **RsyncFileUtils**: Utility methods for file operations

## Performance

### Block Size Considerations

The block size significantly impacts performance:

- **Small blocks (512B - 2KB)**: Better granularity, more overhead
- **Medium blocks (2KB - 8KB)**: Good balance for most use cases
- **Large blocks (8KB+)**: Less granularity, better for large files

### Typical Performance

For a 100MB file with 10% changes:
- **Transfer reduction**: ~90% less data
- **Processing time**: 200-500ms on modern hardware
- **Memory usage**: ~10-50MB depending on block size

### Use Cases

1. **Software Updates**: Distribute only changed parts of applications
2. **Document Synchronization**: Sync large documents with minimal changes
3. **Backup Systems**: Incremental backup with reduced storage
4. **Distributed Systems**: Efficient data replication
5. **Mobile Applications**: Minimize data usage for file sync

## Requirements

- **Java**: 8 or higher
- **Dependencies**: 
  - SLF4J (logging API)
  - Logback (logging implementation)
  - Commons Codec (hex encoding utilities)

## Building

```bash
# Clone the repository
git clone https://github.com/jiuyuehe/rsync4j.git
cd rsync4j

# Build with Maven
mvn clean compile

# Run tests
mvn test

# Create JAR
mvn package

# Run the demo
mvn exec:java -Dexec.mainClass="free.xiaomin.rsync4j.demo.RsyncDemo"
```

## Example Output

When running the demo, you'll see output similar to:

```
INFO - === Basic Synchronization Example ===
INFO - Starting synchronization: source.txt + modified.txt -> synced.txt
DEBUG - Generated checksums for file: source.txt (1 blocks)
DEBUG - Source file block count: 1
DEBUG - File total size: 63 bytes; Different blocks to transfer: 63 bytes
INFO - Synchronization completed in 11 ms
INFO - Files synchronized successfully!
```

## Algorithm Research

This implementation is based on the original rsync algorithm research:

- **Original Paper**: "The rsync algorithm" by Andrew Tridgell and Paul Mackerras
- **Full Thesis**: [https://www.samba.org/~tridge/phd_thesis.pdf](https://www.samba.org/~tridge/phd_thesis.pdf)

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

## License

This project is provided as open source software. Please check the license file for specific terms.

## Support

This project is maintained by [Qiyun Technology](http://www.yliyun.com) and is used in production for Qiyun Cloud Storage systems.

---

**Note**: This is a Java implementation of the rsync algorithm, not a replacement for the rsync command-line tool. It's designed for integration into Java applications that need efficient file synchronization capabilities.