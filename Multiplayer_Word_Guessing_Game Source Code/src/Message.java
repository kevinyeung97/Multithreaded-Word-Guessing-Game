import java.io.Serializable;

public class Message implements Serializable{
	
	public static final long serialVersionUID = 1;
	
	private String tag;
	private String message;
	
	public Message(String tag, String message) {
		this.tag = tag;
		this.message = message;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}

}
