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

import java.util.Random;

import com.github.cage.IGenerator;
import com.github.cage.IGeneratorFactory;

/**
 * A simple random String generator that can be used to generate tokens for the
 * captcha images.
 * 
 * In its default mode instances of this class generate words from English lower
 * cased letters where vowels and consonants are alternating.
 * 
 * @author akiraly
 */
public class RandomTokenGenerator implements IGenerator<String> {

	/**
	 * Default minimum length of token.
	 */
	protected static final int DEFAULT_TOKEN_LEN_MIN = 8;

	/**
	 * Default maximum length of token is {@link #DEFAULT_TOKEN_LEN_MIN} +
	 * {@value #DEFAULT_TOKEN_LEN_DELTA}.
	 */
	protected static final int DEFAULT_TOKEN_LEN_DELTA = 2;

	private final IGeneratorFactory<Character> characterGeneratorFactory;
	private final int minLength;
	private final int delta;
	private final Random rnd;

	/**
	 * Constructor.
	 */
	public RandomTokenGenerator() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public RandomTokenGenerator(Random rnd) {
		this(rnd, DEFAULT_TOKEN_LEN_MIN, DEFAULT_TOKEN_LEN_DELTA);
	}

	/**
	 * Constructor.
	 * 
	 * @param length
	 *            the length of the generated words, must be > 0
	 * @param rnd
	 *            random generator to be used, can be null
	 */
	public RandomTokenGenerator(Random rnd, int length) {
		this(rnd, length, 0);
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            random generator to be used, can be null
	 * @param minLength
	 *            the minimum length of the generated words, must be > 0
	 * @param delta
	 *            minLength + delta is the maximum length of the generated
	 *            words, delta must be >= 0
	 */
	public RandomTokenGenerator(Random rnd, int minLength, int delta) {
		this(rnd, new RandomCharacterGeneratorFactory(rnd != null ? rnd
				: new Random()), minLength, delta);
	}

	/**
	 * Constructor.
	 * 
	 * @param rnd
	 *            random generator to be used, can be null
	 * @param characterGeneratorFactory
	 *            character generator factory to be used for the actual
	 *            character creation, can be null
	 * @param minLength
	 *            the minimum length of the generated words, must be > 0
	 * @param delta
	 *            minLength + delta is the maximum length of the generated
	 *            words, delta must be >= 0
	 */
	public RandomTokenGenerator(Random rnd,
			IGeneratorFactory<Character> characterGeneratorFactory,
			int minLength, int delta) {
		this.characterGeneratorFactory = characterGeneratorFactory != null ? characterGeneratorFactory
				: new RandomCharacterGeneratorFactory();
		this.minLength = Math.abs(minLength);
		this.delta = Math.abs(delta);
		this.rnd = rnd != null ? rnd : new Random();
	}

	public String next() {
		int length = (delta <= 1 ? 0 : rnd.nextInt(delta) + 1) + minLength;
		char[] word = new char[length];
		IGenerator<Character> generator = characterGeneratorFactory.next();

		for (int fi = 0; fi < word.length; fi++) {
			word[fi] = generator.next();
		}

		return new String(word);
	}

	/**
	 * @return character generator factory used by this class, not null
	 */
	public IGeneratorFactory<Character> getCharacterGeneratorFactory() {
		return characterGeneratorFactory;
	}

	/**
	 * @return minimum length of generated tokens
	 */
	public int getMinLength() {
		return minLength;
	}

	/**
	 * @return maximum length difference to add to the minimum length
	 */
	public int getDelta() {
		return delta;
	}
}
