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

import com.sun.enterprise.deployment.web.MultipartConfig;

import org.glassfish.deployment.common.Descriptor;

/**
 * This represents the multipart-config resided in web.xml.
 *
 * @author Shing Wai Chan
 */

public class MultipartConfigDescriptor extends Descriptor implements MultipartConfig {
    private String location = null;
    private Long maxFileSize = null;
    private Long maxRequestSize = null;
    private Integer fileSizeThreshold = null;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public Long getMaxRequestSize() {
        return maxRequestSize;
    }

    public void setMaxRequestSize(Long maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public Integer getFileSizeThreshold() {
        return fileSizeThreshold;
    }

    public void setFileSizeThreshold(Integer fileSizeThreshold) {
        this.fileSizeThreshold = fileSizeThreshold;
    }

    public void print(StringBuffer toStringBuffer) {
        if (location != null) {
            toStringBuffer.append("\n multipart location ").append(location);
        }
        toStringBuffer.append("\n multipart maxFileSize ").append(maxFileSize);
        toStringBuffer.append("\n multipart maxRequestSize ").append(maxRequestSize);
        toStringBuffer.append("\n multipart fileSizeThreshold ").append(fileSizeThreshold);
    }
}
