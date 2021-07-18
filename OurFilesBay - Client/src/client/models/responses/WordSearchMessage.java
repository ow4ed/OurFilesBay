package client.models.responses;

import java.io.Serializable;

public class WordSearchMessage implements Serializable{//alows us to convert the state of the object to a byte stream and vice versa

	private static final long serialVersionUID = -4638084621849319776L;
	
	private String message;
	
	public WordSearchMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
}
