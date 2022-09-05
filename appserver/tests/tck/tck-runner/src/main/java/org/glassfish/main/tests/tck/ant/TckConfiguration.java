/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author David Matejcek
 */
public class TckConfiguration {

    private final Properties cfg;

    public TckConfiguration(File cfgFile) {
        this.cfg = new Properties();
        try (FileInputStream is = new FileInputStream(cfgFile)) {
            this.cfg.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load TCK configuration from '" + cfgFile + "'!");
        }
    }


    public TckConfiguration(InputStream cfgStream) {
        this.cfg = new Properties();
        try {
            this.cfg.load(cfgStream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load TCK configuration!");
        }
    }


    public String getTckVersion() {
        return cfg.getProperty("tck.version");
    }


    public File getJakartaeeDir() {
        return new File(getTargetDir(), "jakartaeetck");
    }


    public Path getJakartaeetckCommand() {
        return getJakartaeeDir().toPath().resolve(Path.of("docker", "run_jakartaeetck.sh"));
    }


    public File getTargetDir() {
        String dir = cfg.getProperty("target.directory");
        if (dir == null) {
            throw new IllegalStateException("The property target.directory is not set!");
        }
        return new File(dir);
    }


    public File getPomFile() {
        String property = cfg.getProperty("pomFile");
        if (property == null) {
            throw new IllegalStateException("The property 'pomFile' is not set!");
        }
        return new File(property);
    }


    public File getJdkDirectory() {
        return new File(cfg.getProperty("jdk.directory"));
    }


    public File getAntDirectory() {
        return new File(cfg.getProperty("ant.directory"));
    }


    public String getGlassFishVersion() {
        return cfg.getProperty("glassfish.version");
    }
}
