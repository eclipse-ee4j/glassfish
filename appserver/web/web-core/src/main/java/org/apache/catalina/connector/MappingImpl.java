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

package org.apache.catalina.connector;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.MappingMatch;

import java.io.Serializable;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.catalina.LogFacade;
import org.glassfish.grizzly.http.server.util.MappingData;

public class MappingImpl implements HttpServletMapping, Serializable {

    private static final long serialVersionUID = -5134622427867249518L;

    private String matchValue;

    private String pattern;

    private String servletName;

    private MappingMatch mappingMatch;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    public MappingImpl(MappingData mappingData) {
        if (null == mappingData) {
            throw new NullPointerException(rb.getString(LogFacade.MAPPING_ERROR_EXCEPTION));
        }

        // Trim leading "/"
        matchValue = (null != mappingData.matchedPath) &&
                     (mappingData.matchedPath.length() >= 2) ? mappingData.matchedPath.substring(1) : "";
        pattern = null != mappingData.descriptorPath ? mappingData.descriptorPath : "";
        servletName = null != mappingData.servletName ? mappingData.servletName : "";

        switch (mappingData.mappingType) {
            case MappingData.CONTEXT_ROOT:
                mappingMatch = MappingMatch.CONTEXT_ROOT;
                break;
            case MappingData.DEFAULT:
                mappingMatch = MappingMatch.DEFAULT;
                matchValue = "";
                break;
            case MappingData.EXACT:
                mappingMatch = MappingMatch.EXACT;
                break;
            case MappingData.EXTENSION:
                mappingMatch = MappingMatch.EXTENSION;
                // Ensure pattern is valid
                if (null != pattern && '*' == pattern.charAt(0)) {
                    // Mutate matchValue to mean "what * was matched with".
                    int i = matchValue.indexOf(pattern.substring(1));
                    if (-1 != i) {
                        matchValue = matchValue.substring(0, i);
                    }
                }
                break;
            case MappingData.PATH:
                mappingMatch = MappingMatch.PATH;
                // Ensure pattern is valid
                if (null != pattern) {
                    int patternLen = pattern.length();
                    if (0 < patternLen && '*' == pattern.charAt(patternLen-1)) {
                        int indexOfPatternStart = patternLen - 2;
                        int matchValueLen = matchValue.length();
                        if (0 <= indexOfPatternStart && indexOfPatternStart < matchValueLen) {
                            // Remove the pattern from the end of matchValue
                            matchValue = matchValue.substring(indexOfPatternStart);
                        }
                    }
                }
                break;
        }

    }

    @Override
    public String getMatchValue() {
        return matchValue;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public String getServletName() {
        return servletName;
    }



    @Override
    public MappingMatch getMappingMatch() {
        return mappingMatch;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.matchValue);
        hash = 29 * hash + Objects.hashCode(this.pattern);
        hash = 29 * hash + Objects.hashCode(this.servletName);
        hash = 29 * hash + Objects.hashCode(this.mappingMatch);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MappingImpl other = (MappingImpl) obj;
        if (!Objects.equals(this.matchValue, other.matchValue)) {
            return false;
        }
        if (!Objects.equals(this.pattern, other.pattern)) {
            return false;
        }
        if (!Objects.equals(this.servletName, other.servletName)) {
            return false;
        }
        if (this.mappingMatch != other.mappingMatch) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MappingImpl{" + "matchValue=" + matchValue
                + ", pattern=" + pattern
                + ", servletName=" + servletName
                + ", mappingMatch=" + mappingMatch + '}';
    }



}
