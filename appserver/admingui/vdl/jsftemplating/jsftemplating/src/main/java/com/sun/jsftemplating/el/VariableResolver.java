/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2006, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.el;

import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutComposition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.util.LogUtil;
import com.sun.jsftemplating.util.MessageUtil;
import com.sun.jsftemplating.util.Util;

import jakarta.el.ExpressionFactory;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * <p>
 * VariableResolver is used to parse expressions of the format.
 * </p>
 *
 * <p>
 * <dd>$&lt;type&gt;{&lt;key&gt;}</dd>
 * </p>
 *
 * <p>
 * &lt;type&gt; refers to a registerd {@link VariableResolver.DataSource}, custom {@link VariableResolver.DataSource}s
 * can be registered via: {@link #setDataSource(FacesContext ctx, String key, VariableResolver.DataSource dataSource)}.
 * However, there are many built-in {@link VariableResolver.DataSource} types that are pre-registered.
 * </p>
 *
 * <p>
 * Below are the pre-registered types:
 * </p>
 *
 * <ul>
 * <li>{@link #ATTRIBUTE} -- {@link AttributeDataSource}</li>
 * <li>{@link #APPLICATION} -- {@link ApplicationDataSource}</li>
 * <li>{@link #BOOLEAN} -- {@link BooleanDataSource}</li>
 * <li>{@link #CONSTANT} -- {@link ConstantDataSource}</li>
 * <li>{@link #COPY_PROPERTY} -- {@link CopyPropertyDataSource}</li>
 * <li>{@link #ESCAPE} -- {@link EscapeDataSource}</li>
 * <li>{@link #EVAL} -- {@link EvalDataSource}</li>
 * <li>{@link #HAS_FACET} -- {@link HasFacetDataSource}</li>
 * <li>{@link #HAS_PROPERTY} -- {@link HasPropertyDataSource}</li>
 * <li>{@link #INT} -- {@link IntDataSource}</li>
 * <li>{@link #METHOD_BINDING} -- {@link MethodBindingDataSource}</li>
 * <li>{@link #METHOD_EXPRESSION} -- {@link MethodExpressionDataSource}</li>
 * <li>{@link #OPTION} -- {@link OptionDataSource}</li>
 * <li>{@link #PAGE_SESSION} -- {@link PageSessionDataSource}</li>
 * <li>{@link #PROPERTY} -- {@link PropertyDataSource}</li>
 * <li>{@link #REQUEST_PARAMETER} -- {@link RequestParameterDataSource}</li>
 * <li>{@link #RESOURCE} -- {@link ResourceBundleDataSource}</li>
 * <li>{@link #SESSION} -- {@link SessionDataSource}</li>
 * <li>{@link #STACK_TRACE} -- {@link StackTraceDataSource}</li>
 * <li>{@link #THIS} -- {@link ThisDataSource}</li>
 * </ul>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class VariableResolver {

    /**
     * <p>
     * Defines "attribute" in $attribute{...}. This allows you to retrieve an HttpRequest attribute.
     * </p>
     */
    public static final String ATTRIBUTE = "attribute";

    /**
     * <p>
     * Defines "application" in $application{...}. This allows you to retrieve an application-scoped attribute.
     * </p>
     */
    public static final String APPLICATION = "application";

    /**
     * <p>
     * Defines "copyProperty" in $copyProperty{...}. This allows you to copy a property from the current UIComponent (or
     * search for the property to copy by passing in "propName,true".
     * </p>
     */
    public static final String COPY_PROPERTY = "copyProperty";

    /**
     * <p>
     * Defines "option" in $option{...}. This allows you to obtain an "option" that is defined in a {@link LayoutComponent}.
     * </p>
     */
    public static final String OPTION = "option";

    /**
     * <p>
     * Defines "pageSession" in $pageSession{...}. This allows you to retrieve a PageSession attribute.
     * </p>
     */
    public static final String PAGE_SESSION = "pageSession";

    /**
     * <p>
     * Defines "property" in $property{...}. This allows you to retrieve a property from the UIComponent.
     * </p>
     */
    public static final String PROPERTY = "property";

    /**
     * <p>
     * Defines "hasProperty" in $hasProperty{...}. This allows you to see if a property from the UIComponent exists.
     * </p>
     */
    public static final String HAS_PROPERTY = "hasProperty";

    /**
     * <p>
     * Defines "hasFacet" in $hasFacet{...}. This allows you to see if a facet from the UIComponent exists.
     * </p>
     */
    public static final String HAS_FACET = "hasFacet";

    /**
     * <p>
     * Defines "session" in $session{...}. This allows you to retrieve an HttpSession attribute.
     */
    public static final String SESSION = "session";

    /**
     * <p>
     * Defines "stackTrace" in $stackTrace{...}. This allows you to get a stack trace from the current <code>Thread</code>.
     */
    public static final String STACK_TRACE = "stackTrace";

    /**
     * <p>
     * Defines "requestParameter" in $requestParameter{...}. This allows you to retrieve a HttpRequest parameter
     * (QUERY_STRING parameter).
     * </p>
     */
    public static final String REQUEST_PARAMETER = "requestParameter";

    /**
     * <p>
     * Defines "display" in $display{...}. This allows you to retrive a DisplayField value.
     * </p>
     * public static final String DISPLAY = "display";
     */

    /**
     * <p>
     * Defines "this" in $this{...}. This allows you to retrieve a number of different objects related to the relative
     * placement of this expression.
     * </p>
     *
     * @see ThisDataSource
     */
    public static final String THIS = "this";

    /**
     * <p>
     * Defines "escape" in $escape{...}. This allows some reserved characters to be escaped in "if" attributes. Such as '='
     * or '|'.
     * </p>
     */
    public static final String ESCAPE = "escape";

    /**
     * <p>
     * Defines "eval" in $eval{...}. This allows a boolean expression to be evaulated.
     * </p>
     */
    public static final String EVAL = "eval";

    /**
     * <p>
     * Defines "boolean" in $boolean{...}. This converts the given String to a Boolean.
     * </p>
     */
    public static final String BOOLEAN = "boolean";

    /**
     * <p>
     * Defines "browser" in $browser{...}. This checks properties of the browser that sent the request.
     * </p>
     */
    public static final String BROWSER = "browser";

    /**
     * <p>
     * Defines "int" in $int{...}. This converts the given String to an Integer.
     * </p>
     */
    public static final String INT = "int";

    /**
     * <p>
     * Defines "methodBinding" in $methodBinding{...}. This allows MethodBindings to be created.
     * </p>
     */
    public static final String METHOD_BINDING = "methodBinding";

    /**
     * <p>
     * Defines "methodExpression" in $methodExpression{...}. This allows MethodExpressions to be created.
     * </p>
     */
    public static final String METHOD_EXPRESSION = "methodExpression";

    /**
     * <p>
     * Defines "constant" in $constant{...}. This allows constants in java classes to be accessed.
     * </p>
     */
    public static final String CONSTANT = "constant";

    /**
     * <p>
     * Defines "resource" in $resource{...}. This allows resource to be accessed.
     * </p>
     */
    public static final String RESOURCE = "resource";

    /**
     * Constant defining the arguments required for a Action MethodBinding.
     */
    private static final Class[] ACTION_ARGS = { ActionEvent.class };

    /**
     * Empty <code>Class[]</code> for methods that take no arguments.
     */
    private static final Class[] EMPTY_CLASS_ARRAY = {};

    /**
     * <p>
     * Application scope key to hold the VariableResolver DataSources.
     * </p>
     */
    public static final String VR_APP_KEY = "__jsft_vrds_map";

    /**
     * Escape character.
     */
    public static final char ESCAPE_CHAR = '\\';

    /**
     * The '$' character marks the beginning of a substituion in a String.
     */
    public static final String SUB_START = "$";

    /**
     * The '(' character marks the beginning of the data content of a String substitution.
     */
    public static final String SUB_TYPE_DELIM = "{";

    /**
     * The ')' character marks the end of the data content for a String substitution.
     */
    public static final String SUB_END = "}";

    /**
     * <p>
     * This method will substitute variables into the given String, or return the variable if the substitution is the whole
     * String. This method looks for the LAST occurance of startToken in the given String. It then searches from that
     * location (if found) to the first occurance of typeDelim. The value inbetween is used as the type of substitution to
     * perform (i.e. request attribute, session, etc.). It next looks for the next occurance of endToken. The value
     * inbetween is used as the key passed to the {@link VariableResolver.DataSource} specified by the type. The String
     * value from the {@link VariableResolver.DataSource} replaces the portion of the String from the startToken to the
     * endToken. If this is the entire String, the Object is returned instead of the String value. This process is repeated
     * until no more substitutions are * needed.
     * </p>
     *
     * <p>
     * This algorithm will accomodate nested variables (e.g. "${A{$x}}"). It also allows the replacement value itself to
     * contain variables. Care should be taken to ensure that the replacement String included does not directly or
     * indirectly refer to itself -- this will cause an infinite loop.
     * </p>
     *
     * <p>
     * There is one special case where the string to be evaluated begins with the startToken and ends with the endToken. In
     * this case, string substitution is NOT performed. Instead the value of the request attribute is returned.
     * </p>
     *
     * <p>
     * This method has a "hack" attached at the end of its processing which looks at the resulting value and does magic. If
     * the value is a String that starts with "#{" then it will attempt to locate a composition parameter matching the next
     * token value and replace or merge its content with the value. It will replace the value if the value is in the format
     * #{key}. If there is additonal information after "key", it will attempt to merge value from the template parameter
     * with the content after "key". A default value may be provided for the template parameter by providing a ",default"
     * (e.g. #{key,default}.
     * </p>
     *
     * @param ctx The FacesContext
     * @param desc The closest LayoutElement to this string
     * @param component The assoicated UIComponent
     * @param string The string to be evaluated.
     * @param startToken Marks the beginning "$"
     * @param typeDelim Marks separation of type/variable "{"
     * @param endToken Marks the end of the variable "}"
     *
     * @return The new string with substitutions, or the specified request attribute value.
     */
    public static Object resolveVariables(FacesContext ctx, LayoutElement desc, UIComponent component, String string, String startToken,
            String typeDelim, String endToken) {

        int stringLen = string.length();
        int delimIndex;
        int endIndex;
        int parenSemi;
        int startTokenLen = startToken.length();
        int delimLen = typeDelim.length();
        int endTokenLen = endToken.length();
        boolean expressionIsWholeString = false;
        char firstEndChar = SUB_END.charAt(0);
        char firstDelimChar = SUB_TYPE_DELIM.charAt(0);
        char currChar;
        String type;
        Object variable;

        for (int startIndex = string.lastIndexOf(startToken); startIndex != -1; startIndex = string.lastIndexOf(startToken,
                startIndex - 1)) {

            // Make sure the startToken isn't escaped
            if (startIndex > 0 && string.charAt(startIndex - 1) == ESCAPE_CHAR) {
                string = string.substring(0, startIndex - 1) // Before '\\'
                        + string.substring(startIndex); // After
                stringLen--;
                startIndex--;
                continue;
            }

            // Find first typeDelim
            delimIndex = string.indexOf(typeDelim, startIndex + startTokenLen);
            if (delimIndex == -1) {
                continue;
            }

            // Next find the end token
            parenSemi = 0;
            endIndex = -1;
            // Iterate through the string looking for the matching end
            for (int curr = delimIndex + delimLen; curr < stringLen;) {
                // Get the next char...
                currChar = string.charAt(curr);
                if (currChar == firstDelimChar && typeDelim.equals(string.substring(curr, curr + delimLen))) {
                    // Found the start of another... inc the semi
                    parenSemi++;
                    curr += delimLen;
                    continue;
                }
                if (currChar == firstEndChar && endToken.equals(string.substring(curr, curr + endTokenLen))) {
                    parenSemi--;
                    if (parenSemi < 0) {
                        // Found the right one!
                        endIndex = curr;
                        break;
                    }
                    // Found one, but this isn't the right one
                    curr += endTokenLen;
                    continue;
                }
                curr++;
            }
            if (endIndex == -1) {
                // We didn't find a matching end...
                continue;
            }

            /*
             * // Next find end token endIndex = string.indexOf(endToken, delimIndex+delimLen); matchingIndex =
             * string.lastIndexOf(typeDelim, endIndex); while ((endIndex != -1) && (matchingIndex != delimIndex)) { // We found a
             * endToken, but not the matching one...keep looking endIndex = string.indexOf(endToken, endIndex+endTokenLen);
             * matchingIndex = string.lastIndexOf(typeDelim, matchingIndex-delimLen); } if ((endIndex == -1) || (matchingIndex ==
             * -1)) { continue; }
             */

            // Handle special case where string starts with startToken and ends
            // with endToken (and no replacements inbetween). This is special
            // because we don't want to convert the attribute to a string, we
            // want to return it (this allows Object types).
            if (startIndex == 0 && endIndex == string.lastIndexOf(endToken) && string.endsWith(endToken)) {
                // This is the special case...
                expressionIsWholeString = true;
            }

            // Pull off the type...
            type = string.substring(startIndex + startTokenLen, delimIndex);
            DataSource ds = getDataSource(ctx, type);
            if (ds == null) {
                if (type.indexOf('<') > -1 || type.indexOf('&') > -1 || type.indexOf('[') > -1 || type.indexOf('#') > -1
                        || type.indexOf('$') > -1 || type.indexOf('%') > -1 || type.indexOf('(') > -1 || type.indexOf(')') > -1) {
                    // Do not consider this a valid EL expression, continue...
                    continue;
                }
                throw new IllegalArgumentException("Invalid type '" + type + "' in attribute value: '" + string + "'.");
            }

            // Pull off the variable...
            variable = string.substring(delimIndex + delimLen, endIndex);

            // Get the value...
            variable = ds.getValue(ctx, desc, component, (String) variable);
            if (expressionIsWholeString) {
                if (variable instanceof String) {
                    // See if we need to do EL magic manipulation...
                    variable = replaceCompParams(ctx, desc, component, (String) variable);
                }
                return variable;
            }

            // Make new string
            string = string.substring(0, startIndex) + // Before replacement
                    (variable == null ? "" : variable.toString()) + string.substring(endIndex + endTokenLen); // After
            stringLen = string.length();
        }

        // Return the string
        return replaceCompParams(ctx, desc, component, string);
    }

    /**
     * <p>
     * This method implements a "hack" which manipulates the given String when it starts with "#{". In this case, it will
     * attempt to locate a composition parameter matching the next token and replace or merge its content with this String.
     * It will replace the String if the value is in the format #{key}. If there is additonal content after "key" (besides a
     * (,) comma), it will attempt to merge the value of the composition parameter matching key with the content after
     * "key". A default value may be provided for the template parameter by providing a ",default" at the end of the
     * expression (e.g. #{key,default}.
     * </p>
     *
     * <p>
     * This method does not support replacing template paramters in other locations within EL.
     * </p>
     *
     * @param ctx The <code>FacesContext</code>.
     * @param string The String to evaluate and manipulate if necessary.
     *
     * @return The same String passed in, or a new one based on method description.
     */
    private static Object replaceCompParams(FacesContext ctx, LayoutElement desc, UIComponent comp, String string) {
        // Sanity check
        if (string == null) {
            return null;
        }

        // First see if we have any params
        Map<String, Object> globalParams = LayoutComposition.getGlobalParamMap(ctx);
        if (globalParams.size() == 0) {
            // No mappings, nothing to do
            return string;
        }

        String token = null;
        Object value = null;
        int len, startEL, endEL = 0;
        int loopStart = 0;
        char chars[] = string.toCharArray();
        boolean foundAtLeastOne = false, isWholeString = false;
        StringBuilder buff = null;
        Stack<LayoutElement> stack = null;

        while (true) {
            startEL = findOpenEL(chars, loopStart);
            // Detect
            while (startEL != -1) {
                // Get the next token
// FIXME: This is not nearly adequate!!
// Need to find all real tokens in #{!(some == expression) || foo}
                endEL = findChar(chars, startEL + 2, '}', '[', '.', '=', '>', '<', '!', '&', '|', '*', '+', '-', '?', '/', '%', '(');
                if (endEL == -1) {
                    // Not a match, non-fatal
                    startEL = -1;
                    break;
                }
                token = string.substring(startEL + 2, endEL);
                token = token.trim();

                // Check to see if this is template param
                value = globalParams.get(token);
                if (value != null) {
                    // We're not done yet! This value is only a flag, we have to
                    // look at the composition stack to be accurate!
                    if (stack == null) {
                        stack = LayoutComposition.getCompositionStack(ctx);
                    }
                    value = LayoutComposition.findTemplateParam(stack, token);
                    break;
                }
                startEL = string.indexOf("#{", endEL + 1);
            }

            if (startEL == -1) {
                if (!foundAtLeastOne) {
                    // We didn't find anything to substitute
                    return string;
                }

                // Append the rest of the String
                buff.append(chars, loopStart, chars.length - loopStart);

                // We're done
                break;
            }

            // Only get here when we find one...
            foundAtLeastOne = true;
            if (buff == null) {
                // Initialize the buffer
                buff = new StringBuilder(100);
            }

            // Check to see if this expression starts at the beginning
            isWholeString = startEL == 0;

            // Add everything before the "#{"
            buff.append(chars, loopStart, startEL - loopStart);

            // We got "...#{replaceMe?*", look at the next char to see what to do
            if (chars[endEL] == '}') {
                // Swap everything, no merge
                endEL++; // Move past '}'
                if (isWholeString && endEL >= chars.length) {
                    // Special case, #{} is entire string, return it
                    return resolveVariables(ctx, desc, comp, value);
                }
                isWholeString = false;

                // Ok, we just take the String value of "value" and add it
                if (value != null) {
                    buff.append(value);
                }
            } else {
                // Merge... we're just going to strip off "#{}" from value and
                // insert it for now, later we might support more.
                int start = 0;
                String strVal = value.toString();
                if (strVal.startsWith("#{")) {
                    start = 2;
                }
                int end = strVal.length();
                if (strVal.charAt(end - 1) == '}') {
                    end--;
                }

                // Add merged content...
                buff.append("#{").append(strVal, start, end);

                // Find the end of the rest of the #{}...
                end = findChar(chars, endEL + 1, '}');
                if (end == -1) {
                    throw new IllegalArgumentException("EL has unterminated #{} expression: (" + string + ")");
                }
                buff.append(chars, endEL, ++end - endEL);
                endEL = end;
            }
            loopStart = endEL;
        }

        // Replace ${}, return the result...
        return resolveVariables(ctx, desc, comp, buff.toString());
    }

    /**
     * <p>
     * This looks for the first occurance of "<code>#{</code>" in <code>chars</code>. It returns the index of the starting
     * character, or -1 if not found.
     * </p>
     */
    private static int findOpenEL(char chars[], int idx) {
        // Allow for a minimum of 3 characters after the # (i.e. {x})
        int len = chars.length - 3;
        for (; idx < len; idx++) {
            if (chars[idx] == '#' && chars[idx + 1] == '{') {
                return idx;
            }
        }
        return -1;
    }

    /**
     * <p>
     * This method searches the given <code>chars</code> from the <code>startingIndex</code> for any of the characters in
     * <code>matchChars</code>.
     * </p>
     */
    private static int findChar(char chars[], int idx, char... matchChars) {
        int len = chars.length;
        for (; idx < len; idx++) {
            for (char ch : matchChars) {
                if (chars[idx] == ch) {
                    return idx;
                }
            }
        }

        // Not found
        return -1;
    }

    /**
     * This method replaces the ${..} variables with their values. It will only do this for Strings and List's that contain
     * Strings.
     *
     * @param desc The <code>LayoutElement</code> descriptor
     * @param component The <code>UIComponent</code>
     * @param value The value to resolve
     *
     * @return The result
     */
    public static Object resolveVariables(LayoutElement desc, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        return VariableResolver.resolveVariables(FacesContext.getCurrentInstance(), desc, component, value);
    }

    /**
     * This method replaces the ${..} variables with their attribute values. It will only do this for Strings and List's
     * that contain Strings.
     *
     * @param ctx The <code>FacesContext</code>
     * @param desc The <code>LayoutElement</code> descriptor
     * @param component The <code>UIComponent</code>
     * @param value The value to resolve
     *
     * @return The result
     */
    public static Object resolveVariables(FacesContext ctx, LayoutElement desc, UIComponent component, Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            value = VariableResolver.resolveVariables(ctx, desc, component, (String) value, VariableResolver.SUB_START,
                    VariableResolver.SUB_TYPE_DELIM, VariableResolver.SUB_END);
        } else if (value instanceof List) {
            // Create a new List b/c invalid to change shared List
            List<Object> list = (List<Object>) value;
            List<Object> newList = new ArrayList<>(list.size());
            Iterator it = list.iterator();
            while (it.hasNext()) {
                newList.add(VariableResolver.resolveVariables(ctx, desc, component, it.next()));
            }
            return newList;
        } else if (value instanceof Object[]) {
            // Create a new array b/c invalid to change shared array
            Object[] arr = (Object[]) value;
            Object[] newArr = new Object[arr.length];
            int idx = 0;
            for (Object obj : arr) {
                newArr[idx++] = VariableResolver.resolveVariables(ctx, desc, component, obj);
            }
            return newArr;
        }
        return value;
    }

    /**
     * <p>
     * This method looks up the requested {@link VariableResolver.DataSource} by the given key.
     *
     * @param key The key identifying the desired {@link VariableResolver.DataSource}
     *
     * @return The requested {@link VariableResolver.DataSource}
     */
    public static VariableResolver.DataSource getDataSource(FacesContext ctx, String key) {
        // Get the Map... and pull off the value (may be null)
        return VariableResolver.getDataSourceMap(ctx).get(key);
    }

    /**
     * <p>
     * Provides access to the application-scoped Map which stores the {@link VariableResolver#DataSource}'s for this
     * application.
     * </p>
     */
    private static Map<String, VariableResolver.DataSource> getDataSourceMap(FacesContext ctx) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        Map<String, VariableResolver.DataSource> dataSourceMap = null;
        if (ctx != null) {
            dataSourceMap = (Map<String, VariableResolver.DataSource>) ctx.getExternalContext().getApplicationMap().get(VR_APP_KEY);
        }
        if (dataSourceMap == null) {
            // 1st time... initialize it
            dataSourceMap = new HashMap<>();
            AttributeDataSource attrDS = new AttributeDataSource();
            dataSourceMap.put("", attrDS);
            dataSourceMap.put(ATTRIBUTE, attrDS);
            dataSourceMap.put(APPLICATION, new ApplicationDataSource());
            dataSourceMap.put(COPY_PROPERTY, new CopyPropertyDataSource());
            dataSourceMap.put(OPTION, new OptionDataSource());
            dataSourceMap.put(PAGE_SESSION, new PageSessionDataSource());
            dataSourceMap.put(PROPERTY, new PropertyDataSource());
            dataSourceMap.put(HAS_PROPERTY, new HasPropertyDataSource());
            dataSourceMap.put(HAS_FACET, new HasFacetDataSource());
            dataSourceMap.put(SESSION, new SessionDataSource());
            dataSourceMap.put(STACK_TRACE, new StackTraceDataSource());
            dataSourceMap.put(REQUEST_PARAMETER, new RequestParameterDataSource());
//    dataSourceMap.put(DISPLAY, new DisplayFieldDataSource());
            dataSourceMap.put(THIS, new ThisDataSource());
            dataSourceMap.put(ESCAPE, new EscapeDataSource());
            dataSourceMap.put(EVAL, new EvalDataSource());
            dataSourceMap.put(INT, new IntDataSource());
            dataSourceMap.put(BOOLEAN, new BooleanDataSource());
            dataSourceMap.put(CONSTANT, new ConstantDataSource());
            dataSourceMap.put(RESOURCE, new ResourceBundleDataSource());
            dataSourceMap.put(METHOD_BINDING, new MethodBindingDataSource());
            dataSourceMap.put(METHOD_EXPRESSION, new MethodExpressionDataSource());
            if (ctx != null) {
                ctx.getExternalContext().getApplicationMap().put(VR_APP_KEY, dataSourceMap);
            }
        }

        // Return the map...
        return dataSourceMap;
    }

    /**
     * <p>
     * This method sets the given {@link VariableResolver.DataSource} to be used for $[type]{...} when key matches type.
     * </p>
     *
     * @param key The key identifying the {@link VariableResolver.DataSource}
     * @param dataSource The {@link VariableResolver.DataSource}
     */
    public static synchronized void setDataSource(FacesContext ctx, String key, VariableResolver.DataSource dataSource) {
        // Get the Map... and pull off the value (may be null)
        // NOTE: This is not thread safe.... although this is very rare
        VariableResolver.getDataSourceMap(ctx).put(key, dataSource);
    }

    /**
     * <p>
     * This interface defines a String substitution data source. This is used to retrieve values when a
     * $&lt;type&gt;{&lt;data&gt;} is encountered within a parameter value.
     * </p>
     *
     * <p>
     * Implementations of this interface may register themselves statically to extend the capabilities of the ${}
     * substitution mechanism.
     * </p>
     */
    public interface DataSource {
        /**
         * <p>
         * This method should return the resolved value based on the given key and contextual information.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key);
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to Application-scoped attributes. It uses the data portion
     * of the substitution String as a key to the Map.
     * </p>
     */
    public static class ApplicationDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return ctx.getExternalContext().getApplicationMap().get(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to HttpRequest attributes. It uses the data portion of the
     * substitution String as a key to the HttpRequest attribute Map.
     * </p>
     */
    public static class AttributeDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return ctx.getExternalContext().getRequestMap().get(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to "option" values that are set on a
     * {@link LayoutComponent}. It uses the data portion of the substitution String as a key to the
     * {@link LayoutComponent}'s options. If a value is not found, it will walk up the LayoutComponent's parents looking for
     * a defined value.
     * </p>
     */
    public static class OptionDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key. (null) if not found.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            Object value = null;
            while (value == null && desc != null) {
                if (desc instanceof LayoutComponent) {
                    value = ((LayoutComponent) desc).getEvaluatedOption(ctx, key, component);
                }
                desc = desc.getParent();
            }
            return value;
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to PageSession attributes. It uses the data portion of the
     * substitution String as a key to the PageSession attribute Map.
     * </p>
     */
    public static class PageSessionDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            Map<String, Serializable> map = PageSessionResolver.getPageSession(ctx, ctx.getViewRoot());
            Serializable value = null;
            if (map != null) {
                value = map.get(key);
            }
            return value;
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to HttpRequest Parameters. It uses the data portion of the
     * substitution String as a key to the HttpRequest Parameter Map.
     * </p>
     */
    public static class RequestParameterDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return ctx.getExternalContext().getRequestParameterMap().get(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} copies <code>UIComponent</code> properties. It uses the data portion of the
     * substitution String as a key to the UIComponent's properties via the attribute map. If the property is null, it will
     * attempt to look at the parent's properties.
     * </p>
     */
    public static class CopyPropertyDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return findPropertyValue(ctx, desc, component, key, true);
        }

        public Object findPropertyValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key, boolean checkVE) {
            if (component == null) {
                return "";
            }

            // Check to see if we should walk up the tree or not
            int idx = key.indexOf(',');
            boolean walk = false;
            if (idx > 0) {
                walk = Boolean.valueOf(key.substring(idx + 1).trim()).booleanValue();
                key = key.substring(0, idx);
            }

            Object value = null;
            if (checkVE) {
                value = component.getValueExpression(key);
                if (value == null) {
                    value = component.getAttributes().get(key);
                }
            } else {
                value = component.getAttributes().get(key);
            }
            if (walk) {
                while (value == null && component.getParent() != null) {
                    component = component.getParent();
                    if (checkVE) {
                        value = component.getValueExpression(key);
                        if (value == null) {
                            value = component.getAttributes().get(key);
                        }
                    } else {
                        value = component.getAttributes().get(key);
                    }
                }
            }
            /*
             * if (LogUtil.finestEnabled()) { // Trace information LogUtil.finest(this, "RESOLVING ('" + key + "') for ('" +
             * component.getId() + "'): '" + value + "'"); }
             */
            return value;
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to UIComponent Properties. It uses the data portion of the
     * substitution String as a key to the UIComponent's properties via the attribute Map. If the property is null, it will
     * attempt to look at the parent's properties.
     * </p>
     */
    public static class PropertyDataSource extends CopyPropertyDataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return findPropertyValue(ctx, desc, component, key, false);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} tests if the given property exists on the UIComponent. It uses the data
     * portion of the substitution String as a key to the UIComponent's properties via the attribute Map.
     * </p>
     */
    public static class HasPropertyDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            boolean hasKey = component.getAttributes().containsKey(key);
            if (!hasKey) {
                // Check the getter... JSF sucks when wrt attrs vs. props
                if (component.getValueExpression(key) != null || component.getAttributes().get(key) != null) {
                    hasKey = true;
                }
            }
            if (!hasKey && desc instanceof LayoutComponent) {
                // In some cases, the component is a TemplateComponent child
                return getValue(ctx, desc.getParent(), component.getParent(), key);
            }
            return Boolean.valueOf(hasKey);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} tests if the given facet exists on the UIComponent. It uses the data portion
     * of the substitution String as a key to the UIComponent's facets.
     * </p>
     */
    public static class HasFacetDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            boolean hasFacet = component.getFacets().containsKey(key);
            if (!hasFacet && desc instanceof LayoutComponent) {
                // In some cases, the component is a TemplateComponent child
                return getValue(ctx, desc.getParent(), component.getParent(), key);
            }
            return Boolean.valueOf(hasFacet);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} simply returns the key that it is given. This is useful for supplying ${}'s
     * around the string you wish to mark as a string. If not used, characters such as '=' will be interpretted as a
     * separator causing your string to be split -- which can be very undesirable. Mostly useful in "if" statements.
     * </p>
     */
    public static class EscapeDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return key;
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} evaluates the given boolean expression.
     * </p>
     */
    public static class EvalDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            PermissionChecker checker = new PermissionChecker(desc, component, key);
            return Boolean.valueOf(checker.hasPermission());
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} converts the given <code>key</code> to a <code>Boolean</code>. This is
     * needed because JSF does not do this for you. When you call <code>UIComponent.getAttributes().put(key, value)</code>,
     * <code>value</code> is expected to be the correct type. Often <code>Boolean</code> types are needed. This
     * {@link VariableResolver.DataSource} provides a means to supply a <code>Boolean</code> value.
     * </p>
     */
    public static class BooleanDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return Boolean.valueOf(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} converts the given <code>key</code> to an <code>Integer</code>. This is
     * needed because JSF does not do this for you. When you call <code>UIComponent.getAttributes().put(key, value)</code>,
     * <code>value</code> is expected to be the correct type. Often <code>Integer</code> types are needed. This
     * {@link VariableResolver.DataSource} provides a means to supply an <code>Integer</code> value.
     * </p>
     */
    public static class IntDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return Integer.valueOf(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} allows access to constants in java classes. It expects the key to be a fully
     * qualified Java classname plus the variable name. Example:
     * </p>
     *
     * <p>
     * $constant{java.lang.Integer.MAX_VALUE}
     * </p>
     */
    public static class ConstantDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            // First check to see if we've already found the value before.
            Object value = constantMap.get(key);
            if (value == null) {
                // Not found, lets resolve it, duplicate the old Map to avoid
                // sync problems
                Map<String, Object> map = new HashMap<>(constantMap);
                value = resolveValue(map, key);

                // Replace the shared Map w/ this new one.
                constantMap = map;
            }
            return value;
        }

        /**
         * <p>
         * This method resolves key. Key is expected to be in the format:
         * </p>
         *
         * <p>
         * some.package.Class.STATIC_VARIBLE
         * </p>
         *
         * <p>
         * This method will first resolve Class. It will then walk through all its variables adding each static final variable
         * to the Map.
         * </p>
         *
         * @param map The map to add variables to
         * @param key The fully qualified CONSTANT name
         *
         * @return The value of the CONSTANT, or null if not found
         */
        private Object resolveValue(Map<String, Object> map, String key) {
            int lastDot = key.lastIndexOf('.');
            if (lastDot == -1) {
                throw new IllegalArgumentException("Unable to resolve '" + key + "' in $constant{" + key + "}.  '" + key + "' must be a "
                        + "fully qualified classname plus the constant name.");
            }

            // Get the classname / constant name
            String className = key.substring(0, lastDot);

            // Add all constants to the Map
            try {
                addConstants(map, Util.loadClass(className, key));
            } catch (ClassNotFoundException ex) {
                RuntimeException iae = new IllegalArgumentException("'" + className + "' was not found!  This must be a valid "
                        + "classname.  This was found in expression $constant{" + key + "}.");
                iae.initCause(ex);
                throw iae;
            }

            // The constant hopefully is in the Map now, null if not
            return map.get(key);
        }

        /**
         * This method adds all constants in the given class to the Map. The Map key will be the fully qualified class name,
         * plus a '.', plus the constant name.
         *
         * @param map <code>Map</code> to store <code>cls</code>
         * @param cls The <code>Class</code> to store in <code>map</code>
         */
        private void addConstants(Map<String, Object> map, Class cls) {
            // Get the class name
            String className = cls.getName();

            // Get the fields
            Field[] fields = cls.getFields();

            // Add the static final fields to the Map
            Field field = null;
            for (Field field2 : fields) {
                field = field2;
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    try {
                        map.put(className + '.' + field.getName(), field.get(null));
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }

        /**
         * This embedded Map caches constant value lookups. It is static and is shared by all users.
         */
        private static Map<String, Object> constantMap = new HashMap<>();
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} creates a MethodExpression from the supplied key. Example:
     * </p>
     *
     * <p>
     * $methodExpression{#{bean.key}}
     * </p>
     */
    public static class MethodExpressionDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            Class[] args = EMPTY_CLASS_ARRAY;
            key = key.trim();
            int commaIdx = key.lastIndexOf(',');
            if (commaIdx != -1) {
                if (key.endsWith("true")) {
                    args = ACTION_ARGS;
                    key = key.substring(0, commaIdx);
                } else if (key.endsWith("false")) {
                    key = key.substring(0, commaIdx);
                }
            }
            return ctx.getApplication().getExpressionFactory().createMethodExpression(ctx.getELContext(), key, Object.class, args);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} creates a MethodBinding from the supplied key. Example:
     * </p>
     *
     * <p>
     * $methodBinding{#{bean.key}}
     * </p>
     */
    public static class MethodBindingDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            ExpressionFactory factory = ctx.getApplication().getExpressionFactory();
            return factory.createMethodExpression(ctx.getELContext(), key, null, ACTION_ARGS);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} allows access to resource bundle keys. It expects the key to be a resource
     * bundle key plus a '.' then the actual resouce bundle key Example:
     * </p>
     *
     * <p>
     * $resource{bundleID.bundleKey}
     * </p>
     *
     * <p>
     * The bundleID should not contain '.' characters. The bundleKey may.
     * </p>
     */
    public static class ResourceBundleDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            // Get the Request attribute key
            int separator = key.indexOf(".");
            if (separator == -1) {
                throw new IllegalArgumentException("'" + key + "' is not in format: \"[bundleID].[bundleKey]\"!");
            }
            String value = key.substring(0, separator);

            // Get the Resource Bundle
            Object obj = ctx.getExternalContext().getRequestMap().get(value);

            // Make sure we have something...
            if (obj == null) {
                // Only check the request scope b/c the RB is request specific
                // Should we throw an exception? For now return the key
                return key;
            }

            // Make sure its a RB
            if (!(obj instanceof ResourceBundle)) {
                throw new IllegalArgumentException("\"" + value + "\" in: \"" + SUB_START + RESOURCE + SUB_TYPE_DELIM + key + SUB_END
                        + "\" did not resolve to a ResourceBundle!  Found: \"" + obj.getClass().getName() + "\" instead.  (toString(): "
                        + obj.toString() + ")");
            }
            ResourceBundle bundle = (ResourceBundle) obj;

            // Return the result of the ResouceBundle lookup
            String str = null;
            int argSep = key.indexOf(",", separator);
            try {
                if (argSep > -1) {
                    str = bundle.getString(key.substring(separator + 1, argSep));
                } else {
                    str = bundle.getString(key.substring(separator + 1));
                }
                if (str == null) {
                    str = key;
                } else {
                    // Parse arguments
                    if (argSep > -1) {
                        StringTokenizer st = new StringTokenizer(key.substring(argSep), ",");
                        String[] tokens = new String[st.countTokens()];
                        int i = 0;
                        while (st.hasMoreTokens()) {
                            tokens[i++] = st.nextToken().trim();
                        }
                        str = MessageUtil.getFormattedMessage(str, tokens);
                    }
                }
            } catch (MissingResourceException ex) {
                if (LogUtil.configEnabled()) {
                    LogUtil.config("Unable to find key: '" + key.substring(separator + 1) + "' in ResourceBundle '" + value
                            + "'.  Perhaps this needs to be added?", ex);
                } else if (LogUtil.infoEnabled()) {
                    // Info log level, don't be verbose, just display a benign
                    // warning.
                    LogUtil.info("JSFT0003", new Object[] { key.substring(separator + 1), value });
                }
                str = key;
            }

            return str;
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to HttpSession attributes. It uses the data portion of the
     * substitution String as a key to the HttpSession Map.
     * </p>
     */
    public static class SessionDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            return ctx.getExternalContext().getSessionMap().get(key);
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} returns a strack trace (as a String) from the current <code>Thread</code>.
     * The data portion of the "substitution String" is used as a message prefixing the trace.
     * </p>
     */
    public static class StackTraceDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param component The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent component, String key) {
            // Get the trace information
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int len = trace.length;

            // Create a String w/ this info...
            StringBuffer buf = new StringBuffer(key + "\n");
            for (int idx = 0; idx < len; idx++) {
                buf.append(trace[idx] + "\n");
            }

            // Return it.
            return buf.toString();
        }
    }

    /**
     * <p>
     * This {@link VariableResolver.DataSource} provides access to DisplayField values. It uses the data portion of the
     * substitution String as the DisplayField name to find. This is a non-qualified DisplayField name. It will walk up the
     * View tree starting at the View object cooresponding to the LayoutElement which contained this expression. At each
     * ContainerView, it will look for a child with a matching name.
     * </p>
     * public static class DisplayFieldDataSource implements DataSource { public Object getValue(FacesContext ctx,
     * LayoutElement desc, UIComponent component, String key) { while (desc != null) { View view = desc.getView(ctx); if
     * (view instanceof ContainerView) { View child = null; //FIXME: use a better way to find if 'key' is a child of 'view'
     * try { child = (((ContainerView)(view)).getChild(key)); } catch (Exception ex) { } if (child != null) { return
     * ((ContainerView) view).getDisplayFieldValue(key); } } desc = desc.getParent(); } return null; } }
     */

    /**
     * <p>
     * This class provides an implementation for the syntax $this{xyz} where xyz can be any of the following.
     * </p>
     *
     * <ul>
     * <li>component -- Current <code>UIComponent</code></li>
     * <li>clientId -- Current <code>UIComponent</code>'s client id</li>
     * <li>id -- Current <code>UIComponent</code>'s id</li>
     * <li>layoutElement -- Current {@link LayoutElement}</li>
     * <li>parent -- Parent <code>UIComponent</code></li>
     * <li>parentId -- Parent <code>UIComponent</code>'s client id</li>
     * <li>parentLayoutElement -- Parent {@link LayoutElement}</li>
     * <li>namingContainer -- Nearest <code>NamingContainer</code></li>
     * <li>valueBinding -- <code>ValueBinding</code> representing the <code>UIComponent</code></li>
     * <li>children -- Current <code>UIComponent</code>'s children</li>
     * </ul>
     */
    public static class ThisDataSource implements DataSource {
        /**
         * <p>
         * See class JavaDoc.
         * </p>
         *
         * @param ctx The <code>FacesContext</code>
         * @param desc The <code>LayoutElement</code>
         * @param comp The <code>UIComponent</code>
         * @param key The key used to obtain information from this <code>DataSource</code>.
         *
         * @return The value resolved from key.
         */
        @Override
        public Object getValue(FacesContext ctx, LayoutElement desc, UIComponent comp, String key) {
            Object value = null;

            if (key.equalsIgnoreCase(CLIENT_ID) || key.length() == 0) {
                value = comp.getClientId(ctx);
            } else if (key.equalsIgnoreCase(ID)) {
                value = comp.getId();
            } else if (key.equalsIgnoreCase(CHILDREN)) {
                value = comp.getChildren();
            } else if (key.equalsIgnoreCase(COMPONENT)) {
                value = comp;
            } else if (key.equalsIgnoreCase(LAYOUT_ELEMENT)) {
                value = desc;
            } else if (key.equalsIgnoreCase(PARENT_ID)) {
                value = comp.getParent().getId();
            } else if (key.equalsIgnoreCase(PARENT_CLIENT_ID)) {
                value = comp.getParent().getClientId(ctx);
            } else if (key.equalsIgnoreCase(PARENT)) {
                value = comp.getParent();
            } else if (key.equalsIgnoreCase(PARENT_LAYOUT_ELEMENT)) {
                value = desc.getParent();
            } else if (key.equalsIgnoreCase(NAMING_CONTAINER)) {
                for (value = comp.getParent(); value != null; value = ((UIComponent) value).getParent()) {
                    if (value instanceof NamingContainer) {
                        break;
                    }
                }
            } else if (key.equalsIgnoreCase(VALUE_BINDING)) {
                // Walk backward up the tree generate the path
                Stack stack = new Stack();
                String id = null;
                // FIXME: b/c of a bug, the old behavior actually returned the
                // FIXME: parent component... the next line is here to persist
                // FIXME: this behavior b/c some code depends on this, fix this
                // FIXME: when you have a chance.
                comp = comp.getParent();
                while (comp != null && !(comp instanceof UIViewRoot)) {
                    id = comp.getId();
                    if (id == null) {
                        // Generate an id based on the clientId
                        id = comp.getClientId(ctx);
                        id = id.substring(id.lastIndexOf(NamingContainer.SEPARATOR_CHAR) + 1);
                    }
                    stack.push(id);
                    comp = comp.getParent();
                }
                StringBuffer buf = new StringBuffer();
                buf.append("view");
                while (!stack.empty()) {
                    buf.append("['" + stack.pop() + "']");
                }
                value = buf.toString();
            } else {
                throw new IllegalArgumentException("'" + key + "' is not valid in $this{" + key + "}.");
            }

            return value;
        }

        /**
         * <p>
         * Defines "children" in $this{children}. Returns the <code>UIComponent</code>'s children.
         * </p>
         */
        public static final String CHILDREN = "children";

        /**
         * <p>
         * Defines "component" in $this{component}. Returns the UIComponent object.
         * </p>
         */
        public static final String COMPONENT = "component";

        /**
         * <p>
         * Defines "clientId" in $this{clientId}. Returns the String representing the client id for the UIComponent.
         * </p>
         */
        public static final String CLIENT_ID = "clientId";

        /**
         * <p>
         * Defines "id" in $this{id}. Returns the String representing the id for the UIComponent.
         * </p>
         */
        public static final String ID = "id";

        /**
         * <p>
         * Defines "layoutElement" in $this{layoutElement}. Returns the LayoutElement.
         * </p>
         */
        public static final String LAYOUT_ELEMENT = "layoutElement";

        /**
         * <p>
         * Defines "parent" in $this{parent}. Returns the parent UIComponent object.
         * </p>
         */
        public static final String PARENT = "parent";

        /**
         * <p>
         * Defines "parentId" in $this{parentId}. Returns the parent UIComponent object's Id.
         * </p>
         */
        public static final String PARENT_ID = "parentId";

        /**
         * <p>
         * Defines "parentClientId" in $this{parentClientId}. Returns the parent UIComponent object's client Id.
         * </p>
         */
        public static final String PARENT_CLIENT_ID = "parentClientId";

        /**
         * <p>
         * Defines "parentLayoutElement" in $this{parentLayoutElement}. Returns the parent LayoutElement.
         * </p>
         */
        public static final String PARENT_LAYOUT_ELEMENT = "parentLayoutElement";

        /**
         * <p>
         * Defines "namingContainer" in $this{namingContainer}. Returns the nearest naming container object (i.e. the form).
         * </p>
         */
        public static final String NAMING_CONTAINER = "namingContainer";

        /**
         * <p>
         * Defines "valueBinding" in $this{valueBinding}. Returns a <code>ValueBinding</code> to this UIComponent.
         * </p>
         */
        public static final String VALUE_BINDING = "valueBinding";
    }

    /**
     * The main function for this class provides some simple test cases.
     *
     * @param args The commandline arguments.
     */
    public static void main(String[] args) {
        String test = null;
        String good = null;

        test = "" + VariableResolver.resolveVariables(null, null, null, "$escape($escape(LayoutElement))", "$", "(", ")");
        good = "LayoutElement";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null, "$escape($escape(EEPersistenceManager))", "$", "(", ")");
        good = "EEPersistenceManager";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null, "$es$cape$escape(EEPersistenceManager))", "$", "(", ")");
        good = "$es$capeEEPersistenceManager)";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null, "$escape($escapeEEP$ersistenceManager))", "$", "(", ")");
        good = "$escapeEEP$ersistenceManager)";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null, "$escape($escape(EEPersistenceManager)))", "$", "(", ")");
        good = "EEPersistenceManager)";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null, "$escape($escape(EEPersistenceManager())", "$", "(", ")");
        good = "$escape(EEPersistenceManager()";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null,
                "$escape($escape($escape(EEPersistenceManager()))==$escape(" + "EEPersistenceManager()))", "$", "(", ")");
        good = "EEPersistenceManager()==EEPersistenceManager()";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        test = "" + VariableResolver.resolveVariables(null, null, null,
                "$escape($escape($escape(EEPersistenceManager()))==$escape(" + "EEPersistenceManager()))", "$", "(", ")");
        good = "EEPersistenceManager()==EEPersistenceManager()";
        System.out.println("Expected Result: '" + good + "'");
        System.out.println("         Result: '" + test + "'");
        if (!test.equals(good)) {
            System.out.println("FAILED!!!!");
        }

        /*
         * for (int x = 0; x < 100000; x++) { System.out.println("" + VariableResolver.resolveVariables( null, null, null,
         * "$escape($escape(EEPers" + x + "istenceManager()))==$escape(" + "EEPersistenceManager())", "$", "(", ")")); }
         */
    }

}
