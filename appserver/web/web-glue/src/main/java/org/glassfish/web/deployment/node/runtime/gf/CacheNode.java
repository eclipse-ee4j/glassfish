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

package org.glassfish.web.deployment.node.runtime.gf;

import com.sun.enterprise.deployment.node.XMLElement;
import com.sun.enterprise.deployment.node.runtime.RuntimeDescriptorNode;
import com.sun.enterprise.deployment.runtime.RuntimeDescriptor;
import com.sun.enterprise.deployment.xml.RuntimeTagNames;

import org.glassfish.web.deployment.runtime.Cache;
import org.glassfish.web.deployment.runtime.CacheHelper;
import org.glassfish.web.deployment.runtime.CacheMapping;
import org.glassfish.web.deployment.runtime.DefaultHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
* node for cache tag
*
* @author Jerome Dochez
*/
public class CacheNode extends RuntimeDescriptorNode<Cache> {

    public CacheNode() {
        registerElementHandler(new XMLElement(RuntimeTagNames.CACHE_HELPER),
                CacheHelperNode.class, "addNewCacheHelper");
        registerElementHandler(new XMLElement(RuntimeTagNames.DEFAULT_HELPER),
                DefaultHelperNode.class, "setDefaultHelper");
        registerElementHandler(new XMLElement(RuntimeTagNames.PROPERTY),
                WebPropertyNode.class, "addWebProperty");
        registerElementHandler(new XMLElement(RuntimeTagNames.CACHE_MAPPING),
                CacheMappingNode.class, "addNewCacheMapping");
    }

    protected Cache descriptor = null;

    /**
     * @return the descriptor instance to associate with this XMLNode
     */
    @Override
    public Cache  getDescriptor() {
        if (descriptor==null) {
            descriptor = new Cache();
        }
        return descriptor;
    }

    /**
     * parsed an attribute of an element
     *
     * @param elementName the element name
     * @param attributeName the attribute name
     * @param value the attribute value
     * @return true if the attribute was processed
     */
    @Override
    protected boolean setAttributeValue(XMLElement elementName, XMLElement attributeName, String value) {
        RuntimeDescriptor descriptor = getDescriptor();
        if (attributeName.getQName().equals(RuntimeTagNames.MAX_ENTRIES)) {
            descriptor.setAttributeValue(Cache.MAX_ENTRIES, value);
            return true;
        } else if (attributeName.getQName().equals(RuntimeTagNames.TIMEOUT_IN_SECONDS)) {
            descriptor.setAttributeValue(Cache.TIMEOUT_IN_SECONDS, value);
            return true;
        } else if (attributeName.getQName().equals(RuntimeTagNames.ENABLED)) {
            descriptor.setAttributeValue(Cache.ENABLED, value);
            return true;
        } else {
            return false;
        }
    }


    /**
     * write the descriptor class to a DOM tree and return it
     *
     * @param parent node for the DOM tree
     * @param nodeName node name
     * @param descriptor the descriptor to write
     * @return the DOM tree top node
     */
    @Override
    public Node writeDescriptor(Node parent, String nodeName, Cache descriptor) {

        Element cache = (Element) super.writeDescriptor(parent, nodeName, descriptor);

        // cache-helpers*
        CacheHelper[] cacheHelpers = descriptor.getCacheHelper();
        if (cacheHelpers!=null && cacheHelpers.length>0) {
            CacheHelperNode chn = new CacheHelperNode();
            for (int i=0;i<cacheHelpers.length;i++) {
                chn.writeDescriptor(cache, RuntimeTagNames.CACHE_HELPER, cacheHelpers    [i]);
            }
        }

        WebPropertyNode wpn = new WebPropertyNode();

        // default-helper?
        DefaultHelper dh = descriptor.getDefaultHelper();
        if (dh!=null && dh.getWebProperty()!=null) {
            Node dhn = appendChild(cache, RuntimeTagNames.DEFAULT_HELPER);
            wpn.writeDescriptor(dhn, RuntimeTagNames.PROPERTY, dh.getWebProperty());
        }

        // property*
        wpn.writeDescriptor(cache, RuntimeTagNames.PROPERTY, descriptor.getWebProperty());

        // cache-mapping
        CacheMapping[] mappings = descriptor.getCacheMapping();
        if (mappings!=null && mappings.length>0) {
            CacheMappingNode cmn = new CacheMappingNode();
            for (int i=0;i<mappings.length;i++) {
                cmn.writeDescriptor(cache, RuntimeTagNames.CACHE_MAPPING, mappings[i]);
            }
        }

        // max-entries, timeout-in-seconds, enabled
        setAttribute(cache, RuntimeTagNames.MAX_ENTRIES, (String) descriptor.getAttributeValue(Cache.MAX_ENTRIES));
        setAttribute(cache, RuntimeTagNames.TIMEOUT_IN_SECONDS, (String) descriptor.getAttributeValue(Cache.TIMEOUT_IN_SECONDS));
        setAttribute(cache, RuntimeTagNames.ENABLED, (String) descriptor.getAttributeValue(Cache.ENABLED));

        return cache;
    }
}
