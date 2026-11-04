/*
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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
package jakarta.faces.component;

/**
 * Property editor constants.
 */
final class PropertyEditorConstants {

    /**
     * Cannot be instanciated.
     */
    private PropertyEditorConstants() {
    }

    /**
     * Method binding editor.
     */
    static final String METHODBINDING_EDITOR =
            "com.sun.rave.propertyeditors.MethodBindingPropertyEditor";

    /**
     * URL editor.
     */
    static final String URL_EDITOR =
            "com.sun.rave.propertyeditors.UrlPropertyEditor";

    /**
     * Validator editor.
     */
    static final String VALIDATOR_EDITOR =
            "com.sun.rave.propertyeditors.ValidatorPropertyEditor";

    /**
     * Value binding editor.
     */
    static final String VALUEBINDING_EDITOR =
            "com.sun.rave.propertyeditors.binding.ValueBindingPropertyEditor";
}
