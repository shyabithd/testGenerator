package generator.utils;

import java.io.Serializable;

public interface Listener<T> extends Serializable {
	/**
	 * <p>receiveEvent</p>
	 *
	 * @param event a T object.
	 * @param <T> a T object.
	 */
	void receiveEvent(T event);
}
