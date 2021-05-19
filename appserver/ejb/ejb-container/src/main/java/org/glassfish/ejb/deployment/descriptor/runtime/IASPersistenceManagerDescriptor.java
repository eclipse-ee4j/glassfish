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

package org.glassfish.ejb.deployment.descriptor.runtime;

import org.glassfish.deployment.common.Descriptor;


/**
 * This class contains information about the persistent state
 * (abstract persistence schema)
 * for EJB2.0 CMP EntityBeans .
 *
 * @author Prashant Jamkhedkar
 */

public class IASPersistenceManagerDescriptor extends Descriptor {

    public static final String PM_IDENTIFIER_DEFAULT = "SunOne"; // NOI18N
    public static final String PM_VERSION_DEFAULT = "1.0"; // NOI18N
    public static final String PM_CONFIG_DEFAULT = "myconfig.config";
    public static final String PM_CLASS_GENERATOR_DEFAULT = "com.sun.jdo.spi.persistence.support.ejb.ejbc.JDOCodeGenerator"; // NOI18N
    public static final String PM_CLASS_GENERATOR_DEFAULT_OLD = "com.iplanet.ias.persistence.internal.ejb.ejbc.JDOCodeGenerator"; //NOI18N
    public static final String PM_MAPPING_FACTORY_DEFAULT = "com.sun.ffj.MyFactory"; // NOI18N
    private String pm_identifier = null;
    private String pm_version = null;
    private String pm_config = null;
    private String pm_class_generator = null;
    private String pm_mapping_factory = null;

    public IASPersistenceManagerDescriptor() {
       pm_identifier = PM_IDENTIFIER_DEFAULT;
       pm_version = PM_VERSION_DEFAULT;
       pm_config = PM_CONFIG_DEFAULT;
       pm_class_generator = PM_CLASS_GENERATOR_DEFAULT;
       pm_mapping_factory = PM_MAPPING_FACTORY_DEFAULT;
    }

    /**
     * The copy constructor.
     */
    public IASPersistenceManagerDescriptor(String id, String ver, String conf, String generator, String factory) {
       pm_identifier = id;
       pm_version = ver;
       pm_config = conf;
       pm_class_generator = generator;
       pm_mapping_factory = factory;
    }

    public String getPersistenceManagerIdentifier() {
      return pm_identifier;
    }

    public void setPersistenceManagerIdentifier(String pm_identifier) {
        if (pm_identifier == null) {
            this.pm_identifier = PM_IDENTIFIER_DEFAULT;
        } else {
            this.pm_identifier = pm_identifier;
        }
    }

    public String getPersistenceManagerVersion() {
      return pm_version;
    }

    public void setPersistenceManagerVersion(String pm_version) {
        if (pm_version == null) {
            this.pm_version = PM_VERSION_DEFAULT;
        } else {
            this.pm_version = pm_version;
        }
    }

    public String getPersistenceManagerConfig () {
      return pm_config;
    }

    public void setPersistenceManagerConfig(String pm_config) {
        if (pm_config == null) {
            this.pm_config = PM_CONFIG_DEFAULT;
        } else {
            this.pm_config = pm_config;
        }
    }

    public String getPersistenceManagerClassGenerator() {
      return pm_class_generator;
    }

    public void setPersistenceManagerClassGenerator(String pm_class_generator) {
        if (pm_class_generator == null) {
            this.pm_class_generator = PM_CLASS_GENERATOR_DEFAULT;
        } else {
            this.pm_class_generator = pm_class_generator;
        }
    }

    public String getPersistenceManagerMappingFactory() {
      return pm_mapping_factory;
    }

    public void setPersistenceManagerMappingFactory(String pm_mapping_factory) {
        if (pm_mapping_factory == null) {
            this.pm_mapping_factory = PM_MAPPING_FACTORY_DEFAULT;
        } else {
            this.pm_mapping_factory = pm_mapping_factory;
        }
    }

    /**
     * Called from EjbCMPEntityDescriptor
     * when some classes in this object are updated.
     */
    public boolean classesChanged() {
        return false;
    }

}
