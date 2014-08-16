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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FilePathResolver implements ResourcePathResolver {
    protected final String directory;

    public FilePathResolver(String path) {
        this.directory = path;
    }

    public Path getPath(String host, String path) {
        return Paths.get(directory, host, path);
    }

    @Override
    public boolean existsInPath(String host, String path) {
        return Files.exists(getPath(host, path));
    }

    @Override
    public boolean existsInPath(URI uri) {
        return this.existsInPath(uri.getHost(), uri.getPath());
    }

    @Override
    public InputStream getStream(String host, String path) {
        try {
            return Files.newInputStream(getPath(host, path));
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public InputStream getStream(URI uri) {
        return this.getStream(uri.getHost(), uri.getPath());
    }

    @Override
    public String[] list(String host, String path) {
        DirectoryStream<Path> stream;
        try {
            stream = Files.newDirectoryStream(getPath(host, path), new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path entry) throws IOException {
                    return Files.isRegularFile(entry);
                }
            });
        } catch (IOException ex) {
            return new String[0];
        }
        List<String> list = new ArrayList<>();
        for (Path local : stream) {
            list.add(local.getFileName().toString());
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public String[] list(URI uri) {
        return list(uri.getHost(), uri.getPath());
    }
}
