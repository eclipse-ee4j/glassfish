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

package com.sun.jsftemplating.layout.descriptors.handler;

import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.el.PermissionChecker;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.event.UIComponentHolder;
import com.sun.jsftemplating.util.LogUtil;
import com.sun.jsftemplating.util.TypeConverter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * This class contains the information necessary to invoke a Handler. The {@link HandlerDefinition} class provides a
 * definition of how to invoke a Handler, this class uses that information with in conjuction with information provided
 * in this class to execute the <strong>handler method</strong>. This class typically will hold input values and specify
 * where output should be stored.
 * </p>
 *
 * <p>
 * The <strong>handler method</strong> to be invoked must have the following method signature:
 * </p>
 *
 * <p>
 * <BLOCKQUOTE> <CODE>
 *        public void doSomething(HandlerContext handlerCtx)
 *        </CODE> </BLOCKQUOTE>
 * </p>
 *
 * <p>
 * <code>void</code> be replaced with any type. Depending on the type of event, return values may be handled
 * differently.
 * </p>
 *
 * <p>
 * It is advisable to use Java annotations when defining a <strong>handler method</strong>. See examples of annotations
 * in the <code>com.sun.jsftemplating.handlers package</code>. Here is an example:
 * </p>
 *
 * <p>
 * <BLOCKQUOTE> <CODE>
 *        &#64;Handler(id="abc:doSomething",<br />
 *            input={<br />
 *            &#64;HandlerInput(name="foo", type=Integer.class),<br />
 *            &#64;HandlerInput(name="bar", type=My.class, required=true)<br />
 *            },<br />
 *            output={<br />
 *            &#64;HandlerOutput(name="result", type=String.class)<br />
 *            })<br />
 *        public void doSomething(HandlerContext handlerCtx)
 *        </CODE> </BLOCKQUOTE>
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class Handler implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public Handler(HandlerDefinition handlerDef) {
        setHandlerDefinition(handlerDef);
    }

    /**
     * <p>
     * Accessor for the {@link HandlerDefinition}.
     * </p>
     */
    public HandlerDefinition getHandlerDefinition() {
        return _handlerDef;
    }

    /**
     * <p>
     * This method sets the HandlerDefinition used by this Handler.
     * </p>
     */
    protected void setHandlerDefinition(HandlerDefinition handler) {
        _handlerDef = handler;
    }

    /**
     * <p>
     * This method is invoked by a parser to help populate this <code>Handler</code>. This information is generally static
     * but may contain expressions to make the value dynamic.
     * </p>
     */
    public void setInputValue(String name, Object value) {
        IODescriptor inDesc = getHandlerDefinition().getInputDef(name);
        if (inDesc == null) {
            throw new RuntimeException("Attempted to set input value '" + name + "' with value '" + value + "', however, '" + name
                    + "' is not a declared input parameter in HandlerDefinition '" + getHandlerDefinition().getId() + "'!");
        }
        _inputs.put(name, value);
    }

    /**
     * <p>
     * This method returns a Map of NVPs representing the input to this handler.
     * </p>
     */
    protected Map<String, Object> getInputMap() {
        return _inputs;
    }

    /**
     * <p>
     * This method simply returns the named input value, null if not found. It will not attempt to resolve $...{...}
     * expressions or do modifications of any kind. If you are looking for a method to do these types of operations, try
     * {@link #getInputValue(HandlerContext, String)}.
     * </p>
     *
     * @param name The name used to identify the input value.
     */
    public Object getInputValue(String name) {
        return _inputs.get(name);
    }

    /**
     * <p>
     * This method returns the value for the named input. Input values are not stored in this HandlerContext itself, but in
     * the Handler. If you are trying to set input values for a handler, you must create a new Handler object and set its
     * input values.
     * </p>
     *
     * <p>
     * This method attempts to resolve $...{...} expressions. It also will return the default value if the value is null. If
     * you don't want these things to happen, look at Handler.getInputValue(String).
     * </p>
     *
     * @param name The input name
     *
     * @return The value of the input (null if not found)
     */
    public Object getInputValue(HandlerContext ctx, String name) {
        // Make sure the requested name is valid
        IODescriptor inDesc = getHandlerDefinition().getInputDef(name);
        FacesContext facesCtx = ctx.getFacesContext();
        if (inDesc == null) {
            throw new RuntimeException("Attempted to get input value '" + name + "', however, this is not a declared input "
                    + "parameter in handler definition '" + getHandlerDefinition().getId() + "'!  Check your handler " + " and/or the XML (near LayoutElement '"
                    + ctx.getLayoutElement().getId(facesCtx, null) + "')");
        }

        // Get the value, and parse it
        Object value = getInputValue(name);
        if (value == null) {
            if (inDesc.isRequired()) {
                throw new RuntimeException("'" + name + "' is required for handler '" + getHandlerDefinition().getId() + "'!");
            }
            value = inDesc.getDefault();
        }

        // Resolve any expressions
        EventObject event = ctx.getEventObject();
        UIComponent component = null;
        if (event instanceof UIComponentHolder) {
            component = ((UIComponentHolder) event).getUIComponent();
        } else if (event != null) {
            Object src = event.getSource();
            if (src instanceof UIComponent) {
                component = (UIComponent) src;
            }
        }
        value = ComponentUtil.getInstance(facesCtx).resolveValue(facesCtx, ctx.getLayoutElement(), component, value);

        // Make sure the value is the correct type...
        value = TypeConverter.asType(inDesc.getType(), value);

        return value;
    }

    /**
     * <p>
     * This method retrieves an output value. Output values are stored in the location specified by the {@link OutputType}
     * in the Handler.
     * </p>
     *
     * @param context The HandlerContext
     * @param name The output name
     *
     * @return The value of the output (null if not set)
     */
    public Object getOutputValue(HandlerContext context, String name) {
        // Make sure the requested name is valid
        HandlerDefinition handlerDef = getHandlerDefinition();
        IODescriptor outIODesc = handlerDef.getOutputDef(name);
        if (outIODesc == null) {
            throw new RuntimeException("Attempted to get output value '" + name + "' from handler '" + handlerDef.getId()
                    + "', however, this is not a declared output parameter!  " + "Check your handler and/or the XML.");
        }

        // Get the OutputMapping that describes how to store this output
        OutputMapping outputDesc = getOutputValue(name);

        // NOTE: Interesting that this method does not evaluate the EL in
        // NOTE: getOutputKey... probably a bug. Although it is very uncommon
        // NOTE: (to get a output types output key which is dynamic from a
        // NOTE: handler which just set the output value). It is uncommon to
        // NOTE: use this method at all, let alone for dynamicly key'd
        // NOTE: OutputTypes, so this code path probably hasn't ever been
        // NOTE: executed.
        // Return the value
        return outputDesc.getOutputType().getValue(context, outIODesc, outputDesc.getOutputKey());
    }

    /**
     * <p>
     * This method stores an output value. Output values are stored as specified by the {@link OutputType} in the Handler.
     * This method is not used to create the "mapping" of an output value, for that see
     * {@link #setOutputMapping(String, String, String)}.
     * </p>
     *
     * @param context The HandlerContext
     * @param name The name the Handler uses for the output
     * @param value The value to set
     */
    public void setOutputValue(HandlerContext context, String name, Object value) {
        // Make sure the requested name is valid
        HandlerDefinition handlerDef = getHandlerDefinition();
        IODescriptor outIODesc = handlerDef.getOutputDef(name);
        if (outIODesc == null) {
            throw new RuntimeException("Attempted to set output value '" + name + "' from handler '" + handlerDef.getId()
                    + "', however, this is not a declared output parameter!  " + "Check your handler and/or the XML.");
        }

        // Get the OutputMapping that describes how to store this output
        OutputMapping outputMapping = getOutputValue(name);
        if (outputMapping == null) {
            // They did not Map the output, do nothing...
            return;
        }

        // Make sure the value is the correct type...
        value = TypeConverter.asType(outIODesc.getType(), value);

        // Set the value
        EventObject event = context.getEventObject();
        UIComponent component = null;
        if (event instanceof UIComponentHolder) {
            component = ((UIComponentHolder) event).getUIComponent();
        }

        // Most output types appreciate resolving EL for the "key", however
        // in some cases (such as the EL output type), resolving the key
        // is counterproductive. Check output type to see if the output
        // key should be resolved.
        OutputType outType = outputMapping.getOutputType();
        String outputKey = outputMapping.getOutputKey();
// FIXME: For now I'm going to do instanceof instead of modifying the
// FIXME: OutputType interface.  I can't think of another OutputType that would
// FIXME: want this... if anyone ever has a use case, I'll happily modify the
// FIXME: OutputType interface, or add a new interface to flag this code-path.
        if (!(outType instanceof ELOutputType)) {
            FacesContext ctx = context.getFacesContext();
            outputKey = "" + ComponentUtil.getInstance(ctx).resolveValue(ctx, context.getLayoutElement(), component, outputKey);
        }
        outType.setValue(context, outIODesc, outputKey, value);
    }

    /**
     * <p>
     * This method adds a new OutputMapping to this handler. An OutputMapping allows the handler to return a value and have
     * it "mapped" to the location of your choice. The "outputType" corresponds to a registered {@link OutputType} (see
     * {@link OutputTypeManager}).
     * </p>
     *
     * @param outputName The Handler's name for the output value
     * @param targetKey The 'key' the OutputType uses to store the output
     * @param targetType The OutputType implementation map the output
     */
    public void setOutputMapping(String outputName, String targetKey, String targetType) {
        // Ensure we have a valid outputName (check HandlerDefinition)
        if (getHandlerDefinition().getOutputDef(outputName) == null) {
            throw new IllegalArgumentException(
                    "Handler named '" + getHandlerDefinition().getId() + "' does not declare output " + "mapping named '" + outputName + "'.");
        }

        // Ensure the data is trim
        if (targetKey != null) {
            targetKey = targetKey.trim();
            if (targetKey.length() == 0) {
                targetKey = null;
            }
        }
        targetType = targetType.trim();

        try {
            _outputs.put(outputName, new OutputMapping(outputName, targetKey, targetType));
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Unable to create OutputMapping with given information: " + "outputName='" + outputName + "', targetKey='" + targetKey
                    + "', targetType=" + targetType + "'", ex);
        }
    }

    /**
     * <p>
     * This method returns the {@link OutputMapping} for the given <code>name</code>. If not found, it will return
     * <code>null</code>.
     * </p>
     */
    public OutputMapping getOutputValue(String name) {
        return _outputs.get(name);
    }

    /**
     * <p>
     * This method returns the condition that must be true in order for this <code>Handler</code> (or its child
     * <code>Handler</code>s) to be invoked. If this condition is empty ("") or <code>null</code>, it is considered to be
     * true.
     * </p>
     */
    public String getCondition() {
        return _condition;
    }

    /**
     * <p>
     * This method sets the condition that must evaluate to true in order for this <code>Handler</code> to be invoked.
     * </p>
     */
    public void setCondition(String cond) {
        if (cond != null) {
            cond = cond.trim();
        }
        _condition = cond;
    }

    /**
     * <p>
     * This method determines if the handler is static.
     * </p>
     */
    public boolean isStatic() {
        return getHandlerDefinition().isStatic();
    }

    /**
     * <p>
     * This method adds a <code>Handler</code> to the list of child <code>Handler</code>s. Child <code>Handler</code>s are
     * executed AFTER this <code>Handler</code> is executed.
     * </p>
     *
     * @param desc The <code>Handler</code> to add.
     */
    public void addChildHandler(Handler desc) {
        if (_childHandlers == _emptyList) {
            _childHandlers = new ArrayList();
        }
        _childHandlers.add(desc);
    }

    /**
     * <p>
     * This method sets the <code>List</code> of child <code>Handler</code>s.
     * </p>
     *
     * @param childHandlers The <code>List</code> of child <code>Handler</code>s.
     */
    public void setChildHandlers(List<Handler> childHandlers) {
        if (childHandlers == null || childHandlers.size() == 0) {
            childHandlers = _emptyList;
        }
        _childHandlers = childHandlers;
    }

    /**
     * <p>
     * This method retrieves the <code>List</code> of child {@link Handler}s. This <code>List</code> should not be changed
     * directly. Call {@link #addChildHandler(Handler)}, or make a copy and call {@link #setChildHandlers(List)}.
     * </p>
     *
     * @return The <code>List</code> of child {@link Handler}s.
     */
    public List<Handler> getChildHandlers() {
        return _childHandlers;
    }

    /**
     * <p>
     * This method is responsible for invoking this <code>Handler</code> as well as all child <code>Handler</code>s. Neither
     * will be invoked if this methods condition is non-null and unstatisfied (see {@link #getCondition()}). The method
     * associated with this <code>Handler</code> will be invoked first, then any child <code>Handler</code>s.
     * </p>
     *
     * @param handlerContext The {@link HandlerContext}.
     */
    public Object invoke(HandlerContext handlerContext) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object result = null;
        HandlerDefinition handlerDef = getHandlerDefinition();

        // Invoke
        if (hasPermission(handlerContext)) {
            // Only attempt to do this if there is a handler method, there
            // might only be child handlers
            Method method = handlerDef.getHandlerMethod();
            if (method != null) {
                Object instance = null;
                if (!isStatic()) {
                    // Get the class that contains the method
                    instance = method.getDeclaringClass().newInstance();
                }

                // Invoke the Method
                result = method.invoke(instance, handlerContext);
            }

            // Execute all the child handlers
            if (result == null || !result.toString().equals("false")) {
                // NOTE: 'handler' in handlerContext will change.
                // before we execute this Handler.
                // FIRST: Execute handlerDef child handlers
                List<Handler> handlers = handlerDef.getChildHandlers();
                Object retVal = null;
                LayoutElement elt = handlerContext.getLayoutElement();
                if (handlers.size() > 0) {
                    retVal = elt.dispatchHandlers(handlerContext, handlers);
                    if (retVal != null) {
                        result = retVal;
                    }
                }

                // NEXT: Execute instance child handlers
                // Useful for applying a condition to a group
                handlers = getChildHandlers();
                if (handlers.size() > 0) {
                    retVal = elt.dispatchHandlers(handlerContext, handlers);
                    if (retVal != null) {
                        result = retVal;
                    }
                }
            }
        } else {
            if (LogUtil.finerEnabled()) {
                LogUtil.finer("Handler '" + handlerDef.getId() + "' skipped because condition not met: '" + getCondition() + "'.");
            }
        }

        // Return the result (null if no result)
        return result;
    }

    /**
     * <p>
     * This method determines if the condition (see {@link #getCondition()}) is satisfied.
     * </p>
     *
     * @return true if the condition is met.
     */
    public boolean hasPermission(HandlerContext handlerContext) {
        String cond = getCondition();
        if (cond == null || cond.equals("")) {
            return true;
        }

        // Try to get the UIComponent
        UIComponent comp = null;
        Object obj = handlerContext.getEventObject().getSource();
        if (obj instanceof UIComponent) {
            comp = (UIComponent) obj;
        }

        // Create a PermissionChecker
        PermissionChecker checker = new PermissionChecker(handlerContext.getLayoutElement(), comp, cond);

        // Return the result
        return checker.hasPermission();
    }

    private HandlerDefinition _handlerDef = null;

    private String _condition = null;

    private List<Handler> _childHandlers = _emptyList;

    private Map<String, Object> _inputs = new HashMap<>();
    private Map<String, OutputMapping> _outputs = new HashMap<>();

    private static final List<Handler> _emptyList = new ArrayList<>(0);
}
