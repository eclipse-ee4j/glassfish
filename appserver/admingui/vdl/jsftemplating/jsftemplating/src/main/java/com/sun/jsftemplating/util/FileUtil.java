/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.jsftemplating.util;

import com.sun.jsftemplating.layout.LayoutDefinitionException;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * <p>
 * This class is for general purpose utility methods.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class FileUtil {

    /**
     * <p>
     * This method calculates the system path to the given filename that is relative to the docroot. It takes the
     * <code>ServletContext</code> or <code>PortletContext</code> (which is why this method takes an <code>Object</code> for
     * this parameter) and the relative path to find. It then invokes the <code>getRealPath(String)</code> method of the
     * <code>ServletContext</code> / <code>PortletContext</code> and returns the result. This method uses reflection.
     * </p>
     */
    public static String getRealPath(Object ctx, String relativePath) {
        String path = null;

        // The following should work w/ a ServletContext or PortletContext
        Method method = null;
        try {
            method = ctx.getClass().getMethod("getRealPath", REALPATH_ARGS);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        try {
            path = (String) method.invoke(ctx, relativePath);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        // Return Result
        return path;
    }

    /**
     * <p>
     * This method checks for the <code>relPath</code> in the docroot of the application. This should work in both Portlet
     * and Servlet environments. If <code>FacesContext</code> is null, null will be returned.
     * </p>
     */
    public static URL getResource(String relPath) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        Object ctx = facesContext.getExternalContext().getContext();
        URL url = null;

        // The following should work w/ a ServletContext or PortletContext
        Method method = null;
        try {
            method = ctx.getClass().getMethod("getResource", GET_RES_ARGS);
        } catch (NoSuchMethodException ex) {
            throw new LayoutDefinitionException("Unable to find " + "'getResource' method in this environment!", ex);
        }
        try {
            url = (URL) method.invoke(ctx, "/" + relPath);
        } catch (IllegalAccessException ex) {
            throw new LayoutDefinitionException(ex);
        } catch (InvocationTargetException ex) {
            throw new LayoutDefinitionException(ex);
        }

        return url;
    }

    /**
     * <p>
     * This method searches for the given relative path filename. It first looks relative the context root of the
     * application, it then looks in the classpath, including relative to the <code>META-INF</code> folder. If found a
     * <code>URL</code> to the file will be returned.
     * </p>
     *
     * @param path The Path.
     *
     * @param defSuff The suffix to use if the file specified in path is not found, it is sometimes useful to translate the
     * path using a default suffix.
     */
    public static URL searchForFile(String path, String defSuff) throws IOException {
        // Remove leading '/' characters if needed
        boolean absolutePath = false;
        String newPath = path;
        while (newPath.startsWith("/")) {
            newPath = newPath.substring(1);
            absolutePath = true;
        }

        // Check to see if we have already found this before (on this request)
        URL url = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        Map<String, URL> filesFound = getFilesFoundMap(ctx);
        if (filesFound != null) {
            url = filesFound.get(newPath);
            if (url != null) {
                // We've already figured this out, abort before we start
                return url;
            }
        }

        // Next check relative newPath (i.e. determine the directory w/i the app
        // they are in and prepend it to the newPath)
        if (!absolutePath) {
            // Check for URL syntax... at this point it will look like a relative
            // path.
            //
            // NOTE: While this should not be exposed from the browser, it is
            // valid for server-side code to request page fragments via URLs.
            // If this is the case, "newPath" will be in the form:
            // <protocol>://... We'll simply detect by checking for "://".
            if (newPath.contains("://")) {
                // Looks like a URL...
                try {
                    // Read the contents
                    byte[] content = readFromURL(new URL(newPath));

                    // Use request scope in order to persist it appropriately...
                    // Not retrievied, prevents GC from happenning early
                    if (ctx != null) {
                        ctx.getExternalContext().getRequestMap().put("__gf." + newPath, content);
                    }

                    // Use special URL which will buffer the contents
                    url = new URL(null, newPath, new CachedURLStreamHandler<>(content));

                    // Cache the URL...
                    if (filesFound != null) {
                        // Cache what we found -- each LDM calls this method, help them...
                        filesFound.put(newPath, url);
                    }

                    // We need to end early b/c this is a special case...
                    return url;
                } catch (MalformedURLException ex) {
                    // This is probably bad, but we'll ignore it and see if
                    // it can be found via a relative path.
                } catch (IOException ex) {
                    // Rethrow it b/c this error probably should be shown.
                    throw ex;
                }
            }

            String absPath = getAbsolutePath(ctx, newPath);
            url = searchForFile(absPath, defSuff);

            // We're done, don't search anymore even if not found
            return url;
        }

        // Check for file in docroot.
        url = getResource(newPath);
        if (url == null) {
            // Check the classpath for the file
            ClassLoader loader = Util.getClassLoader(path);
            url = loader.getResource(newPath);
            if (url == null) {
                // Check w/ a leading '/'
                url = loader.getResource("/" + newPath);
                if (url == null) {
                    // Check in "META-INF/"
                    url = loader.getResource("META-INF/" + newPath);
                    if (url == null && defSuff != null) {
                        // Check to see if the extension is not .jsf, if
                        // not then try finding w/ the extension of .jsf
                        // This allows developers to write .jsf files and
                        // share them even if the FacesServlet is mapped
                        // differently
                        int idx = path.lastIndexOf('.');
                        if (idx != -1) {
                            String ext = path.substring(idx);
                            if (!ext.equalsIgnoreCase(defSuff)) {
                                return searchForFile(path.substring(0, idx) + defSuff, null);
                            }
                        } else {
                            return searchForFile(path + defSuff, null);
                        }
                    }
                }
            }
        }

        if (url != null && filesFound != null) {
            // Cache what we found -- each LDM calls this method, help them...
            filesFound.put(newPath, url);
        }

        // Return a url to the file (hopefully)...
        return url;
    }

    /**
     * <p>
     * This method converts a path relative to the current viewId into an absolute path from the context-root. It does this
     * by prepending the current viewId to it. It is expected that relPath does not contain a leading '/'.
     * </p>
     *
     * @param ctx The <code>FacesContext</code>.
     * @param relPath The relative path to convert.
     *
     * @return The absolute path (relative to the context-root).
     */
    public static String getAbsolutePath(FacesContext ctx, String relPath) {
        // Sanity check
        String absPath = null;
        if (ctx != null) {
            // Make sure we have a ViewRoot
            UIViewRoot viewRoot = ctx.getViewRoot();
            if (viewRoot != null) {
                // Get the viewId
                String viewId = viewRoot.getViewId();
                if (viewId == null) {
                    viewId = "/";
                } else if (!viewId.startsWith("/")) {
                    // Ensure our viewId starts with a '/'
                    viewId = "/" + viewId;
                }
                int slash = viewId.lastIndexOf('/');

                // This will give our our base directory...
                absPath = viewId.substring(0, ++slash);

                // Append on the relative path
                absPath += relPath;
            }
        }
        return absPath == null ? "/" + relPath : absPath;
    }

    /**
     * <p>
     * This method looks for "/./" or "/../" elements in an absolute path and removes them. If a "/./" is found, it simply
     * removes it. If a "/../" is found, it removes it and the preceeding path element. This method also removes duplate '/'
     * characters (i.e. "//" becomes "/").
     * </p>
     */
    public static String cleanUpPath(String absPath) {
        // First lets remove any "/./" elements
        int idx;
        while ((idx = absPath.indexOf("/./")) != -1) {
            absPath = absPath.substring(0, idx) + absPath.substring(idx + 2);
        }

        // Next remove any "/../" elements
        while ((idx = absPath.indexOf("/../")) != -1) {
            int prevElement = 0;
            if (idx > 0) {
                // Find previous element
                prevElement = absPath.lastIndexOf('/', idx - 1);
                if (prevElement == -1) {
                    prevElement = 0;
                }
            }
            absPath = absPath.substring(0, prevElement) + absPath.substring(idx + 3);
        }

        // Remove "//"
        while ((idx = absPath.indexOf("//")) != -1) {
            absPath = absPath.substring(0, idx) + absPath.substring(idx + 1);
        }

        // Return the fixed-up path
        return absPath;
    }

    /**
     * <p>
     * This method looks for resources in jar files without using the ClassLoader. It accepts directories in which it should
     * scan for jar files.
     * </p>
     *
     * @param facesContext The <code>FacesContext</code>.
     * @param resourcePath The resource name to search in all jar files.
     * @param searchPaths The array of paths to search for jar files.
     */
    public static List<Tuple> getJarResources(FacesContext facesContext, String resourcePath, String... searchPaths) throws IOException {
        if (searchPaths == null) {
            // Use default jar search path...
            searchPaths = DEFAULT_SEARCH_PATH;
        }
        List<Tuple> entries = new ArrayList<>();
        ExternalContext ec = facesContext.getExternalContext();
        for (String searchPath : searchPaths) {
            Set<String> paths = ec.getResourcePaths(searchPath);
            for (String path : paths) {
                if ("jar".equalsIgnoreCase(path.substring(path.length() - 3))) {
// FIXME: Can this be a URL?
                    JarFile jarFile = new JarFile(new File(ec.getResource(path).getFile()));
                    JarEntry jarEntry = jarFile.getJarEntry(resourcePath);
                    if (jarEntry != null) {
                        entries.add(new Tuple(jarFile, jarEntry));
                    }
                }
            }
        }
        return entries;
    }

    /**
     * <p>
     * This method read content from a <code>URL</code> and returns the result as a <code>byte[]</code>.
     * </p>
     */
    public static byte[] readFromURL(URL url) throws IOException {
        byte buffer[] = new byte[10000];
        byte result[] = new byte[0];

        // try {
        int count = 0;
        int offset = 0;
        InputStream in = url.openStream();

        // Attempt to read up to 10K bytes.
        count = in.read(buffer);
        while (count != -1) {
            // Make room for new content...
            // result = Arrays.copyOf(result, offset + count); Java 6 only...
            // When I can depend on Java 6... replace the following 3 lines
            // with the line above.
            byte oldResult[] = result;
            result = new byte[offset + count];
            System.arraycopy(oldResult, 0, result, 0, offset);

            // Copy in new content...
            System.arraycopy(buffer, 0, result, offset, count);

            // Increment the offset
            offset += count;

            // Attempt to read up to 10K more bytes...
            count = in.read(buffer);
        }
        // } catch (IOException ex) {
        // throw new RuntimException("Error while trying to read from URL: " + url);
        // }
        return result;
    }

    /**
     * <p>
     * This method provides access to a Map containing the URLs of files that have been found already on this particular
     * request. This is done to speed up the task of locating the appropriate URL.
     * </p>
     *
     * <p>
     * The <code>Map</code> returned is keyed by the viewId (or String representation of the URL) for the file in question.
     * If the given <code>FacesContext</code> is <code>null</code>, <code>null</code> will be returned from this method.
     * </p>
     */
    private static Map<String, URL> getFilesFoundMap(FacesContext ctx) {
        Map<String, URL> filesFound = null;
        // Only do this caching if we're in Faces...
        if (ctx != null) {
            filesFound = (Map<String, URL>) ctx.getExternalContext().getRequestMap().get(FILES_FOUND);
            if (filesFound == null) {
                // Not yet created, create it...
                filesFound = new HashMap<>(8);
                ctx.getExternalContext().getRequestMap().put(FILES_FOUND, filesFound);
            }
        }
        return filesFound;
    }

    private static final String FILES_FOUND = "_filesFoundThisRequest";
    private static final Class[] REALPATH_ARGS = new Class[] { String.class };
    private static final Class[] GET_RES_ARGS = new Class[] { String.class };
    private static final String[] DEFAULT_SEARCH_PATH = new String[] { "/WEB-INF/lib/" };
}
