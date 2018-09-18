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

package com.sun.enterprise.tools.verifier;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import com.sun.enterprise.tools.verifier.web.TagDescriptor;
import com.sun.enterprise.tools.verifier.web.FunctionDescriptor;

/**
 * class which defines methods required for implementing tests based
 * out of jsp tag library files.
 *
 * @author Sudipto Ghosh
 */
public class TagLibDescriptor {
    public static final String TAG = "tag"; // NOI18N
    public static final String LISTENER_CLASS = "listener-class"; // NOI18N
    public static final String FUNCTION = "function"; // NOI18N

    private Document doc = null;
    private String version = null;
    private String uri = null;

    public TagLibDescriptor(Document doc, String version, String uri) {
        this.doc = doc;
        this.version = version;
        this.uri = uri;
    }
    /**
     * @return spec version of tld file
     */
    public String getSpecVersion() {
        return this.version;
    }

    /**
     * @return location of the tld file
     */
    public String getUri() {
        return this.uri;
    }

    public String getPublicID() {
        DocumentType docType = doc.getDoctype();
        return ((docType == null) ? null : docType.getPublicId());
    }

    /**
     * @return system-id of the tld file.
     */
    public String getSystemID() {
        DocumentType docType = doc.getDoctype();
        return ((docType == null) ? null : docType.getSystemId());
    }

    public String[] getListenerClasses(){
        NodeList nl = doc.getElementsByTagName(LISTENER_CLASS);
        String[] classes = null;
        if (nl != null) {
            int size = nl.getLength();
            classes = new String[size];
            for (int i = 0; i < size; i++) {
                classes[i] = nl.item(i).getFirstChild().getNodeValue();
            }
        }
        return classes;
    }

    /**
     * for each tag in the tag lib descriptor create a TagDescriptor and return
     * the array of TagDescriptors present in the tag lib.
     * @return
     */
    public TagDescriptor[] getTagDescriptors() {
        NodeList nl = doc.getElementsByTagName(TAG);
        TagDescriptor[] tagdescriptor = null;
        if (nl != null) {
            int size = nl.getLength();
            tagdescriptor = new TagDescriptor[size];
            for (int i = 0; i < size; i++) {
                tagdescriptor[i] = new TagDescriptor(nl.item(i));
            }
        }
        return tagdescriptor;
    }

    /**
     * for each functions in tag lib descriptor creates a function descritor and
     * return the array of FunctionDescriptors 
     * @return array of function descriptor.
     */
    public FunctionDescriptor[] getFunctionDescriptors() {
        NodeList nl = doc.getElementsByTagName(FUNCTION);
        List<FunctionDescriptor> list = new ArrayList<FunctionDescriptor>();
        if (nl != null) {
            int size = nl.getLength();
            for (int i = 0; i < size; i++) {
                list.add(new FunctionDescriptor(nl.item(i)));
            }
        }
        return list.toArray(new FunctionDescriptor[0]);
    }
}
