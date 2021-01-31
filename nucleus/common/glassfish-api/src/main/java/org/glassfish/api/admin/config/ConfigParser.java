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

package org.glassfish.api.admin.config;

import java.io.IOException;
import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;

/**
 * @author Jerome Dochez
 * @author Vivek Pandey
 */
@Contract
public interface ConfigParser {

    /**
     * Parse a Container's configuration defined by it's XML template pointed by configuration URL. <br/>
     * <br/>
     * Example:<br/>
     *
     * Inside your {@link org.glassfish.api.container.Sniffer}:
     *
     * <pre>
     *
     * {@link @Inject}
     * ConfigParser parser;
     *
     * {@link @Inject}
     * JrubyContainer container;
     *
     * public Module[] setup(java.lang.String s, java.util.logging.Logger logger) throws java.io.IOException{
     *     if(container == null){
     *         URL xml = getClass().getClassLoader().getResource("jruby-container-config.xml");
     *         config = parser.parseContainerConfig(habitat, xml, JrubyContainer.class);
     *         //Now do stuff with config
     *     }
     * }
     * </pre>
     *
     * @return Confgured container
     * @throws IOException
     */
    <T extends Container> T parseContainerConfig(ServiceLocator habitat, URL configuration, Class<T> containerType) throws IOException;

}
