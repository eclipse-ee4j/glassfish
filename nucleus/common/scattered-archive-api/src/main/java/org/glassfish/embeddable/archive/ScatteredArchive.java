/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable.archive;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Abstraction for a Scattered Jakarta EE module (parts disseminated in various directories).
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish();
 * glassfish.start();
 * // Create a scattered web application.
 * ScatteredArchive archive = ScatteredArchive("testapp", ScatteredArchive.Type.WAR);
 * // target/classes directory contains my complied servlets
 * archive.addClassPath(new File("target", "classes"));
 * // resources/sun-web.xml is my WEB-INF/sun-web.xml
 * archive.addMetadata(new File("resources", "sun-web.xml"));
 * // resources/MyLogFactory is my META-INF/services/org.apache.commons.logging.LogFactory
 * archive.addMetadata(new File("resources", "MyLogFactory"), "META-INF/services/org.apache.commons.logging.LogFactory");
 *
 * Deployer deployer = glassfish.getDeployer();
 * // Deploy my scattered web application
 * deployer.deploy(archive.toURI());
 * }</pre>
 *
 * @author Jerome Dochez
 * @author bhavanishankar@java.net
 */
public class ScatteredArchive {

    private static System.Logger logger = System.getLogger(ScatteredArchive.class.getName());
    static final String JAVA_CLASS_PATH_PROPERTY_KEY = "java.class.path";

    String name;
    Type type;
    File rootDirectory;
    List<File> classpaths = new ArrayList<>();
//    File resourcespath;
    Map<String, File> metadatas = new HashMap<>();
    String metadataEntryPrefix;

    /**
     * Construct a new empty scattered archive.
     *
     * @param name name of the archive.
     * @param type type of the archive
     * @throws NullPointerException if name or type is null
     */
    public ScatteredArchive(String name, Type type) {
        if (name == null) {
            throw new NullPointerException("name must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("type must not be null.");
        }
        this.name = name;
        this.type = type;
        this.metadataEntryPrefix = (type == Type.WAR) ? "WEB-INF/" : "META-INF/";
    }

    /**
     * Construct a new scattered archive with all the contents from the rootDirectory.
     * <p/>
     * By default, a scattered archive is not different from any other
     * archive where all the files are located under a top level
     * directory (rootDirectory).
     * <p/>
     * For example, In case of a WAR type archive, the rootDirectory should look like this:
     * <pre>
     *      rootDirectory/WEB-INF/classes/org/myorg/FooServlet.class
     *      rootDirectory/WEB-INF/classes/org/myorg/Bar.class
     *      rootDirectory/WEB-INF/web.xml
     *      rootDirectory/WEB-INF/lib/myjar.jar
     *      rootDirectory/index.jsp
     *      rootDirectory/theme.css
     *      rootDirectory/helper.js
     * </pre>
     * Some files can then be scattered in different locations and be specified
     * through the appropriate add methods of this class.
     * <p/>
     *
     * @param name          name of the archive.
     * @param type          type of the archive
     * @param rootDirectory root directory.
     * @throws NullPointerException     if name, type or rootDirectory is null.
     * @throws IOException              if rootDirectory does not exist.
     * @throws IllegalArgumentException if rootDirectory is not a directory.
     */
    public ScatteredArchive(String name, Type type, File rootDirectory)
            throws IOException {
        this(name, type);
        if (rootDirectory == null) {
            throw new NullPointerException("rootDirectory must not be null.");
        }
        if (!rootDirectory.exists()) {
            throw new IOException(rootDirectory + " does not exist.");
        }
        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory + " is not a directory.");
        }
        this.rootDirectory = rootDirectory;
    }

    /**
     * Add a directory or a JAR file to this scattered archive.
     * <p/>
     * The classpath that is added is considered as a plain Java CLASSPATH.
     * <p/>
     * <b>Case 1 : classpath is a directory:</b>
     * <p/>
     * Let us say there is TEMP/abc directory, which has following contents:
     * <pre>
     *      TEMP/abc/org/myorg/a/A.class
     *      TEMP/abc/org/myorg/b/B.class
     *      TEMP/abc/com/xyz/c/C.class
     *      TEMP/abc/LocalStrings.properties
     *      TEMP/abc/image/1.png
     * </pre>
     * then addClassPath(new File("TEMP", "abc") will make:
     * <p/>
     * (a) The following classes available in the deployed scattered archive application:
     * <pre>
     *          org.myorg.a.A
     *          org.myorg.b.B
     *          com.xyz.c.C
     * </pre>
     * (b) LocalStrings.properties available in the deployed scattered archive application.
     * So, the deployed application can do ResourceBundle.getBundle("LocalStrings");
     * <p/>
     * (c) image/1.png available in the deployed scattered archive application.
     * So, the deployed application can load the image file via getClass().getClassLoader().getResource("image/1.png");
     * <p/>
     * If there is any other type of file under TEMP/abc then it will also be available
     * in the deployed scattered archive application's classloader.
     * <p/>
     * <b>Case 2: classpath is a JAR file</b>
     * <p/>
     * Let us say there is TEMP/xyz.jar, then addClassPath(new File("TEMP", "xyz.jar"))
     * will make all the classes and any random files inside TEMP/xyz.jar
     * available in the deployed scattered archive application.
     *
     * @param classpath A directory or a JAR file.
     * @throws NullPointerException if classpath is null
     * @throws IOException          if the classpath is not found.
     */
    public void addClassPath(File classpath) throws IOException {
        if (classpath == null) {
            throw new NullPointerException("classpath must not be null.");
        }
        if (!classpath.exists()) {
            throw new IOException(classpath + " does not exist.");
        }
        this.classpaths.add(classpath);
    }

    /**
     * Add all directories and JAR files on the current classpath to this scattered archive
     * using {@link #addClassPath(File)}.
     * Ignores Jakarta EE API and GlassFish Embedded JAR files (those that match the {@code jakarta.}
     * and {@code glassfish-embedded-} prefixes).
     * <p/>
     * Reads the current classpath from the {@code java.class.path} system property.
     * If it's not available, nothing is added to the classpath.
     * <p/>
     * The classpath that is added is considered as a plain Java CLASSPATH.
     * <p/>
     * If a classpath element is not found, a warning is logged using {@link Logger} and
     * the element is ignored.
     *
     * @param excludePatterns If a JAR file name matches any of these regular expressions
     *
     * @see #addCurrentClassPath(java.util.function.Predicate)
     * @see #addClassPath(java.io.File)
     */
    public void addCurrentClassPath(String... excludePatterns) {
        addCurrentClassPath(path -> {
            var fileName = new File(path).getName();
            return fileNameMatchesAny(fileName, excludePatterns)
                    || fileNameMatchesAny(fileName, "jakarta\\..*\\.jar", "glassfish-embedded-.*\\.jar");
        });
    }


    /**
     * Add all directories and JAR files on the current classpath to this scattered archive
     * using {@link #addClassPath(java.io.File)}.
     * Ignores Jakarta EE API and GlassFish Embedded JAR files (those that match the {@code jakarta.}
     * and {@code glassfish-embedded-} prefixes).
     * <p/>
     * Reads the current classpath from the {@code java.class.path} system property.
     * If it's not available, nothing is added to the classpath.
     * <p/>
     * The classpath that is added is considered as a plain Java CLASSPATH.
     * <p/>
     * If a classpath element is not found, a warning is logged using {@link Logger} and
     * the element is ignored.
     *
     * @param exclude A predicate to exclude mathing elements from the classpath.
     *                If this predicate returns {@code true} for the whole path to the classpath element,
     *                the element is not added to the classpath
     *
     * @see #addCurrentClassPath(java.lang.String...)
     * @see #addClassPath(java.io.File)
     */
    public void addCurrentClassPath(Predicate<String> exclude) {
        final String classpath = System.getProperty(JAVA_CLASS_PATH_PROPERTY_KEY, "");
        if (classpath.isBlank()) {
            return;
        }
        for (String pathElem : classpath.split(File.pathSeparator)) {
            if ( ! exclude.test(pathElem)) {
                try {
                    this.addClassPath(new File(pathElem));
                } catch (IOException ex) {
                    logger.log(Level.WARNING,
                            () -> "Could not add " + pathElem + " to the classpath: " + ex.getMessage(),
                            ex);
                }
            }
        }
    }

    private static boolean fileNameMatchesAny(String fileName, String... matches) {
        for (String match : matches) {
            if (fileName.matches(match)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a new metadata to this scattered archive.
     * <p/>
     * The addMetadata(metadata) method has the same effect as:
     * <pre>
     *      addMetadata(metadata, null)
     * </pre>
     * Follows the same semantics as {@link #addMetadata(File, String)} method.
     */
    public void addMetadata(File metadata) throws IOException {
        addMetadata(metadata, null);
    }

    /**
     * Add a new metadata to this scattered archive.
     * <p/>
     * A metadata is identified by its name (e.g., META-INF/ejb.xml).
     * <p/>
     * If the specified name is null, then the metadata is considered as a
     * deployment descriptor metadata and the name is computed as:
     * <pre>
     *      "WEB-INF/" + metadata.getName() for WAR type archive.
     *      "META-INF/" + metadata.getName() for other type of archive.
     * </pre>
     * If the scattered archive already contains the metadata with the same name,
     * then the old value is replaced.
     *
     * @param metadata location of the metadata
     * @param name     name of the metadata (e.g.,
     *                 META-INF/ejb.xml or META-INF/sun-ejb-jar.xml)
     * @throws NullPointerException     if metadata is null
     * @throws IOException              if metadata does not exist.
     * @throws IllegalArgumentException if metadata is a directory.
     */
    public void addMetadata(File metadata, String name) throws IOException {
        if (metadata == null) {
            throw new NullPointerException("metadata must not be null.");
        }
        if (!metadata.exists()) {
            throw new IOException(metadata + " does not exist.");
        }
        if (metadata.isDirectory()) {
            throw new IllegalArgumentException(metadata + " is a directory.");
        }
        if (name == null) {
            name = metadataEntryPrefix + metadata.getName();
        }
        this.metadatas.put(name, metadata);
    }

    /**
     * Get the deployable URI for this scattered archive.
     * <p/>
     * <i>Note : java.io.tmpdir is used while building the URI.</i>
     *
     * @return Deployable scattered archive URI.
     * @throws IOException if any I/O error happens while building the URI
     *                     or while reading metadata, classpath elements, rootDirectory.
     */
    public URI toURI() throws IOException {
        return new Assembler().assemble(this);
    }

    /**
     * Enumeration values for the scattered Jakarta EE module types.
     *
     * @author bhavanishankar@java.net
     */
    public enum Type {
        /**
         * The module is an Enterprise Java Bean or Application Client archive.
         */
        JAR,
        /**
         * The module is a Web Application archive.
         */
        WAR,
        /**
         * The module is a Connector archive.
         */
        RAR,
    }
}
