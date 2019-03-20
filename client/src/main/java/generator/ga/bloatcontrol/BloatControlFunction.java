package generator.ga.bloatcontrol;

import generator.ga.Chromosome;

import java.io.Serializable;

public interface BloatControlFunction extends Serializable {

	public boolean isTooLong(Chromosome chromosome);

}
