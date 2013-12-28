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

import com.flowpowered.filesystem.resolver.ResourcePathResolver;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

public interface FileSystem {

	/**
	 * Returns a set of all registered loaders.
	 *
	 * @return all loaders
	 */
	public Set<ResourceLoader> getLoaders();

	/**
	 * Returns the loader with the specified scheme. There can only ever be one loader per scheme in the FileSystem.
	 *
	 * @param scheme to get
	 * @return loader with specified scheme
	 */
	public ResourceLoader getLoader(String scheme);

	/**
	 * Registers the specified loader.
	 *
	 * @param loader to register
	 */
	public void registerLoader(ResourceLoader loader);

	/**
	 * Returns an {@link InputStream} of a resource at the specified {@link URI}.
	 *
	 * @param uri to get stream from
	 * @return input stream
	 * @throws ResourceNotFoundException if there is no resource at specified path
	 */
	public InputStream getResourceStream(URI uri) throws ResourceNotFoundException;

	/**
	 * Returns an {@link InputStream} of a resource at the specified {@link URI}.
	 *
	 * @param uri to get stream from
	 * @return input stream
	 * @throws ResourceNotFoundException if there is no resource at specified path
	 */
	public InputStream getResourceStream(String uri) throws ResourceNotFoundException;

	/**
	 * Adds the resource at the specified location to the system's resource cache.
	 *
	 * @param uri to load resource at
	 * @return the loaded resource
	 * @throws LoaderNotFoundException if there is no loader for the specified scheme
	 * @throws ResourceNotFoundException if there is no resource at the specified path
	 * @throws IOException if there was a problem obtaining/disposing the input stream
	 */
	public Object loadResource(URI uri) throws LoaderNotFoundException, ResourceNotFoundException, IOException;

	/**
	 * Adds the resource at the specified location to the system's resource cache.
	 *
	 * @param uri to load resource at
	 * @return the loaded resource
	 * @throws LoaderNotFoundException if there is no loader for the specified scheme
	 * @throws ResourceNotFoundException if there is no resource at the specified path
	 * @throws IOException if there was a problem obtaining/disposing the input stream
	 */
	public Object loadResource(String uri) throws LoaderNotFoundException, ResourceNotFoundException, IOException;

	/**
	 * Returns the resource at the specified path with an inferred type. If this resource is not loaded when this is called, it will be automatically loaded and cached before returning the resource. This
	 * call assumes that the inferred return type is actually the correct type of the resource loader's return type. If the resource is not found in the specified path, this call will then attempt to
	 * load the fallback resource specified in the {@link ResourceLoader} of this scheme; if that is null, the returned resource will be null.
	 *
	 * @param uri to get resource from
	 * @param <R> inferred type of resource
	 * @return resource at path
	 */
	public <R> R getResource(URI uri);

	/**
	 * Returns the resource at the specified path with an inferred type. If this resource is not loaded when this is called, it will be automatically loaded and cached before returning the resource. This
	 * call assumes that the inferred return type is actually the correct type of the resource loader's return type. If the resource is not found in the specified path, this call will then attempt to
	 * load the fallback resource specified in the {@link ResourceLoader} of this scheme; if that is null, the returned resource will be null.
	 *
	 * @param uri to get resource from
	 * @param <R> inferred type of resource
	 * @return resource at path
	 */
	public <R> R getResource(String uri);

	/**
	 * Returns a list of all the resources in the specified directory.
	 *
	 * @param uri to get resources from
	 * @param <R> type of resources
	 * @return resources
	 * @see {@link #getResource(java.net.URI)}
	 */
	public <R> List<R> getResources(URI uri);

	/**
	 * Returns a list of all the resources in the specified directory.
	 *
	 * @param uri to get resources from
	 * @param <R> type of resources
	 * @return resources
	 * @see #getResource(java.net.URI)
	 */
	public <R> List<R> getResources(String uri);

	/**
	 * Returns a list of all {@link ResourcePathResolver}s that are currently on the system. These resolvers handle the {@link URI}s passed to {@link #getResource(java.net.URI)} to find a suitable input
	 * stream for the resource.
	 *
	 * @return list of path resolvers
	 */
	public List<ResourcePathResolver> getPathResolvers();

	/**
	 * Adds a new path resolver to be queried when attempting to find a suitable input stream for a specified {@link URI} in {@link #getResource(java.net.URI)}
	 *
	 * @param pathResolver to add
	 */
	public void addPathResolver(ResourcePathResolver pathResolver);

	/**
	 * Removes the path resolver to be queried when attempting to find a suitable input stream for a specified {@link URI} in {@link #getResource(java.net.URI)}
	 *
	 * @param pathResolver to remove
	 */
	public void removePathResolver(ResourcePathResolver pathResolver);
}