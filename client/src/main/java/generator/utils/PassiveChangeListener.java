package generator.utils;

import generator.Listener;

import java.io.Serializable;

public class PassiveChangeListener<T> implements Listener<T>, Serializable {

	private static final long serialVersionUID = -8661407199741916844L;

	protected boolean changed = false;

	public boolean hasChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public void receiveEvent(T event) {
		changed = true;
	}

}
