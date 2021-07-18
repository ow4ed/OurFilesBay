package client.models.requests;

import java.io.Serializable;

public class SearchRequest implements Serializable{//alows us to convert the state of the object to a byte stream and vice versa

	private static final long serialVersionUID = -4638084621849319776L;
	
	private String keyWord;
	
	public SearchRequest(String keyWord) {
		this.keyWord = keyWord;
	}
	
	public String getKeyWord() {
		return keyWord;
	}
	
}
