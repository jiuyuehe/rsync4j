package free.xiaomin.rsync4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class QuickMD5 {

	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	protected static MessageDigest messagedigest = null;
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsaex) {
			System.err.println(QuickMD5.class.getName()
					+ "��ʼ��ʧ�ܣ�MessageDigest��֧��MD5Util��");
			nsaex.printStackTrace();
		}
	}

	public static String getFileMD5Channel(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		FileChannel ch = in.getChannel();
		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
				file.length());
		messagedigest.update(byteBuffer);
		return bufferToHex(messagedigest.digest());
	}
	
	public static String getFileMD5Buffer(File file) throws IOException{
		byte [] by = new byte[8192*2];
		FileInputStream in = new FileInputStream(file);
		int length;
        while ((length = in.read(by)) != -1) {
        	messagedigest.update(by, 0, length);
        }
		return bufferToHex(messagedigest.digest());
	}
	/**
	 * get file md5 list 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<String> getFileMD5Buffer(File file ,int size) throws IOException{
		List<String> md5List = new ArrayList<String>();
		byte [] by = new byte[size];
		FileInputStream in = new FileInputStream(file);
		int length;
		while ((length = in.read(by)) != -1) {
			messagedigest.update(by, 0, length);
			md5List.add(bufferToHex(messagedigest.digest()));
		}
		in.close();
		return md5List;
		//return bufferToHex(messagedigest.digest());
	}
	
	/**
	 * get file md5 list 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<String> getReverseFileMD5Buffer(File file ,int size) throws IOException{
		List<String> md5List = new ArrayList<String>();
		byte [] by = new byte[size];
		FileInputStream in = new FileInputStream(file);
		int length;
		while ((length = in.read(by)) != -1) {
			messagedigest.update(by, 0, length);
			md5List.add(bufferToHex(messagedigest.digest()));
		}
		in.close();
		return md5List;
		//return bufferToHex(messagedigest.digest());
	}

	public static String getMD5String(String s) {
		return getMD5String(s.getBytes());
	}

	public static String getMD5String(byte[] bytes) {
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public static void main(String[] args) throws IOException {
		File big = new File(
				"E:\\tools\\VMware-workstation-full-9.0.2-1031769.exe");
		File small = new File(
				"C:\\Users\\jiuyuehe\\Desktop\\other\\addtest\\word1.docx");
//		long begin = System.currentTimeMillis();
//		String md5 = getFileMD5Channel(big);
//		long end = System.currentTimeMillis();
//		System.out.println("md5:" + md5 + " time:" + ((end - begin)) + "ms");
//		
//		String md52 = getFileMD5Buffer(big);
//		long t = System.currentTimeMillis();
//		System.out.println("md52:" + md52 + " time:" + ((t - end)) + "ms");
		
		String md5 = getFileMD5Channel(small);
		System.out.println("md5:"+ md5);
		List<String> md5List =  getFileMD5Buffer(small,512);
		System.out.println(md5List.size());
		System.out.println(md5List);
		
		
		RandomAccessFile randomFile = new RandomAccessFile(small, "r");
		byte [] bys = new byte[1024*1024];
		randomFile.seek(4);
		//randomFile.write(ByteTool.intToByte(startIndex, 4));
		randomFile.read(bys, 0, 1024*1024);
		
	}

}
