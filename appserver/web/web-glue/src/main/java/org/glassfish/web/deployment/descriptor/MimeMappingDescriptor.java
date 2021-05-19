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

import com.sun.enterprise.deployment.web.MimeMapping;

import java.io.Serializable;

/***
 * I represent a mapping between a mime type and a file extension for specifiying how
 * to handle mime types in a J2EE WAR.
 *
 * @author Danny Coward
 */
public class MimeMappingDescriptor implements MimeMapping, Serializable {

    private String extension;
    private String mimeType;

    /** copy constructor */
    public MimeMappingDescriptor(MimeMappingDescriptor other) {
        // super(other);
        extension = other.extension;
        mimeType = other.mimeType;
    }


    /** Construct the mapping for the given extension to the given mime type. */
    public MimeMappingDescriptor(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }


    /* Default constructor. */
    public MimeMappingDescriptor() {
    }


    /** Return the filename extension for this mapping. */
    @Override
    public String getExtension() {
        if (this.extension == null) {
            this.extension = "";
        }
        return this.extension;
    }


    /** Set the filename extension for this mapping. */
    @Override
    public void setExtension(String extension) {
        this.extension = extension;
    }


    /** Get the mime type for this mapping. */
    @Override
    public String getMimeType() {
        if (this.mimeType == null) {
            this.mimeType = "";
        }
        return this.mimeType;
    }


    /** Set the mime type for this mapping. */
    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    /** My pretty format. */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("MimeMapping: ").append(this.getExtension()).append("@").append(this.getMimeType());
    }

}
