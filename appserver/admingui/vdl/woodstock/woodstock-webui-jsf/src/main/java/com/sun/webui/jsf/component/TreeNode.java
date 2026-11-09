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
package com.sun.webui.jsf.component;

import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.event.ToggleActionListener;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.LogUtil;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * The TreeNode component is used to insert a node in a tree structure.
 */
@Component(type = "com.sun.webui.jsf.TreeNode",
        family = "com.sun.webui.jsf.TreeNode",
        displayName = "Tree Node",
        tagName = "treeNode",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_tree_node",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_tree_node_props")
        //CHECKSTYLE:ON
public class TreeNode extends UIComponentBase
        implements NamingContainer, Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -4316201329117848574L;

    /**
     * This Map maps the standard first line image icon to the "special case"
     * one. The special case is when the root is not visible, the icon directly
     * below where the root should be looks different than all others.
     */
    private static final Map<String, String> TOP_LINE_IMAGE_MAPPING =
            new HashMap<String, String>(6);

    static {
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_HANDLE_DOWN_MIDDLE,
                ThemeImages.TREE_HANDLE_DOWN_TOP);
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_HANDLE_DOWN_LAST,
                ThemeImages.TREE_HANDLE_DOWN_TOP_NOSIBLING);
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_HANDLE_RIGHT_MIDDLE,
                ThemeImages.TREE_HANDLE_RIGHT_TOP);
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_HANDLE_RIGHT_LAST,
                ThemeImages.TREE_HANDLE_RIGHT_TOP_NOSIBLING);
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_LINE_MIDDLE_NODE,
                ThemeImages.TREE_LINE_FIRST_NODE);
        TOP_LINE_IMAGE_MAPPING.put(ThemeImages.TREE_LINE_LAST_NODE,
                ThemeImages.TREE_BLANK);
    }

    /**
     * This is the facet key used to set a custom image for this
     * {@code TreeNode}.
     */
    public static final String IMAGE_FACET_KEY = "image";

    /**
     * This is the facet key used to define the content for the
     * {@code TreeNode}.
     */
    public static final String CONTENT_FACET_KEY = "content";

    /**
     * The actionExpression attribute is used to specify the action to take when
     * this component is activated by the user. The value of the action
     * attribute must be one of the following:
     * <ul>
     * <li>an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file {@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     * <p>
     * In the Tree and TreeNode components, the action applies only when
     * attributes are used to define the tree and tree nodes. When facets are
     * used, the action attribute does not apply to the facets.</p>
     */
    @Property(name = "actionExpression",
            displayName = "Hyperlink Action",
            isHidden = true,
            isAttribute = true)
    @Property.Method(signature = "java.lang.String action()")
    private jakarta.el.MethodExpression actionExpression;

    /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when a component is activated by
     * the user. The actionListenerExpression attribute value must be a Unified
     * EL expression that resolves to a method in a backing bean. The method
     * must take a single parameter that is an ActionEvent, and its return type
     * must be {@code void}. The class that defines the method must
     * implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * In the TreeNode component, the method specified by the
     * actionListenerExpression attribute is invoked when the node's handle
     * icon is clicked.</p>
     */
    @Property(name = "actionListenerExpression",
            isHidden = false,
            displayName = "Action Listener Expression",
            category = "Advanced")
    @Property.Method(
            signature = "void processAction(jakarta.faces.event.ActionEvent)")
    private MethodExpression actionListenerExpression;

    /**
     * Set the expanded attribute to true to display the tree node as expanded
     * when the component is initially rendered. When a node is expanded, its
     * child tree nodes are displayed. By default, nodes are collapsed
     * initially.
     */
    @Property(name = "expanded",
            displayName = "Expanded",
            category = "Appearance")
    private boolean expanded;

    /**
     * expanded set flag.
     */
    private boolean expandedSet;

    /**
     * <p>
     * Absolute or relative URL to the image to be rendered for the tree node.
     * Note that you cannot use the imageURL to display a theme image in the
     * tree. You should use an image facet that contains a
     * {@code webuijsf:image} or {@code webuijsf:imageHyperlink} tag to use a
     * theme image. The imageURL attribute is overridden by the {@code image}
     * facet.</p>
     * <p>
     * When the imageURL attribute is used with the URL attribute, the image is
     * hyperlinked.</p>
     */
    @Property(name = "imageURL",
            displayName = "Image URL",
            isHidden = true, isAttribute = true,
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.ImageUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String imageURL;

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "style",
            displayName = "CSS Style(s)",
            category = "Appearance",
            editorClassName = "com.sun.jsfcl.std.css.CssStylePropertyEditor")
    private String style;

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     */
    @Property(name = "styleClass",
            displayName = "CSS Style Class(es)",
            category = "Appearance",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StyleClassPropertyEditor")
            //CHECKSTYLE:ON
    private String styleClass;

    /**
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of the &lt;a&gt; HTML element are also
     * valid for this attribute in the tree components. The target attribute is
     * useful only with the URL attribute, and does not apply when a facet is
     * used.
     */
    @Property(name = "target",
            displayName = "Hyperlink Target",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FrameTargetsEditor")
            //CHECKSTYLE:ON
    private String target;

    /**
     * Specifies the text for this component. If the URL or action attributes
     * are also specified, the text is rendered as a hyperlink. If neither the
     * URL or action attributes are specified, the specified text is rendered as
     * static text. The text attribute does not apply when the content facet is
     * used.
     */
    @Property(name = "text",
            displayName = "Text",
            category = "Appearance",
            isDefault = true,
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String text;

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     */
    @Property(name = "toolTip",
            displayName = "Tool Tip",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String toolTip;

    /**
     * Indicates that the text that is specified with the text attribute should
     * be rendered as a hyperlink that resolves to the specified URL. If the
     * imageURL attribute is used with the URL attribute, the image is
     * hyperlinked. The URL attribute does not apply to facets.
     */
    @Property(name = "url",
            displayName = "Hyperlink Url",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.SunWebUrlPropertyEditor")
            //CHECKSTYLE:ON
    private String url;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     */
    @Property(name = "visible",
            displayName = "Visible",
            category = "Behavior")
    private boolean visible;

    /**
     * visible set flag.
     */
    private boolean visibleSet;

    /**
     * Constructor.
     */
    public TreeNode() {
        super();
        setRendererType("com.sun.webui.jsf.TreeNode");
    }

    /**
     * This implementation returns {@code "com.sun.webui.jsf.TreeNode"}.
     * @return String
     */
    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.TreeNode";
    }

    /**
     * The component identifier for this component. This value must be unique
     * within the closest parent component that is a naming container.
     * This implementation invokes {@code super.setId}.
     * @param id id
     */
    @Property(name = "id")
    @Override
    public void setId(final String id) {
        super.setId(id);
    }

    /**
     * Use the rendered attribute to indicate whether the HTML code for the
     * component should be included in the rendered HTML page. If set to false,
     * the rendered HTML page does not include the HTML for the component. If
     * the component is not rendered, it is also not processed on any subsequent
     * form submission.
     * This implementation invokes {@code super.setRendered}.
     * @param rendered rendered
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * <p>
     * The actionExpression attribute is used to specify the action to take when
     * this component is activated by the user. The value of the action
     * attribute must be one of the following:
     * </p>
     * <ul>
     * <li>an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file {@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     * <p>
     * In the Tree and TreeNode components, the action applies only when
     * attributes are used to define the tree and tree nodes. When facets are
     * used, the action attribute does not apply to the facets.
     * </p>
     * @return {@code jakarta.el.MethodExpression}
     */
    public jakarta.el.MethodExpression getActionExpression() {
        if (this.actionExpression != null) {
            return this.actionExpression;
        }
        ValueExpression vb = getValueExpression("actionExpression");
        if (vb != null) {
            return (jakarta.el.MethodExpression) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * <p>
     * The actionExpression attribute is used to specify the action to take when
     * this component is activated by the user. The value of the action
     * attribute must be one of the following:
     * </p>
     * <ul>
     * <li>an outcome string, used to indicate which page to display next, as
     * defined by a navigation rule in the application configuration resource
     * file {@code (faces-config.xml)}.</li>
     * <li>a JavaServer Faces EL expression that resolves to a backing bean
     * method. The method must take no parameters and return an outcome string.
     * The class that defines the method must implement the
     * {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.</li>
     * </ul>
     * <p>
     * In the Tree and TreeNode components, the action applies only when
     * attributes are used to define the tree and tree nodes. When facets are
     * used, the action attribute does not apply to the facets.
     * </p>
     *
     * @see #getActionExpression()
     * @param newActionExpression actionExpression
     */
    public void setActionExpression(
            final jakarta.el.MethodExpression newActionExpression) {

        this.actionExpression = newActionExpression;
    }

    /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when a component is activated by
     * the user. The actionListenerExpression attribute value must be a Unified
     * EL expression that resolves to a method in a backing bean. The method
     * must take a single parameter that is an ActionEvent, and its return type
     * must be {@code void}. The class that defines the method must
     * implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * In the TreeNode component, the method specified by the
     * actionListenerExpression attribute is invoked when the node's handle
     * icon is clicked.</p>
     * @return MethodExpression
     */
    public MethodExpression getActionListenerExpression() {
        return this.actionListenerExpression;
    }

    /**
     * The actionListenerExpression attribute is used to specify a method to
     * handle an action event that is triggered when a component is activated by
     * the user. The actionListenerExpression attribute value must be a Unified
     * EL expression that resolves to a method in a backing bean. The method
     * must take a single parameter that is an ActionEvent, and its return type
     * must be {@code void}. The class that defines the method must
     * implement the {@code java.io.Serializable} interface or
     * {@code jakarta.faces.component.StateHolder} interface.
     *
     * <p>
     * In the TreeNode component, the method specified by the
     * actionListenerExpression attribute is invoked when the node's handle
     * icon is clicked.</p>
     *
     * @see #getActionListenerExpression()
     * @param newActionListenerExpression actionListenerExpression
     */
    public void setActionListenerExpression(
            final MethodExpression newActionListenerExpression) {

        this.actionListenerExpression = newActionListenerExpression;
    }

    /**
     * Set the expanded attribute to true to display the tree node as expanded
     * when the component is initially rendered. When a node is expanded, its
     * child tree nodes are displayed. By default, nodes are collapsed
     * initially.
     * @return {@code boolean}
     */
    public boolean isExpanded() {
        if (this.expandedSet) {
            return this.expanded;
        }
        ValueExpression vb = getValueExpression("expanded");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return false;
    }

    /**
     * Set the expanded attribute to true to display the tree node as expanded
     * when the component is initially rendered. When a node is expanded, its
     * child tree nodes are displayed. By default, nodes are collapsed
     * initially.
     *
     * @see #isExpanded()
     * @param newExpanded expanded
     */
    public void setExpanded(final boolean newExpanded) {
        this.expanded = newExpanded;
        this.expandedSet = true;
        UIComponent toggleLink = this.getNodeImageHyperlink();
        Map<String, Object> attributes = toggleLink.getAttributes();
        attributes.put("icon", this.getHandleIcon(
                (String) attributes.get("icon")));
    }

    /**
     * Absolute or relative URL to the image to be rendered for the tree node.
     * Note that you cannot use the imageURL to display a theme image in the
     * tree. You should use an image facet that contains a
     * {@code webuijsf:image} or {@code webuijsf:imageHyperlink} tag to use a
     * theme image. The imageURL attribute is overridden by the {@code image}
     * facet.
     * <p>
     * When the imageURL attribute is used with the URL attribute, the image is
     * hyperlinked.</p>
     *
     * @return String
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueExpression vb = getValueExpression("imageURL");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Absolute or relative URL to the image to be rendered for the tree node.
     * Note that you cannot use the imageURL to display a theme image in the
     * tree. You should use an image facet that contains a
     * {@code webuijsf:image} or {@code webuijsf:imageHyperlink} tag to use a
     * theme image. The imageURL attribute is overridden by the {@code image}
     * facet.
     * <p>
     * When the imageURL attribute is used with the URL attribute, the image is
     * hyperlinked.</p>
     *
     * @see #getImageURL()
     * @param newImageURL imageURL
     */
    public void setImageURL(final String newImageURL) {
        this.imageURL = newImageURL;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueExpression vb = getValueExpression("style");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style(s) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyle()
     * @param newStyle style
     */
    public void setStyle(final String newStyle) {
        this.style = newStyle;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     * @return String
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueExpression vb = getValueExpression("styleClass");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * CSS style class(es) to be applied to the outermost HTML element when this
     * component is rendered.
     *
     * @see #getStyleClass()
     * @param newStyleClass styleClass
     */
    public void setStyleClass(final String newStyleClass) {
        this.styleClass = newStyleClass;
    }

    /**
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of the &lt;a&gt; HTML element are also
     * valid for this attribute in the tree components. The target attribute is
     * useful only with the URL attribute, and does not apply when a facet is
     * used.
     * @return String
     */
    public String getTarget() {
        if (this.target != null) {
            return this.target;
        }
        ValueExpression vb = getValueExpression("target");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The resource at the specified URL is displayed in the frame that is
     * specified with the target attribute. Values such as "_blank" that are
     * valid for the target attribute of the &lt;a&gt; HTML element are also
     * valid for this attribute in the tree components. The target attribute is
     * useful only with the URL attribute, and does not apply when a facet is
     * used.
     *
     * @see #getTarget()
     * @param newTarget target
     */
    public void setTarget(final String newTarget) {
        this.target = newTarget;
    }

    /**
     * Specifies the text for this component. If the URL or action attributes
     * are also specified, the text is rendered as a hyperlink. If neither the
     * URL or action attributes are specified, the specified text is rendered as
     * static text. The text attribute does not apply when the content facet is
     * used.
     * @return String
     */
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        ValueExpression vb = getValueExpression("text");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Specifies the text for this component. If the URL or action attributes
     * are also specified, the text is rendered as a hyperlink. If neither the
     * URL or action attributes are specified, the specified text is rendered as
     * static text. The text attribute does not apply when the content facet is
     * used.
     *
     * @see #getText()
     * @param newText text
     */
    public void setText(final String newText) {
        this.text = newText;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     * @return String
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueExpression vb = getValueExpression("toolTip");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Sets the value of the title attribute for the HTML element. The specified
     * text will display as a tool tip if the mouse cursor hovers over the HTML
     * element.
     *
     * @see #getToolTip()
     * @param newToolTip tool tip
     */
    public void setToolTip(final String newToolTip) {
        this.toolTip = newToolTip;
    }

    /**
     * Indicates that the text that is specified with the text attribute should
     * be rendered as a hyperlink that resolves to the specified URL. If the
     * imageURL attribute is used with the URL attribute, the image is
     * hyperlinked. The URL attribute does not apply to facets.
     * @return String
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueExpression vb = getValueExpression("url");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Indicates that the text that is specified with the text attribute should
     * be rendered as a hyperlink that resolves to the specified URL. If the
     * imageURL attribute is used with the URL attribute, the image is
     * hyperlinked. The URL attribute does not apply to facets.
     *
     * @see #getUrl()
     * @param newUrl URL
     */
    public void setUrl(final String newUrl) {
        this.url = newUrl;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     * @return {@code boolean}
     */
    public boolean isVisible() {
        if (this.visibleSet) {
            return this.visible;
        }
        ValueExpression vb = getValueExpression("visible");
        if (vb != null) {
            Object result = vb.getValue(getFacesContext().getELContext());
            if (result == null) {
                return false;
            } else {
                return ((Boolean) result);
            }
        }
        return true;
    }

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page. If set to false, the HTML
     * code for the component is present in the page, but the component is
     * hidden with style attributes. By default, visible is set to true, so HTML
     * for the component HTML is included and visible to the user. If the
     * component is not visible, it can still be processed on subsequent form
     * submissions because the HTML is present.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    /**
     * This implementation invokes {@code super.broadcast} and invokes
     * the action listener.
     * @param event faces event
     * @throws AbortProcessingException if an error occurs
     */
    @Override
    public void broadcast(final FacesEvent event)
            throws AbortProcessingException {

        super.broadcast(event);
        MethodExpression mex = this.getActionListenerExpression();
        boolean expandToggleIcon = true;
        if (mex != null) {
            try {
                if (event instanceof ActionEvent) {
                    Object[] objArray = {(ActionEvent) event};
                    mex.invoke(FacesContext
                            .getCurrentInstance().getELContext(), objArray);
                }
            } catch (Exception e) {
                // If there is any error associated with the
                // invokation of the ActionListenerExpression
                // the node should not expand
                expandToggleIcon = false;
                LogUtil.warning(e.getMessage(), e);
            }
        }
        if (expandToggleIcon) {
            // Nodes need to expand by default when Toggle icon is clicked
            this.setExpanded(!(this.isExpanded()));
        }
    }

    /**
     * This component renders its children.
     * This implementation always returns {@code true}.
     * @return {@code boolean}
     */
    @Override
    public boolean getRendersChildren() {
        return true;
    }

    /**
     * This method determines the theme images that should be drawn from left to
     * right (0 to x) when rendering the lines before the text for this
     * node.
     *
     * @return A {@code List} of Strings that represent theme keys for the
     * images to be drawn. The first list element is the first image to display
     * when rendering left to right.
     */
    @SuppressWarnings("checkstyle:methodlength")
    public List getImageKeys() {
        // Walk backward up the tree, calculate the theme image
        Stack<String> stack = new Stack<String>();
        Stack<UIComponent> tempStack = new Stack<UIComponent>();

        Object value;
        boolean last;
        boolean first = true;
        boolean bottomNode = false;
        int count = 1;
        TreeNode prevNode = getParentTreeNode(this);

        for (TreeNode node = prevNode; node != null;
                prevNode = node, node = getParentTreeNode(node)) {

            // get the list of peers and find if the current node
            // (this instance) is the last one among them
            TreeNode[] peerList = getChildTreeNodes(node);
            int len = peerList.length;
            TreeNode thisNode = peerList[len - 1];
            if (thisNode == this) {
                // the current node is the last one
                bottomNode = true;
            }

            if (first) {
                // Direct parent is special
                first = false;
                TreeNode[] list = getChildTreeNodes(this);
                if (list != null && list.length > 0) {
                    String imageIcon;
                    // For this property, we use 'this' for attributes
                    if (((Boolean) getAttributes().get("expanded"))) {
                        if (bottomNode) {
                            imageIcon = ThemeImages.TREE_HANDLE_DOWN_LAST;
                        } else {
                            imageIcon = ThemeImages.TREE_HANDLE_DOWN_MIDDLE;
                        }
                    } else {
                        if (bottomNode) {
                            imageIcon = ThemeImages.TREE_HANDLE_RIGHT_LAST;
                        } else {
                            imageIcon = ThemeImages.TREE_HANDLE_RIGHT_MIDDLE;
                        }
                    }
                    stack.push(imageIcon);
                    IconHyperlink ihl = getTurnerImageHyperlink();
                    ihl.setIcon(imageIcon);
                    ihl.setToolTip("Toggle " + getText() + " node");
                    ihl.setAlt("Toggle " + getText() + " node icon");
                    Tree rootNode = getAbsoluteRoot(this);
                    if (rootNode != null) {
                        if (rootNode.isClientSide()) {
                            ihl.setOnClick("return false;");
                        }
                    }
                    ihl.setBorder(0);
                    tempStack.push(ihl);
                } else {
                    String imageIcon;
                    if (bottomNode) {
                        imageIcon = ThemeImages.TREE_LINE_LAST_NODE;
                    } else {
                        imageIcon = ThemeImages.TREE_LINE_MIDDLE_NODE;
                    }
                    stack.push(imageIcon);
                    String id = "icon" + count++;
                    ImageComponent ic = (ImageComponent) ComponentUtilities
                            .getChild(this, id);
                    if (ic == null) {
                        ic = new ImageComponent();

                        ic.setId(id);
                        // GF-required 508 change
                        ic.setToolTip(getText() + " node");
                        // GF-required 508 change
                        ic.setAlt(getText() + " node icon");
                        this.getFacets().put(id, ic);
                    }
                    ic.setIcon(imageIcon);
                    tempStack.push(ic);
                }
            } else {
                // We get the attributes this way because we really want to
                // parent's values
                // to see if we have a peer
                String imageIcon;
                value = node.getAttributes().get("lastChild");
                TreeNode[] list = getChildTreeNodes(node);
                if (value == null) {
                    last = false;
                } else {
                    last = value.toString().equals("true");
                }
                if (!last && (list != null && list.length > 0)) {
                    imageIcon = ThemeImages.TREE_LINE_VERTICAL;
                } else {
                    imageIcon = ThemeImages.TREE_BLANK;
                }
                stack.push(imageIcon);
                String id = "icon" + count++;
                ImageComponent ic = (ImageComponent) ComponentUtilities
                        .getChild(this, id);
                if (ic == null) {
                    ic = new ImageComponent();
                    ic.setIcon(imageIcon);
                    // GF-required 508 change
                    ic.setToolTip(prevNode.getText() + " child");
                    // GF-required 508 change
                    ic.setAlt(prevNode.getText() + " child icon");
                    ic.setId(id);
                    this.getFacets().put(id, ic);
                }
                tempStack.push(ic);
            }
        }

        // Handle special case where this.getParent() is the root node...
        // don't draw a line up to it unless the root node has an icon.
        TreeNode parent = getParentTreeNode(this);
        if (parent instanceof Tree) {
            // Ok, so this is a direct child of the root...
            // but is it the first?
            Iterator children = parent.getChildren().iterator();
            Object child;
            while (children.hasNext()) {
                child = children.next();
                if (child instanceof TreeNode) {
                    // Check to see if the child is 'this'
                    if (child == this) {
                        // Ok, so this is the child that is effected... make
                        // sure the root node doesn't have an icon
                        String imgURL = parent.getImageURL();
                        if (((imgURL == null) || imgURL.equals(""))
                                && (parent.getFacet(IMAGE_FACET_KEY) == null)) {
                            // This is the special case
                            // Get the top image and change it
                            // stack.push(topLineImageMapping.get(stack.pop()));
                            String imageIcon = (String)
                                    TOP_LINE_IMAGE_MAPPING.get(stack.pop());

                            // how to find the component associated with it and
                            // then change the image icon of that component?
                            Object topmost = tempStack.pop();
                            if (topmost instanceof ImageComponent) {
                                ((ImageComponent) topmost).setIcon(imageIcon);
                            } else if (topmost instanceof IconHyperlink) {
                                ((IconHyperlink) topmost).setIcon(imageIcon);
                            }
                            stack.push(imageIcon);
                            tempStack.push((UIComponent) topmost);
                        }
                    }
                    // break b/c we only want to check the first TreeNode
                    break;
                }
            }
        }

        // Reverse the order
        List<UIComponent> list = new ArrayList<UIComponent>();
        while (!tempStack.empty()) {
            list.add(tempStack.pop());
        }
        // Return the list
        return list;
    }

    /**
     * Given the ID of a child of this node this method returns the TreeNode
     * component corresponding to this ID.
     *
     * @param id The id of the child node being searched for.
     * @return the TreeNode object if found, null otherwise.
     */
    public TreeNode getChildNode(final String id) {
        String thisID = getId();
        if (thisID != null && thisID.equals(id)) {
            return this;
        } else {
            if (getChildCount() == 0) {
                return null;
            } else {
                for (UIComponent kid : this.getChildren()) {
                    if (kid instanceof TreeNode) {
                        String kidId = kid.getId();
                        if (kidId != null && kidId.equals(id)) {
                            return (TreeNode) kid;
                        } else {
                            TreeNode node = (TreeNode) kid;
                            TreeNode foo = node.getChildNode(id);
                            if (foo != null) {
                                return foo;
                            }
                        }
                    }
                }
                return null;
            }
        }
    }

    /**
     * This method returns the closest parent that is a TreeNode, or null if not
     * found.
     *
     * @param node The starting {@code TreeNode}.
     *
     * @return The closest parent {@code TreeNode}
     */
    public static TreeNode getParentTreeNode(final UIComponent node) {
        if (node == null) {
            return null;
        }
        UIComponent n = node.getParent();
        while ((n != null) && !(n instanceof TreeNode)) {
            n = n.getParent();
        }
        return (TreeNode) n;
    }

    /**
     * This method returns the root Tree object reference. This method is
     * required to find out this is a client side tree and if nodes should
     * expand on selection of the anything other than the handler images. The
     * "clientSide" and "expandOnSelect" attributes of the Tree component are
     * used to supply this information.
     *
     * @param node node
     * @return The root {@code Tree} component.
     */
    public static Tree getAbsoluteRoot(final UIComponent node) {
        if (node == null) {
            return null;
        }
        if (node instanceof Tree) {
            return (Tree) node;
        }
        if (node instanceof TreeNode) {
            UIComponent n = node.getParent();
            while ((n != null) && !(n instanceof Tree)) {
                n = n.getParent();
            }
            if (n != null) {
                return (Tree) n;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * This method returns an array of child treeNodes only.
     *
     * @param node The starting {@code TreeNode}.
     *
     * @return The closest parent {@code TreeNode}
     */
    public static TreeNode[] getChildTreeNodes(final UIComponent node) {
        if (node == null) {
            return null;
        }
        if (node instanceof TreeNode) {
            Iterator<UIComponent> nodeList = node.getChildren().iterator();
            List<TreeNode> childNodeList = new ArrayList<TreeNode>();
            while (nodeList.hasNext()) {
                UIComponent comp = nodeList.next();
                if (comp instanceof TreeNode) {
                    childNodeList.add((TreeNode) comp);
                }
            }
            TreeNode[] arr = new TreeNode[childNodeList.size()];
            for (int i = 0; i < childNodeList.size(); i++) {
                arr[i] = (TreeNode) childNodeList.get(i);
            }
            return arr;
        } else {
            return null;
        }
    }

    /**
     * Add an action listener instance for the IconHyperlink representing this
     * node's turner.
     *
     * @param listener The ActionListener instance to register for turner
     * IconHyperlink clicks.
     */
    public void addActionListener(final ActionListener listener) {
        addFacesListener(listener);
    }

    /**
     * Get all ActionListener instances for this node's turner IconHyperlink
     * click.
     *
     * @return ActionListener[] The list of listeners for this node's turner
     * IconHyperlink click.
     */
    public ActionListener[] getActionListeners() {
        ActionListener[] al = (ActionListener[])
                getFacesListeners(ActionListener.class);
        return (al);
    }

    /**
     * Remove an action listener instance from the list for this node's turner
     * IconHyperlink.
     *
     * @param listener The ActionListener instance to remove.
     */
    public void removeActionListener(final ActionListener listener) {
        removeFacesListener(listener);
    }

    /**
     * This method enables the icon to switch from expanded to collapsed, or
     * from collapsed to expanded depending on the current state of this
     * component.
     *
     * @param value The current value of the Icon. It will use the current value
     * to re-use first/last information from the old key.
     *
     * @return The new (or same if the state hasn't changed) icon state
     */
    public String getHandleIcon(final String value) {
        // Convert it to the current state
        if (isExpanded()) {
            return ThemeImages.TREE_HANDLE_DOWN_TOP_NOSIBLING;
        }
        return ThemeImages.TREE_HANDLE_RIGHT_TOP_NOSIBLING;
    }

    /**
     * get the content hyperlink.
     * @return Hyperlink
     */
    public Hyperlink getContentHyperlink() {
        Hyperlink child = (Hyperlink) ComponentUtilities.getPrivateFacet(this,
                "link", true);
        if (child == null) {
            child = new Hyperlink();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    "link"));
            child.addActionListener(new ToggleActionListener());
            ComponentUtilities.putPrivateFacet(this,
                    "link", child);
        }
        child.setText(this.getText());
        child.setUrl(this.getUrl());
        if (this.getTarget() != null) {
            child.setTarget(this.getTarget());
        }
        if (this.getActionExpression() != null) {
            child.setActionExpression(this.getActionExpression());
        }

        ActionListener[] nodeListeners = this.getActionListeners();
        if ((nodeListeners != null) && (nodeListeners.length > 0)) {
            for (ActionListener nodeListener : nodeListeners) {
                child.addActionListener(nodeListener);
            }
        }
        if (this.getToolTip() != null) {
            child.setToolTip(this.getToolTip());
        }
        return child;
    }

    /**
     * Get the node image hyperlink.
     * @return ImageHyperlink
     */
    public ImageHyperlink getNodeImageHyperlink() {
        ImageHyperlink child = (ImageHyperlink) ComponentUtilities
                .getPrivateFacet(this, "image", true);
        if (child == null) {
            child = new ImageHyperlink();
            child.setId(ComponentUtilities.createPrivateFacetId(this,
                    "image"));
            child.addActionListener(new ToggleActionListener());
            ComponentUtilities.putPrivateFacet(this,
                    "image", child);
        }
        child.setImageURL(this.getImageURL());
        child.setUrl(this.getUrl());
        child.setBorder(0);
        if (this.getTarget() != null) {
            child.setTarget(this.getTarget());
        }
        if (this.getActionExpression() != null) {
            child.setActionExpression(this.getActionExpression());
        }

        ActionListener[] nodeListeners = this.getActionListeners();
        if ((nodeListeners != null) && (nodeListeners.length > 0)) {
            for (ActionListener nodeListener : nodeListeners) {
                child.addActionListener(nodeListener);
            }
        }
        if (this.getToolTip() != null) {
            child.setToolTip(this.getToolTip());
        }
        return child;
    }

    /**
     * Get the turner image hyperlink.
     * @return IconHyperlink
     */
    public IconHyperlink getTurnerImageHyperlink() {
        IconHyperlink ihl
                = (IconHyperlink) ComponentUtilities.getPrivateFacet(this,
                        "turner", true);
        if (ihl == null) {
            ihl = new IconHyperlink();
            ihl.setId(ComponentUtilities.createPrivateFacetId(this,
                    "turner"));
            ihl.addActionListener(new ToggleActionListener());
            ComponentUtilities.putPrivateFacet(this,
                    "turner", ihl);
        }
        return ihl;
    }

    /**
     * This implementation restores the state of all properties.
     * @param context faces context
     * @param state state object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.actionExpression = (jakarta.el.MethodExpression)
                restoreAttachedState(context, values[1]);
        this.actionListenerExpression = (jakarta.el.MethodExpression) values[2];
        this.expanded = ((Boolean) values[3]);
        this.expandedSet = ((Boolean) values[4]);
        this.imageURL = (String) values[5];
        this.style = (String) values[6];
        this.styleClass = (String) values[7];
        this.target = (String) values[8];
        this.text = (String) values[9];
        this.toolTip = (String) values[10];
        this.url = (String) values[11];
        this.visible = ((Boolean) values[12]);
        this.visibleSet = ((Boolean) values[13]);
    }

    /**
     * This implementation saves the state of all properties.
     * @param context faces context
     * @return Object
     */
    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[14];
        values[0] = super.saveState(context);
        values[1] = saveAttachedState(context, actionExpression);
        values[2] = this.actionListenerExpression;
        if (this.expanded) {
            values[3] = Boolean.TRUE;
        } else {
            values[3] = Boolean.FALSE;
        }
        if (this.expandedSet) {
            values[4] = Boolean.TRUE;
        } else {
            values[4] = Boolean.FALSE;
        }
        values[5] = this.imageURL;
        values[6] = this.style;
        values[7] = this.styleClass;
        values[8] = this.target;
        values[9] = this.text;
        values[10] = this.toolTip;
        values[11] = this.url;
        if (this.visible) {
            values[12] = Boolean.TRUE;
        } else {
            values[12] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[13] = Boolean.TRUE;
        } else {
            values[13] = Boolean.FALSE;
        }
        return values;
    }
}
