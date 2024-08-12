/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.util.StringUtils;

import jakarta.xml.bind.DatatypeConverter;

import java.security.MessageDigest;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandModel;

/**
 * Stores ETeg with command model.
 *
 * @author mmares
 */
public class CachedCommandModel extends CommandModelData {

    private String eTag;
    private String usage;
    private boolean addedUploadOption = false;

    public CachedCommandModel(String name) {
        super(name);
    }

    public CachedCommandModel(String name, String eTag) {
        super(name);
        this.eTag = eTag;
    }

    public String getETag() {
        if (eTag == null) {
            eTag = computeETag(this);
        }
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public boolean isAddedUploadOption() {
        return addedUploadOption;
    }

    public void setAddedUploadOption(boolean addedUploadOption) {
        this.addedUploadOption = addedUploadOption;
    }

    public static String computeETag(CommandModel cm) {
        if (cm instanceof CachedCommandModel) {
            String result = ((CachedCommandModel) cm).eTag;
            if (result != null && !result.isEmpty()) {
                return ((CachedCommandModel) cm).eTag;
            }
        }
        StringBuilder tag = new StringBuilder();
        tag.append(cm.getCommandName());
        if (cm.isManagedJob()) {
            tag.append('m');
        }
        if (cm.unknownOptionsAreOperands()) {
            tag.append('o');
        }
        if (cm.getParameters() != null) {
            //sort
            SortedSet<ParamModel> tree = new TreeSet<ParamModel>(new Comparator<ParamModel>() {
                @Override
                public int compare(ParamModel o1, ParamModel o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (ParamModel paramModel : cm.getParameters()) {
                tree.add(paramModel);
            }
            //print
            for (ParamModel pm : tree) {
                if ("upload".equals(pm.getName())) {
                    continue;
                }
                tag.append(pm.getName());
                tag.append(pm.getClass().getCanonicalName());

                Param param = pm.getParam();
                if (param.multiple()) {
                    tag.append('M');
                }
                if (param.optional()) {
                    tag.append('P');
                }
                if (param.primary()) {
                    tag.append('1');
                }
                if (param.obsolete()) {
                    tag.append('O');
                }
                if (param.shortName() != null && !param.shortName().isEmpty()) {
                    tag.append(param.shortName());
                }
                if (param.alias() != null && !param.alias().isEmpty()) {
                    tag.append(param.alias());
                }
                if (StringUtils.ok(param.defaultValue())) {
                    tag.append(param.defaultValue());
                    tag.append("A"); //TODO: removeit
                }
            }
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(tag.toString().getBytes("UTF-8"));
            return DatatypeConverter.printBase64Binary(md.digest());
        } catch (Exception ex) {
            return "v2" + tag.toString();
        }
    }

}
