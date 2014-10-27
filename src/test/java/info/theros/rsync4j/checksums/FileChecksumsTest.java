package info.theros.rsync4j.checksums;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import free.xiaomin.rsync4j.checksums.BlockChecksums;
import free.xiaomin.rsync4j.checksums.FileChecksums;

public class FileChecksumsTest {

	private File emptyFile;

	private static String basePath;

	@BeforeClass
	public static void setupPaths() {
		basePath = System.getProperty("user.dir");
	}

	@Before
	public void setUp() throws Exception {
		emptyFile = File.createTempFile("rsync4j", null);
	}

	@After
	public void tearDown() throws Exception {
		emptyFile.deleteOnExit();
	}

	@Test
	public void testGenerate() {
		FileChecksums checksums = new FileChecksums(emptyFile);
		Assert.assertNotNull(checksums);
		Assert.assertEquals(emptyFile.getName(), checksums.getName());
		Assert
				.assertEquals(
						"e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
						checksums.getHexChecksum());
		Assert.assertNotNull(checksums.getBlockChecksums());
		Assert.assertEquals(0, checksums.getBlockChecksums().size());
	}

	@Test
	public void testGenerateWithRandomFile() {
		calc(new File(basePath + "/src/test/resources/" + "lorem.txt"));
		System.out.println("file ---------------------");
		
		calc(new File(basePath + "/src/test/resources/" + "lorem2.txt"));
	}

	private void calc(File file) {
		FileChecksums checksums = new FileChecksums(file);
		System.out.println(checksums.getHexChecksum());
		List<BlockChecksums> blockChecksums = checksums.getBlockChecksums();
		for (BlockChecksums blockChecksums2 : blockChecksums) {
			System.out.println(blockChecksums2.getOffset() + "\t->\t"
					+ Long.toHexString(blockChecksums2.getWeakChecksum())
					+ "\t->\t" + blockChecksums2.getHexStrongChecksum());
		}
	}

}
