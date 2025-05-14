/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.modularity.customization;

/**
 * Will carry a set of four strings which will be used during domain creation to find what initial values are required
 * by a config bean to acquire them during the domain creation process.
 *
 * @author Masoud Kalali
 */
public class ConfigCustomizationToken {

    public static enum CustomizationType {
        PORT, FILE, STRING
    }

    private String name;
    private String title;
    private String description;
    private String value;
    private String validationExpression;
    private TokenTypeDetails tokenTypeDetails;
    private CustomizationType customizationType;

    public ConfigCustomizationToken(String name, String title, String description, String value, String validationExpression,
            TokenTypeDetails tokenTypeDetails, CustomizationType customizationType) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.value = value;
        this.validationExpression = validationExpression;
        this.tokenTypeDetails = tokenTypeDetails;
        this.customizationType = customizationType;
    }

    public String getValidationExpression() {
        return validationExpression;
    }

    public TokenTypeDetails getTokenTypeDetails() {
        return tokenTypeDetails;
    }

    public CustomizationType getCustomizationType() {
        return customizationType;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String tokenValue) {
        this.value = tokenValue;
    }

    @Override
    public String toString() {
        return "ConfigCustomizationToken[" + "name='" + name + ", value='" + value + "' description='" + description
            + "', validationExpression='" + validationExpression + "', tokenTypeDetails=" + tokenTypeDetails
            + ", customizationType=" + customizationType + ']';
    }
}
