package free.xiaomin.rsync4j.checksums;

import java.util.Arrays;

public class DiffCheckItem {
	
	/**
	 * 是否匹配
	 */
	private boolean isMatch;
	
	/**
	 * 匹配，加入匹配号
	 */
	private int index;
	
	/**
	 * 不匹配，写入数据
	 */
	private byte [] data;

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "DiffCheckList [isMatch=" + isMatch + ", index=" + index
				+ ", data=" + Arrays.toString(data) + "]";
	}

	
	
	

}
