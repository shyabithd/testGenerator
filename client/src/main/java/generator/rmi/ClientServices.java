package generator.rmi;

import generator.Properties;
import generator.rmi.service.ClientNodeImpl;
import generator.rmi.service.ClientNodeLocal;
import generator.rmi.service.ClientNodeRemote;
import generator.rmi.service.DummyClientNodeImpl;
import generator.statistics.RuntimeVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientServices {

	private static Logger logger = LoggerFactory.getLogger(ClientServices.class);
	
	private static ClientServices instance = new ClientServices();
	
	private volatile ClientNodeImpl clientNode = new DummyClientNodeImpl();
	
	protected ClientServices(){		
	}
	
	public static ClientServices getInstance(){
		return instance;
	}

	public boolean registerServices() {

		UtilsRMI.ensureRegistryOnLoopbackAddress();
		
		try{
			int port = Properties.PROCESS_COMMUNICATION_PORT;
			Registry registry = LocateRegistry.getRegistry(port);
			clientNode = new ClientNodeImpl(registry);
			ClientNodeRemote stub = (ClientNodeRemote) UtilsRMI.exportObject(clientNode);
			registry.rebind(clientNode.getClientRmiIdentifier(), stub);
			return clientNode.init();
		} catch(Exception e){
			logger.error("Failed to register client services",e);
			return false;
		}
	}

	public ClientNodeLocal getClientNode() {
		return clientNode;
	}
	
	public void stopServices(){
		if(clientNode!=null){
			clientNode.stop();
			int i = 0;
			final int tries = 10;
			boolean done = false;
			try {
				while(!done){
					/*
					 * A call from Master could still be active on this node. so we cannot
					 * forcely stop the client, we need to wait
					 */
					done = UnicastRemoteObject.unexportObject(clientNode, false);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {	
						break;
					}
					i++;
					if(i>=tries){
						logger.error("Tried "+tries+" times to stop RMI ClientNode, giving up");
						break;
					}
				}
			} catch (NoSuchObjectException e) {
				//this could happen if Master has removed the registry
				logger.debug("Failed to delete ClientNode RMI instance",e);
			}
			clientNode = new DummyClientNodeImpl();
		}
	}

	/**
	 * Shorthand for the commonly used trackOutputVariable method
	 *
	 * @param outputVariable The runtime variable to track
	 * @param value The value of the runtime variable
	 */
	public static void track(RuntimeVariable outputVariable, Object value) {
		ClientServices.getInstance().getClientNode().trackOutputVariable(outputVariable, value);
	}
}
