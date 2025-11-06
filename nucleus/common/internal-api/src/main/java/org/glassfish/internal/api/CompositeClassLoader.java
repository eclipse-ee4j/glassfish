package org.glassfish.internal.api;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A ClassLoader that aggregates multiple ClassLoaders and returns unique resources/classes.
 * This addresses the issue that when multiple ClassLoaders share the same parent,
 * some resources or classes are duplicated if fetched from all of these ClassLoaders individually.
 *
 * For single resource/class lookups, returns the first match found,
 * coming from the first ClassLoader in the list that contains it.
 * For enumeration methods, returns all unique resources from all ClassLoaders.
 *
 * This ClassLoader doesn't delegate to any parent ClassLoader. Instead, it delegates to the
 * provided list of ClassLoaders, which should delegate to their parent ClassLoaders.
 */
public class CompositeClassLoader extends ClassLoader {

    static {
        registerAsParallelCapable();
    }

    private final List<ClassLoader> classLoaders = new CopyOnWriteArrayList<>();

    public CompositeClassLoader() {
        super(null);
    }

    public void addClassLoader(ClassLoader classLoader) {
        if (classLoader != null) {
            classLoaders.add(classLoader);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : classLoaders) {
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                // Continue to next ClassLoader
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader cl : classLoaders) {
            URL resource = cl.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (classLoaders.size() == 1) {
            return classLoaders.get(0).getResources(name);
        }
        List<Enumeration<URL>> enumerations = new ArrayList<>();
        for (ClassLoader cl : classLoaders) {
            enumerations.add(cl.getResources(name));
        }
        return new LazyEnumeration(enumerations);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return loadClass(name);
    }

    @Override
    protected URL findResource(String name) {
        return getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return getResources(name);
    }

    public List<ClassLoader> getClassLoaders() {
        return Collections.unmodifiableList(classLoaders);
    }
}
