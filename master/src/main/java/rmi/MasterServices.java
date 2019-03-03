package rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rmi.service.MasterNodeImpl;
import rmi.service.MasterNodeLocal;
import generator.utils.Randomness;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MasterServices {

	private static Logger logger = LoggerFactory.getLogger(MasterServices.class);
	
	private static MasterServices instance = new MasterServices();
	
	private int registryPort = -1;
	
	/**
	 *  We store it to avoid issues with GC
	 */
	private Registry registry;

	private MasterNodeImpl masterNode;
	
	
	protected MasterServices(){		
	}
	
	
	public static MasterServices getInstance(){
		return instance;
	}
	
	
	public boolean startRegistry() throws IllegalStateException{
		
		if(registry != null){
			throw new IllegalStateException("RMI registry is already running");
		}
		
		/*
		 * Unfortunately, it does not seem possible to start a RMI registry on an
		 * ephemeral port. So, we start with a port, and see if free. If not, try the
		 * next one, etc. Note, it is important to start from a random port to avoid issues
		 * with several masters running on same node, eg when experiments on cluster.
		 */
		
		int port = 2000;
		port += Randomness.nextInt(20000);
		
		final int TRIES = 100;
		for(int i=0; i<TRIES; i++){
			try {
				int candidatePort = port+i;								
				//UtilsRMI.ensureRegistryOnLoopbackAddress();
				
				registry = LocateRegistry.createRegistry(candidatePort);
				registryPort = candidatePort;
				return true;
			} catch (RemoteException e) {								
			}		
		}
		
		return false;
	}
	
	/**
	 * Return the port on which the registry is running.
	 * 
	 * @return a negative value if no registry is running
	 */
	public int getRegistryPort(){
		return registryPort; 
	}
	
	public void registerServices() throws RemoteException{
		masterNode = new MasterNodeImpl(registry);
	}
	


	public MasterNodeLocal getMasterNode() {
		return masterNode;
	}
	
	public void stopServices(){
		
		if(registry != null){
			try {
				UnicastRemoteObject.unexportObject(registry,true);
			} catch (NoSuchObjectException e) {
				logger.warn("Failed to stop RMI registry",e);
			}
			registry = null;
		}
	}
}
