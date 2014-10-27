package free.xiaomin.rsync4j.util;

import java.io.OutputStream;

/**
 * 将字符，数字 与byte 互相转化工具
 * @author jiuyuehe
 *
 */
public class ByteTool {

	/**
	 * 读取byte []
	 * 
	 * @param b
	 * @param target
	 * @throws Exception
	 */
	public static void addByte(byte[] b, OutputStream os, int len)
			throws Exception {
		
		byte[] by = new byte[len];
		if (b.length > len) {
			os.write(b, 0, len);
		} else {
			int l = len - (b.length - 1);

			while (--l > 0) {
				os.write(0);
			}
			os.write(b, 0, b.length);
		}
	}

	/**
	 * int to byte[] 最多4个字节
	 * 
	 * @param i
	 * @param len
	 * @return
	 */
	public static byte[] intToByte(int i, int len) {
		byte[] abyte = new byte[len];
		for (int j = 0; j < abyte.length; j++) {
			abyte[j] = (byte) ((i >>> ((len-j-1)*8)) & 0xff );
		}
		return abyte;
	}
	
	/**
	 * 最多支持4字节
	 * @param bytes
	 * @return
	 */
	public static int bytesToInt(byte[] bytes) {
		int addr = 0;
		int len = bytes.length;
		for (int j = 0; j < len; j++) {
			addr = (addr << 8) | (bytes[j] & 0xff);
		}
		return addr;
	}
	
	public static void main(String[] args) {
	
		
		int a = bytesToInt(	intToByte(1,2));
		
		System.out.println(a);
	}
}
