package free.xiaomin.rsync4j.util;

import free.xiaomin.rsync4j.checksums.DiffCheckItem;
import free.xiaomin.rsync4j.checksums.DiffFileMeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class RsyncFileUtils {
	
	private static int SAME = 0;
	private static  int DIFF = 1;
	
	/**
	 * 生成临时上传文件
	 * @param dciList
	 * 0: 同样的块
	 * 1： 不同的块
	 * @throws Exception 
	 */
	public  static void createRsyncFile(List<DiffCheckItem> dciList, File tmpFile, int blockSize) throws Exception{
		
		try {
			FileOutputStream fos = new FileOutputStream(tmpFile);
			
			
			fos.write(ByteTool.intToByte(blockSize, 4));
			
			
			for (DiffCheckItem diffCheckItem : dciList) {
				if(diffCheckItem.isMatch()){
					
					fos.write(ByteTool.intToByte(SAME, 1), 0, 1);
					
					//System.out.println("block-"+diffCheckItem.getIndex());
					
					fos.write(ByteTool.intToByte(diffCheckItem.getIndex(), 2), 0, 2);
					
				}else{
					fos.write(ByteTool.intToByte(DIFF, 1), 0, 1);
					
					int length = diffCheckItem.getData().length;
					
					fos.write(ByteTool.intToByte(length, 2), 0, 2);
					
					fos.write(diffCheckItem.getData(), 0, length);
					
				}
			}
			
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 合并上传临时文件[可优化]
	 * @param srcFile
	 * @param newFile
	 * @param rsyncFile
	 * @throws IOException
	 */
	public static void  combineRsyncFile(File srcFile, File newFile,File rsyncFile) throws IOException{
		//FileChecksums checksums = new FileChecksums(srcFile);
		
		//System.out.println(checksums.getHexChecksum());
		
		//List<BlockChecksums> blockChecksums = checksums.getBlockChecksums();
		
		DiffFileMeta dfm = tmp2Item(rsyncFile);
		
		int blockSize = dfm.getBlockSize();
		
		FileOutputStream fos = new FileOutputStream(newFile);
		
		RandomAccessFile srcraf = new RandomAccessFile(srcFile, "r");
		
		for (DiffCheckItem item : dfm.getDiffList()) {
			
			if(item.isMatch()){
				int i = item.getIndex();
				srcraf.seek(i*blockSize);
				//srcraf.write(b, off, len);
				byte []  by = new byte[blockSize];
				srcraf.read(by);
				fos.write(by);
			}else{
				byte []  difby = item.getData();
				fos.write(difby);
			}
		}
		
		srcraf.close();
		
		fos.close();
		
		
	}
	
	
	/**
	 * 将上传的文件转化为对象
	 * @param tmpFile
	 * @return
	 * @throws IOException
	 */
	private static DiffFileMeta  tmp2Item(File tmpFile) throws IOException {
		
		RandomAccessFile diffraf = new RandomAccessFile(tmpFile, "r");
		
		DiffFileMeta dfm = new DiffFileMeta();
		
		int start = 0;
		
		int blockSize = getBlockSize(diffraf ,4);
		
		dfm.setBlockSize(blockSize);
		
		//dfm.setDiffList(diffList);
		
		List<DiffCheckItem> difList = new ArrayList<DiffCheckItem>();
		
		start +=4;
		
		do{
			start =  readBuf(diffraf, start,difList);
			
		}while(start < tmpFile.length());
		
		dfm.setDiffList(difList);
		
		return dfm;
	}

	/**
	 * 
	 * @param diffraf
	 * @param size
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	private static int readBuf(RandomAccessFile diffraf , int offset,List<DiffCheckItem> difList) throws IOException {
		
		int index = offset;
		byte[] matchby = new byte[1];
		//diffraf.seek(index);
		diffraf.read(matchby);
		int match = ByteTool.bytesToInt(matchby);
		
		byte[] sizeby = new byte[2];
		//diffraf.seek(index+1);
		diffraf.read(sizeby);
		if(match ==0){
			int blockindex = ByteTool.bytesToInt(sizeby);
			//System.out.println("match "+blockindex);
			
			DiffCheckItem di= new DiffCheckItem();
			di.setMatch(true);
			di.setIndex(blockindex);
			
			difList.add(di);
			
			return  index +3;
			
		}else{
			int dataSize = ByteTool.bytesToInt(sizeby);
		//	System.out.println("dataSize"+dataSize);
			byte [] by = new  byte[dataSize];
			
			diffraf.read(by);
			
			DiffCheckItem di= new DiffCheckItem();
			di.setMatch(false);
			di.setData(by);

			difList.add(di);
			
			return index+3+dataSize;
		}
		
	}
	
	/**
	 * 获取分块大小
	 * @param diffraf
	 * @param i
	 * @return
	 * @throws IOException
	 */
	private static int getBlockSize(RandomAccessFile diffraf, int i) throws IOException {
		
		byte[] by = new byte[4];
		
		diffraf.read(by);
		
		return ByteTool.bytesToInt(by);
	}
	
	/**
	 * 
	 * @param file1
	 * @param file2
	 * @return
	 * @throws IOException 
	 */
	public static boolean checkFileSame(File file1,File file2) throws IOException{
		String m1 = QuickMD5.getFileMD5Buffer(file1);
		String m2 = QuickMD5.getFileMD5Buffer(file2);
		if(m1.equals(m2))
			return true;
		else
			return false;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		
		String basePath = System.getProperty("user.dir");
		
		File rsyncFile = new File(basePath + "/src/test/resources/" + "tmp");
		File srcFile = new File(basePath + "/src/test/resources/" + "lorem.txt");
		File newFile = new File(basePath + "/src/test/resources/" + "lorem-new.txt");
		File tarFile = new File(basePath + "/src/test/resources/" + "lorem2.txt");
		
		
		RsyncFileUtils ru =  new RsyncFileUtils();
//		try {
//			ru.tmp2Item(tmp);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//ru.combineRsyncFile(srcFile, newFile, rsyncFile);
		
		System.out.println(RsyncFileUtils.checkFileSame(newFile,tarFile));
	}
	
	

}
