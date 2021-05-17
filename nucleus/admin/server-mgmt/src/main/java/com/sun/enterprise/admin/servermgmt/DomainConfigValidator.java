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

package com.sun.enterprise.admin.servermgmt;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.servermgmt.InvalidConfigException;

import java.util.HashMap;

/**
 * This class validates the domain config Map object. It does this by invoking the validator of each required entry.
 * Subclasses must specify the required set of DomainConfigEntryInfo objects.
 */
public abstract class DomainConfigValidator extends Validator {
    /**
     * i18n strings manager object
     */
    private static final StringManager strMgr = StringManager.getManager(DomainConfigValidator.class);

    /**
     * Holder class for domain config entry meta info. The meta info of a domainConfig entry is the key, dataType,
     * displayText and an optional validator object. The key must be defined in the DomainConfig class.
     */
    protected static class DomainConfigEntryInfo {
        final String key;
        final String dataType;
        final Validator validator;

        /** Creates a new DomainConfigEntryInfo object */
        public DomainConfigEntryInfo(String key, String dataType, Validator validator) {
            this.key = key;
            this.dataType = dataType;
            this.validator = validator;
        }

        /**
         * Returns true if a non-null Validator object was specified during construction.
         */
        public boolean hasValidator() {
            return (validator != null);
        }
    }

    /**
     * An array of DomainConfigEntryInfo objects that must be initialized by subclasses.
     */
    private DomainConfigEntryInfo[] entries;

    /**
     * Constructs a new DomainConfigValidator object.
     *
     * @param entries An array of required DomainConfigEntryInfo objects. Must be supplied by subclasses.
     */
    protected DomainConfigValidator(DomainConfigEntryInfo[] entries) {
        super(strMgr.getString("domainConfig"), DomainConfig.class);
        this.entries = entries;
    }

    protected DomainConfigValidator(String name, Class type, DomainConfigEntryInfo[] entries) {
        super(name, type);
        this.entries = entries;
    }

    /**
     * Validates the domainConfig. For each required domain config entry in the entries, gets the value from the
     * domainConfig object and invokes the validator of that entry. Skips the validation of an entry if no validator is
     * specified for that entry.
     *
     * @param domainConfig The domainConfig object that needs to be validated. A domainConfig object is valid if it
     * <ul>
     * is of type DomainConfig contains the required set of DomainConfig keys the value for each required key is valid.
     * </ul>
     * @throws InvalidConfigException If invalid domainConfig is supplied.
     */
    public void validate(Object domainConfig) throws InvalidConfigException {
        super.validate(domainConfig);
        for (int i = 0; i < entries.length; i++) {
            if (isValidate(entries[i].key, domainConfig)) {
                final Object value = ((HashMap) domainConfig).get(entries[i].key);
                if (entries[i].hasValidator()) {
                    entries[i].validator.validate(value);
                }
            }
        }
    }

    /**
     * @param key
     * @return Returns true if the key is valid and required.
     */
    public boolean isKeyAllowed(Object key) {
        return (get(key) != null);
    }

    /**
     * @param key
     * @param value
     * @return Returns true if the key is valid and required and the value for that key is valid.
     */
    public boolean isValueValid(Object key, Object value) {
        boolean isValid = false;
        final DomainConfigEntryInfo info = get(key);
        if (info != null) {
            if (info.hasValidator()) {
                try {
                    info.validator.validate(value);
                } catch (InvalidConfigException idce) {
                    isValid = false;
                }
            } else {
                isValid = true;
            }
        }
        return isValid;
    }

    /**
     * @return Returns the accepted datatype for the key. The returned value is the fully qualified class name of the
     * datatype. If the key is invalid or doesnot belong to the valid domain config key set, "" is returned.
     */
    public String getDataType(Object key) {
        final DomainConfigEntryInfo info = get(key);
        if (info != null) {
            return info.dataType;
        }
        return "";
    }

    /**
     * This method allows subclasses to say if an entry should be validated at all. This is an attempt to add some
     * flexibility to the otherwise static validation. (Eg:- If we donot want to validate the ports during domain creation)
     */
    protected abstract boolean isValidate(String name, Object domainConfig);

    /**
     * @return Returns the DomainConfigEntryInfo corresponding to the key. Returns null if no DomainConfigEntryInfo exists
     * in the entries for the given key.
     */
    private DomainConfigEntryInfo get(Object key) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].key.equals(key)) {
                return entries[i];
            }
        }
        return null;
    }
}
