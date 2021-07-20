package directory.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GetClientsResponseType implements Serializable {
	
	private static final long serialVersionUID = 6516144264731239126L;
	
	private List<String> newClients;

	public GetClientsResponseType() {
		this.newClients = new ArrayList<String>();
	}

	public List<String> getNewClients() {
		return newClients;
	}

	public void setNewClients(List<String> newClients) {
		this.newClients = newClients;
	}
	
	

}
