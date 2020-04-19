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

package org.glassfish.web.deployment.descriptor;

import org.glassfish.deployment.common.Descriptor;

import java.util.*;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

/**
 * This is a descriptor for the taglib config used in a web application.
 */
public class JspConfigDescriptorImpl extends Descriptor
        implements JspConfigDescriptor {

    private Set<TaglibDescriptor> taglibs = null;
    private Vector<JspPropertyGroupDescriptor> jspGroups = null;

    public void add(JspConfigDescriptorImpl jspConfigDesc) {
        if (jspConfigDesc.taglibs != null) {
            getTaglibs().addAll(jspConfigDesc.taglibs);
        }

        if (jspConfigDesc.jspGroups != null) {
            getJspPropertyGroups().addAll(jspConfigDesc.jspGroups);
        }
    }

    /**
     * return the set of tag lib elements
     */
    public Set<TaglibDescriptor> getTaglibs() {
        if (taglibs == null) {
            taglibs = new HashSet<TaglibDescriptor>();
        }
        return taglibs;
    }

    /**
     * add a tag lib element to the set.
     */
    public void addTagLib(TagLibConfigurationDescriptor desc) {
        getTaglibs().add(desc);
    }

    /**
     * remove a tag lib element from the set.
     */
    public void removeTagLib(TagLibConfigurationDescriptor desc) {
        getTaglibs().remove(desc);
    }

    /**
     * return Collection of jsp-group elements
     */
    public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
        if (jspGroups == null) {
            jspGroups = new Vector<JspPropertyGroupDescriptor>();
        }
        return jspGroups;
    }

    /**
     * add a jsp group element to the set.
     */
    public void addJspGroup(JspGroupDescriptor desc) {
        getJspPropertyGroups().add(desc);
    }

    /**
     * remove a jsp group element from the set.
     */
    public void removeJspGroup(JspGroupDescriptor desc) {
        getJspPropertyGroups().remove(desc);
    }

    /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nTagLibs : ").append(taglibs).append(
            " jsp groups:").append(jspGroups);
    }
}
