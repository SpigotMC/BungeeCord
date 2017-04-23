/*
 * Copyright 2011 Király Attila
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cage.token;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.github.cage.IGenerator;
import com.github.cage.IGeneratorFactory;

/**
 * Factory that can generate {@link Character} generating {@link IGenerator}s.
 * 
 * @author akiraly
 */
public class RandomCharacterGeneratorFactory implements
		IGeneratorFactory<Character> {
	/**
	 * English lower cased vowel character array.
	 */
	public static final char[] ENGLISH_VOWELS = "aeiou".toCharArray();

	/**
	 * English lower cased consonant character array.
	 */
	public static final char[] ENGLISH_CONSONANTS = "bcdfghjklmnpqrstxyvz"
			.toCharArray();

	/**
	 * Arabic numeral character array.
	 */
	public static final char[] ARABIC_NUMERALS = "0123456789".toCharArray();

	/**
	 * Default character set for the default case. It contains the English lower
	 * cased letters.
	 */
	public static final char[] DEFAULT_DEFAULT_CHARACTER_SET = (new String(
			ENGLISH_VOWELS) + new String(ENGLISH_CONSONANTS)).toCharArray();

	/**
	 * Special character sets for the default case.
	 */
	public static final Map<Character, char[]> DEFAULT_SPECIAL_CHARACTER_SETS = Collections
			.unmodifiableMap(createDefaultSpecialCharacterSets());

	private final char[] defaultCharacterSet;

	private final Map<Character, char[]> specialCharacterSets;

	private final Random rnd;

	/**
	 * Generates characters based on the settings of the factory. This is
	 * <b>not</b> thread safe!
	 */
	public class RandomCharacterGenerator implements IGenerator<Character> {
		private char[] currentCharacterSet = defaultCharacterSet;

		public Character next() {
			char next = currentCharacterSet[rnd
					.nextInt(currentCharacterSet.length)];

			if (specialCharacterSets != null) {
				char[] nextCharacterSet = specialCharacterSets.get(next);
				if (nextCharacterSet != null) {
					if (nextCharacterSet.length < 1) {
						throw new IllegalStateException(
								"specialCharacterSets should not hold an empty char[] value");
					}
					currentCharacterSet = nextCharacterSet;
				} else {
					currentCharacterSet = defaultCharacterSet;
				}
			}

			return next;
		}
	}

	/**
	 * Constructor.
	 */
	public RandomCharacterGeneratorFactory() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            used to generate random numbers. Can be null.
	 */
	public RandomCharacterGeneratorFactory(Random rnd) {
		this(null, DEFAULT_SPECIAL_CHARACTER_SETS, rnd);
	}

	/**
	 * Constructor.
	 * 
	 * @param defaultCharacterSet
	 *            used generally for character choosing, can be null.
	 * @param specialCharacterSets
	 *            map contains special cases. A (k, v) pair in this map means
	 *            that character k can only be immediately followed by a
	 *            character in v array. No value should be empty or null. The
	 *            map parameter itself can be null.
	 * @param rnd
	 *            used to generate random numbers. Can be null.
	 */
	public RandomCharacterGeneratorFactory(char[] defaultCharacterSet,
			Map<Character, char[]> specialCharacterSets, Random rnd) {
		this.defaultCharacterSet = defaultCharacterSet != null
				&& defaultCharacterSet.length > 0 ? defaultCharacterSet
				: DEFAULT_DEFAULT_CHARACTER_SET;
		this.specialCharacterSets = specialCharacterSets != null
				&& !specialCharacterSets.isEmpty() ? specialCharacterSets
				: null;
		this.rnd = rnd != null ? rnd : new Random();
	}

	/**
	 * Helper function to build {@link #DEFAULT_SPECIAL_CHARACTER_SETS}. It
	 * contains some rules to avoid confusing character pairs. Also makes sure
	 * that vowels and consonants follow each other in an alternating fashion.
	 * 
	 * @return populated map
	 */
	protected static Map<Character, char[]> createDefaultSpecialCharacterSets() {
		Map<Character, char[]> m = new HashMap<Character, char[]>();

		char[] con = ENGLISH_CONSONANTS;
		String conS = new String(con);
		char[] vow = ENGLISH_VOWELS;
		String vowS = new String(vow);

		m.put('a', con);
		m.put('b', vow);
		m.put('c', vowS.replaceAll("o", "").toCharArray());
		m.put('d', vowS.replaceAll("o", "").toCharArray());
		m.put('e', con);
		m.put('f', vow);
		m.put('g', vow);
		m.put('h', vow);
		m.put('i', conS.replaceAll("j|l", "").toCharArray());
		m.put('j', vowS.replaceAll("i", "").toCharArray());
		m.put('k', vow);
		m.put('l', vowS.replaceAll("i|o", "").toCharArray());
		m.put('m', vow);
		m.put('n', vowS.replaceAll("u", "").toCharArray());
		m.put('o', conS.replaceAll("b|l|p", "").toCharArray());
		m.put('p', vow);
		m.put('q', vowS.replaceAll("o", "").toCharArray());
		m.put('r', vowS.replaceAll("u", "").toCharArray());
		m.put('s', vow);
		m.put('t', vow);
		m.put('u', con);
		m.put('v', vow);
		m.put('w', vow);
		m.put('x', vow);
		m.put('y', vow);
		m.put('z', vow);

		return m;
	}

	public IGenerator<Character> next() {
		return new RandomCharacterGenerator();
	}

	/**
	 * @return default character set for character generation, not null
	 */
	public char[] getDefaultCharacterSet() {
		return defaultCharacterSet;
	}

	/**
	 * @return map holding the special rules for each character, can be null
	 */
	public Map<Character, char[]> getSpecialCharacterSets() {
		return specialCharacterSets;
	}
}
