/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.amx.config;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
public final class AMXConfigConstants
{
    private AMXConfigConstants()
    {
    }

    /** prefix for all Descriptor fields for config */
    public static final String DESC_CONFIG_PREFIX = "amx.configbean.";

    /** prefix for all Descriptor fields for config */
    public static final String DESC_ANNOTATION_PREFIX = DESC_CONFIG_PREFIX + "annotation.";

    /** Descriptor: annotation type (full classname): HK2 @Attribute, @Element */
    public static final String DESC_KIND = DESC_CONFIG_PREFIX + "kind";

    /** Descriptor: class of items in an @Element collection (fully-qualified class name) */
    public static final String DESC_ELEMENT_CLASS = DESC_CONFIG_PREFIX + "elementClass";

    /** Descriptor: the xml name as found in domain.xml */
    public static final String DESC_XML_NAME = DESC_CONFIG_PREFIX + "xmlName";

    /** Descriptor: classname of data type (@Attribute only) */
    public static final String DESC_DATA_TYPE = DESC_CONFIG_PREFIX + "dataType";

    /** Descriptor: default value, omitted if none */
    public static final String DESC_DEFAULT_VALUE = DESC_CONFIG_PREFIX + "defaultValue";

    /** Descriptor: true | false: whether this is the primary key (name) */
    public static final String DESC_KEY = DESC_CONFIG_PREFIX + "key";

    /** Descriptor: true | false if this is required (implied if 'key') */
    public static final String DESC_REQUIRED = DESC_CONFIG_PREFIX + "required";

    /** Descriptor:  true | false whether this is a reference to another element */
    public static final String DESC_REFERENCE = DESC_CONFIG_PREFIX + "reference";

    /** Descriptor:  true | false whether variable expansion should be supplied */
    public static final String DESC_VARIABLE_EXPANSION = DESC_CONFIG_PREFIX + "variableExpansion";

    /** Descriptor:  true | false whether this field is required to be non-null */
    public static final String DESC_NOT_NULL = DESC_CONFIG_PREFIX + "notNull";

    /** Descriptor:  units of attribute quantities */
    public static final String DESC_UNITS = DESC_CONFIG_PREFIX + "units";

    /** Descriptor:  true | false whether variable expansion should be supplied */
    public static final String DESC_PATTERN_REGEX = DESC_CONFIG_PREFIX + "pattern";

    /** Descriptor:  minimum value, as a String */
    public static final String DESC_MIN = DESC_CONFIG_PREFIX + "min";

    /** Descriptor:  maximum value, as a String */
    public static final String DESC_MAX = DESC_CONFIG_PREFIX + "max";

    /**
    The type of the Notification emitted when a config element
    is created.
     */
    public static final String CONFIG_CREATED_NOTIFICATION_TYPE =
            "org.glassfish.admin.amx.intf.ConfigCreated";

    /**
    The type of the Notification emitted when a config element
    is removed.
     */
    public static final String CONFIG_REMOVED_NOTIFICATION_TYPE =
            "org.glassfish.admin.amx.config.ConfigRemoved";

    /**
    The key within the Notification's Map of type
    CONFIG_REMOVED_NOTIFICATION_TYPE which yields the ObjectName
    of the  created or removed config.
     */
    public static final String CONFIG_OBJECT_NAME_KEY = "ConfigObjectName";


    /** feature stating that the AMXConfig is ready for use after having been started.  Data is the ObjectName of the DomainConfig MBean */
    public static final String AMX_CONFIG_READY_FEATURE   = "AMXConfigReady";

}








