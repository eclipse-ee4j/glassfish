/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * CommonHandlers.java
 *
 * Created on August 30, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.handlers;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jsftemplating.el.PageSessionResolver;
import org.glassfish.admingui.common.tree.FilterTreeEvent;
import org.glassfish.admingui.common.util.GuiUtil;
import org.glassfish.admingui.common.util.MiscUtil;
import org.glassfish.admingui.common.util.RestUtil;
import org.glassfish.admingui.common.util.TargetUtil;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.handlers.NavigationHandlers;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;
import com.sun.jsftemplating.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;


public class CommonHandlers {

    private static final String AIX = "AIX";

    /** Creates a new instance of CommonHandlers */
    public CommonHandlers() {
    }

    /**
     * This handler will be called during initialization when Cluster Support is detected.
     */
    @Handler(id="initClusterSessionAttribute")
    public static void initClusterSessionAttribute(HandlerContext handlerCtx){
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();
        //The summary or detail view of deploy tables is stored in session to remember user's previous
        //preference.
        sessionMap.put("appSummaryView", true);
        sessionMap.put("webSummaryView", true);
        sessionMap.put("ejbSummaryView", true);
        sessionMap.put("appclientSummaryView", true);
        sessionMap.put("rarSummaryView", true);
        sessionMap.put("lifecycleSummaryView", true);

        sessionMap.put("adminObjectSummaryView", true);
        sessionMap.put("connectorResSummaryView", true);
        sessionMap.put("customResSummaryView", true);
        sessionMap.put("externalResSummaryView", true);
        sessionMap.put("javaMailSessionSummaryView", true);
        sessionMap.put("jdbcResSummaryView", true);
        sessionMap.put("jmsConnectionSummaryView", true);
        sessionMap.put("jmsDestinationSummaryView", true);
    }

    /**
     * This handler will be called during initialization for doing any initialization.
     */
    @Handler(id="initSessionAttributes")
    public static void initSessionAttributes(HandlerContext handlerCtx){

        //Ensure this method is called once per session
        Object initialized = FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("_SESSION_INITIALIZED");
        if (initialized == null){
            GuiUtil.initSessionAttributes();
        }
        return;
    }

    /**
     * <p>
     * This handler will be called from baseLayout.xhtml to load the maximum
     * field lengths (maximum number of characters that a user can enter in each
     * field).
     */
    @Handler(id="getFieldLengths",
        input={
            @HandlerInput(name="bundle", type=java.util.ResourceBundle.class, required=true)
        },
        output={
            @HandlerOutput(name="result", type=Map.class)
    })
    public static void getFieldLengths(HandlerContext handlerCtx) {
        ResourceBundle bundle = (ResourceBundle) handlerCtx.getInputValue("bundle");
        Map<String, Integer> result = new HashMap<>();
        for (String key : bundle.keySet()) {
            try {
                result.put(key, Integer.decode(bundle.getString(key)));
            } catch (NumberFormatException ex) {
                // Log warning about expecting a number...
                // This should never happen; if it does it's a bug, so no need to localize.
                GuiUtil.getLogger().warning(
                    "Field length is expected to be a number, but got ('"
                    + bundle.getString(key) + "') instead for key '"
                    + key + "'.");
            }
        }
        handlerCtx.setOutputValue("result", result);
    }

    /** This function is called in login.jsf to set the various product specific attributes such as the
     *  product GIFs and product names. A similar function is called for Sailfin to set Sailfin specific
     *  product GIFs and name.
     *  The function is defined in com.sun.extensions.comms.SipUtilities
     */
    @Handler(id="initProductInfoAttributes")
    public static void initProductInfoAttributes(HandlerContext handlerCtx) {
        Map sessionMap = handlerCtx.getFacesContext().getExternalContext().getSessionMap();

        //Ensure this method is called once per session
        Object initialized = sessionMap.get("_INFO_SESSION_INITIALIZED");
        if (initialized != null) {
            return;
        }

        // Initialize Product Specific Attributes
        sessionMap.put("productImageURL", GuiUtil.getMessage("productImage.URL"));
        sessionMap.put("productImageWidth", Integer.parseInt(GuiUtil.getMessage("productImage.width")));
        sessionMap.put("productImageHeight", Integer.parseInt(GuiUtil.getMessage("productImage.height")));
        sessionMap.put("loginProductImageURL", GuiUtil.getMessage("login.productImage.URL"));
        sessionMap.put("loginProductImageWidth", Integer.parseInt(GuiUtil.getMessage("login.productImage.width")));
        sessionMap.put("loginProductImageHeight", Integer.parseInt(GuiUtil.getMessage("login.productImage.height")));
        sessionMap.put("fullProductName", GuiUtil.getMessage("versionImage.description"));
        sessionMap.put("loginButtonTooltip", GuiUtil.getMessage("loginButtonTooltip"));
        sessionMap.put("mastHeadDescription", GuiUtil.getMessage("mastHeadDescription"));

        // showLoadBalancer is a Sailfin specific attribute. Sailfin uses Converged LB instead
        // of HTTP LB. It is true for GF and false for Sailfin. In sailfin this is set in
        // com.sun.extensions.comms.SipUtilities.initProductInfoAttributes() called for Sailfin in login.jsf

        //TODO-V3 may need to set this back to true
        //sessionMap.put("showLoadBalancer", true);

        sessionMap.put("_INFO_SESSION_INITIALIZED","TRUE");
    }


    /**
     * This handler returns String[] of the given java.util.List
     * <p>
     * Output value: "selectedIndex" -- Type: <code>Object</code>/
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="getListElement",
            input={
        @HandlerInput(name="list", type=java.util.List.class, required=true ),
        @HandlerInput(name="index", type=Integer.class)},
        output={
        @HandlerOutput(name="selectedIndex", type=Object.class)})
    public static void getListElement(HandlerContext handlerCtx) {
        List<String> list = (List)handlerCtx.getInputValue("list");
        Integer selectedIndex = (Integer)handlerCtx.getInputValue("index");
        String[] listItem = null;
        if(list != null) {
            if(selectedIndex == null) {
                //default to 0
                selectedIndex = Integer.valueOf(INDEX);
            }
            listItem = new String[]{list.get(selectedIndex)};
        }
        handlerCtx.setOutputValue("selectedIndex", listItem);
    }


    /**
     * This handler removes the given element from the list
     * <p>
     * Output value: "finalList" -- Type: <code>List</code>/
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="removeListElement",
            input={
        @HandlerInput(name="list", type=java.util.List.class, required=true ),
        @HandlerInput(name="name", type=String.class)},
        output={
        @HandlerOutput(name="finalList", type=java.util.List.class)})
    public static void removeListElement(HandlerContext handlerCtx) {
        List<String> list = (List) handlerCtx.getInputValue("list");
        String name = (String) handlerCtx.getInputValue("name");
        if (list != null) {
            list.remove(name);
        }
        handlerCtx.setOutputValue("finalList", list);
    }


    /**
     * This handler converts the milliseconds to readable format
     * <p>
     * Output value: "readableString" -- Type: <code>String</code>/
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="convertMillisToReadable",
            input={
            @HandlerInput(name="milliseconds", type=Long.class, required=true )},
        output={
            @HandlerOutput(name="readableString", type=String.class)})
    public static void convertMillisToReadable(HandlerContext handlerCtx) {
        Long milliseconds = (Long) handlerCtx.getInputValue("milliseconds");
        final long MSEC_PER_SECOND = 1000;
        final long MSEC_PER_MINUTE = 60 * MSEC_PER_SECOND;
        final long MSEC_PER_HOUR = MSEC_PER_MINUTE * 60;
        final long MSEC_PER_DAY = MSEC_PER_HOUR * 24;
        final long MSEC_PER_WEEK = MSEC_PER_DAY * 7;
        String FORMAT2 = "%d %s %d %s";
        String FORMAT1 = "%d %s";

        String readableString = "";

        long msecLeftover = milliseconds;

        long numWeeks = msecLeftover / MSEC_PER_WEEK;
        msecLeftover -= numWeeks * MSEC_PER_WEEK;

        long numDays = msecLeftover / MSEC_PER_DAY;
        msecLeftover -= numDays * MSEC_PER_DAY;

        long numHours = msecLeftover / MSEC_PER_HOUR;
        msecLeftover -= numHours * MSEC_PER_HOUR;

        long numMinutes = msecLeftover / MSEC_PER_MINUTE;
        msecLeftover -= numMinutes * MSEC_PER_MINUTE;

        long numSeconds = msecLeftover / MSEC_PER_SECOND;
        msecLeftover -= numSeconds * MSEC_PER_SECOND;

        long numMilliSeconds = msecLeftover;
        if (numWeeks > 0) {
            readableString = String.format(FORMAT2, numWeeks, GuiUtil.getMessage("common.Weeks"), numDays, GuiUtil.getMessage("common.Days"));
        } else if (numDays > 0) {
            readableString = String.format(FORMAT1, numDays, GuiUtil.getMessage("common.Days"));
        } else if (numHours > 0) {
            readableString = String.format(FORMAT2, numHours, GuiUtil.getMessage("common.Hours"), numMinutes, GuiUtil.getMessage("common.Minutes"));
        } else if (numMinutes > 0) {
            readableString = String.format(FORMAT2, numMinutes, GuiUtil.getMessage("common.Minutes"), numSeconds, GuiUtil.getMessage("common.Seconds"));
        } else if (numSeconds > 0) {
            readableString = String.format(FORMAT1, numSeconds, GuiUtil.getMessage("common.Seconds"));
        } else {
            readableString = String.format(FORMAT1, numMilliSeconds, GuiUtil.getMessage("common.Milliseconds"));
        }
        handlerCtx.setOutputValue("readableString", readableString);
    }


    /**
     * This handler creates a map with the given keys and values
     * <p>
     * Output value: "map" -- Type: <code>Map</code>/
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="gf.createAttributeMap",
            input={
        @HandlerInput(name="keys", type=java.util.List.class),
        @HandlerInput(name="values", type=java.util.List.class)},
        output={
        @HandlerOutput(name="map", type=java.util.Map.class)})
    public static void createAttributeMap(HandlerContext handlerCtx) {
        List<String> keys = (List<String>) handlerCtx.getInputValue("keys");
        List values = (List) handlerCtx.getInputValue("values");
        Map<String, Object> map = new HashMap<>();
        if (keys != null && values != null) {
            for (int i = 0; i < keys.size(); i++) {
                map.put(keys.get(i), values.get(i));
            }
        }
        handlerCtx.setOutputValue("map", map);
    }


    /**
     * This handler returns the encoded String using the type specified.
     * <p>
     * If type is not specified, it defaults to UTF-8.
     * <ul>
     * <li>Input value: "value" -- Type: <code>String</code>
     * <li>Input value: "delim" -- Type: <code>String</code>
     * <li>Input Value: "type" -- Type: <code>String</code>
     * <li>Output Value: "result" -- Type: <code>String</code>
     * </ul>
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="selectiveEncode",
    input={
        @HandlerInput(name="value", type=String.class, required=true ),
        @HandlerInput(name="delim", type=String.class),
        @HandlerInput(name="type", type=String.class)},
    output={
        @HandlerOutput(name="result", type=String.class)}
    )
    public static void selectiveEncode(HandlerContext handlerCtx) {

        String value = (String) handlerCtx.getInputValue("value");
        String delim = (String) handlerCtx.getInputValue("delim");
        String encType = (String) handlerCtx.getInputValue("type");
                String encodedString = GuiUtil.encode(value, delim, encType);
        handlerCtx.setOutputValue("result", encodedString);
   }


   /**
    * This method kills the session, and logs out
    * Server Domain Attributes Page.
    * <p>
    * Input value: "page" -- Type: <code>java.lang.String</code>
    *
    * @param handlerCtx The HandlerContext.
    */
    @Handler(id="logout")
    public static void logout(HandlerContext handlerCtx) {
        handlerCtx.getFacesContext().getExternalContext().invalidateSession();
    }


    /**
     * This method sets the required attribute of a UI component .
     * <ul>
     * <li>Input value: "id" -- Type: <code>java.lang.String</code>
     * <li>Input value: "required" -- Type: <code>java.lang.String</code>
     * </ul>
     *
     * @param handlerCtx The HandlerContext.
     */
    @Handler(id="setComponentRequired",
    input={
        @HandlerInput(name="id",     type=String.class, required=true),
        @HandlerInput(name="required",     type=String.class, required=true)
    })
    public static void setComponentRequired(HandlerContext handlerCtx) {
        String id = (String) handlerCtx.getInputValue("id");
        String required = (String) handlerCtx.getInputValue("required");
        UIComponent viewRoot = handlerCtx.getFacesContext().getViewRoot();
        if (viewRoot == null) {
            return;
        }
        try {
            UIInput targetComponent = (UIInput) viewRoot.findComponent(id);
            if (targetComponent != null ){
                targetComponent.setRequired(Boolean.valueOf(required));
            }

        }catch(Exception ex){
            //Cannot find component, do nothing.
        }
    }


    /**
     * Test if a particular attribute exists.
     * It will look at request scope, then page, then session.
     */
    @Handler(id="testExists",
    input={
        @HandlerInput(name="attr", type=String.class, required=true )},
    output={
        @HandlerOutput(name="defined", type=Boolean.class)}
    )
    public static void testExists(HandlerContext handlerCtx) {
        String attr = (String) handlerCtx.getInputValue("attr");
        if(GuiUtil.isEmpty(attr)){
            handlerCtx.setOutputValue("defined", false);
        }else{
            handlerCtx.setOutputValue("defined", true);
        }
    }

    /**
     * Remove the properties if the key(name) is empty.
     */
    @Handler(id="removeEmptyProps",
    input={
        @HandlerInput(name="props", type=List.class, required=true )},
    output={
        @HandlerOutput(name="modifiedProps", type=List.class)}
    )
    public static void removeEmptyProps(HandlerContext handlerCtx) {
        List<Map<String, String>> props = (List<Map<String, String>>) handlerCtx.getInputValue("props");
        List<Map<String, String>> modifiedProps = new java.util.ArrayList<>();
        if (props != null) {
            for (Map<String, String> prop : props) {
                if (!(GuiUtil.isEmpty(prop.get("name")))) {
                    if (GuiUtil.isEmpty(prop.get("value"))) {
                        continue;
                    } else if (prop.get("value").equals("()")) {
                        prop.put("value", "");
                    }
                    modifiedProps.add(prop);
                }
            }
        }
        handlerCtx.setOutputValue("modifiedProps", modifiedProps);
    }


    /**
     * This handler returns the requestParameter value based on the key.
     * If it doesn't exists, then it will look at the request
     * attribute. If there is no request attribute, it will return the
     * default, if specified.
     * <p>
     * This method will "html escape" any &lt;, &gt;, or &amp; characters
     * that appear in a String from the QUERY_STRING. This is to help
     * prevent XSS vulnerabilities.
     * <p>
     * orig without escape is available, but be very cautious when using it.
     * <ul>
     * <li>Input value: "key" -- Type: <code>String</code>
     * <li>Output value: "value" -- Type: <code>String</code>
     * <li>Output value: "orig" -- Type: <code>String</code>
     * </ul>
     */
    @Handler(id="getRequestValue",
    input={
        @HandlerInput(name="key", type=String.class, required=true),
        @HandlerInput(name="default", type=String.class)},
    output={
        @HandlerOutput(name="value", type=Object.class),
        @HandlerOutput(name="orig", type=Object.class)}
    )
    public static void getRequestValue(HandlerContext handlerCtx) {
        String key = (String) handlerCtx.getInputValue("key");
        Object defaultValue = handlerCtx.getInputValue("default");
        Object value = handlerCtx.getFacesContext().getExternalContext().getRequestParameterMap().get(key);
        Object orig = value;
        if ((value == null) || "".equals(value)) {
            value = handlerCtx.getFacesContext().getExternalContext().getRequestMap().get(key);
            if ((value == null) && (defaultValue != null)){
                value = defaultValue;
            }
        } else {
            // For URLs, the following could be used, but it URLEncodes  the
            // values, which are not ideal for displaying in HTML... so I will
            // instead call htmlEscape()
            //value = GuiUtil.encode(value, "#=@%+;-&_.?:/()", "UTF-8");

            // Only need to do this for QUERY_STRING values...
            value = Util.htmlEscape((String) value);
        }
        handlerCtx.setOutputValue("value", value);
        handlerCtx.setOutputValue("orig", orig);
    }

    /**
     * This method adds two long integers together.  The 2 longs should be
     * stored in "long1" and "long2".  The result will be stored as "result".
     */
    @Handler(id="longAdd",
    input={
        @HandlerInput(name="Long1", type=Long.class, required=true ),
        @HandlerInput(name="Long2", type=Long.class, required=true )},
    output={
        @HandlerOutput(name="LongResult", type=Long.class)}
    )
    public void longAdd(HandlerContext handlerCtx) {
        Long long1 = (Long)handlerCtx.getInputValue("Long1");
        Long long2 = (Long)handlerCtx.getInputValue("Long2");
        Long result = Long.valueOf(0);
        try{
            // Add the 2 numbers together
            result = Long.valueOf(long1.longValue()+long2.longValue());
        }catch(Exception ex){
            Logger logger = GuiUtil.getLogger();
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, GuiUtil.getCommonMessage("LOG_LONGADD_ERROR", new Object[]{""+long1, ""+long2}));
            }
        }
        // Set the result
        handlerCtx.setOutputValue("LongResult", result);
    }

    /**
     * Returns the current system time formatted<p>
     * Output value: "Time" -- Type: <code>String</code></p>
     *
     */
    @Handler(id="getCurrentTime",
    output={
        @HandlerOutput(name="CurrentTime", type=String.class)}
    )
    public void getCurrentTime(HandlerContext handlerCtx) {
        Date d = new Date(System.currentTimeMillis());
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.MEDIUM, handlerCtx.getFacesContext().getViewRoot().getLocale());
        String currentTime = dateFormat.format(d);
        handlerCtx.setOutputValue("CurrentTime", currentTime);
    }


    @Handler(id="gf.handleError",
    input={
        @HandlerInput(name="detail", type=String.class)}
    )
    public void handleError(HandlerContext handlerCtx) {
        String detail = (String)handlerCtx.getInputValue("detail");
        GuiUtil.prepareAlert("error", GuiUtil.getMessage("msg.Error"), detail);
        handlerCtx.getFacesContext().renderResponse();
    }


    @Handler(id="gf.onlyDASExist",
    output={
        @HandlerOutput(name="onlyDAS", type=Boolean.class)}
    )
    public static void onlyDas(HandlerContext handlerCtx){
        boolean onlyDAS = TargetUtil.getClusters().isEmpty() && TargetUtil.getStandaloneInstances().isEmpty();
        handlerCtx.setOutputValue("onlyDAS", onlyDAS);
    }

    /**
     * This handler sets a property on an object which is stored in an existing key
     * For example "advance.lazyConnectionEnlistment".  <strong>Note</strong>:  This does
     * <em>not</em> evaluate the EL expression.  Its value (e.g., "#{advance.lazyConnectionEnlistment}")
     * is passed as is to the EL API.
     */
    @Handler(id = "setValueExpression",
        input = {
            @HandlerInput(name = "expression", type = String.class, required = true),
            @HandlerInput(name = "value", type = Object.class, required = true)
    })
    public static void setValueExpression(HandlerContext handlerCtx) {
        MiscUtil.setValueExpression((String) handlerCtx.getHandler().getInputValue("expression"),
                handlerCtx.getInputValue("value"));
    }


    @Handler(id = "gf.convertDateTime",
        input = {
            @HandlerInput(name = "dateTime", type = String.class, required = true),
            @HandlerInput(name = "format", type = String.class)},
    output={
        @HandlerOutput(name="result", type=String.class)})

    public static void convertDateTimeFormat(HandlerContext handlerCtx) {
        String dateTime = (String)handlerCtx.getInputValue("dateTime");
        String result = "";
        if (!GuiUtil.isEmpty(dateTime)) {
            try {
                long longValue = Long.parseLong(dateTime);
                String format = (String)handlerCtx.getHandler().getInputValue("format");
                if (format == null) {
                    format = "yyyy-MM-dd HH:mm:ss z";
                }
                result = new SimpleDateFormat(format).format(new Date(longValue));
            } catch (NumberFormatException ex) {
                //ignore
            }
        }
        handlerCtx.setOutputValue("result", result);
    }


    /**
     * <p> This handler allows the "partialRequest" flag to be set.  This
     *     was added to work-a-round a bug in JSF where the behavior was
     *     inconsistent between FF and other browsers.  Namely it recognized
     *     redirects as "partial" requets in other browsers due to the
     *     header being preserved across the redirect, but not in FF.</p>
     */
    @Handler(id="setPartialRequest",
        input={
            @HandlerInput(name="value", type=Boolean.class, required=true)})
    public static void setPartialRequest(HandlerContext context) {
        boolean isPartial = (Boolean) context.getInputValue("value");
        context.getFacesContext().getPartialViewContext().setPartialRequest(isPartial);
    }

    /**
     * <p> This handler is different than JSFT's default navigate handler in
     *     that it forces the request to NOT be a "partial request".  The
     *     effect is that no wrapping of the response will be done.  This is
     *     normally done in JSF2 in order to work with the faces.js JS code
     *     that handles the response.  In the Admin Console, we typically do
     *     not use this JS, so this is not desirable behavior.</p>
     *
     * <p> Input value: "page" -- Type: <code>Object</code> (should be a
     *     <code>String</code> or a <code>UIViewRoot</code>).</p>
     *
     * <p> See JSFTemplating's built-in navigate handler for more info.</p>
     *
     * @param        context        The {@link HandlerContext}.
     */
    @Handler(id="gf.navigate",
        input={
            @HandlerInput(name="page", type=Object.class, required=true)
        })
    public static void navigate(HandlerContext context) {
        context.getFacesContext().getPartialViewContext().setPartialRequest(false);
        NavigationHandlers.navigate(context);
    }

    /**
     * <p> This handler redirects to the given page.</p>
     *
     * <p> Input value: "page" -- Type: <code>String</code></p>
     *
     * @param        context        The {@link HandlerContext}.
     */
    @Handler(id="gf.redirect",
        input={
            @HandlerInput(name="page", type=String.class, required=true)
        })
    public static void redirect(HandlerContext context) {
        String page = (String) context.getInputValue("page");
        FacesContext ctx = context.getFacesContext();
        page = handleBareAttribute(ctx, page);
        //if (ctx.getPartialViewContext().isPartialRequest()) {
            // FIXME: I should be able to call setPartialRequest(false),
            // FIXME: however, isAjaxRequest will still return true, and the
            // FIXME: following line will not work correctly (it'll wrap it in
            // FIXME: <xml> stuff and send it to the client):
            // FIXME:   ctx.getExternalContext().redirect(page);
            // FIXME: Work-a-round: call servlet api's directly
        //}
        try {
            // FIXME: Should be: ctx.getExternalContext().redirect(page);  See FIXME above.
            ((HttpServletResponse) ctx.getExternalContext().getResponse()).sendRedirect(page);
        } catch (IOException ex) {
            throw new RuntimeException(
                "Unable to redirect to page '" + page + "'!", ex);
        }
        ctx.responseComplete();
    }

    @Handler(id = "gf.filterTable",
        input = {
            @HandlerInput(name = "table", type = java.util.List.class, required = true),
            @HandlerInput(name = "key", type = java.lang.String.class, required = true),
            @HandlerInput(name = "value", type = java.lang.String.class, required = true),
            @HandlerInput(name = "keep", type = java.lang.Boolean.class, defaultValue="true")
        },
        output = {
            @HandlerOutput(name = "table", type = java.util.List.class)
    })
    public static void filterTable(HandlerContext handlerCtx) {
        List<Map> table = (List) handlerCtx.getInputValue("table");
        String key = (String) handlerCtx.getInputValue("key");
        String value = (String) handlerCtx.getInputValue("value");
        Boolean keep = (Boolean) handlerCtx.getInputValue("keep");
        if ((key == null) || ("".equals(key))) {
            GuiUtil.getLogger().info("'attr' must be non-null, and non-blank");
        }
        if ((value == null) || ("".equals(value))) {
            GuiUtil.getLogger().info("'value' must be non-null, and non-blank");
        }
        if (keep == null) {
            keep = Boolean.TRUE;
        }
        List<Map> results = new java.util.ArrayList<>();

        // If we're stripping keys we don't want, prep the results table with all of the
        // current values.  Those we don't want will be removed later.
        if (!keep) {
            results.addAll(table);
        }

        // Concurrent acces problems?
        for (Map child : table) {
            if (value.equals(child.get(key))) {
                if (keep) {
                    results.add(child);
                } else {
                    results.remove(child);
                }
            }
        }

        handlerCtx.setOutputValue("table", results);
    }

    @Handler(id = "gf.filterMap",
        input = {
            @HandlerInput(name = "map", type = java.util.Map.class, required = true),
            @HandlerInput(name = "attrNames", type = java.util.List.class, required = true),
            @HandlerInput(name = "keep", type = java.lang.Boolean.class, defaultValue="true")
        },
        output = {
            @HandlerOutput(name = "resultMap", type = java.util.Map.class)
    })
    public static void filterMap(HandlerContext handlerCtx) {
        Map<String, String> map = (Map<String, String>) handlerCtx.getInputValue("map");
        List<String> attrNames = (List<String>) handlerCtx.getInputValue("attrNames");
        Boolean keep = (Boolean) handlerCtx.getInputValue("keep");
        Map<String, String> resultMap = new HashMap<>();
        if (map != null) {
            if (keep == null) {
                keep = Boolean.TRUE;
            }
            if (attrNames == null) {
                resultMap = map;
            } else {

                for(Map.Entry<String,String> e : map.entrySet()){
                    String key = e.getKey();
                    if (attrNames.contains(key) && keep) {
                        resultMap.put(key, e.getValue());
                    } else if ((!attrNames.contains(key)) && (!keep)) {
                        resultMap.put(key, e.getValue());
                    }
                }
            }
        }

        handlerCtx.setOutputValue("resultMap", resultMap);
    }


    @Handler(id = "gf.isAIX",
        output = {
            @HandlerOutput(name = "result", type =Boolean.class)
    })
    public static void isAIX(HandlerContext handlerCtx) {
        Boolean isAIX = AIX.equalsIgnoreCase(System.getProperty("os.name"));
        handlerCtx.setOutputValue("result", isAIX);
    }

    /**
      *  <p> This handler filters out not required protocols from the list of protocols available
      */
    @Handler( id="filterProtocols")
    public static List filterProtocols(HandlerContext context) {
        FilterTreeEvent event = FilterTreeEvent.class.cast(context.getEventObject());
        List protocols = event.getChildObjects();
        ArrayList result = new ArrayList();

        if(protocols != null && protocols.size() > 0){
           for (Object protocol2 : protocols) {
               String protocol = (String) protocol2;
               if (!(protocol.equals(ServerTags.PORT_UNIF_PROTOCOL_NAME) || protocol.equals(ServerTags.REDIRECT_PROTOCOL_NAME))) {
                    result.add(protocol);
               }
           }
        }
        return result;
    }

    /**
     *  <p> This handler filters out AdminObjects from a list-jms-resources, only return Connection Factories.
     */
    @Handler( id="filterAdminObjects")
    public static List filterAdminObjects(HandlerContext context) {
        List result = new ArrayList();
        FilterTreeEvent event = null;
        try{
            if (context.getEventObject() instanceof FilterTreeEvent){
                event = FilterTreeEvent.class.cast(context.getEventObject());
            }else{
                return result;
            }
            List<String> jmsResources = event.getChildObjects();
            if (jmsResources == null || jmsResources.size() <=0){
                return result;
            }
            List adminObjs = new ArrayList();
            Map responseMap = RestUtil.restRequest(GuiUtil.getSessionValue("REST_URL") +"/resources/admin-object-resource" , null, "GET", null, false);
            Map<String, Object> extraPropsMap = (Map<String, Object>) ((Map<String, Object>) responseMap.get("data")).get("extraProperties");
            if ( extraPropsMap != null) {
                Map<String, Object> childRes = (Map<String, Object> )extraPropsMap.get("childResources");
                if (childRes != null){
                    adminObjs = new ArrayList(childRes.keySet());
                }
            }
            for(String oneJms: jmsResources){
                if (!adminObjs.contains(oneJms)){
                    result.add(oneJms);
                }
            }
        }catch(Exception ex){
            //shouldn't happen.  But weill log in and return empty list.
            GuiUtil.getLogger().warning("Exception in filterAdminObjects()");
        }
        return result;
    }

    @Handler(id = "getDefaultAdminTimeout",
            output = {
                @HandlerOutput(name = "result", type = String.class)
            })
    public static void getDefaultAdminTimeout(HandlerContext handlerCtx) {
        String result = SystemPropertyConstants.getDefaultAdminTimeout().toString();
        handlerCtx.setOutputValue("result", result);
    }

    /**
     * If the bare attribute is found in the query string and the value is "true",
     * then add "bare=true" to the specified url string.
     * @param url
     * @return
     */
    private static String handleBareAttribute(FacesContext ctx, String url) {
        // Get Page Session...
        UIViewRoot root = ctx.getViewRoot();
        Map<String, Serializable> pageSession = PageSessionResolver.getPageSession(ctx, root);
        if (pageSession == null) {
            pageSession = PageSessionResolver.createPageSession(ctx, root);
        }
        String request = ctx.getExternalContext().getRequestParameterMap().get("bare");
        if (request != null) {
            // It was specified, use this.
            if (request.equalsIgnoreCase("true")) {
                url = addQueryStringParam(url, "bare", "true");
                request = "true";
            } else {
                request = "false";
            }
            pageSession.put("bare", request);
        } else {
            // Get the Page Session Map
            Object pageSessionValue = pageSession.get("bare");
            if (Boolean.TRUE.equals(pageSessionValue)) {
                url = addQueryStringParam(url, "bare", "true");
            } else {
                pageSession.put("bare", "false");
            }
        }
        return url;
    }

    /**
     * Add the name/value pair to the given url.
     * @param url
     * @param name
     * @param value
     * @return
     */
    private static String addQueryStringParam(String url, String name, String value) {
        String sep = "?";
        // If a query string exists (i.e., the url already has "?foo=bar", then we
        // want to append to that string rather than starting a new one
        if (url.indexOf("?") > -1) {
            sep = "&";
        }
        String insert = sep + name + "=" + value; // TODO: HTML encode this

        // Should the url have a hash in it, we need the query string (addition) to
        // be inserted before that.
        int hash = url.indexOf("#");
        if (hash > -1) {
            url = url.substring(0, hash-1) + insert + url.substring(hash);
        } else {
            url = url + insert;
        }
        return url;
    }

    private static final int INDEX=0;

}
