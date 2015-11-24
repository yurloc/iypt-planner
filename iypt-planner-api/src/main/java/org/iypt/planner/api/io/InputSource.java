package org.iypt.planner.api.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class InputSource {

    private final String name;
    private final URL url;
    private final Charset charset;

    public InputSource(String name, URL url, Charset charset) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        if (url == null) {
            throw new IllegalArgumentException();
        }
        if (charset == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.url = url;
        this.charset = charset;
    }

    public static InputSource fromFile(File f, Charset charset) {
        try {
            URL url = f.toURI().toURL();
            return new InputSource(f.getName(), url, charset);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Cannot get file's URL", ex);
        }
    }

    public static InputSource fromResource(Class<?> baseType, String resourcePath, Charset charset) {
        String name = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        URL url = baseType.getResource(resourcePath);
        return new InputSource(name, url, charset);
    }

    public Charset getCharset() {
        return charset;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public static ClasspathFactory newClasspathFactory(Class<?> baseClass) {
        return new ClasspathFactory(baseClass, "");
    }

    public static ClasspathFactory newClasspathFactory(Class<?> baseClass, String commonPath) {
        return new ClasspathFactory(baseClass, commonPath);
    }

    public static class ClasspathFactory {

        private final Class<?> baseClass;
        private final String commonPath;
        private Charset charset = StandardCharsets.UTF_8;

        private ClasspathFactory(Class<?> baseClass, String commonPath) {
            this.baseClass = baseClass;
            this.commonPath = commonPath;
        }

        public InputSource newInputSource(String path) {
            return InputSource.fromResource(baseClass, commonPath + path, charset);
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }
    }
}
