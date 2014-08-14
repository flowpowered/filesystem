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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JarFilePathResolver implements ResourcePathResolver {
    private final Logger logger;
    private final File directory;

    public JarFilePathResolver(File directory) {
        this.directory = directory;
        this.logger = LoggerFactory.getLogger(getClass().getSimpleName());
    }

    public JarFilePathResolver(File directory, Logger logger) {
        this.directory = directory;
        this.logger = logger;
    }

    public JarFile getJar(String host) throws IOException {
        File jar = new File(this.directory, host + ".jar");
        if (!jar.exists()) {
            return null;
        }
        return new JarFile(jar);
    }

    @Override
    public boolean existsInPath(String host, String path) {
        JarFile f = null;
        boolean b = false;
        try {
            f = getJar(host);
            b = f.getJarEntry(path.substring(1)) != null;
        } catch (IOException e) {
            this.logger.error("Caught:", e); // TODO: More descriptive message?
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    this.logger.error("Caught when cosing jar:", e);
                }
            }
        }
        return b;
    }

    @Override
    public boolean existsInPath(URI uri) {
        return existsInPath(uri.getHost(), uri.getPath());
    }

    @Override
    public InputStream getStream(String host, String path) {
        try {
            JarFile f = getJar(host);
            if (f == null) {
                return null;
            }
            JarEntry entry = f.getJarEntry(path.substring(1));
            if (entry == null) {
                return null;
            }
            return f.getInputStream(entry);
        } catch (IOException e) {
            this.logger.error("Caught:", e); // TODO: More descriptive message?
            return null;
        }
    }

    @Override
    public InputStream getStream(URI uri) {
        return getStream(uri.getHost(), uri.getPath());
    }

    @Override
    public String[] list(String host, String path) {
        JarFile jar = null;
        try {
            jar = getJar(host);
            if (jar == null) {
                throw new IllegalArgumentException("Specified JarFile does not exist.");
            }
            // iterate through the JarEntries
            Enumeration<JarEntry> entries = jar.entries();
            List<String> list = new ArrayList<>();
            path = path.substring(1);
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                // we can't load directories, no point in returning them
                // verify that the entry is in the given path
                if (!entry.isDirectory() && name.startsWith(path)) {
                    list.add(name.replaceFirst(path, ""));
                }
            }
            return list.toArray(new String[list.size()]);
        } catch (IOException e) {
            this.logger.error("Caught:", e); // TODO: More descriptive message?
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                    this.logger.error("Caught:", e);
                }
            }
        }
        return null;
    }

    @Override
    public String[] list(URI uri) {
        return list(uri.getHost(), uri.getPath());
    }
}
