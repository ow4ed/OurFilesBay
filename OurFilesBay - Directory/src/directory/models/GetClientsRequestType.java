package directory.models;

import java.io.Serializable;
import java.util.List;

public class GetClientsRequestType implements Serializable {

	private static final long serialVersionUID = -4488881138454505316L;
	
	// number of clients data to send
	private int numberOfClients;
	// already known clients
	private List<String> currentClients;
	
	public GetClientsRequestType(int numberOfClients, List<String> currentClients) {
		this.numberOfClients = numberOfClients;
		this.currentClients = currentClients;
	}

	public int getNumberOfClients() {
		return numberOfClients;
	}

	public List<String> getCurrentClients() {
		return currentClients;
	}

}
