package generator;

import java.util.ArrayList;
import java.util.Collection;

public class SimpleListenable<T> implements Listenable<T> {

	private static final long serialVersionUID = 8100518628763448338L;

	protected final Collection<Listener<T>> listeners = new ArrayList<Listener<T>>();

	/** {@inheritDoc} */
	@Override
	public void addListener(Listener<T> listener) {
		listeners.add(listener);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteListener(Listener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * <p>fireEvent</p>
	 *
	 * @param event a T object.
	 */
	public void fireEvent(T event) {
		for (Listener<T> listener : listeners) {
			listener.receiveEvent(event);
		}
	}

}
