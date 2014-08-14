/*
 * This file is part of Flow Filesystem, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowpowered.filesystem.resolver.ResourcePathResolver;

public abstract class SimpleFileSystem implements FileSystem {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFileSystem.class); // TODO: Make it non-static and accept it in constructor.

    protected final Set<ResourceLoader> loaders = new HashSet<>();
    protected final Map<URI, Object> loadedResources = new HashMap<>();
    protected final List<ResourcePathResolver> pathResolvers = new ArrayList<>();
    protected final Map<String, URI> requestedInstallations = new HashMap<>();

    private void loadFallback(ResourceLoader loader) {
        String fallback = loader.getFallback();
        if (fallback != null) {
            try {
                loadResource(fallback);
            } catch (LoaderNotFoundException e) {
                throw new IllegalArgumentException("Specified fallback has no associated loader", e);
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Specified fallback does not exist.", e);
            } catch (IOException e) {
                throw new IllegalStateException("Error while loading fallback resource", e);
            }
        }
    }

    @Override
    public Set<ResourceLoader> getLoaders() {
        return Collections.unmodifiableSet(this.loaders);
    }

    @Override
    public ResourceLoader getLoader(String scheme) {
        for (ResourceLoader loader : this.loaders) {
            if (loader.getScheme().equalsIgnoreCase(scheme)) {
                return loader;
            }
        }
        return null;
    }

    @Override
    public void registerLoader(ResourceLoader loader) {
        // load the fallback
        this.loaders.add(loader);
        loadFallback(loader);
    }

    @Override
    public InputStream getResourceStream(URI path) throws ResourceNotFoundException {
        // Find the correct search path
        for (ResourcePathResolver resolver : this.pathResolvers) {
            InputStream stream = resolver.getStream(path);
            if (stream != null) {
                return stream;
            }
        }
        throw new ResourceNotFoundException(path.toString());
    }

    @Override
    public InputStream getResourceStream(String path) throws ResourceNotFoundException {
        try {
            return getResourceStream(new URI(path));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Tried to get a Resource Stream URI, but" + path + " isn't a URI", e);
        }
    }

    @Override
    public Object loadResource(URI uri) throws LoaderNotFoundException, ResourceNotFoundException, IOException {
        // find the loader
        // this needs to be thrown first, so we can use a fallback loader and know it exists
        String scheme = uri.getScheme();
        ResourceLoader loader = getLoader(scheme);
        if (loader == null) {
            throw new LoaderNotFoundException(scheme);
        }

        // grab the input stream
        try (InputStream in = new BufferedInputStream(getResourceStream(uri))) {
            Object resource = loader.load(in);
            if (resource == null) {
                throw new IllegalStateException("Loader for scheme '" + scheme + "' returned a null resource.");
            }
            this.loadedResources.put(uri, resource);
            return resource;
        }
    }

    @Override
    public Object loadResource(String uri) throws LoaderNotFoundException, ResourceNotFoundException, IOException {
        try {
            return loadResource(new URI(uri));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Specified URI is not valid.", e);
        }
    }

    @SuppressWarnings ("unchecked")
    private <R> R tryCast(Object obj, String scheme) {
        try {
            return (R) obj;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Specified scheme '" + scheme + "' does not point to the inferred resource type.", e);
        }
    }

    @Override
    public <R> R getResource(URI uri) {
        if (this.loadedResources.containsKey(uri)) {
            // already loaded
            return tryCast(this.loadedResources.get(uri), uri.getScheme());
        }

        try {
            // not loaded yet
            return tryCast(loadResource(uri), uri.getScheme());
        } catch (LoaderNotFoundException e) {
            // scheme has not loader
            throw new IllegalArgumentException("No loader found for scheme " + uri.getScheme(), e);
        } catch (IOException e) {
            // error closing the stream
            throw new IllegalArgumentException("An exception occurred when loading the resource at " + uri.toString(), e);
        } catch (ResourceNotFoundException e) {
            // not found in path, try to load fallback resource
            LOGGER.warn("No resource found at " + uri.toString() + ", loading fallback..."); // TODO: Use parametrized message instead of string concatation.
            String fallback = getLoader(uri.getScheme()).getFallback(); // assumption: loader is never null here
            if (fallback == null) {
                throw new IllegalStateException("No resource found at " + uri.toString() + " and has no fallback resource.");
            }

            try {
                return tryCast(this.loadedResources.get(new URI(fallback)), uri.getScheme());
            } catch (URISyntaxException se) {
                IllegalStateException ise = new IllegalStateException("Fallback name for scheme " + uri.getScheme() + " is invalid.", e);
                ise.addSuppressed(se);
                throw ise;
            }
        }
    }

    @Override
    public <R> R getResource(String uri) {
        try {
            return getResource(new URI(uri));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Specified URI '" + uri + "' is invalid.", e);
        }
    }

    private ResourcePathResolver getPathResolver(URI uri) {
        for (ResourcePathResolver resolver : this.pathResolvers) {
            if (resolver.existsInPath(uri)) {
                return resolver;
            }
        }
        return null;
    }

    @Override
    public <R> List<R> getResources(URI uri) {
        ResourcePathResolver resolver = getPathResolver(uri);
        if (resolver == null) {
            throw new IllegalArgumentException("Could not resolve path '" + uri.toString() + "'");
        }

        String[] files = resolver.list(uri);
        List<R> resources = new ArrayList<>();
        for (String file : files) {
            resources.add((R) getResource(uri.getScheme() + "://" + uri.getHost() + uri.getPath() + file));
        }
        return resources;
    }

    @Override
    public <R> List<R> getResources(String uri) {
        try {
            return getResources(new URI(uri));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Specified uri is invalid", e);
        }
    }

    @Override
    public List<ResourcePathResolver> getPathResolvers() {
        return Collections.unmodifiableList(this.pathResolvers);
    }

    @Override
    public void addPathResolver(ResourcePathResolver pathResolver) {
        this.pathResolvers.add(pathResolver);
    }

    @Override
    public void removePathResolver(ResourcePathResolver pathResolver) {
        this.pathResolvers.remove(pathResolver);
    }
}
