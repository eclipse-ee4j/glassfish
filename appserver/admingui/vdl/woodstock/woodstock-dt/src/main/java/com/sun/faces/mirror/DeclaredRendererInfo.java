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
package com.sun.faces.mirror;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Represents a JSF renderer class declared in the current compilation unit.
 */
public final class DeclaredRendererInfo extends DeclaredClassInfo {

    /**
     * Renderer value key.
     */
    static final String VALUE = "value";

    /**
     * Annotation value map.
     */
    private final Map<String, Object> annotationValueMap;

    /**
     * List of render info.
     */
    private final List<RendersInfo> renderings;

    /**
     * Create a new instance.
     * @param annotValueMap annotation value map
     * @param env annotation processing environment
     * @param decl element declaration representing this renderer
     */
    DeclaredRendererInfo(final Map<String, Object> annotValueMap,
            final ProcessingEnvironment env, final TypeElement decl) {

        super(env, decl);
        this.annotationValueMap = annotValueMap;
        renderings = new ArrayList<RendersInfo>();
        if (this.annotationValueMap.containsKey(VALUE)) {
            for (Object value : (List) this.annotationValueMap.get(VALUE)) {
                Map nestedAnnotationValueMap = (Map) value;
                renderings.add(new RendersInfo(nestedAnnotationValueMap));
            }
        }
    }

    /**
     * Get the renderings.
     * @return {@code List<RendersInfo>}
     */
    public List<RendersInfo> getRenderings() {
        return this.renderings;
    }

    /**
     * Represents a single rendering declared within a renderer annotation.
     */
    public static final class RendersInfo {

        /**
         * Renderer type key.
         */
        static final String RENDERER_TYPE = "rendererType";

        /**
         * Component family key.
         */
        static final String COMPONENT_FAMILY = "componentFamily";

        /**
         * Annotation value map.
         */
        private final Map annotationValueMap;

        /**
         * Create a new instance.
         * @param annotValueMap annotation value map
         */
        RendersInfo(final Map annotValueMap) {
            this.annotationValueMap = annotValueMap;
        }

        /**
         * The renderer type.
         * @return String
         */
        public String getRendererType() {
            if (this.annotationValueMap.containsKey(RENDERER_TYPE)) {
                return (String) this.annotationValueMap.get(RENDERER_TYPE);
            }
            String[] componentFamilies = this.getComponentFamilies();
            if (componentFamilies.length > 0) {
                return componentFamilies[0];
            }
            return null;
        }

        /**
         * One or more component families to which this render type applies.
         * @return String
         */
        @SuppressWarnings("unchecked")
        public String[] getComponentFamilies() {
            if (this.annotationValueMap.containsKey(COMPONENT_FAMILY)) {
                List componentFamilies = (List) this.annotationValueMap
                        .get(COMPONENT_FAMILY);
                if (componentFamilies != null) {
                    return (String[]) componentFamilies.toArray(
                            new String[componentFamilies.size()]);
                }
            }
            return new String[0];
        }
    }
}
