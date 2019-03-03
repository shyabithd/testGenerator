package generator.ga;

import java.io.Serializable;

public interface ChromosomeFactory<T extends Chromosome> extends Serializable {

	public T getChromosome();

}
