/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.osgi.felixwebconsoleextension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.webconsole.BrandingPlugin;
import org.apache.felix.webconsole.DefaultBrandingPlugin;

/**
 * This is a customization of {@link BrandingPlugin} for GlassFish.
 *
 * If a properties file <code>META-INF/webconsole.properties</code> is available
 * through the class loader of this class, the properties overwrite the default
 * settings according to the property names listed in {@link BrandingPlugin}.
 * The easiest way to add such a properties file is to provide a fragment bundle with the file.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
public class GlassFishBrandingPlugin implements BrandingPlugin {
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());

    private final String brandName;
    private final String prouctName;
    private final String productImage;
    private final String productUrl;
    private final String vendorName;
    private final String vendorUrl;
    private final String vendorImage;
    private final String favIcon;
    private final String mainStyleSheet;

    // default values
    private static final String NAME = "GlassFish OSGi Administration Console";
    private static final String PROD_NAME = "Eclipse GlassFish";
    private static final String PROD_IMAGE = "https://projects.eclipse.org/sites/default/files/glassfish_logo_475_475_transparent.png";
    private static final String PROD_URL = "http://glassfish.org";
    private static final String VENDOR = "Eclipse Foundation";
    private static final String VENDOR_URL = PROD_URL;
    private static final String VENDOR_IMAGE = PROD_IMAGE;

    // This is where we look for any custom/localized branding information Must be made available via a fragment
    private final String path = "/META-INF/webconsole.properties";
    Properties branding = new Properties();

    public GlassFishBrandingPlugin() {
        InputStream inStream = getClass().getResourceAsStream(path);
        if (inStream != null) {
            try {
                branding.load(inStream);
                logger.logp(Level.INFO, "GlassFishBrandingPlugin", "GlassFishBrandingPlugin", "branding = {0}",
                        new Object[]{branding});
            } catch (IOException e) {
                logger.logp(Level.INFO, "GlassFishBrandingPlugin", "GlassFishBrandingPlugin",
                        "Failed to read properties file", e);
                // we will use defaults if we fail here
            } finally {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
        }

        brandName = getBranding().getProperty("webconsole.brand.name", NAME);
        prouctName = getBranding().getProperty("webconsole.product.name", PROD_NAME);
        productImage = getBranding().getProperty("webconsole.product.image", PROD_IMAGE);
        productUrl = getBranding().getProperty("webconsole.product.url", PROD_URL);
        vendorName = getBranding().getProperty("webconsole.vendor.name", VENDOR);
        vendorUrl = getBranding().getProperty("webconsole.vendor.url", VENDOR_URL);
        vendorImage = getBranding().getProperty("webconsole.vendor.image", VENDOR_IMAGE);
        // we don't have our own default
        favIcon = getBranding().getProperty("webconsole.favicon", getDefaultPlugin().getFavIcon());
        // we don't have our own default
        mainStyleSheet = getBranding().getProperty("webconsole.stylesheet", getDefaultPlugin().getMainStyleSheet());
    }

    private DefaultBrandingPlugin getDefaultPlugin() {
        return DefaultBrandingPlugin.getInstance();
    }

    @Override
    public String getBrandName() {
        return brandName;
    }

    @Override
    public String getProductName() {
        return prouctName;
    }

    @Override
    public String getProductURL() {
        return productUrl;
    }

    @Override
    public String getProductImage() {
        return productImage;
    }

    @Override
    public String getVendorName() {
        return vendorName;
    }

    @Override
    public String getVendorURL() {
        return vendorUrl;
    }

    @Override
    public String getVendorImage() {
        return vendorImage;
    }

    @Override
    public String getFavIcon() {
        return favIcon;
    }

    @Override
    public String getMainStyleSheet() {
        return mainStyleSheet;
    }

    public Properties getBranding() {
        return branding;
    }
}
