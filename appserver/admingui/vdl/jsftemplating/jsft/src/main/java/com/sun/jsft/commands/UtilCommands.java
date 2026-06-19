/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2011, 2022 Oracle and/or its affiliates. All rights reserved.
 * Portions Copyright (c) 2011 Ken Paulsen
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
 *  UtilCommands.java
 *
 *  Created March 29, 2011
 *  @author Ken Paulsen kenapaulsen@gmail.com
 */
package com.sun.jsft.commands;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.inject.Named;

import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 * This class contains methods that perform common utility-type functionality.
 * </p>
 *
 * @author Ken Paulsen (kenapaulsen@gmail.com)
 */
@ApplicationScoped
@Named("util")
public class UtilCommands {

    /**
     * <p>
     * Default Constructor.
     * </p>
     */
    public UtilCommands() {
    }

    /**
     * <p>
     * This command prints out the contents of the given <code>UIComponent</code>'s attribute map.
     * </p>
     */
    public void dumpAttributeMap(UIComponent comp) {
        if (comp != null) {
            Map<String, Object> map = comp.getAttributes();
            for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
                Map.Entry me = (Map.Entry) iter.next();
                System.out.println("key=" + me.getKey() + "'" + "value=" + me.getValue());
            }
        } else {
            System.out.println("UIComponent is null");
        }
    }

    /**
     * <p>
     * This method returns an <code>Iterator</code> for the given <code>List</code>. The <code>List</code> input value key
     * is: "list". The output value key for the <code>Iterator</code> is: "iterator".
     * </p>
     *
     * @param context The HandlerContext. @Handler(id="getIterator", input={ @HandlerInput(name="list", type=List.class,
     * required=true)}, output={
     * @HandlerOutput(name="iterator", type=Iterator.class)}) public static void getIterator(HandlerContext context) {
     * List<Object> list = (List<Object>) context.getInputValue("list"); context.setOutputValue("iterator",
     * list.iterator()); }
     */

    /**
     * <p>
     * This method returns a <code>Boolean</code> value representing whether another value exists for the given
     * <code>Iterator</code>. The <code>Iterator</code> input value key is: "iterator". The output value key is "hasNext".
     * </p>
     *
     * @param context The HandlerContext. @Handler(id="iteratorHasNext", input={ @HandlerInput(name="iterator",
     * type=Iterator.class, required=true)}, output={
     * @HandlerOutput(name="hasNext", type=Boolean.class)}) public static void iteratorHasNext(HandlerContext context) {
     * Iterator<Object> it = (Iterator<Object>) context.getInputValue("iterator"); context.setOutputValue("hasNext",
     * Boolean.valueOf(it.hasNext())); }
     */

    /**
     * <p>
     * This method returns the next object in the <code>List</code> that the given <code>Iterator</code> is iterating over.
     * The <code>Iterator</code> input value key is: "iterator". The output value key is "next".
     * </p>
     *
     * @param context The HandlerContext. @Handler(id="iteratorNext", input={ @HandlerInput(name="iterator",
     * type=Iterator.class, required=true)}, output={
     * @HandlerOutput(name="next")}) public static void iteratorNext(HandlerContext context) { Iterator<Object> it =
     * (Iterator<Object>) context.getInputValue("iterator"); context.setOutputValue("next", it.next()); }
     */

    /**
     * <p>
     * This method creates a List. Optionally you may supply "size" to create a List of blank "" values of the specified
     * size. The output value from this command is "result".
     * </p>
     *
     * @param context The HandlerContext @Handler(id="createList", input={ @HandlerInput(name="size", type=Integer.class,
     * required=true)}, output={
     * @HandlerOutput(name="result", type=List.class)}) public static void createList(HandlerContext context) { int size =
     * ((Integer) context.getInputValue("size")).intValue(); List<Object> list = new ArrayList<Object>(size); for (int count
     * = 0; count < size; count++) { list.add(""); } context.setOutputValue("result", list); }
     */

    /**
     * <p>
     * This method creates a <code>Map</code> (<code>HashMap</code>). The output value from this command is "result".
     * </p>
     *
     * @param context The <code>HandlerContext<code> @Handler(id="createMap", output={
     * @HandlerOutput(name="result", type=Map.class)}) public static void createMap(HandlerContext context) { Map<Object,
     * Object> map = new HashMap<Object, Object>(); context.setOutputValue("result", map); }
     */

    /**
     * <p>
     * This method adds a value to a </code>Map</code>. You must supply <code>map</code> to use as well as the
     * <code>key</code> and <code>value</code> to add.
     * </p>
     *
     * @param context The <code>HandlerContext<code> @Handler(id="mapPut", input={ @HandlerInput(name="map", type=Map.class,
     * required=true), @HandlerInput(name="key", type=Object.class, required=true), @HandlerInput(name="value",
     * type=Object.class, required=true)} ) public static void mapPut(HandlerContext context) { Map map = (Map)
     * context.getInputValue("map"); Object key = context.getInputValue("key"); Object value =
     * context.getInputValue("value"); map.put(key, value); }
     */

    /**
     * <p>
     * This command url-encodes the given String. It will return null if null is given and it will use a default encoding of
     * "UTF-8" if no encoding is specified.
     * </p>
     *
     * @param context The HandlerContext. @Handler(id="urlencode", input={ @HandlerInput(name="value", type=String.class,
     * required=true), @HandlerInput(name="encoding", type=String.class) }, output={
     * @HandlerOutput(name="result", type=String.class)}) public static void urlencode(HandlerContext context) { String
     * value = (String) context.getInputValue("value"); String encoding = (String) context.getInputValue("encoding"); if
     * (encoding == null) { encoding = "UTF-8"; } // The value could be null if an EL expression maps to null if (value !=
     * null) { try { value = java.net.URLEncoder.encode(value, encoding); } catch (java.io.UnsupportedEncodingException ex)
     * { throw new IllegalArgumentException(ex); } } context.setOutputValue("result", value); }
     */

    /**
     * <p>
     * This command gets the current system time in milliseconds. It may be used to time things.
     * </p>
     * @Handler(id="getDate", output={
     *
     * @HandlerOutput(name="time", type=Long.class) }) public static void getDate(HandlerContext context) {
     * context.setOutputValue("time", new java.util.Date().getTime()); }
     */

    /**
     * <p>
     * This method converts '&lt;' and '&gt;' characters into "&amp;lt;" and "&amp;gt;" in an effort to avoid HTML from
     * being processed. This can be used to avoid &lt;script&gt; tags, or to show code examples which might include HTML
     * characters. '&amp;' characters will also be converted to "&amp;amp;".
     * </p>
     * @Handler(id="htmlEscape", input={ @HandlerInput(name="value", type=String.class, required=true) }, output={
     *
     * @HandlerOutput(name="result", type=String.class)}) public static void htmlEscape(HandlerContext context) { String
     * value = (String) context.getInputValue("value"); value = com.sun.jsft.util.Util.htmlEscape(value);
     * context.setOutputValue("result", value); }
     */

    /**
     * <p>
     * A utility command that resembles the for() method in Java. Commands inside the for loop will be executed in a loop.
     * The starting index is specified by <code>start</code>. The index will increase sequentially untill it is equal to
     * <code>end</code>. <code>var</code> will be a request attribute that is set to the current index value as the loop
     * iterates.
     * </p>
     * <p>
     * For example:
     * </p>
     *
     * <code>forLoop(start="1"  end="3" var="foo") {...}</code>
     *
     * <p>
     * The commands inside the {...} will be executed 2 times (with foo=1 and foo=2).
     * </p>
     *
     * <ul>
     * <li><code>start</code> -- type: <code>Integer</code> Starting index, defaults to zero if not specified.</li>
     * <li><code>end</code> -- type: <code>Integer</code>; Ending index. Required.</li>
     * <li><code>var</code> -- type: <code>String</code>; Request attribute to be set in the for loop to the value of the
     * index.</li>
     * </ul>
     * public static boolean forLoop(int start, int end, String var) { List<> commands =
     * handlerCtx.getHandler().getChildHandlers(); if (commands.size() > 0) { // We have child commands in the loop...
     * execute while we iterate Map<String, Object> requestMap = FacesContext.getCurrentInstance().
     * getExternalContext().getRequestMap(); for (int idx=start; idx < end; idx++) { requestMap.put(var, idx); // Ignore
     * what is returned by the commands... we need to return // false anyway to prevent children from being executed again
     * elt.dispatchHandlers(commands); } }
     *
     * // This will prevent the child commands from executing again return false; }
     */
}
