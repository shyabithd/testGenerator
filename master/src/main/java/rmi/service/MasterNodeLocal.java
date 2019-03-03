package rmi.service;

import generator.rmi.service.ClientNodeRemote;
import generator.rmi.service.ClientState;
import generator.rmi.service.ClientStateInformation;
import generator.utils.Listenable;

import java.util.Collection;
import java.util.Set;

public interface MasterNodeLocal extends Listenable<ClientStateInformation> {
	
	public String getSummaryOfClientStatuses();
	
	public Collection<ClientState> getCurrentState();

	public Collection<ClientStateInformation> getCurrentStateInformation();

	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs) throws InterruptedException;
	
	public void cancelAllClients();
}
