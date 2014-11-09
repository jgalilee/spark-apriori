package com.jgalilee.spark.apriori;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Simple combination generator using a pseudo-iterator interface so it can
 * lazily generate combinations.
 *
 * @author jgalilee
 */
public class CombinationGenerator {

	public static final int[] BLANK_COMBINATION = new int[] { Integer.MIN_VALUE };

	private int k;
	private int n;
	private int[] result;
	private Stack<Integer> stack;
	private int[] source;
	private int[] next;

	/**
	 * Create a new instance of the combination generator.
	 */
	public CombinationGenerator() {
		this.stack = new Stack<Integer>();
	}

	/**
	 * Reset the generator generate combinations of size k, with the given source.
	 *
	 * @param k Size of the combinations to generate.
	 * @param source Source of the combinations to be generated.
	 */
	public void reset(int k, int[] source) {
		this.k = k;
		n = source.length;
		this.source = source;
		result = new int[k];
		stack.removeAllElements();
		stack.push(0);
		next = nextCombination();
		return;
	}

	/**
	 * Is there another possible combination to be generated or have they all been
	 * generated.
	 *
	 * @return True if there is another combination to generate, false otherwise.
	 */
	public boolean hasNext() {
		if (k == 0 || BLANK_COMBINATION == next || !hasNextCombination()) {
			return false;
		}
		return true;
	}

	/**
	 * Return the next combination.
	 *
	 * @return int array representation of the next combination.
	 */
	public int[] next() {
		int[] result = next;
		next = nextCombination();
		Arrays.sort(next);
		return result;
	}

	// Internal method to check if there are more combinations.
	private boolean hasNextCombination() {
		return stack.size() > 0;
	}

	// Internal method to generate the next combination. Assumes setup is valid
	// otherwise it returns a blank combination.
	private int[] nextCombination() {
		while (k != 0 && hasNextCombination()) {
			int index = stack.size() - 1;
			int value = stack.pop();
			while (value < n) {
				result[index++] = value++;
				stack.push(value);
				if (index == k) {
					int[]combination = new int[k];
					for (int i = 0; i < k; i++) {
						combination[i] = source[result[i]];
					}
					return combination;
				}
			}
		}
		return BLANK_COMBINATION;
	}

}
