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

import com.sun.enterprise.deployment.OrderedSet;
import org.glassfish.deployment.common.Descriptor;

import java.util.Set;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;

/**
 */
public class JspGroupDescriptor extends Descriptor
        implements JspPropertyGroupDescriptor {

    private String elIgnored;
    private String scriptingInvalid;
    private String isXml;
    private String deferredSyntaxAllowedAsLiteral;
    private String trimDirectiveWhitespaces;
    private Set<String> urlPatterns = null;
    private Set<String> includePreludes = null;
    private Set<String> includeCodas = null;
    private String pageEncoding = null;
    private String defaultContentType = null;
    private String buffer = null;
    private String errorOnUndeclaredNamespace;

    /**
     * Return the set of URL pattern aliases for this group.
     */
    public Set<String> getUrlPatterns() {
        if (this.urlPatterns == null) {
            this.urlPatterns = new OrderedSet<String>();
        }
        return this.urlPatterns;
    }

    /**
     * Adds an alias to this jsp group.
     */
    public void addUrlPattern(String urlPattern) {
        this.getUrlPatterns().add(urlPattern);

    }

    /**
     * Removes a URL pattern from this jsp group.
     */
    public void removeUrlPattern(String urlPattern) {
        this.getUrlPatterns().remove(urlPattern);

    }

    /**
     * Return an Iterable over the include prelude elements for this group.
     */
    public Set<String> getIncludePreludes() {
        if (this.includePreludes == null) {
            this.includePreludes = new OrderedSet<String>();
        }
        return this.includePreludes;
    }

    /**
     * Adds an element
     */
    public void addIncludePrelude(String prelude) {
        this.getIncludePreludes().add(prelude);

    }

    /**
     * Removes an element
     */
    public void removeIncludePrelude(String prelude) {
        this.getIncludePreludes().remove(prelude);

    }

    /**
     * Return an Iterable over include coda elements for this group.
     */
    public Set<String> getIncludeCodas() {
        if (this.includeCodas == null) {
            this.includeCodas = new OrderedSet<String>();
        }
        return this.includeCodas;
    }

    /**
     * Adds an element
     */
    public void addIncludeCoda(String coda) {
        this.getIncludeCodas().add(coda);

    }

    /**
     * Removes an element
     */
    public void removeIncludeCoda(String coda) {
        this.getIncludeCodas().remove(coda);

    }

    /**
     * elIgnored
     */
    public void setElIgnored(String value) {
        elIgnored = value;
    }

    public String getElIgnored() {
        return elIgnored;
    }

    /**
     * enable/disable scripting
     */
    public void setScriptingInvalid(String value) {
        scriptingInvalid = value;
    }

    public String getScriptingInvalid() {
        return scriptingInvalid;
    }

    /**
     * enable/disable xml
     */
    public void setIsXml(String value) {
        isXml = value;
    }

    public String getIsXml() {
        return isXml;
    }

    /**
     * enable/disable deferredSyntaxAllowedAsLiteral
     */
    public void setDeferredSyntaxAllowedAsLiteral(String value) {
        deferredSyntaxAllowedAsLiteral = value;
    }

    public String getDeferredSyntaxAllowedAsLiteral() {
        return deferredSyntaxAllowedAsLiteral;
    }

    /**
     * enable/disable trimDirectiveWhitespaces
     */
    public void setTrimDirectiveWhitespaces(String value) {
        trimDirectiveWhitespaces = value;
    }

    public String getTrimDirectiveWhitespaces() {
        return trimDirectiveWhitespaces;
    }

    /**
     * get display name.
     */
    public String getDisplayName() {
        // bug#4745178  other code requires the
        // display name to be localized.
        return super.getName();
    }

    /**
     * set display name.
     */
    public void setDisplayName(String name) {
        // bug#4745178  other code requires the
        // display name to be localized.
        super.setName(name);
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public void setPageEncoding(String encoding) {
        pageEncoding = encoding;
    }

    /**
     * get defaultContentType
     */
    public String getDefaultContentType() {
        return defaultContentType;
    }

    /**
     * set defaultContentType
     */
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    /**
     * get buffer
     */
    public String getBuffer() {
        return buffer;
    }

    /**
     * set buffer
     */
    public void setBuffer(String value) {
        buffer = value;
    }

    /**
     * set errorOnUndeclaredNamespace
     */
    public void setErrorOnUndeclaredNamespace(String value) {
        errorOnUndeclaredNamespace = value;
    }

    public String getErrorOnUndeclaredNamespace() {
        return errorOnUndeclaredNamespace;
    }

    /**
     * @return a string describing the values I hold
     */
    public void print(StringBuffer toStringBuffer) {
        toStringBuffer.append("\n JspGroupDescriptor");
        toStringBuffer.append( "\n");
        super.print(toStringBuffer);
        toStringBuffer.append( "\n DisplayName:").append(this.getDisplayName());
        toStringBuffer.append( "\n PageEncoding:").append(pageEncoding);
        toStringBuffer.append( "\n El-Ignored:").append(elIgnored);
        toStringBuffer.append( "\n Scripting Invalid:").append(scriptingInvalid);
        toStringBuffer.append( "\n urlPatterns: ").append(urlPatterns);
        toStringBuffer.append( "\n includePreludes: ").append(includePreludes);
        toStringBuffer.append( "\n includeCoda: ").append(includeCodas);
        toStringBuffer.append( "\n Is XML:").append(isXml);
        toStringBuffer.append( "\n DeferredSyntaxAllowedAsLiteral: ").append(deferredSyntaxAllowedAsLiteral);
        toStringBuffer.append( "\n TrimDirectiveWhitespaces:").append(trimDirectiveWhitespaces);
        toStringBuffer.append( "\n defaultContentType: ").append(defaultContentType);
        toStringBuffer.append( "\n buffer: ").append(buffer);
        toStringBuffer.append( "\n errorOnUndeclaredNamespace: ").append(errorOnUndeclaredNamespace);
    }
}
