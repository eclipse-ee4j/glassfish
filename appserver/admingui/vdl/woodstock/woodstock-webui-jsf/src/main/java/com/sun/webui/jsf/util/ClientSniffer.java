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
package com.sun.webui.jsf.util;

import jakarta.faces.context.FacesContext;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * This utility class parses the user agent of a HttpServletRequest object to
 * determine browser type, version, and platform.
 * <p>
 * The code of this utility class is based on "The Ultimate JavaScript Client
 * Sniffer", version 3.03 which is located at the following URL.
 * </p><p>
 * http://www.mozilla.org/docs/web-developer/sniffer/browser_type.html
 * </p><p>
 * Usage Example:
 * </p>
 * <pre>
 * FacesContext context = FacesContext.getCurrentInstance();
 * ClientSniffer cs = new ClientSniffer(context);
 *
 * String stylesheet = CCStyle.IE6_UP_CSS;
 *
 * if (isIe6up()) {
 *     stylesheet = CCStyle.IE6_UP_CSS;
 * } else if (isIe5up()) {
 *     stylesheet = CCStyle.IE5_UP_CSS;
 * } else if (isNav6up()) {
 *     stylesheet = CCStyle.NS6_UP_CSS;
 * } else if (isNav4up() &amp;&amp; isWin()) {
 *     stylesheet = CCStyle.NS4_WIN_CSS;
 * } else if (isNav4up() &amp;&amp; isSun()) {
 *     stylesheet = CCStyle.NS4_SOL_CSS;
 * }
 * </pre>
 *
 */
@SuppressWarnings("checkstyle:magicnumber")
public final class ClientSniffer {

    // User Agent Headers (DON'T DELETE).
    //
    // Windows 2000
    // ------------
    //
    // IE 5.0   Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)
    // Netscape 4.7     Mozilla/4.7 [en] (WinNT; U)
    // Netscape 6.2.1   mozilla/5.0 (windows; u; win98; en-us; rv:0.9.4)
    //      gecko/20011128 netscape6/6.2.1
    // Netscape 7.02    Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US;
    //      rv:1.0.2) Gecko/20030208 Netscape/7.02
    // Netscape 7.1 Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4)
    //      Gecko/20030624 Netscape/7.1 (ax)
    // Mozilla 1.4  Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4)
    //      Gecko/20030624
    //
    // SunOS
    // -----
    //
    // Netscape 4.78    Mozilla/4.78 [en] (X11; U; SunOS 5.10 sun4u)
    // Netscape 6.2.1   mozilla/5.0 (x11; u; sunos sun4u; en-us; rv:0.9.4)
    //      gecko/20011206 netscape6/6.2.1
    // Netscape 6.2.2   Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:0.9.4.1)
    //      Gecko/20020406 Netscape6/6.2.2
    // Netscape 7.0     Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.0.1)
    //      Gecko/20020920 Netscape/7.0
    // Mozilla 1.1      Mozilla/5.0 (X11; U; SunOS sun4u; en-US; rv:1.1)
    //      Gecko/20020827
    // HotJava 1.0.1    HotJava/1.0.1/JRE1.1.3
    // Generic    Profile/MIDP-1.0 Configuration/CLDC-1.0

    /**
     * User agent.
     */
    private String agent;

    /**
     * User agent major version number.
     */
    private int major = -1;

    /**
     * Default constructor.
     *
     * @param context {@code FacesContext} which should be used to extract
     * the user agent.
     */
    public ClientSniffer(final FacesContext context) {
        String version = null;
        setUserAgent(context);
        agent = getUserAgent();

        // Parse user agent.
        if (agent != null) {

            StringTokenizer st = new StringTokenizer(agent, "/");

            // Parse out user agent name.
            if (st.hasMoreTokens()) {
                st.nextToken();
            }

            // Get user agent version number.
            if (st.hasMoreTokens()) {
                version = st.nextToken();
            }

            if (version != null) {
                // Remove white space & extra info.
                st = new StringTokenizer(version);
                if (st.hasMoreTokens()) {
                    version = st.nextToken();
                }
            }
        }

        // Parse user agent major version number.
        if (version != null) {
            StringTokenizer st = new StringTokenizer(version, ".");

            if (st.hasMoreTokens()) {
                try {
                    major = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException ex) {
                    // Ignore
                }
            }
        }
    }

    /**
     * This method gets an instance of this class associated with the given
     * {@code FacesContext}. It will look in the request scope to see if an
     * instance already exists, if not, it will create one.
     *
     * @param context The {@code FacesContext}
     *
     * @return  A {@code ClientSniffer} instance.
     */
    public static ClientSniffer getInstance(final FacesContext context) {
        // Look for a cached one
        Map<String, Object> requestMap = context.getExternalContext()
                .getRequestMap();
        ClientSniffer sniffer = (ClientSniffer) requestMap.get("__sniffer");

        if (sniffer == null) {
            // Not yet created, create one
            sniffer = new ClientSniffer(context);
            requestMap.put("__sniffer", sniffer);
        }

        // Return the sniffer
        return sniffer;
    }

    /**
     * This method initializes the user agent via the supplied
     * {@code FacesContext}. It will use the {@code ExternalContext}
     * to get at the request header Map. It will use this Map to obtain the
     * value for {@code USER-AGENT}.
     *
     * @param context The {@code FacesContext}
     */
    protected void setUserAgent(final FacesContext context) {
        Map headerMap = context.getExternalContext().getRequestHeaderMap();
        if (null != headerMap) {
            agent = (String) headerMap.get("USER-AGENT");
            if (null != agent) {
                agent = agent.toLowerCase();
            }
        }
    }

    /**
     * Get the user agent.
     *
     * @return The user agent.
     */
    public String getUserAgent() {
        return agent;
    }

    /**
     * Get the user agent major version number.
     *
     * @return The user agent major version number or -1 if the version number
     * was not retrieved.
     */
    public int getUserAgentMajor() {
        return major;
    }

    /**
     * Test if the user agent was generated on Windows platform.
     *
     * @return {@code true} if Windows {@code false} otherwise
     */
    public boolean isWin() {
        boolean result = false;

        if ((agent != null) && ((agent.contains("win"))
                || (agent.contains("16bit")))) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated on Sun platform.
     *
     * @return {@code true} if SunOs {@code false} otherwise
     */
    public boolean isSun() {
        boolean result = false;

        if ((agent != null) && (agent.contains("sunos"))) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Gecko engine.
     *
     * @return {@code true} if Gecko {@code false} otherwise
     */
    public boolean isGecko() {
        boolean result = false;

        if ((agent != null)
                && (agent.contains("gecko"))) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Navigator.
     *
     * @return {@code true} if Navigator {@code false} otherwise
     */
    public boolean isNav() {
        boolean result = false;

        if ((agent != null)
                && (agent.contains("mozilla"))
                && (agent.contains("spoofer"))
                && (!agent.contains("compatible"))
                && (!agent.contains("opera"))
                && (!agent.contains("webtv"))
                && (!agent.contains("hotjava"))) {
            // The header for Netscape 4.x is similar to the header
            // for the Mozilla browser; however, Netscape 4.x does not
            // implement the Gecko engine.
            if (!(isGecko() && (!agent.contains("netscape")))) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 4.x.
     *
     * @return {@code true} if Navigator 4.x {@code false} otherwise
     */
    public boolean isNav4() {
        boolean result = false;

        if (isNav() && (major == 4)) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 4.x or above.
     *
     * @return {@code true} if Navigator 4.x or above {@code false} otherwise
     */
    public boolean isNav4up() {
        boolean result = false;

        if (isNav() && (major >= 4)) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 6.x.
     *
     * @return {@code true} if Navigator 6.x {@code false} otherwise
     */
    public boolean isNav6() {
        boolean result = false;
        if (isNav() && (major == 5) && (null != agent)
                && (agent.contains("netscape6"))) {
            result = true;
        }

        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 6.x or above.
     *
     * @return {@code true} if Navigator 6.x or above {@code false} otherwise
     */
    public boolean isNav6up() {
        boolean result = false;

        if (isNav() && major >= 5) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 7.x.
     *
     * @return {@code true} if Navigator 7.x {@code false} otherwise
     */
    public boolean isNav7() {
        boolean result = false;
        if (isNav()
                && major == 5
                && (null != agent)
                && (agent.contains("netscape/7"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 7.0.
     *
     * @return {@code true} if Navigator 7.0 {@code false} otherwise
     */
    public boolean isNav70() {
        boolean result = false;
        if (isNav()
                && major == 5
                && (null != agent)
                && (agent.contains("netscape/7.0"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Navigator, version 7.x or above.
     *
     * @return {@code true} if Navigator 7 or above {@code false} otherwise
     */
    public boolean isNav7up() {
        boolean result = false;

        if (isNav()
                && (major >= 5)
                && !isNav4()
                && !isNav6()) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer.
     *
     * @return {@code true} if IE {@code false} otherwise
     */
    public boolean isIe() {
        boolean result = false;

        if ((agent != null)
                && (agent.contains("msie"))
                && (!agent.contains("opera"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 3.x.
     *
     * @return {@code true} if IE3 {@code false} otherwise
     */
    public boolean isIe3() {
        boolean result = false;

        if (isIe() && (major < 4)) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 4.x.
     *
     * @return {@code true} if IE4 {@code false} otherwise
     */
    public boolean isIe4() {
        boolean result = false;
        if (isIe()
                && (major == 4)
                && (null != agent)
                && (agent.contains("msie 4"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 5.x.
     *
     * @return {@code true} if IE5 {@code false} otherwise
     */
    public boolean isIe5() {
        boolean result = false;
        if (isIe()
                && (major == 4)
                && (null != agent)
                && (agent.contains("msie 5"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 5.x or
     * above.
     *
     * @return {@code true} if IE5 or above {@code false} otherwise
     */
    public boolean isIe5up() {
        boolean result = false;

        if (isIe() && !isIe3() && !isIe4()) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 6.x.
     *
     * @return {@code true} if IE6 {@code false} otherwise
     */
    public boolean isIe6() {
        boolean result = false;
        if (isIe()
                && (major == 4)
                && (null != agent)
                && (agent.contains("msie 6"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 7.x.
     *
     * @return {@code true} if IE7 {@code false} otherwise
     */
    public boolean isIe7() {
        boolean result = false;
        if (isIe()
                && (major == 4)
                && (null != agent)
                && (agent.contains("msie 7"))) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 6.x or
     * above.
     *
     * @return {@code true} if IE6 or above {@code false} otherwise
     */
    public boolean isIe6up() {
        boolean result = false;

        if (isIe()
                && !isIe3()
                && !isIe4()
                && !isIe5()) {
            result = true;
        }
        return result;
    }

    /**
     * Test if the user agent was generated by Internet Explorer, version 7.x or
     * above.
     *
     * @return {@code true} if IE7 or above {@code false} otherwise
     */
    public boolean isIe7up() {
        boolean result = false;

        if (isIe()
                && !isIe3()
                && !isIe4()
                && !isIe5()
                && !isIe6()) {
            result = true;
        }
        return result;
    }

    /**
     * This method is used by the Theme.
     *
     * @param context The {@code FacesContext}
     * @return  The {@link ClientType}.
     */
    public static ClientType getClientType(final FacesContext context) {
        Map map = context.getExternalContext().getRequestHeaderMap();
        if (null == map) {
            return ClientType.OTHER;
        }
        String agent = (String) map.get("USER-AGENT");

        if (agent == null) {
            return ClientType.OTHER;
        }
        agent = agent.toLowerCase();
        // Need to test for Safari first since it has "Gecko like"
        // in the user agent string and this will match "gecko"
        if (agent.contains("safari")) {
            return ClientType.SAFARI;
        } else if (agent.contains("gecko")) {
            return ClientType.GECKO;
        } else if (agent.contains("msie 7")) {
            return ClientType.IE7;
        } else if (agent.contains("msie 6")) {
            return ClientType.IE6;
        } else if (agent.contains("msie 5.5")) {
            return ClientType.IE5_5;
        } else {
            return ClientType.OTHER;
        }
    }
}
