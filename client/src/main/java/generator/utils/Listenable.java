package generator.utils;

import java.io.Serializable;

/**
 * {@link java.util.Observable} should be an interface. It is not so we cannot
 * use it if the class to implement it has already a certain parent.
 *
 * @author roessler
 * @param <T>
 *            The type of the event.
 */
public interface Listenable<T> extends Serializable {

	void addListener(Listener<T> listener);

	/**
	 * Remove the given Listener such that he will no longer receive event
	 * updates. If the listener is removed several times or was not added prior
	 * to removal, nothing happens.
	 *
	 * @param listener
	 *            The listener to remove.
	 */
	void deleteListener(Listener<T> listener);
}
