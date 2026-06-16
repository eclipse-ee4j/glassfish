/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * <p>
 * This class evaluates a Boolean equation. The supported functions / operators are:
 * </p>
 *
 * <ul>
 * <li>$&lt;type&gt;{&lt;key&gt;} -- To read a value according to &lt;type&gt; using &lt;key&gt; (See:
 * {@link VariableResolver}). (null is interpretted as false, non boolean values (besides the string "false") are
 * interpretted to mean true)</li>
 * <li>'(' and ')' can be used to override order of operation</li>
 * <li>'!' negate a boolean value</li>
 * <li>'|' logical OR</li>
 * <li>'&amp;' logical AND</li>
 * <li>'=' String equals</li>
 * <li>'&lt;' less than between 2 Integers</li>
 * <li>'&gt;' greater than between 2 Integers</li>
 * <li>'%' modulus of 2 Integers</li>
 * </ul>
 *
 *
 * <p>
 * Operator Precedence (for infix notation) is:
 * </p>
 *
 * <ul>
 * <li>() -- Highest</li>
 * <li>!</li>
 * <li>%</li>
 * <li>&amp;</li>
 * <li>|</li>
 * <li>&lt; and &gt;</li>
 * <li>=</li>
 * </ul>
 *
 * @see VariableResolver
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class PermissionChecker {

    /**
     * <p>
     * This variable represents a "false" BooleanFunction.
     * </p>
     */
    public static final BooleanFunction FALSE_BOOLEAN_FUNCTION = new BooleanFunction(false);

    /**
     * <p>
     * This variable represents a "true" BooleanFunction.
     * </p>
     */
    public static final BooleanFunction TRUE_BOOLEAN_FUNCTION = new BooleanFunction(true);

    protected static final char POST_TRUE = 't';
    protected static final char POST_FALSE = 'f';
    protected static final char POST_TRUE_CAP = 'T';
    protected static final char POST_FALSE_CAP = 'F';

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    // Function representation in postfix String
    public static final char FUNCTION_MARKER = 'F';

    // Operator constants
    public static final char LEFT_PAREN = '(';
    public static final char RIGHT_PAREN = ')';
    public static final char EQUALS_OPERATOR = '=';
    public static final char OR_OPERATOR = '|';
    public static final char AND_OPERATOR = '&';
    public static final char NOT_OPERATOR = '!';
    public static final char LESS_THAN_OPERATOR = '<';
    public static final char MORE_THAN_OPERATOR = '>';
    public static final char MODULUS_OPERATOR = '%';
    public static final char DIVIDE_OPERATOR = '/';

    // The COMMA separates function arguments... not really an operator
    public static final char ARGUMENT_SEPARATOR = ',';

    // Reserved operators, although not currently used...
    /*
     * These may be added eventually
     *
     * public static final char AT_OPERATOR = '@'; public static final char POUND_OPERATOR = '#'; public static final char
     * DOLLAR_OPERATOR = '$'; public static final char UP_OPERATOR = '^'; public static final char STAR_OPERATOR = '*';
     * public static final char TILDA_OPERATOR = '~';
     */

    private static final String PERMISSION_FUNCTIONS = "__jsft_permFuncs";

    /**
     * <p>
     * This holds the infix equation.
     * </p>
     */
    private String _infixStr;

    /**
     * <p>
     * This holds the postfix equation.
     * </p>
     */
    private char[] _postfixArr;

    /**
     * <p>
     * This List holds the actual Function objects that correspond to the 'F' markers in the postfix string.
     * </p>
     */
    private List<Function> _functionList;

    /**
     * <p>
     * This List of functions maintains variableSubstitution Functions which happen out-of-order. They will be pulled from
     * this list as placed into the actual _functionList when the are encountered during the preProcessing.
     * </p>
     */
    private LayoutElement _desc;
    private UIComponent _component;

    /**
     * This is the constructor method that is required to create this object.
     */
    public PermissionChecker(LayoutElement desc, UIComponent component, String infixStr) {
        if (infixStr == null) {
            infixStr = "false";
        }
        setLayoutElement(desc);
        setUIComponent(component);
        setInfix(stripWhiteSpace(infixStr));
    }

    /**
     * <p>
     * This method sets the LayoutElement that is associated with the 'if' check being evaluated. This is not normally
     * needed, it is only needed if the 'if' check contains an expression which requires a LayoutElement to be properly
     * evaluated.
     * </p>
     */
    protected void setUIComponent(UIComponent component) {
        _component = component;
    }

    /**
     * <p>
     * Retreives the LayoutElement associated with this PermissionChecker (only needed in cases where a expression requires
     * a LayoutElement for evaluation).
     * </p>
     */
    public UIComponent getUIComponent() {
        return _component;
    }

    /**
     * <p>
     * This method sets the LayoutElement that is associated with the 'if' check being evaluated. This is not normally
     * needed, it is only needed if the 'if' check contains an expression which requires a LayoutElement to be properly
     * evaluated.
     * </p>
     */
    protected void setLayoutElement(LayoutElement desc) {
        _desc = desc;
    }

    /**
     * <p>
     * Retreives the LayoutElement associated with this PermissionChecker (only needed in cases where a expression requires
     * a LayoutElement for evaluation).
     * </p>
     */
    public LayoutElement getLayoutElement() {
        return _desc;
    }

    /**
     * <p>
     * This method returns the precedence of the given operator. This only applies to infix notation (of course) and is
     * needed to correctly order the operators when converting to postfix.
     * </p>
     *
     * <ul>
     * <li>! (not) has the highest precedence</li>
     * <li>% (modulus)</li>
     * <li>&amp; (and)</li>
     * <li>| (or)</li>
     * <li>&lt; (less than) and &gt; (greater than)</li>
     * <li>= (equals)</li>
     * </ul>
     *
     * <p>
     * Of course '(' and ')' can be used to override the order of operations in infix notation.
     * </p>
     *
     * @param op The operator to evaluate.
     *
     * @return A number that can be used to compare its precedence.
     */
    private static int getPrecedence(char op) {
        switch (op) {
        case LEFT_PAREN:
            return 1;
        case RIGHT_PAREN:
            return 999;
        case EQUALS_OPERATOR:
            return 2;
        case LESS_THAN_OPERATOR:
        case MORE_THAN_OPERATOR:
            return 4;
        case OR_OPERATOR:
            return 8;
        case AND_OPERATOR:
            return 16;
        case MODULUS_OPERATOR:
        case DIVIDE_OPERATOR:
            return 32;
        case NOT_OPERATOR:
            return 64;
        }
        return 1;
    }

    /**
     * <p>
     * This method replaces all "true" / "false" strings w/ 't'/'f'. It converts the String into a char[]. It replaces all
     * user defined functions w/ 'F' and places a Function in a list per the registered user-defined function. All other
     * strings are converted to an 'F' and a StringFunction is added to the function list.
     * </p>
     */
    protected char[] preProcessString(String source) {
        char[] arr = source.toCharArray();
        int sourceLen = arr.length;
        int destLen = 0;

        // Loop through the String, char by char
        for (int idx = 0; idx < sourceLen; idx++) {
            switch (arr[idx]) {
            case POST_TRUE:
            case POST_TRUE_CAP:
                if (idx + TRUE.length() <= sourceLen && TRUE.equalsIgnoreCase(new String(arr, idx, TRUE.length()))) {
                    arr[destLen++] = POST_TRUE;
                    idx += TRUE.length() - 1;
                } else {
                    idx = storeFunction(arr, idx);
                    arr[destLen++] = FUNCTION_MARKER;
                }
                break;
            case POST_FALSE:
            case POST_FALSE_CAP:
                if (idx + FALSE.length() <= sourceLen && FALSE.equalsIgnoreCase(new String(arr, idx, FALSE.length()))) {
                    arr[destLen++] = POST_FALSE;
                    idx += FALSE.length() - 1;
                } else {
                    idx = storeFunction(arr, idx);
                    arr[destLen++] = FUNCTION_MARKER;
                }
                break;
            case OR_OPERATOR:
            case EQUALS_OPERATOR:
            case LESS_THAN_OPERATOR:
            case MORE_THAN_OPERATOR:
            case MODULUS_OPERATOR:
            case DIVIDE_OPERATOR:
            case AND_OPERATOR:
            case NOT_OPERATOR:
            case LEFT_PAREN:
            case RIGHT_PAREN:
                arr[destLen++] = arr[idx];
                break;
            default:
                idx = storeFunction(arr, idx);
                arr[destLen++] = FUNCTION_MARKER;
            }
        }
        char[] dest = new char[destLen];
        for (int idx = 0; idx < destLen; idx++) {
            dest[idx] = arr[idx];
        }
        return dest;
    }

    /**
     * <p>
     * This method looks at the given char array starting at index and continues until an operator (or end of String) is
     * encountered. It then uses this string to lookup a registered function (if any), it stores that function (with
     * parameters)... or if the function is not found, it registers a "String function" (which always returns true).
     * </p>
     */
    protected int storeFunction(char[] arr, int idx) {
        // Find string...
        int start = idx;
        int len = arr.length;
        while (idx < len && !isOperator(arr[idx])) {
            idx++;
        }

        // Create String...
        String str = new String(arr, start, idx - start);

        // Check to see if it is a registered function...
        Function function = getFunction(str);
        if (function != null) {
            // Find the left paren...
            int left = idx;
            if (left >= len || arr[left] != LEFT_PAREN) {
                throw new RuntimeException("Function '" + str + "' is expected to have a '" + LEFT_PAREN
                        + "' immediately following it.  Equation: '" + new String(arr) + "'.");
            }

            List<String> arguments = new ArrayList<>();

            // Find the right Paren...
            while (++idx < len && arr[idx] != RIGHT_PAREN) {
                if (arr[idx] == ARGUMENT_SEPARATOR) {
                    left++;
                    arguments.add(new String(arr, left, idx - left));
                    left = idx;
                }
            }

            // Make sure we don't have ()
            left++;
            if (idx > left) {
                arguments.add(new String(arr, left, idx - left));
            }

            // Set the arguments...
            function.setArguments(arguments);
        } else {
            // Not a registered function...
            idx--; // In this case, there are no ()'s to consume, backup 1
            /*
             * if ((str.charAt(0) == FUNCTION_MARKER) && (str.length() == 1) && !_tmpFunctionStack.empty()) { // We have a function
             * added during the subtitute() phase function = (Function) _tmpFunctionStack.pop(); } else {
             */
            // Create a StringFunction
            function = new StringFunction(str);
            /*
             * }
             */
        }

        // Add the function to the function list
        _functionList.add(function);

        // Return the number of characters that we consumed...
        return idx;
    }

    /**
     * <p>
     * This returns a <code>Map&lt;String, Class&gt;</code> which represent user-registered functions.
     * </p>
     */
    private static Map<String, Class> getFunctions(FacesContext ctx) {
        Map<String, Class> funcs = null;
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        if (ctx != null) {
            Map<String, Object> appMap = ctx.getExternalContext().getApplicationMap();
            funcs = (Map<String, Class>) appMap.get(PERMISSION_FUNCTIONS);
            if (funcs == null) {
                // Perhaps a SoftReference would be a good idea here?
                funcs = new HashMap<>();
            }
        } else {
            // Not JSF env, create every time...
            funcs = new HashMap<>();
        }
        return funcs;
    }

    /**
     * <p>
     * This sets a <code>Map&lt;String, Class&gt;</code> which represent user-registered functions.
     * </p>
     */
    private static void setFunctions(FacesContext ctx, Map<String, Class> map) {
        if (ctx == null) {
            ctx = FacesContext.getCurrentInstance();
        }
        if (ctx != null) {
            // Only set if we're in a JSF env
            ctx.getExternalContext().getApplicationMap().put(PERMISSION_FUNCTIONS, map);
        }
    }

    /**
     * <p>
     * This method is a factory method for constructing a new function based on the function name passed in. The function
     * must be registered prior to invoking this method.
     * </p>
     */
    protected static Function getFunction(String functionName) {
        // Get the Function class
        Class functionClass = getFunctions(null).get(functionName);
        if (functionClass == null) {
            return null;
        }

        // Create a new instance
        Function function = null;
        try {
            function = (Function) functionClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to instantiate '" + functionClass.getName() + "' for '" + functionName + "'", ex);
        }

        // Return the instance
        return function;
    }

    /**
     * <p>
     * This method allows arbitrary functions to be registered. Function names should only contain letters or numbers, other
     * characters or whitespace may cause problems. No checking is done to ensure this, however.
     * </p>
     *
     * <p>
     * Functions will be expressed in an equation as follows:
     * </p>
     *
     * <ul>
     * <li>function_name(param1,param2)</li>
     * </ul>
     *
     * <p>
     * Function parameters also should only contain alpha-numeric characters.
     * </p>
     *
     * <p>
     * Functions must implement PermissionChecker.Function interface
     * </p>
     */
    public static void registerFunction(String functionName, Class function) {
        // Get a copy of the existing f()'s
        Map<String, Class> newFuncs = new HashMap<>(getFunctions(null));
        if (function == null) {
            // Remove it...
            newFuncs.remove(functionName);
        } else {
            if (!Function.class.isAssignableFrom(function)) {
                throw new RuntimeException("'" + function.getName() + "' must implement '" + Function.class.getName() + "'");
            }
            // Add it...
            newFuncs.put(functionName, function);
        }

        // Save new copy of function Map
        setFunctions(null, newFuncs);
    }

    /**
     * <p>
     * This method returns true if the given character is a valid operator.
     * </p>
     */
    public static boolean isOperator(char ch) {
        switch (ch) {
        case LEFT_PAREN:
        case RIGHT_PAREN:
        case EQUALS_OPERATOR:
        case LESS_THAN_OPERATOR:
        case MORE_THAN_OPERATOR:
        case MODULUS_OPERATOR:
        case DIVIDE_OPERATOR:
        case OR_OPERATOR:
        case AND_OPERATOR:
        case NOT_OPERATOR:
//        case AT_OPERATOR:
//        case POUND_OPERATOR:
//        case DOLLAR_OPERATOR:
//        case UP_OPERATOR:
//        case STAR_OPERATOR:
//        case TILDA_OPERATOR:
//        case ARGUMENT_SEPARATOR:
            return true;
        }
        return false;
    }

    /**
     * <p>
     * This method calculates the postfix representation of the infix equation passed in. It returns the postfix equation as
     * a char[].
     * </p>
     *
     * @param infixStr The infix representation of the equation.
     *
     * @return postfix representation of the equation as a char[] (the f()'s are removed and stored in _functionList).
     */
    protected char[] generatePostfix(String infixStr) {
        // Reset the _functionList
        _functionList = new ArrayList<>();

        // Convert string to our parsable format
        char[] result = preProcessString(infixStr);
//System.out.println("DEBUG: Initial String: '"+infixStr+"'");
//System.out.println("DEBUG: After Pre-process: '"+new String(result)+"'");
        int resultLen = result.length;
        int postIdx = 0;
        int precedence = 0;
        Stack opStack = new Stack();

        // Put f()'s directly into result, push operators into right order
        for (int idx = 0; idx < resultLen; idx++) {
            switch (result[idx]) {
            case FUNCTION_MARKER:
            case POST_TRUE:
            case POST_FALSE:
                result[postIdx++] = result[idx];
                break;
            case LEFT_PAREN:
                opStack.push(new Character(LEFT_PAREN));
                break;
            case RIGHT_PAREN:
                while (!opStack.empty() && ((Character) opStack.peek()).charValue() != LEFT_PAREN) {
                    result[postIdx++] = ((Character) opStack.pop()).charValue();
                }
                if (!opStack.empty()) {
                    // Throw away the LEFT_PAREN that should still be there
                    opStack.pop();
                }
                break;
            default:
                // clear stuff
                precedence = getPrecedence(result[idx]);
                while (!opStack.empty() && getPrecedence(((Character) opStack.peek()).charValue()) >= precedence) {
                    result[postIdx++] = ((Character) opStack.pop()).charValue();
                }

                /* Put it on the stack */
                opStack.push(new Character(result[idx]));
                break;
            }
        }

        // empty the rest of the stack to the result
        while (!opStack.empty()) {
            result[postIdx++] = ((Character) opStack.pop()).charValue();
        }
        // Copy the result to the postfixStr
        char[] postfixStr = new char[postIdx];
        for (int idx = 0; idx < postIdx; idx++) {
            postfixStr[idx] = result[idx];
        }
//System.out.println("DEBUG: Postfix String: '"+new String(postfixStr)+"'");
        return postfixStr;
    }

    /**
     * <p>
     * This method is invoked to determine if the equation evaluates to true or false.
     * </p>
     */
    public boolean hasPermission() {
        char[] postfixArr = getPostfixArr();
        int len = postfixArr.length;
        Stack result = new Stack();
        result.push(FALSE_BOOLEAN_FUNCTION); // Default to false
        boolean val1, val2;
        Iterator<Function> it = _functionList.iterator();

        // Iterate through the postfix array
        for (int idx = 0; idx < len; idx++) {
            switch (postfixArr[idx]) {
            case POST_TRUE:
                result.push(TRUE_BOOLEAN_FUNCTION);
                break;
            case POST_FALSE:
                result.push(FALSE_BOOLEAN_FUNCTION);
                break;
            case FUNCTION_MARKER:
                if (!it.hasNext()) {
                    throw new RuntimeException(
                            "Unable to evaluate: '" + toString() + "' -- found function marker " + "w/o cooresponding function!");
                }
                result.push(it.next());
                break;
            case EQUALS_OPERATOR:
                try {
                    // Allow reg expression matching
                    String matchStr = result.pop().toString();
                    val1 = result.pop().toString().matches(matchStr);
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            case LESS_THAN_OPERATOR:
                try {
                    // The stack reverses the order, so check greater than
                    val1 = Integer.parseInt(result.pop().toString()) > Integer.parseInt(result.pop().toString());
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            case MORE_THAN_OPERATOR:
                try {
                    // The stack reverses the order, so check less than
                    val1 = Integer.parseInt(result.pop().toString()) < Integer.parseInt(result.pop().toString());
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            case MODULUS_OPERATOR:
                try {
                    // The stack reverses the order...
                    int modNumber = Integer.parseInt(result.pop().toString());
                    int num = Integer.parseInt(result.pop().toString());
                    result.push(new StringFunction("" + num % modNumber));
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                break;
            case DIVIDE_OPERATOR:
                try {
                    // The stack reverses the order...
                    int divNumber = Integer.parseInt(result.pop().toString());
                    int num = Integer.parseInt(result.pop().toString());
                    result.push(new StringFunction("" + num / divNumber));
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                break;
            case OR_OPERATOR:
                try {
                    val1 = ((Function) result.pop()).evaluate();
                    val2 = ((Function) result.pop()).evaluate();
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(val1 || val2 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            case AND_OPERATOR:
                try {
                    val1 = ((Function) result.pop()).evaluate();
                    val2 = ((Function) result.pop()).evaluate();
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(val1 && val2 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            case NOT_OPERATOR:
                try {
                    val1 = ((Function) result.pop()).evaluate();
                } catch (EmptyStackException ex) {
                    throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
                }
                result.push(!val1 ? TRUE_BOOLEAN_FUNCTION : FALSE_BOOLEAN_FUNCTION);
                break;
            }
        }

        // Return the only element on the stack (hopefully)
        try {
            val1 = ((Function) result.pop()).evaluate();
        } catch (EmptyStackException ex) {
            throw new RuntimeException("Unable to evaluate: '" + toString() + "'.", ex);
        }
        if (!result.empty()) {
            result.pop(); // We added a false that wasn't needed
            if (!result.empty()) {
                throw new RuntimeException("Unable to evaluate: '" + toString() + "' -- values left on the stack.");
            }
        }
        return val1;
    }

    /**
     * <p>
     * This method returns the infix representation of the equation, in other words: the original String passed in.
     * </p>
     */
    public String getInfix() {
        return _infixStr;
    }

    /**
     * <p>
     * This method sets the equation and forces a re-evaluation of the equation. It returns the postfix representation of
     * the equation.
     * </p>
     *
     * @param equation The infix equation to use.
     *
     */
    public void setInfix(String equation) {
        _infixStr = equation;
        setPostfixArr(generatePostfix(equation));
    }

    /**
     * <p>
     * Getter for the post fix array. If it is currently null, an empty <code>char[]</code> array will be returned.
     * </p>
     */
    protected char[] getPostfixArr() {
        if (_postfixArr == null) {
            _postfixArr = new char[] { ' ' };
        }
        return _postfixArr;
    }

    /**
     * <p>
     * Setter for the postfix array of chars.
     * </p>
     */
    protected void setPostfixArr(char[] postfix) {
        _postfixArr = postfix;
    }

    /**
     * <p>
     * This method provides access to a <code>String</code> representation of the postfix equation held by this
     * <code>Object</code>.
     * </p>
     */
    public String getPostfix() {
        if (getPostfixArr() == null) {
            return "";
        }
        return new String(getPostfixArr());
    }

    /**
     * <p>
     * Displays the infix and postfix version of the equation.
     * </p>
     */
    @Override
    public String toString() {
        return _infixStr + " = " + toString(getPostfixArr());
    }

    /**
     * <p>
     * This toString(...) method generates just the postfix representation of the equation. The postfix notation is stored
     * as a char[] and it has the functions removed from the char[]. This method iterates through the char[] and generates a
     * String with the functions put back into the equation.
     * </p>
     *
     * @param post The char[] representation of the postfix equation.
     */
    private String toString(char[] post) {
        int len = post.length;
        StringBuffer result = new StringBuffer("");
        Iterator<Function> it = _functionList.iterator();

        for (int idx = 0; idx < len; idx++) {
            switch (post[idx]) {
            case POST_TRUE:
                result.append(TRUE);
                break;
            case POST_FALSE:
                result.append(FALSE);
                break;
            case FUNCTION_MARKER:
                result.append(it.next().toString());
                break;
            default:
                result.append(post[idx]);
            }
        }

        return result.toString();
    }

    /**
     * <p>
     * This method removes all whitespace from the given String.
     * </p>
     */
    public static String stripWhiteSpace(String input) {
        char[] arr = input.toCharArray();
        int len = arr.length;
        int destLen = 0;

        // Loop through the array skipping whitespace
        for (int idx = 0; idx < len; idx++) {
            if (Character.isWhitespace(arr[idx])) {
                continue;
            }
            arr[destLen++] = arr[idx];
        }

        // Return the result
        return new String(arr, 0, destLen);
    }

    /**
     * <p>
     * This class must be implemented by all user defined Functions.
     * </p>
     *
     * <p>
     * In addition to these methods, a toString() should be implemented that reconstructs the original format of the
     * function (i.e. function_name(arg1,arg2...)).
     * </p>
     */
    public interface Function {

        /**
         * <p>
         * This method returns the List of arguments.
         * </p>
         */
        List<String> getArguments();

        /**
         * <p>
         * This method is invoked be the PermissionChecker to set the arguments.
         * </p>
         */
        void setArguments(List<String> args);

        /**
         * <p>
         * This method is invoked by the PermissionCheck to evaluate the function to true or false.
         * </p>
         */
        boolean evaluate();
    }

    /**
     * <p>
     * <code>StringFunction</code> implements <code>Function</code> and serves as the default function. This function is
     * special in that it is NEVER registered and is the only function that SHOULD NOT be followed by ()'s. This function
     * will process embedded expressions and evaulate to false if the entire string evaulates to null. Otherwise it will
     * return true. This <code>Function</code> ignores all arguments (arguments only apply if it is registered, which
     * shouldn't be the case anyway).
     * </p>
     */
    protected class StringFunction implements PermissionChecker.Function {

        /**
         * Constructor.
         *
         * @param value The expression to evaluate.
         */
        public StringFunction(String value) {
            _value = value;
        }

        /**
         * Not used.
         */
        @Override
        public List<String> getArguments() {
            return null;
        }

        /**
         * Not used.
         */
        @Override
        public void setArguments(List<String> args) {
        }

        /**
         * <p>
         * Determine if the value of the <code>Function</code> is <code>true</code> or <code>false</code>.
         * </p>
         */
        @Override
        public boolean evaluate() {
            Object obj = getEvaluatedValue();
            if (obj == null) {
                return false;
            }
            obj = obj.toString();
            if (obj.equals("") || ((String) obj).equalsIgnoreCase("false")) {
                return false;
            }
            return true;
        }

        /**
         * <p>
         * This methis uses the {@link VariableResolver} to evaluate the String and returns the result.
         * </p>
         */
        public Object getEvaluatedValue() {
            FacesContext ctx = FacesContext.getCurrentInstance();
            return ComponentUtil.getInstance(ctx).resolveValue(ctx, getLayoutElement(), getUIComponent(), _value);
        }

        /**
         * <p>
         * This implementation of <code>toString()</code> returns the evaluated value, except when the value is
         * <code>(null)</code> in which case it returns the empty string ("").
         * </p>
         */
        @Override
        public String toString() {
            Object obj = getEvaluatedValue();
            if (obj == null) {
                return "";
            }
            return obj.toString();
        }

        private String _value;
    }

    /**
     * <p>
     * <code>BooleanFunction</code> is either <code>true</code> or <code>false</code>. It is used internally by
     * <code>PermissionChecker</code> and is not needed outside <code>PermissionChecker</code> since the Strings "true" or
     * "false" used in an equation are sufficient.
     * </p>
     */
    protected static class BooleanFunction implements PermissionChecker.Function {

        /**
         * <p>
         * Constructor (defaults to false).
         * </p>
         */
        public BooleanFunction() {
        }

        /**
         * <p>
         * Constructor.
         * </p>
         */
        public BooleanFunction(boolean value) {
            _value = value;
        }

        /**
         * <p>
         * Not used.
         * </p>
         */
        @Override
        public List<String> getArguments() {
            return null;
        }

        /**
         * <p>
         * Not used.
         * </p>
         */
        @Override
        public void setArguments(List<String> args) {
        }

        /**
         * <p>
         * Return the value of the <code>Function</code>; <code>true</code> or <code>false</code>.
         * </p>
         */
        @Override
        public boolean evaluate() {
            return _value;
        }

        /**
         * <p>
         * This implementation prints the <code>String</code> "true" or "false" based on the stored value.
         * </p>
         */
        @Override
        public String toString() {
            return _value ? "true" : "false";
        }

        private boolean _value = false;
    }

    /**
     * <p>
     * This is here to provide some test cases. It only tests the conversion to postfix notation.
     * </p>
     */
    public static void main(String[] args) {
        PermissionChecker checker;
        if (args.length > 0) {
            for (String arg : args) {
                checker = new PermissionChecker(null, null, arg);
                System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            }
        } else {
            boolean success = true;
            checker = new PermissionChecker(null, null, "false|false");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("false|false = falsefalse|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "true|false = truefalse|");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "true |false");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("true|false = truefalse|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "true|false = truefalse|");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "true&(false|true)");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("true&(false|true) = truefalsetrue|&")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "true&(false|true) = truefalsetrue|&");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "true&false|true");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("true&false|true = truefalse&true|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "true&false|true = truefalse&true|");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "true&true|false&true");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("true&true|false&true = truetrue&falsetrue&|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "true&true|false&true = truetrue&falsetrue&|");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "!true|false&!(false|true)");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("!true|false&!(false|true) = true!falsefalsetrue|!&|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "!true|false&!(false|true) = true!falsefalsetrue|!&|");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "!(!(true&!true)|!(false|false))|(true|false)&true");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString()
                    .equals("!(!(true&!true)|!(false|false))|(true|false)&true = truetrue!&!falsefalse|!|!truefalse|true&|")) {

                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n"
                        + "!(!(true&!true)|!(false|false))|(true|false)&true = truetrue!&!falsefalse|!|!truefalse|true&|");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            // Test '='
            checker = new PermissionChecker(null, null, "false =false");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("false=false = falsefalse=")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "false=false = falsefalse=");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, " test= me ");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("test=me = testme=")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "test=me = testme=");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, " this should work=thisshouldwork");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("thisshouldwork=thisshouldwork = thisshouldworkthisshouldwork=")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "thisshouldwork=thisshouldwork = thisshouldworkthisshouldwork=");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "false|ab=true");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("false|ab=true = falseab|true=")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "false|ab=true = falseab|true=");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "false|(ab=true)");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("false|(ab=true) = falseabtrue=|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "false|ab=true = falseab|true=");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "false|(ab=ab)");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("false|(ab=ab) = falseabab=|")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "false|ab=true = falseab|true=");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "!");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("! = !")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "! = !");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals(" = ")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + " = ");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "!$escape{}");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("!$escape{} = !")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + "! = !");
                success = false;
            }
            if (!checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            checker = new PermissionChecker(null, null, "$escape{}");
            System.out.println("Output:\n" + checker.toString() + " (" + checker.hasPermission() + ")");
            if (!checker.toString().equals("$escape{} = ")) {
                System.out.println("\tFAILED!");
                System.out.println("Should have been:\n" + " = ");
                success = false;
            }
            if (checker.hasPermission()) {
                System.out.println("\tFAILED!");
                System.out.println("hasPermission(" + checker.toString(checker.getPostfixArr()) + ") returned the wrong result!");
                success = false;
            }

            if (success) {
                System.out.println("\n\tALL TESTS PASSED!");
            } else {
                System.out.println("\n\tNOT ALL TESTS PASSED!");
            }
        }
    }

}
