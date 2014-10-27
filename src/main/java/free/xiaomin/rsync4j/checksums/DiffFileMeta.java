package free.xiaomin.rsync4j.checksums;

import java.util.List;

public class DiffFileMeta {
	private int blockSize;
	
	private List<DiffCheckItem> diffList ;

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public List<DiffCheckItem> getDiffList() {
		return diffList;
	}

	public void setDiffList(List<DiffCheckItem> diffList) {
		this.diffList = diffList;
	}
	
	
	
}
