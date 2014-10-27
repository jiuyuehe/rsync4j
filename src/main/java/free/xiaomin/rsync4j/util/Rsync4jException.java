package free.xiaomin.rsync4j.util;

public class Rsync4jException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2331722209210513479L;

	public Rsync4jException() {

	}

	public Rsync4jException(Throwable throwable) {
		initCause(throwable);
	}

}
