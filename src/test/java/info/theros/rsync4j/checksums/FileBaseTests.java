package info.theros.rsync4j.checksums;


import free.xiaomin.rsync4j.util.ByteTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class FileBaseTests {
	
	private static String basePath;
	
	//private File testFile = new File(basePath + "/src/test/resources/" + "lorem.txt");
	
	private File testFile;

	@BeforeClass
	public static void setupPaths() {
		basePath = System.getProperty("user.dir");
	}
	
	/**
	 * 测试 dowhile
	 */
	@Test
	public void testdowhile(){
		int x = 1 ,j=10;
		do{
			System.out.println(x);
			x++;
			
		}while(x<j);
	}
	
	@Test
	public void testinsertbuf2file() throws IOException{
		File testFile = new File(basePath + "/src/test/resources/" + "test1.txt");
		
		byte [] by = "uio".getBytes();
		
		RandomAccessFile raf = new RandomAccessFile(testFile, "rw");
		
		raf.setLength(raf.length()+by.length);
		
		 for(long i = raf.length() - 1; i > by.length + 5 - 1; i--){  
	            raf.seek(i - by.length);  
	            byte temp = raf.readByte();  
	            raf.seek(i);  
	            raf.writeByte(temp);  
	        }  
		
		
		raf.seek(5);
		
		raf.write(by);

		raf.close();
		
	}
	
	
	
	
	
	
	
	@Test
	public void testwrite() throws IOException{
		
		File tmpFile  = testFile = new File(basePath + "/src/test/resources/" + "tmp");
		
		
		FileOutputStream fos = new FileOutputStream(tmpFile);
		int start = 0;
		fos.write(ByteTool.intToByte(0, 1), 0, 1);
		
		start ++;
		
		fos.write(ByteTool.intToByte(22, 2), 0, 2);
		start +=2;
		
		fos.close();
	}
	
	
	
	@Test
	public void TestZipAdler()  throws IOException{
		testFile = new File(basePath + "/src/test/resources/" + "lorem.txt");
		RandomAccessFile raf = new RandomAccessFile(testFile, "r");
		
		int size =16;
		
		byte [] buf = new byte[150];
		long read = raf.read(buf,0,150);
		String text = new String(buf);
		System.out.println(text);
		
		
		java.util.zip.Adler32 javaAdler32 = new java.util.zip.Adler32();
		javaAdler32.update(buf,0,size);
		System.out.println("1   :  "+javaAdler32.getValue());
		javaAdler32.update(2);
		System.out.println("+1   :  "+javaAdler32.getValue());
		
		javaAdler32.reset();
		javaAdler32.update(buf,0,size+1);
		System.out.println("2   :  "+javaAdler32.getValue());
		javaAdler32.reset();
		
		byte [] by = new byte[size];
		
		System.arraycopy(buf, 1, by, 0, size);
		
		javaAdler32.update(by);
		System.out.println("3   :  "+javaAdler32.getValue());
		javaAdler32.reset();
		raf.close();
		
		 byte [] by2 = new byte[size];
		 raf.seek(1);
		 raf.read(by2,0,size);
		 String text1 = new String(by2);
			System.out.println(text1);
			
		 javaAdler32.reset();
		 javaAdler32.update(by2,0,size);
		 System.out.println("4   :  "+javaAdler32.getValue());
		 javaAdler32.reset();
		
	}
}
