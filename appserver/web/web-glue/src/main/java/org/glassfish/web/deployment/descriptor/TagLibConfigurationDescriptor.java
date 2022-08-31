/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.servlet.descriptor.TaglibDescriptor;

import java.io.Serializable;

import org.glassfish.deployment.common.Descriptor;

/**
 * This descriptor represent the information about a tag library used in a
 * web application.
 *
 * @author Danny Coward
 */
public class TagLibConfigurationDescriptor extends Descriptor implements Serializable, TaglibDescriptor {

    private static final long serialVersionUID = 1L;
    private String uri;
    private String location;

    /**
     * Default constructor.
     */
    public TagLibConfigurationDescriptor() {
    }


    /**
     * Construct a tag library configuration with the given location and URI.
     *
     * @param uri the URI.
     * @param location the location.
     */
    public TagLibConfigurationDescriptor(String uri, String location) {
        this.uri = uri;
        this.location = location;
    }


    /**
     * Sets the URI of this tag lib.
     *
     * @param uri the URI of the tag library.
     */
    public void setTagLibURI(String uri) {
        this.uri = uri;
    }


    /**
     * Return the URI of this tag lib.
     *
     * @return the URI of the tag library.
     */
    public String getTagLibURI() {
        if (this.uri == null) {
            this.uri = "";
        }
        return this.uri;
    }


    @Override
    public String getTaglibURI() {
        return getTagLibURI();
    }


    /**
     * Describe the location of the tag library file.
     *
     * @param location the location of the tag library.
     */
    public void setTagLibLocation(String location) {
        this.location = location;
    }


    /**
     * Describes the location of the tag library file.
     *
     * @return the location of the tag library.
     */
    public String getTagLibLocation() {
        if (this.location == null) {
            this.location = "";
        }
        return this.location;
    }


    @Override
    public String getTaglibLocation() {
        return getTagLibLocation();
    }


    /**
     * Return a formatted String representing my state.
     */
    @Override
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("TGLIB: ").append(uri).append(", ").append(location);
    }
}
