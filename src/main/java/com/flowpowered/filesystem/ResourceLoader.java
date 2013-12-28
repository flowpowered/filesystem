/*
 * This file is part of Flow Filesystem, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.filesystem;

import java.io.InputStream;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents something that loads a resource.
 */
public abstract class ResourceLoader {
	private final String scheme;
	private final String fallback;

	public ResourceLoader(String scheme, String fallback) {
		this.scheme = scheme;
		this.fallback = fallback;
	}

	public ResourceLoader(String scheme) {
		this(scheme, null);
	}

	/**
	 * Returns an {@link Object} loaded from a specified {@link InputStream}.
	 *
	 * @param in input stream to load object from
	 * @return loaded object
	 */
	public abstract Object load(InputStream in);

	/**
	 * Returns the scheme that this loader represents. This scheme will allow developers to choose to load an object from the loader by specifying this scheme in their specified {@link java.net.URI}.
	 *
	 * @return scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Returns the fallback resource to load if the resource is not found.
	 *
	 * @return fallback resource
	 */
	public String getFallback() {
		return fallback;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResourceLoader && ((ResourceLoader) obj).scheme.equalsIgnoreCase(scheme);
	}

	@Override
	public String toString() {
		return "ResourceLoader(" + scheme + ")";
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(scheme).build();
	}
}
