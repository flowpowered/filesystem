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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipFilePathResolver extends FilePathResolver {
    private final Logger logger;

    public ZipFilePathResolver(String path) {
        super(path);
        this.logger = LoggerFactory.getLogger(getClass().getSimpleName());
    }

    public ZipFilePathResolver(String path, Logger logger) {
        super(path);
        this.logger = logger;
    }

    public ZipFile getZip(String host) throws IOException {
        File file = new File(this.directory + File.separatorChar + host + ".zip");
        if (file.exists()) {
            return new ZipFile(file);
        }
        return null;
    }

    @Override
    public boolean existsInPath(String host, String path) {
        ZipFile f = null;
        boolean b = false;
        try {
            f = getZip(host);
            if (f == null) {
                return false;
            }
            b = f.getEntry(path.substring(1)) != null;
        } catch (IOException e) {
            this.logger.error("Caught:", e); // TODO: More descriptive message?
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    this.logger.error("Caught:", e);
                }
            }
        }
        return b;
    }

    @Override
    public InputStream getStream(String host, String path) {
        try {
            ZipFile f = getZip(host);
            if (f == null) {
                return null;
            }
            ZipEntry entry = f.getEntry(path.substring(1));
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
    public String[] list(String host, String path) {
        ZipFile zip = null;
        try {
            zip = getZip(host);
            if (zip == null) {
                throw new IllegalArgumentException("Specified ZipFile does not exist.");
            }
            // iterate through the zip's entries
            Enumeration<? extends ZipEntry> entries = zip.entries();
            List<String> list = new ArrayList<>();
            path = path.substring(1);
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // we can't load directories, no point in returning them
                // verify the entry is within the given path
                if (!entry.isDirectory() && name.startsWith(path)) {
                    list.add(name.replaceFirst(path, ""));
                }
            }
            return list.toArray(new String[list.size()]);
        } catch (IOException e) {
            this.logger.error("Caught:", e); // TODO: More descriptive message?
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    this.logger.error("Caught:", e);
                }
            }
        }
        return null;
    }
}
