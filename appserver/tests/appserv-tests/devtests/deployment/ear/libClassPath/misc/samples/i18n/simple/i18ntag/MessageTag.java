/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

/*
 * MessageTag.java
 *
 * Created on May 21, 2002, 5:17 PM
 */

package samples.i18n.simple.i18ntag;

import java.io.*;
import java.util.*;
import jakarta.servlet.jsp.*;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.jsp.tagext.*;

/**
 * A simple message taghandler to display localized message from a resource bundle based on jsp request
 * @author  Chand Basha
 * @version        1.0
 */

public class MessageTag extends TagSupport {

    private String key                        = null;
        private String bundleName        = null;
        private String language                = null;
    private String country                = null;
    private String variant                = null;

        /**
         * Set the user preferred language
         */
        public void setLanguage(String lang) {
        this.language = lang;
        }

    /**
         * Get the user preferred language
         */
        public String getLanguage() {
                return language;
        }

        /**
         * Set the user preferred country
         */
        public void setCountry(String country) {
                this.country = country;
        }

    /**
         * Get the user preferred country
         */
        public String getCountry() {
                return country;
    }

    /**
         * Set the user preferred variant
         */
    public void setVariant(String variant) {
            this.variant = variant;
        }

    /**
         * Get the user preferred variant
         */
        public String getVariant() {
            return variant;
    }

    /**
         * Set the user preferred resource bundle name
         */
        public void setName (String name) {
                this.bundleName = name;
        }

    /**
         * Set the message key required to retrieve message from the resource bundle
         */
    public void setKey(String key) {
        this.key = key;
    }

    /**
         * Get the message key required to retrieve message from the resource bundle
         */
    public String getKey() {
        return key;
    }

    /**
         * Gets the user preferred resource bundle name
         */
        public String getName() {
                return bundleName;
        }

    /**
         * Will be called by the JSP Engine when it encounters the start of the tag
         */
    public int doStartTag() throws JspTagException {
        return EVAL_BODY_INCLUDE;
    }

    /**
         * Will be called by the JSP Engine when it encounters the end of the tag
         */
    public int doEndTag() throws JspTagException {
        try {
                        if(language != null) {
                                if( country == null)
                                        country = "";
                                if(variant == null)
                                        variant = "";
                        } else language = "en";
                        java.util.Locale locale = new Locale(language, country, variant);
                        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
                        String message = bundle.getString(key);
                        pageContext.getOut().write("Message from resource bundle:" + message);
                } catch(Exception e) {
            throw new JspTagException("Error: " + e);
        }
        return EVAL_PAGE;
    }
}

