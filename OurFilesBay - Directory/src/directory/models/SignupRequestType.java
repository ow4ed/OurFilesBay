package directory.models;

import java.io.Serializable;

public class SignupRequestType implements Serializable { 

	private static final long serialVersionUID = 8821920435378401569L;
	
	private String clientPort;

	public SignupRequestType(String clientPort) {
		this.clientPort = clientPort;
	}

	public String getClientPort() {
		return clientPort;
	}

}
