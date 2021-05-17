/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.node;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract for all BundleNode implementations.
 *
 * @author Tim Quinn
 */
@Contract
public interface BundleNode {

    /**
     * Registers the standard bundle node in the map.
     * <p>
     * The implementation class must add to the map an entry with the key
     * equal to the public ID of the DTD and the value the system ID.
     *
     * @param publicIDToSystemIDMapping map prepared by the caller
     * @param versionUpgrades The list of upgrades from older versions
     * @return top-level element name for the standard descriptor
     */
  String registerBundle(final Map<String,String> publicIDToSystemIDMapping);

    /**
     * Registers all appropriate runtime bundle nodes for this standard node
     * into the map.
     * <p>
     * The implementation class must add to the map one entry for each associated
     * runtime descriptor node, with the entry key equal to the public ID of the
     * runtime DTD and the value the system ID of the runtime DTD.  The
     * implementation must also return a map containing one entry for each
     * associated runtime node, with the entry key equal to the top-level
     * element name for the runtime descriptor and the entry value equal to the
     * class of the runtime node.
     *
     * @param publicIDToSystemIDMapping
     * @param versionUpgrades The list of upgrades from older versions
     * to the latest schema
     * @return map from top-level runtime descriptor element name to the corresponding runtime node class
     */
    Map<String,Class> registerRuntimeBundle(final Map<String,String> publicIDToSystemIDMapping, final Map<String, List<Class>> versionUpgrades);

    /**
     * Returns the element names related to the standard or related runtime nodes
     * for which the parser should allow empty values.
     */
    Collection<String> elementsAllowingEmptyValue();

    /**
     * Returns the element names related to the standard or related runtime nodes
     * for which the parser should preserve whitespace.
     */
    Collection<String> elementsPreservingWhiteSpace();
}
