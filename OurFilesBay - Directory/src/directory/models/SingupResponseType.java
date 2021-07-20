package directory.models;

import java.io.Serializable;

public class SingupResponseType implements Serializable {

	private static final long serialVersionUID = 5554897616486936146L;
	
	private boolean signedUp;

	public boolean isSignedUp() {
		return signedUp;
	}

	public void setSignedUp(boolean signedUp) {
		this.signedUp = signedUp;
	}

}
