package generator;

import java.io.Serializable;

public interface Listener<T> extends Serializable {

	void receiveEvent(T event);
}
