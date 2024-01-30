/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.util.cluster;

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.SshConnector;

import java.beans.PropertyVetoException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author wnevins
 */
public class PathsTest {
    private static final String SPECIFIED_INSTALL_DIR = "D:/glassfish8";
    private static final String NODE_NAME = "thenode";
    private static final String SPECIFIED_NODES_DIR = SPECIFIED_INSTALL_DIR + "/glassfish/mynodes";
    private static final String SPECIFIED_NODE_DIR = SPECIFIED_NODES_DIR + "/" + NODE_NAME;
    private static final String INSTANCE_NAME = "instance1";



    /**
     * Test of getNodeDir method, of class Paths.
     */
    @Test
    public void testGetNodeDir() {
        String nodedir1 = Paths.getNodeDir(new NodeWithNodeDir());
        String nodedir2 = Paths.getNodeDir(new NodeWithoutNodeDir());
        System.out.println("User-specified Node Dir: " + nodedir1);
        System.out.println("Default Node Dir: " + nodedir2);
        assertEquals(SPECIFIED_INSTALL_DIR + "/glassfish/nodes/" + NODE_NAME, nodedir2);
        assertEquals(SPECIFIED_NODE_DIR, nodedir1);
    }

    /**
     * Test of getDasPropsPath method, of class Paths.
     */
    @Test
    public void testGetDasPropsPath() {
        String d1 = Paths.getDasPropsPath(new NodeWithNodeDir());
        String d2 = Paths.getDasPropsPath(new NodeWithoutNodeDir());
        String d1expect = SPECIFIED_NODE_DIR + "/agent/config/das.properties";
        String d2expect = SPECIFIED_INSTALL_DIR + "/glassfish/nodes/" + NODE_NAME + "/agent/config/das.properties";

        System.out.println("User-specified das props: " + d1);
        System.out.println("Default das props: " + d2);
        assertEquals(d1expect, d1);
        assertEquals(d2expect, d2);
    }

    @Test
    public void testGetInstanceDirPath() {
        String d1 = Paths.getInstanceDirPath(new NodeWithNodeDir(), INSTANCE_NAME);
        String d2 = Paths.getInstanceDirPath(new NodeWithoutNodeDir(), INSTANCE_NAME);
        System.out.println("User-specified Instance Dir: " + d1);
        System.out.println("Default Node Dir Instance Dir: " + d2);
        assertEquals(SPECIFIED_INSTALL_DIR + "/glassfish/nodes/" + NODE_NAME + "/" + INSTANCE_NAME, d2);
        assertEquals(SPECIFIED_NODE_DIR + "/" + INSTANCE_NAME, d1);
    }


    static abstract class NodeAdapter implements Node {
        // bless you for spitting this out NetBeans!!!
        @Override
        public void setName(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getName() {
            return NODE_NAME;
        }

        @Override
        public String getNodeDir() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setNodeDir(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNodeHost() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setNodeHost(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getInstallDir() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setInstallDir(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getType() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setType(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getWindowsDomain() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWindowsDomain(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFreeze() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setFreeze(String value) throws PropertyVetoException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public SshConnector getSshConnector() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSshConnector(SshConnector connector) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNodeDirUnixStyle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNodeDirAbsolute() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean nodeInUse() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDefaultLocalNode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isLocal() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean instanceCreationAllowed() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy getParent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ConfigBeanProxy> T getParent(Class<T> type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ConfigBeanProxy deepCopy(ConfigBeanProxy cbp) throws TransactionFailure {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getReference() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCluster() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isServer() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isInstance() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isDas() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ResourceRef> getResourceRef() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<ApplicationRef> getApplicationRef() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<String> getResourceRefNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public List<String> getApplicationRefNames() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    static class NodeWithNodeDir extends NodeAdapter {
        @Override
        public String getInstallDirUnixStyle() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getNodeDirAbsoluteUnixStyle() {
            return PathsTest.SPECIFIED_NODES_DIR;
        }
    }

    static class NodeWithoutNodeDir extends NodeAdapter {
        @Override
        public String getInstallDirUnixStyle() {
             return SPECIFIED_INSTALL_DIR;
        }

        @Override
        public String getNodeDirAbsoluteUnixStyle() {
            return null;
        }
    }
}
