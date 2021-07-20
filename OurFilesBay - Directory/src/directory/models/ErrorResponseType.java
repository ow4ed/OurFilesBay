package directory.models;

import java.io.Serializable;

public class ErrorResponseType implements Serializable {

	private static final long serialVersionUID = -3929182412042221679L;
	
	private String reason;

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}	

}
