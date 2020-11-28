package user;

import java.io.Serializable;

public class WordSearchMessage implements Serializable{

	private static final long serialVersionUID = -4638084621849319776L;
	private String keyWord;
	
	public WordSearchMessage(String keyWord) {
		this.keyWord = keyWord;
	}

	public String getMessage() {
		return keyWord;
	}

	


}
