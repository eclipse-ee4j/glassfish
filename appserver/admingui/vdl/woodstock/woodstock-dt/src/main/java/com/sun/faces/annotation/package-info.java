/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Provides annotations necessary to generate run-time and design-time source
 * to support JSF component development. There are annotations for identifying
 * JSF renderers, JSF components, and component properties. A property may be
 * annotated in any class or interface, not just in a component class.
 *
 * <p>An simple annotated component class might look something like this:</p>
 * <pre>
 *   &#64;Component(
 *       name="myComponent",
 *       family="org.example.mycomponent",
 *       displayName="My Component",
 *       tagName="my-component"
 *   )
 *   public class MyComponent extends UIComponent {
 *
 *       &#64;Property (
 *           name="myProperty",
 *           displayName="My Property",
 *           attribute=&#64;Attribute(name="my-property",isRequired=false)
 *       )
 *       private String myProperty;
 *
 *       public String getMyProperty() {
 *           return this.myProperty;
 *       }
 *
 *       public void setMyProperty(String myProperty) {
 *           this.myProperty = mhyProperty;
 *       }
 *   }
 * </pre>
 */
package com.sun.faces.annotation;
