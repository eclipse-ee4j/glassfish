/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.kernel.deployment.container;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.DeploymentContext;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;
import java.util.Map;
import java.io.IOException;

import com.sun.enterprise.module.HK2Module;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:20:37 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class FakeSniffer implements Sniffer {

    public boolean handles(ReadableArchive source) {
        // I handle everything
        return true;
    }

    public boolean handles(DeploymentContext context) {
        // I handle everything
        return true;
    }

    public String[] getURLPatterns() {
        return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Class<? extends Annotation>[] getAnnotationTypes() {
        return null;
    }

    public String[] getAnnotationNames(DeploymentContext context) {
        return null;
    }

    public String getModuleType() {
        return "fake";
    }

    public HK2Module[] setup(String containerHome, Logger logger) throws IOException {
        return null;
    }

    public void tearDown() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String[] getContainersNames() {
        return new String[] { "FakeContainer" };
    }

    public boolean isUserVisible() {
        return false;
    }

   public boolean isJavaEE() {
        return false;
    }

    public Map<String, String> getDeploymentConfigurations(ReadableArchive source) throws IOException {
        return null;
    }

    public String[] getIncompatibleSnifferTypes() {
        return new String[0];
    }

    public boolean supportsArchiveType(ArchiveType archiveType) {
        return true;
    }

}
