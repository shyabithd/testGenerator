package generator.rmi.service;


import generator.ga.Chromosome;
import generator.statistics.RuntimeVariable;

public interface ClientNodeLocal {

	public boolean init();

	public void trackOutputVariable(RuntimeVariable variable, Object value);
	
    public void publishPermissionStatistics();

	public void changeState(ClientState state);

	public void changeState(ClientState state, ClientStateInformation information);

	public void updateStatistics(Chromosome individual);

	public void flushStatisticsForClassChange();

	public void updateProperty(String propertyName, Object value);

	public void waitUntilDone();
}
