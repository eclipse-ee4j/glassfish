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

/**
 * <p>
 * This class hold environmental information needed while a parsing. This information is specific to the nested level
 * that is currently being processed. This is unlike the {@link ProcessingContext} which is related to the "type" of
 * element that is being processed. Or said another way, the {@link ProcessingContext} specifies how / what sub-elements
 * are to be processed based on the context; this class provides the "where" information for that processing. Another
 * difference is this class is stateful, the {@link ProcessingContext} has stateless methods that parse specific
 * portions of the document.
 * </p>
 *
 * @see TemplateReader
 * @see ProcessingContext
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class ProcessingContextEnvironment {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public ProcessingContextEnvironment(TemplateReader reader, LayoutElement parent, boolean nested) {
        _reader = reader;
        _parent = parent;
        _nested = nested;
    }

    /**
     * @return The <code>TemplateReader</code> instance.
     */
    public TemplateReader getReader() {
        return _reader;
    }

    /**
     * @return <code>true</code> if nested in a LayoutComponent.
     */
    public boolean isNested() {
        return _nested;
    }

    /**
     * @return The parent {@link LayoutElement}.
     */
    public LayoutElement getParent() {
        return _parent;
    }

    /**
     * <p>
     * This method marks the current {@link ProcessingContext} as complete.
     * </p>
     */
    public void setFinished(boolean finished) {
        _finished = finished;
    }

    /**
     * <p>
     * This method indicates if the current {@link ProcessingContext} is still valid.
     * </p>
     */
    public boolean isFinished() {
        return _finished;
    }

    boolean _finished = false;
    boolean _special = false;
    boolean _nested = false;
    LayoutElement _parent = null;
    TemplateReader _reader = null;
}
