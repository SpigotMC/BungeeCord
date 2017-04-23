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
package com.github.cage.image;

import java.awt.Color;

import com.github.cage.IGenerator;

/**
 * {@link IGenerator} implementation that returns always the same {@link Color}.
 * This class is thread safe.
 * 
 * @author akiraly
 * 
 */
public class ConstantColorGenerator implements IGenerator<Color> {
	private final Color color;

	/**
	 * Constructor.
	 * 
	 * @param color
	 *            not null
	 */
	public ConstantColorGenerator(Color color) {
		this.color = color;
	}

	public Color next() {
		return color;
	}

}
