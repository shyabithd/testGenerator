package generator.utils;

import generator.Properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class ListUtil {
	/**
	 * <p>tail</p>
	 *
	 * @param list a {@link List} object.
	 * @param <T> a T object.
	 * @return a {@link List} object.
	 */
	public static <T> List<T> tail(List<T> list) {
		return list.subList(1, list.size());
	}

	/**
	 * <p>anyEquals</p>
	 *
	 * @param list a {@link List} object.
	 * @param obj a T object.
	 * @param <T> a T object.
	 * @return a boolean.
	 */
	public static <T> boolean anyEquals(List<T> list, T obj) {
		for (T item : list) {
			if (item.equals(obj)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>shuffledList</p>
	 *
	 * @param list a {@link List} object.
	 * @param <T> a T object.
	 * @return a {@link List} object.
	 */
	public static <T> List<T> shuffledList(List<T> list) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result);
		return result;
	}

	/**
	 * <p>shuffledList</p>
	 *
	 * @param list a {@link List} object.
	 * @param rnd a {@link Random} object.
	 * @param <T> a T object.
	 * @return a {@link List} object.
	 */
	public static <T> List<T> shuffledList(List<T> list, Random rnd) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result, rnd);
		return result;
	}

	private static int getIndex(List<?> population) {
		double r = Randomness.nextDouble();
		double d = Properties.RANK_BIAS
				- Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS)
				- (4.0 * (Properties.RANK_BIAS - 1.0) * r));
		int length = population.size();

		d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

		//this is not needed because population is sorted based on Maximization
		//if(maximize)
		//	d = 1.0 - d; // to do that if we want to have Maximisation

		int index = (int) (length * d);
		return index;
	}

	public static <T> T selectRankBiased(List<T> list) {
		int index = getIndex(list);
		return list.get(index);
	}
}
