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
package com.flowpowered.filesystem.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class FilePathResolver implements ResourcePathResolver {
    protected final String directory;

    public FilePathResolver(String path) {
        this.directory = path;
    }

    public File getFile(String host, String path) {
        return new File(directory + File.separatorChar + host, path);
    }

    @Override
    public boolean existsInPath(String host, String path) {
        return getFile(host, path).exists();
    }

    @Override
    public boolean existsInPath(URI uri) {
        return this.existsInPath(uri.getHost(), uri.getPath());
    }

    @Override
    public InputStream getStream(String host, String path) {
        try {
            return new FileInputStream(getFile(host, path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public InputStream getStream(URI uri) {
        return this.getStream(uri.getHost(), uri.getPath());
    }

    @Override
    public String[] list(String host, String path) {
        List<String> list = new ArrayList<>();
        for (File file : getFile(host, path).listFiles()) {
            // we can't load directories, no point in returning them
            if (file.isFile()) {
                list.add(file.getName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public String[] list(URI uri) {
        return list(uri.getHost(), uri.getPath());
    }
}

