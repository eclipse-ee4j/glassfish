/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout.template;

import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import java.io.IOException;

/**
 * <p>
 * This interface defines the operations that may be acted upon while a "template" document is traversed. The intent is
 * that this interface may be implemented by the different "processing contexts" which occur throught the template file.
 * This provides the opportunity for context sensitive syntax in an easy to provide way.
 * </p>
 *
 * <p>
 * While the standard <code>ProcessingContext</code> instances are likely to be sufficient, there may be cases where a
 * custom <code>ProcessingContext</code> may be needed to process the children of a {@link CustomParserCommand}. This
 * may be done by implementing this interface, or extending one of the existing implementations. Typically this object
 * is used by passing it to the {@link TemplateReader#process(ProcessingContext, LayoutElement, boolean)} method -- this
 * method will delegate actions back to the given <code>ProcessingContext</code>.
 * </p>
 */
public interface ProcessingContext {

    /**
     * <p>
     * This is called when a component tag is found (&lt;tagname ...).
     * </p>
     */
    void beginComponent(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This is called when an end component tag is found (&lt;/tagname ... or &lt;tagname ... /&gt;).
     * </p>
     */
    void endComponent(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This is called when a special tag is found (&lt;!tagname ...).
     * </p>
     */
    void beginSpecial(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This is called when a special end tag is found (&lt;/tagname ... or &lt;!tagname ... /&gt;).
     * </p>
     */
    void endSpecial(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This is called when static text is found (").
     * </p>
     */
    void staticText(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This is called when escaped static text is found ('). The difference between this and staticText is that HTML is
     * expected to be escaped so the browser does not parse it.
     * </p>
     */
    void escapedStaticText(ProcessingContextEnvironment env, String content) throws IOException;

    /**
     * <p>
     * This method is invoked when nothing else matches.
     * </p>
     */
    void handleDefault(ProcessingContextEnvironment env, String content) throws IOException;
}
