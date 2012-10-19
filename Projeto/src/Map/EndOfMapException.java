package Map;

public class EndOfMapException extends Exception {
	private String msg;
	
	public EndOfMapException(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg;
	}
}

