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
package com.flowpowered.filesystem.resolver;

import java.io.InputStream;
import java.net.URI;

public interface ResourcePathResolver {
    /**
     * Returns true if the specified path exists in the host.
     *
     * @param host of path
     * @param path within the host
     * @return true if specified path exists
     */
    public boolean existsInPath(String host, String path);

    /**
     * Returns true if the specified path exists in the host.
     *
     * @param uri including the host and path of the resource
     * @return true if specified path exists
     */
    public boolean existsInPath(URI uri);

    /**
     * Returns an {@link java.io.InputStream} at the given host and path to be resolved by the implementing class.
     *
     * @param host of stream
     * @param path within the host
     * @return input stream
     */
    public InputStream getStream(String host, String path);

    /**
     * Returns an {@link java.io.InputStream} at the given host and path to be resolved by the implementing class.
     *
     * @param uri including the host and path of the resource
     * @return input stream or null if the stream does not exist
     */
    public InputStream getStream(URI uri);

    /**
     * Lists all files in the specified directory within the host. The specified path must end in a '/' to identify the path as a directory.
     *
     * @param host of the directory
     * @param path within the host
     * @return array of the names of the files within the specified path
     */
    public String[] list(String host, String path);

    /**
     * Lists all files in the specified directory within the host. The specified path must end in a '/' to identify the path as a directory.
     *
     * @param uri including the host and path of the resource
     * @return array of the names of the files within the specified path
     */
    public String[] list(URI uri);
}
