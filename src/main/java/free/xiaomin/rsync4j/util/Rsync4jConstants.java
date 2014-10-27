package free.xiaomin.rsync4j.util;

public  class Rsync4jConstants {

	public static  int BLOCK_SIZE = 1024*5;
	public static final String MD5 = "MD5";
	
	public static int getBLOCK_SIZE() {
		return BLOCK_SIZE;
	}
	public static void setBLOCK_SIZE(int bLOCK_SIZE) {
		BLOCK_SIZE = bLOCK_SIZE;
	}
	
	

}
