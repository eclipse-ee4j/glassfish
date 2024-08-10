/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.embedded;

import java.io.File;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PreDestroy;

/**
 * Abstraction for a virtual filesystem that be used by the server to store important files.
 *
 * @author Jerome Dochez
 */
public class EmbeddedFileSystem implements PreDestroy {

    /**
     * EmbeddedFileSystem builder class. Clients must use one these builder instances
     * to create an EmbeddedFileSystem instance.
     *
     */
    public static class Builder {
        boolean autoDelete=false;
        boolean readOnly = true;
        boolean cookedMode=false;
        File configFile=null;
        File installRoot=null;
        File instanceRoot=null;

        // TODO : add some obvious file errors during the build()


        // todo : considering removing this call.

        /**
         * Sets the auto delete flag. If on, the embedded file system backing store will be
         * deleted once the embedded server is shutdown.
         *
         * @param b true to delete the instance root directory on server shutdown
         * @return itself
         */
        public Builder autoDelete(boolean b) {
            this.autoDelete = b;
            return this;
        }

        /**
         * Sets the location of the read-only domain.xml configuration file. The file can be named anything
         * but must have a valid domain.xml content. Any management operation will not be written to the
         * file.
         *
         * @param f location of the configuration file
         * @return itself
         */
        public Builder configurationFile(File f) {
            return configurationFile(f, true);

        }

        /**
         * Sets the location of the domain.xml configuration file. The file can be named anything
         * but must have a valid domain.xml content.
         *
         * @param f location of the configuration file
         * @param readOnly true if the file is readonly, false if management operations should be
         * persisted.
         * @return itself
         */
        public Builder configurationFile(File f, boolean readOnly) {
            this.configFile = f;
            this.readOnly = readOnly;
            return this;

        }

        /**
         * Sets the installation directory, using the installation module directory content
         * as the application server classpath. The classloader used to load this class will
         * be the parent class loader to the embedded server classloader  which will use the
         * modules located in the passed installation directory.
         *
         * @param f location of the glassfish installation
         * @return itself
         */
        public Builder installRoot(File f) {
            return installRoot(f, false);
        }

        // todo : replace cookedMode to...
        // todo :

        /**
         * Sets the installation directory and direct whether or not to use the installation's
         * module directory content in the embedded server classpath. If cookMode is on, the
         * embedded server will be loaded using the classloader used to load this class.
         *
         * @param f location of the installation
         * @param cookedMode true to use this class classloader, false to create a new classloader
         * with the installation modules directory content.
         * @return itself
         */
        public Builder installRoot(File f, boolean cookedMode) {
            this.installRoot = f;
            this.cookedMode = cookedMode;
            return this;
        }

        /**
         * Sets the location of the domain directory used to load this instance of the
         * embedded server.
         * @param f location of the domain directory
         * @return itself
         */
        public Builder instanceRoot(File f) {
            this.instanceRoot=f;
            if (this.configFile==null) {
                File tmp = new File(instanceRoot, "config");
                configFile = new File(tmp, "domain.xml");
                if (!configFile.exists()) {
                    configFile = null;
                }
            }
            return this;
        }

        /**
         * Builds a configured embedded file system instance that can be used to configure
         * an embedded server.
         *
         * @return an immutable configured instance of an EmbeddedFileSystem
         */
        public EmbeddedFileSystem build() {
            return new EmbeddedFileSystem(this);
        }

        @Override
        public String toString() {
            return "EmbeddedFileSystem>>installRoot = " + installRoot + ", instanceRoot=" +
                    instanceRoot + ",configFile=" + configFile + ",autoDelete=" + autoDelete;
        }

    }

    public final boolean autoDelete;
    public final boolean readOnlyConfigFile;
    public final boolean cookedMode;
    public final File installRoot;
    public final File instanceRoot;
    public final File configFile;

    private EmbeddedFileSystem(Builder builder) {
        autoDelete = builder.autoDelete;
        readOnlyConfigFile = builder.readOnly;
        installRoot = builder.installRoot;
        instanceRoot = builder.instanceRoot;
        configFile = builder.configFile;
        cookedMode = builder.cookedMode;
    }

    public void preDestroy() {
        Logger.getAnonymousLogger().finer("delete " + instanceRoot + " = " + autoDelete);
        if (autoDelete && instanceRoot != null) {
            // recursively delete instanceRoot directory
            Logger.getAnonymousLogger().finer("Deleting recursively" + instanceRoot);
            deleteAll(instanceRoot);
        }

    }

    private void deleteAll(File f) {
        for (File child : f.listFiles()) {
            if (child.isDirectory()) {
                deleteAll(child);
            } else {
                child.delete();
            }
        }
        f.delete();
    }

    void copy(Builder that) {
        that.autoDelete(autoDelete);
        that.configurationFile(configFile, readOnlyConfigFile);
        that.installRoot(installRoot, cookedMode);
        that.instanceRoot(instanceRoot);
    }

}
