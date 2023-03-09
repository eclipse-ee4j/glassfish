/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.glassfish.deployment.common.Descriptor;


/**
 * @author David Matejcek
 */
public class JspConfigDefinitionDescriptor extends Descriptor implements JspConfigDescriptor {

    private final Set<TaglibDescriptor> taglibs = new OrderedSet<>();
    private final List<JspPropertyGroupDescriptor> jspGroups = new OrderedSet<>();

    @Override
    public Collection<TaglibDescriptor> getTaglibs() {
        return taglibs;
    }


    @Override
    public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
        return jspGroups;
    }


    public void add(JspConfigDescriptor jspConfigDesc) {
        if (jspConfigDesc.getTaglibs() != null) {
            getTaglibs().addAll(jspConfigDesc.getTaglibs());
        }
        if (jspConfigDesc.getJspPropertyGroups() != null) {
            getJspPropertyGroups().addAll(jspConfigDesc.getJspPropertyGroups());
        }
    }


    /**
     * Add a tag lib element to the set.
     */
    public void addTagLib(TaglibDescriptor desc) {
        taglibs.add(desc);
    }


    /**
     * Remove a tag lib element from the set.
     */
    public void removeTagLib(TaglibDescriptor desc) {
        taglibs.remove(desc);
    }


    /**
     * Add a jsp group element to the set.
     */
    public void addJspGroup(JspPropertyGroupDescriptor desc) {
        jspGroups.add(desc);
    }


    /**
     * Remove a jsp group element from the set.
     */
    public void removeJspGroup(JspPropertyGroupDescriptor desc) {
        jspGroups.remove(desc);
    }


    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\nTagLibs: ").append(taglibs).append("\njsp groups: ").append(jspGroups);
    }
}
