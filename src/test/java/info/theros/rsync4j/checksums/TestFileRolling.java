package info.theros.rsync4j.checksums;

import free.xiaomin.rsync4j.checksums.DiffCheckItem;
import free.xiaomin.rsync4j.checksums.FileChecksums;
import free.xiaomin.rsync4j.checksums.RollingChecksum;
import free.xiaomin.rsync4j.util.Rsync4jConstants;
import free.xiaomin.rsync4j.util.RsyncFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestFileRolling {
	
	
private static String basePath;
	
	
	private File srcFile;
	
	private File updateFile;

	@BeforeClass
	public static void setupPaths() {
		basePath = System.getProperty("user.dir");
	}
	
	
	private List<DiffCheckItem> roll(File srcFile,File updateFile){
		
		FileChecksums fc = new FileChecksums(srcFile);
		
		List<DiffCheckItem> diffList = new ArrayList<DiffCheckItem>();
		
		RollingChecksum  rck  = new RollingChecksum(fc,updateFile,diffList);
		
		rck.rolling();
		
		return diffList;
	}
	
	
	@Test
	public void testOfficeRolling() throws Exception{
		
		srcFile = new File(basePath + "/src/test/resources/" + "word3.docx");
		
		updateFile = new File(basePath + "/src/test/resources/" + "word3.1.docx");
		
		File  tmp = new File(basePath + "/src/test/resources/" + "word");
		
		File  newFile = new File(basePath + "/src/test/resources/" + "word3.1-new.docx");
		
		
		long t1 = System.currentTimeMillis();
		
		Rsync4jConstants.setBLOCK_SIZE(1024*5);
		
		if(!tmp.exists()){
			tmp.createNewFile();
		}
		List<DiffCheckItem> dciList= roll( srcFile, updateFile);
		
		long t2 = System.currentTimeMillis();
		
		System.out.println("滚动计算： spend time :" + (long)(t2-t1)  +"ms");
		RsyncFileUtils.createRsyncFile(dciList, tmp,Rsync4jConstants.BLOCK_SIZE);
		
		System.out.println("实际需要传输的大小  :" + tmp.length() +" byte ");
		
		long t3 = System.currentTimeMillis();
		
		System.out.println("生成临时文件，耗时 :" + (long)(t3-t2) +"ms");
		
		RsyncFileUtils.combineRsyncFile(srcFile, newFile, tmp);
		
		long t4 = System.currentTimeMillis();
		
		System.out.println("合并文件  耗时:" + (long)(t4-t3) +"ms");
		
		System.out.println("all spend time :" + (long)(t4-t1) +"ms");
		
		System.out.println(RsyncFileUtils.checkFileSame(updateFile, newFile));
	}
	
	
	
	@Test
	public void TestTxtRolling() throws Exception{
		srcFile = new File(basePath + "/src/test/resources/" + "lorem.txt");
		
		updateFile = new File(basePath + "/src/test/resources/" + "lorem2.txt");
		
		File  tmp = new File(basePath + "/src/test/resources/" + "tmp");
		
		if(!tmp.exists()){
			tmp.createNewFile();
		}
		
		List<DiffCheckItem> dciList= roll( srcFile, updateFile);
		
		RsyncFileUtils.createRsyncFile(dciList, tmp,Rsync4jConstants.BLOCK_SIZE);
	}
	
	@Test
	public void TestsmallRolling() throws Exception{
		
		srcFile = new File(basePath + "/src/test/resources/" + "test1.txt");
		
		updateFile = new File(basePath + "/src/test/resources/" + "test2.txt");
		
		File  tmp = new File(basePath + "/src/test/resources/" + "tmp2");
		
		
		Rsync4jConstants.setBLOCK_SIZE(4);
		
		if(!tmp.exists()){
			tmp.createNewFile();
		}
		
		List<DiffCheckItem> dciList= roll( srcFile, updateFile);
		
		RsyncFileUtils.createRsyncFile(dciList, tmp,Rsync4jConstants.BLOCK_SIZE);
	}
	
	@Test
	public void TestPSRolling()throws Exception{
		srcFile = new File(basePath + "/src/test/resources/" + "1.psd");
		
		updateFile = new File(basePath + "/src/test/resources/" + "2.psd");
		
		File  tmp = new File(basePath + "/src/test/resources/" + "tmp3");
		
		
		Rsync4jConstants.setBLOCK_SIZE(1024);
		
		if(!tmp.exists()){
			tmp.createNewFile();
		}
		
		List<DiffCheckItem> dciList= roll( srcFile, updateFile);
		
		RsyncFileUtils.createRsyncFile(dciList, tmp,Rsync4jConstants.BLOCK_SIZE);
	}
	
	@Test
	public void TestAllRolling()throws Exception{
		srcFile = new File(basePath + "/src/test/resources/" + "1.psd");
		
		updateFile = new File(basePath + "/src/test/resources/" + "2.psd");
		
		File  tmp = new File(basePath + "/src/test/resources/" + "tmp3");
		
		File  newFile = new File(basePath + "/src/test/resources/" + "3.psd");
		
		
		Rsync4jConstants.setBLOCK_SIZE(1024);
		
		if(!tmp.exists()){
			tmp.createNewFile();
		}
		
		List<DiffCheckItem> dciList= roll( srcFile, updateFile);
		
		RsyncFileUtils.createRsyncFile(dciList, tmp,Rsync4jConstants.BLOCK_SIZE);
		
		RsyncFileUtils.combineRsyncFile(srcFile, newFile, tmp);
		
		System.out.println(RsyncFileUtils.checkFileSame(updateFile, newFile));
		
	}
	
	
	

	
	
	
	

}
