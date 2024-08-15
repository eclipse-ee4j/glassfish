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

package org.glassfish.admin.amx.impl.mbean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.util.StringUtil;

final class ParentChildren implements Comparable<ParentChildren> {

    final AMXProxy mParent;
    final List<ParentChildren> mChildren;

    public ParentChildren(final AMXProxy parent, final List<ParentChildren> children) {
        mParent = parent;
        mChildren = children;
    }

    public void sortChildren() {
        Collections.sort(mChildren);
    }

    @Override
    public boolean equals(final Object rhs) {
        return rhs instanceof ParentChildren ? compareTo((ParentChildren)rhs) == 0 : false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.mParent != null ? this.mParent.hashCode() : 0);
        hash = 83 * hash + (this.mChildren != null ? this.mChildren.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(final ParentChildren rhs) {
        int cmp = mParent.type().compareTo(rhs.mParent.type());
        if (cmp == 0) {
            cmp = mParent.nameProp().compareTo(rhs.mParent.nameProp());
        }

        if (cmp == 0) {
            cmp = mChildren.size() - rhs.mChildren.size();
        }

        return cmp;
    }

    public AMXProxy parent() {
        return mParent;
    }

    public List<ParentChildren> children() {
        return mChildren;
    }

    public List<String> toLines(final boolean details) {
        sortChildren();
        final List<String> lines = new ArrayList<String>();

        lines.add(descriptionFor(mParent));

        for (final ParentChildren child : mChildren) {
            final List<String> moreLines = indentAll(child.toLines(details));
            lines.addAll(moreLines);
        }
        return lines;
    }

    public List<AMXProxy> asList() {
        final List<AMXProxy> items = new ArrayList<AMXProxy>();

        items.add(mParent);
        for (final ParentChildren child : mChildren) {
            items.addAll(child.asList());
        }
        return items;
    }

    public static ParentChildren hierarchy(final AMXProxy top) {
        // make a list of all children, grouping by type
        final List<AMXProxy> children = new ArrayList<AMXProxy>();
        final Map<String, Map<String, AMXProxy>> childrenMaps = top.childrenMaps();
        for (final Map<String, AMXProxy> childrenOfType : childrenMaps.values()) {
            for (final AMXProxy amx : childrenOfType.values()) {
                children.add(amx);
            }
        }

        final List<ParentChildren> pcList = new ArrayList<ParentChildren>();
        for (final AMXProxy child : children) {
            final ParentChildren pc = hierarchy(child);
            pcList.add(pc);
        }

        final ParentChildren result = new ParentChildren(top, pcList);
        result.sortChildren();
        return result;
    }

    public static String descriptionFor(final AMXProxy proxy) {
        String desc = proxy.type();
        final String name = proxy.nameProp();
        if (name != null) {
            desc = desc + "=" + name;
        }

        return desc;
    }

    private static List<String> indentAll(final List<String> lines) {
        final List<String> linesIndented = new ArrayList<String>();
        final String INDENT = "   ";
        for (final String line : lines) {
            linesIndented.add(INDENT + line);
        }
        return linesIndented;
    }

    public static String getHierarchyString(final AMXProxy top) {
        final ParentChildren pc = hierarchy(top);

        return StringUtil.toLines(pc.toLines(true));
    }
}




































