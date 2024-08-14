/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Java Runtime environment configuration.
 */
@Configured
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-profiler"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-profiler")
})
public interface JavaConfig extends ConfigBeanProxy, PropertyBag, JvmOptionBag {

    /**
     * Gets the value of the {@code javaHome} property.
     *
     * <p>Specifies the installation directory for Java runtime. JDK 1.4 or higher is supported.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "${com.sun.aas.javaRoot}")
    String getJavaHome();

    /**
     * Sets the value of the {@code javaHome} property.
     *
     * @param javaHome allowed object is {@link String}
     */
    void setJavaHome(String javaHome) throws PropertyVetoException;

    /**
     * Gets the value of the {@code debugEnabled} property.
     *
     * <p>If set to {@code true}, the server starts up in debug mode ready for attaching
     * with a JPDA based debugger.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDebugEnabled();

    /**
     * Sets the value of the {@code debugEnabled} property.
     *
     * @param debugEnabled allowed object is {@link String}
     */
    void setDebugEnabled(String debugEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code debugOptions} property.
     *
     * <p>JPDA based debugging options string.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n")
    String getDebugOptions();

    /**
     * Sets the value of the {@code debugOptions} property.
     *
     * @param debugOptions allowed object is {@link String}
     */
    void setDebugOptions(String debugOptions) throws PropertyVetoException;

    /**
     * Gets the value of the {@code rmicOptions} property.
     *
     * <p>Options string passed to RMI compiler, at application deployment time.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "-iiop -poa -alwaysgenerate -keepgenerated -g")
    String getRmicOptions();

    /**
     * Sets the value of the {@code rmicOptions} property.
     *
     * @param rmicOptions allowed object is {@link String}
     */
    void setRmicOptions(String rmicOptions) throws PropertyVetoException;

    /**
     * Gets the value of the {@code javacOptions} property.
     *
     * <p>Options string passed to Java compiler, at application deployment time.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "-g")
    String getJavacOptions();

    /**
     * Sets the value of the {@code javacOptions} property.
     *
     * @param javacOptions allowed object is {@link String}
     */
    void setJavacOptions(String javacOptions) throws PropertyVetoException;

    /**
     * Gets the value of the {@code classpathPrefix} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getClasspathPrefix();

    /**
     * Sets the value of the {@code classpathPrefix} property.
     *
     * <p>A java classpath string that is prefixed to server-classpath.
     *
     * @param classpathPrefix allowed object is {@link String}
     */
    void setClasspathPrefix(String classpathPrefix) throws PropertyVetoException;

    /**
     * Gets the value of the {@code classpathSuffix} property.
     *
     * <p>A java classpath string that is appended to server-classpath.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getClasspathSuffix();

    /**
     * Sets the value of the {@code classpathSuffix} property.
     *
     * @param classpathSuffix allowed object is {@link String}
     */
    void setClasspathSuffix(String classpathSuffix) throws PropertyVetoException;

    /**
     * Gets the value of the {@code serverClasspath} property.
     *
     * <p>A java classpath string that specifies the classes needed by the Application server.
     * Do not expect users to change this under normal conditions. The shared application
     * server classloader forms the final classpath by concatenating {@code classpath-prefix},
     * {@code ${INSTALL_DIR}/lib}, {@code server-classpath}, and {@code classpath-suffix}.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getServerClasspath();

    /**
     * Sets the value of the {@code serverClasspath} property.
     *
     * @param serverClasspath allowed object is {@link String}
     */
    void setServerClasspath(String serverClasspath) throws PropertyVetoException;

    /**
     * Gets the value of the {@code systemClasspath} property.
     *
     * <p>This classpath string supplied to the jvm at server startup. Contains
     * {@code appserv-launch.jar} by default. Users may add to this classpath.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getSystemClasspath();

    /**
     * Sets the value of the {@code systemClasspath} property.
     *
     * @param systemClasspath allowed object is {@link String}
     */
    void setSystemClasspath(String systemClasspath) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nativeLibraryPathPrefix} property.
     *
     * <p>Prepended to the native library path, which is constructed internally.
     *
     * <p>Internally, the native library path is automatically constructed to be
     * a concatenation of Application Server installation relative path for its
     * native shared libraries, standard JRE native library path, the shell
     * environment setting ({@code LD-LIBRARY-PATH} on Unix) and any path
     * that may be specified in the profile element.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getNativeLibraryPathPrefix();

    /**
     * Sets the value of the {@code nativeLibraryPathPrefix} property.
     *
     * @param pathPrefix allowed object is {@link String}
     */
    void setNativeLibraryPathPrefix(String pathPrefix) throws PropertyVetoException;

    /**
     * Gets the value of the {@code nativeLibraryPathSuffix} property.
     *
     * <p>Appended to the native library path, which is constructed as described above.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getNativeLibraryPathSuffix();

    /**
     * Sets the value of the {@code nativeLibraryPathSuffix} property.
     *
     * @param pathSuffix allowed object is {@link String}
     */
    void setNativeLibraryPathSuffix(String pathSuffix) throws PropertyVetoException;

    /**
     * Gets the value of the {@code bytecodePreprocessors} property.
     *
     * <p>A comma separated list of classnames, each of which must implement
     * the {@code com.sun.appserv.BytecodePreprocessor} interface. Each of the
     * specified preprocessor class will be called in the order specified.
     * At the moment the compelling use is for a 3rd party Performance Profiling tool.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getBytecodePreprocessors();

    /**
     * Sets the value of the {@code bytecodePreprocessors} property.
     *
     * @param bytecodePreprocessors allowed object is {@link String}
     */
    void setBytecodePreprocessors(String bytecodePreprocessors) throws PropertyVetoException;

    /**
     * Gets the value of the {@code envClasspathIgnored} property.
     *
     * <p>If set to {@code false}, the {@code CLASSPATH} environment variable will be read
     * and appended to the Application Server classpath, which is constructed as described
     * above. The {@code CLASSPATH} environment variable will be added after the
     * {@code classpath-suffix}, at the very end.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnvClasspathIgnored();

    /**
     * Sets the value of the {@code envClasspathIgnored} property.
     *
     * @param envClasspathIgnored allowed object is {@link String}
     */
    void setEnvClasspathIgnored(String envClasspathIgnored) throws PropertyVetoException;

    /**
     * Gets the value of the {@code profiler} property.
     *
     * @return possible object is {@link Profiler}
     */
    @Element
    Profiler getProfiler();

    /**
     * Sets the value of the {@code profiler} property.
     *
     * @param profiler allowed object is {@link Profiler}
     */
    void setProfiler(Profiler profiler) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    /**
     * Returns the javac options for deployment. The options can be anything except "-d",
     * "-classpath" and "-cp". It tokenizes the options by blank space between them.
     * It does not to detect options like "-g -g -g" since javac handles it.
     *
     * @return javac options as of a list of {@link String}
     */
    default List<String> getJavacOptionsAsList() {
        List<String> javacOptions = new ArrayList<>();

        String options = getJavacOptions();
        StringTokenizer st = new StringTokenizer(options, " ");
        while (st.hasMoreTokens()) {
            String op = st.nextToken();
            if (!(op.startsWith("-d") || op.startsWith("-cp") || op.startsWith("-classpath"))) {
                javacOptions.add(op);
            }
        }

        return Collections.unmodifiableList(javacOptions);
    }
}
