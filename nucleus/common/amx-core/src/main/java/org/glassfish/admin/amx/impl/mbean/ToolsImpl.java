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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.HashSet;

import javax.management.MBeanInfo;
import javax.management.ObjectName;
import org.glassfish.admin.amx.base.Pathnames;
import org.glassfish.admin.amx.base.Tools;
import org.glassfish.admin.amx.core.AMXValidator;
import org.glassfish.admin.amx.util.CollectionUtil;
import org.glassfish.admin.amx.util.SetUtil;
import org.glassfish.admin.amx.util.StringUtil;
import org.glassfish.admin.amx.util.jmx.MBeanInterfaceGenerator;
import org.glassfish.admin.amx.core.Util;

public class ToolsImpl extends AMXImplBase // implements Tools
{

    public ToolsImpl(final ObjectName parent) {
        super(parent, Tools.class);
    }

    private static ObjectName newObjectName(final String s) {
        try {
            return new ObjectName(s);
        } catch (final Exception e) {
        }
        return null;
    }
    static private final String WILD_SUFFIX = ",*";
    static private final String WILD_ALL = "*";

    public String getInfo() {
        return info("*");
    }

    public String infoPP(final String parentPath, final boolean recursive) {
        final Pathnames paths = getDomainRootProxy().getPathnames();

        final ObjectName[] objectNames = paths.listObjectNames(parentPath, recursive);
        final Set<ObjectName> s = SetUtil.newSet(objectNames);
        return info(s);
    }

    public String infoType(final String type) {
        return info("*:type=" + type + WILD_SUFFIX);
    }

    public String infoPath(final String path) {
        final ObjectName objectName = getDomainRootProxy().getPathnames().resolvePath(path);

        Collection<ObjectName> c = objectName == null ? new ArrayList<ObjectName>() : Collections.singleton(objectName);
        return info(c);
    }

    public String java(final ObjectName objectName) {
        final MBeanInfo mbeanInfo = getProxyFactory().getMBeanInfo(objectName);
        if (mbeanInfo == null) {
            return null;
        }

        final MBeanInterfaceGenerator gen = new MBeanInterfaceGenerator();
        final String out = gen.generate(mbeanInfo, true);

        return out;
    }

    public String info(final Collection<ObjectName> objectNames) {
        final Set<String> alreadyDone = new HashSet<String>();

        final StringBuffer buf = new StringBuffer();

        if (objectNames.size() != 0) {
            final String NL = StringUtil.NEWLINE();
            for (final ObjectName objectName : objectNames) {
                final MBeanInfo mbeanInfo = getProxyFactory().getMBeanInfo(objectName);
                if (mbeanInfo == null) {
                    continue;
                }

                // Don't generate info if we've seen that type/class combination already
                final String type = Util.getTypeProp(objectName);
                final String classname = mbeanInfo.getClassName();
                if (alreadyDone.contains(type) && alreadyDone.contains(classname)) {
                    continue;
                }
                alreadyDone.add(type);
                alreadyDone.add(classname);

                buf.append("MBeanInfo for " + objectName + NL);
                //buf.append(JMXUtil.toString(mbeanInfo) + NL + NL );

                buf.append(java(objectName));
                buf.append(NL + NL + NL + NL);
            }
        }

        buf.append("Matched " + objectNames.size() + " mbean(s).");

        return buf.toString();
    }

    public String info(final String searchStringIn) {
        ObjectName pattern = newObjectName(searchStringIn);
        if (pattern == null && (searchStringIn.length() == 0 || searchStringIn.equals(WILD_ALL))) {
            pattern = newObjectName("*:*");
        }

        if (pattern == null) {
            String temp = searchStringIn;

            final boolean hasProps = temp.indexOf("=") >= 0;
            final boolean hasDomain = temp.indexOf(":") >= 0;
            final boolean isPattern = temp.endsWith(WILD_SUFFIX);

            if (!(hasProps || hasDomain || isPattern)) {
                // try it as a type
                pattern = newObjectName("*:type=" + temp + WILD_SUFFIX);

                // if no luck try it as a j2eeType
                if (pattern == null) {
                    pattern = newObjectName("*:j2eeType=" + temp + WILD_SUFFIX);
                }

                // if no luck try it as a name
                if (pattern == null) {
                    pattern = newObjectName("*:name=" + temp + WILD_SUFFIX);
                }
            }

            if (pattern == null) {
                return "No MBeans found for: " + searchStringIn;
            }
        }

        final Set<ObjectName> objectNames = getMBeanServer().queryNames(pattern, null);

        return info(objectNames);
    }

    public String validate(final ObjectName[] targets) {
        final Set<ObjectName> all = new HashSet<ObjectName>();

        for (final ObjectName objectName : targets) {
            if (objectName.isPattern()) {
                final Set<ObjectName> found = getMBeanServer().queryNames(objectName, null);
                all.addAll(found);
            } else {
                all.add(objectName);
            }
        }

        final ObjectName[] allArray = CollectionUtil.toArray(all, ObjectName.class);

        final AMXValidator validator = new AMXValidator(getMBeanServer(), "high", false, true);
        final AMXValidator.ValidationResult result = validator.validate(allArray);

        return result.toString();
    }

    public String validate(final ObjectName objectName) {
        return validate(new ObjectName[]{
                    objectName
                });
    }

    public String validate() {
        final List<ObjectName> all = Util.toObjectNameList(getDomainRootProxy().getQueryMgr().queryAll());

        return validate(CollectionUtil.toArray(all, ObjectName.class));
    }

    public String getHierarchy() {
        try {
            final ParentChildren pc = ParentChildren.hierarchy(getDomainRootProxy());
            final List<String> lines = pc.toLines(false);

            return StringUtil.toLines(lines);
        } catch (final Exception e) {
            return "";
        }
    }
}




































