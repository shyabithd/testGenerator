package rmi.service;

import generator.Listener;
import generator.ga.Chromosome;
import generator.result.TestGenerationResult;
import generator.rmi.service.ClientNodeRemote;
import generator.rmi.service.ClientState;
import generator.rmi.service.ClientStateInformation;
import generator.rmi.service.MasterNodeRemote;
import generator.statistics.RuntimeVariable;
import generator.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class MasterNodeImpl implements MasterNodeRemote, MasterNodeLocal {

	private static final long serialVersionUID = -6329473514791197464L;

	private static Logger logger = LoggerFactory.getLogger(MasterNodeImpl.class);

	private final Registry registry;
	private final Set<ClientNodeRemote> clients;

	protected final Collection<Listener<ClientStateInformation>> listeners = Collections.synchronizedList(new ArrayList<Listener<ClientStateInformation>>());

	/**
	 * It is important to keep track of client states for debugging reasons. For
	 * example, if client crash, could be useful to know in which state it was.
	 * We cannot query the client directly in those cases, because it is
	 * crashed... The "key" is the RMI identifier of the client
	 */
	private final Map<String, ClientState> clientStates;

	private final Map<String, ClientStateInformation> clientStateInformation;

	public MasterNodeImpl(Registry registry) {
		clients = new CopyOnWriteArraySet<ClientNodeRemote>();
		clientStates = new ConcurrentHashMap<String, ClientState>();
		clientStateInformation = new ConcurrentHashMap<String, ClientStateInformation>();
		this.registry = registry;
	}

	@Override
	public void evosuite_registerClientNode(String clientRmiIdentifier) throws RemoteException {

		/*
		 * The client should first register its node, and then inform MasterNode
		 * by calling this method
		 */

		ClientNodeRemote node = null;
		try {
			node = (ClientNodeRemote) registry.lookup(clientRmiIdentifier);
		} catch (Exception e) {
			logger.error("Error when client " + clientRmiIdentifier
			        + " tries to register to master", e);
			return;
		}
		synchronized (clients) {
			clients.add(node);
			clients.notifyAll();
		}
	}

	@Override
	public void evosuite_informChangeOfStateInClient(String clientRmiIdentifier,
	        ClientState state, ClientStateInformation information) throws RemoteException {
		clientStates.put(clientRmiIdentifier, state);
		// To be on the safe side
		information.setState(state);
		clientStateInformation.put(clientRmiIdentifier, information);
		fireEvent(information);
	}

	@Override
	public Collection<ClientState> getCurrentState() {
		return clientStates.values();
	}

	@Override
	public Collection<ClientStateInformation> getCurrentStateInformation() {
		return clientStateInformation.values();
	}

	@Override
	public String getSummaryOfClientStatuses() {
		if (clientStates.isEmpty()) {
			return "No client has registered";
		}
		String summary = "";
		for (String id : clientStates.keySet()) {
			ClientState state = clientStates.get(id);
			summary += id + ": " + state + "\n";
		}
		return summary;
	}

	@Override
	public Set<ClientNodeRemote> getClientsOnceAllConnected(long timeoutInMs)
	        throws InterruptedException {

		long start = System.currentTimeMillis();

		/*
		 * TODO: this will be a parameter
		 */
		int numberOfExpectedClients = 1;

		synchronized (clients) {
			while (clients.size() != numberOfExpectedClients) {
				long elapsed = System.currentTimeMillis() - start;
				long timeRemained = timeoutInMs - elapsed;
				if (timeRemained <= 0) {
					return null;
				}
				clients.wait(timeRemained);
			}
			return Collections.unmodifiableSet(clients);
		}
	}

	@Override
	public void cancelAllClients() {
		for (ClientNodeRemote client : clients) {
			try {
				LoggingUtils.getGeneratorLogger().info("Trying to kill client " + client);
				client.cancelCurrentSearch();
			} catch (RemoteException e) {
				logger.warn("Error while trying to cancel client: " + e);
				e.printStackTrace();
			}
		}
	}

	@Override
	public void evosuite_collectStatistics(String clientRmiIdentifier, Chromosome individual) {
		//SearchStatistics.getInstance().currentIndividual(clientRmiIdentifier, individual);
	}

	@Override
	public void evosuite_collectStatistics(String clientRmiIdentifier, RuntimeVariable variable, Object value)
	        throws RemoteException {
		//SearchStatistics.getInstance().setOutputVariable(variable, value);
	}

	@Override
	public void evosuite_collectTestGenerationResult(
			String clientRmiIdentifier, List<TestGenerationResult> results)
			throws RemoteException {
		//SearchStatistics.getInstance().addTestGenerationResult(results);
		
	}

	@Override
	public void evosuite_flushStatisticsForClassChange(String clientRmiIdentifier)
			throws RemoteException {
		//SearchStatistics.getInstance().writeStatisticsForAnalysis();
	}

	@Override
	public void evosuite_updateProperty(String clientRmiIdentifier, String propertyName, Object value)
			throws RemoteException, IllegalArgumentException, IllegalAccessException, generator.Properties.NoSuchParameterException {
		//Properties.getInstance().setValue(propertyName, value);
	}

	@Override
	public void addListener(Listener<ClientStateInformation> listener) {
		listeners.add(listener);
	}

	@Override
	public void deleteListener(Listener<ClientStateInformation> listener) {
		listeners.remove(listener);
	}

	/**
	 * <p>
	 * fireEvent
	 * </p>
	 * 
	 * @param event
	 *            a T object.
	 */
	public void fireEvent(ClientStateInformation event) {
		for (Listener<ClientStateInformation> listener : listeners) {
			listener.receiveEvent(event);
		}
	}
}
