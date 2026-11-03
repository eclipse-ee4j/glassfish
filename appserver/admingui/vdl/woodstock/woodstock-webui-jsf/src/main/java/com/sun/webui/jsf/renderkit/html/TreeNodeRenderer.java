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
package com.sun.webui.jsf.renderkit.html;

import com.sun.faces.annotation.Renderer;
import com.sun.webui.html.HTMLAttributes;
import com.sun.webui.html.HTMLElements;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.IconHyperlink;
import com.sun.webui.jsf.component.ImageHyperlink;
import com.sun.webui.jsf.component.Tree;
import com.sun.webui.jsf.component.TreeNode;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ComponentUtilities;
import com.sun.webui.jsf.util.JavaScriptUtilities;
import com.sun.webui.jsf.util.RenderingUtilities;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.el.MethodExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.ActionListener;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Renderer for a {@link TreeNode} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.TreeNode"))
public class TreeNodeRenderer extends jakarta.faces.render.Renderer {

    /**
     * This implementation is empty.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     *
     * @exception NullPointerException if {@code context} or
     * {@code component} is {@code null}
     */
    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    /**
     * This implementation is empty.
     *
     * @param context {@code FacesContext} for the current request
     * @param component {@code UIComponent} to be decoded
     *
     * @exception NullPointerException if {@code context} or
     * {@code component} is {@code null}
     */
    @Override
    public void encodeBegin(final FacesContext context,
            final UIComponent component) {
        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    /**
     * This implementation renders a property component.
     *
     * @param context The current FacesContext
     * @param component The Property object to render
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        TreeNode node = (TreeNode) component;

        // Get the theme
        Theme theme = ThemeUtilities.getTheme(context);

        Tree root = TreeNode.getAbsoluteRoot(node);
        boolean csFlag = root.isClientSide();
        // boolean esFlag = root.isExpandOnSelect();

        // Check if the TreeNode has children. If so, render each child which
        // in turn will invoke methods of this class.
        // Check if the treeNode has facets, if so render these facets and
        // disregard the corresponding attribute values that these facets
        // override.
        // If no facets, simply render according to the attribute values or
        // assume default values wherever possible
        writer.startElement(HTMLElements.DIV, node); //start of node div
        writer.writeAttribute(HTMLAttributes.ID, node.getClientId(context),
                null);

        // Set styleclass. First check if node is hidden. Else check if
        // styleclass
        // attribute has been supplied. Else set the default styleclass.
        String nodeStyleClass = theme.getStyleClass(ThemeStyles.TREE_ROW);
        if (!node.isVisible()) {
            nodeStyleClass = theme.getStyleClass(ThemeStyles.HIDDEN);
        } else if (node.getStyleClass() != null) {
            nodeStyleClass = node.getStyleClass();
        }
        writer.writeAttribute(HTMLAttributes.CLASS, nodeStyleClass, null);

        if (node.getStyle() != null) {
            writer.writeAttribute(HTMLAttributes.STYLE,
                    node.getStyle(), null);
        }

        // Getting the list of images for a given row before they are
        // used for the first time.
        Iterator imageIter = node.getImageKeys().iterator();

        IconHyperlink ihl = node.getTurnerImageHyperlink();
        UIComponent imageFacet = null;
        if (ihl != null) {
            imageFacet = ihl.getImageFacet();
        }
        StringBuilder buff = new StringBuilder();
        buff.append(JavaScriptUtilities.getDomNode(context, root))
                .append(".onTreeNodeClick(this, '");
        if (imageFacet != null) {
            buff.append(imageFacet.getClientId(context));
        } else {
            buff.append("null");
        }
        buff.append("', event);");

        writer.writeAttribute(HTMLAttributes.ONCLICK,
                buff.toString(), null);

        // writer.writeText("\n", null);
        // Render the treeRow now. Passing the retrived
        // iterator so that the same task does not have to
        // be repeated inside the method.
        renderTreeRow(node, imageIter, context, writer);
        // treerow div ends
        writer.endElement(HTMLElements.DIV);
        //writer.writeText("\n", null);
        List<UIComponent> children = node.getChildren();

        // If node is expanded or if its a client side tree we need to lay
        // down the next level of child nodes. Child nodes will be visible
        // only if node is visible and the client side node handler
        // is meant to show the nodes.
        if (node.isExpanded() || csFlag) {

            //writer.writeText("\n", null);
            writer.startElement(HTMLElements.DIV, node);
            writer.writeAttribute(HTMLAttributes.ID,
                    node.getClientId(context) + "_children", null);
            if (!node.isVisible()) {
                writer.writeAttribute(HTMLAttributes.CLASS,
                        theme.getStyleClass(ThemeStyles.HIDDEN), null);
            }
            if (!node.isExpanded()) {
                writer.writeAttribute(HTMLAttributes.STYLE, "display:none",
                        null);
            }
            // writer.writeText("\n", null);

            Iterator<UIComponent> iter = children.iterator();
            while (iter.hasNext()) {
                RenderingUtilities.renderComponent(iter.next(), context);
            }

            writer.endElement(HTMLElements.DIV);
            // writer.writeText("\n", null);
        }

    }

    /**
     * This implementation is empty.
     *
     * @param context The current FacesContext
     * @param component The Property object to render
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component) throws IOException {
        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Set the tool-tip on the specified link.
     * @param ihl link to set the tool-tip on
     * @param node tree node
     */
    private void setToolTip(final ImageHyperlink ihl, final TreeNode node) {
        // GF-required 508 change
        ihl.setToolTip(node.getText() + " node");
        // GF-required 508 change
        ihl.setAlt(node.getText() + " node image");
    }

    /**
     * Renders each row of the tree.A tree row consists of a set of images
     * followed by the actual row image or text.
     *
     * @param node The TreeNode object whose row is to be rendered
     * @param imageIter image iterator
     * @param context The current FacesContext
     * @param writer The current ResponseWriter
     *
     * @exception IOException if an input/output error occurs
     */
    protected void renderTreeRow(final TreeNode node, final Iterator imageIter,
            final FacesContext context, final ResponseWriter writer)
            throws IOException {

        Theme theme = ThemeUtilities.getTheme(context);

        //image div
        writer.startElement(HTMLElements.DIV, node);
        String lineimagesID = node.getClientId(context) + "LineImages";
        writer.writeAttribute(HTMLAttributes.ID, lineimagesID, null);
        writer.writeAttribute(HTMLAttributes.CLASS,
                theme.getStyleClass(ThemeStyles.FLOAT), null);

        // render turner and other images before the actual
        // data for a given tree row.
        // Iterator imageIter = node.getImageKeys().iterator();
        while (imageIter.hasNext()) {
            // read each image IconHyperlink or ImageHyperlink and render it
            UIComponent imageComp = (UIComponent) imageIter.next();
            RenderingUtilities.renderComponent(imageComp, context);
            //writer.writeText("\n", null);
        }

        // check if image facet has been supplied. If so, render it.
        UIComponent imageFacet = node.getFacet(Tree.TREE_IMAGE_FACET_NAME);
        if (imageFacet != null) {
            if (imageFacet instanceof ImageHyperlink) {
                ImageHyperlink ihl = (ImageHyperlink) imageFacet;
                setToolTip(ihl, node);
            }
            RenderingUtilities.renderComponent(imageFacet, context);
        } else {
            String imageURL = node.getImageURL();
            if (imageURL != null && imageURL.length() > 0) {
                ImageHyperlink ihl
                        = node.getNodeImageHyperlink();
                setToolTip(ihl, node);
                renderImageOrText(node, ihl, context);
            }
        }
        //writer.writeText("\n", null);
        writer.endElement(HTMLElements.DIV); // handler/image div ends here
        //writer.writeText("\n", null);

        // check if content facet has been supplied. If so, render it.
        UIComponent contentFacet = node.getFacet(Tree.TREE_CONTENT_FACET_NAME);
        writer.startElement(HTMLElements.DIV, node);
        writer.writeAttribute(HTMLAttributes.ID,
                node.getClientId(context) + "Text", null);
        String textStyle = theme.getStyleClass(ThemeStyles.TREE_CONTENT)
                + " "
                + theme.getStyleClass(ThemeStyles.TREE_NODE_IMAGE_HEIGHT);
        writer.writeAttribute(HTMLAttributes.CLASS, textStyle, null);
        //writer.writeText("\n", null);

        if (contentFacet != null) {
            RenderingUtilities.renderComponent(contentFacet, context);

        } else {
            // render text attribute value if supplied
            String treeText = node.getText();
            String nodeURL = node.getUrl();
            boolean hasText = ((treeText != null) && (treeText.length() > 0));
            boolean hasURL = ((nodeURL != null) && (nodeURL.length() > 0));
            MethodExpression mex = node.getActionExpression();
            boolean hasAction = (mex != null);

            if (hasURL || hasAction) {
                // new Hyperlink();
                Hyperlink link = node.getContentHyperlink();
                renderImageOrText(node, link, context);
            } else if (treeText != null && treeText.length() > 0) {
                writer.write(treeText);
            }
        }
        //writer.writeText("\n", null);

        // content DIV ends
        writer.endElement(HTMLElements.DIV);
        //writer.writeText("\n", null);
    }

    /**
     * Render an image or text.
     * @param node tree node
     * @param link link
     * @param context faces context
     * @throws IOException if an IO error occurs
     */
    private void renderImageOrText(final TreeNode node, final Hyperlink link,
            final FacesContext context) throws IOException {

        Tree root = TreeNode.getAbsoluteRoot(node);
        boolean csFlag = root.isClientSide();
        boolean esFlag = root.isExpandOnSelect();
        String nodeURL = node.getUrl();
        MethodExpression mex = node.getActionExpression();
        ActionListener[] nodeListeners = node.getActionListeners();
        boolean hasURL = ((nodeURL != null) && (nodeURL.length() > 0));
        boolean hasAction = (mex != null);

        if (hasURL || hasAction) {
            String jsObject = JavaScriptUtilities.getDomNode(context, root);
            if (hasAction || ((nodeListeners != null)
                    && (nodeListeners.length > 0))) {

                link.setOnClick(jsObject
                        + ".treecontent_submit('"
                        + node.getClientId(context) + "');");
            } else {
                if (esFlag) {
                    IconHyperlink ihl = (IconHyperlink) ComponentUtilities
                            .getPrivateFacet(node, "turner", true);
                    UIComponent iconImage = null;
                    StringBuilder buff = new StringBuilder();
                    buff.append(jsObject).append(".selectTreeNode(")
                            .append(jsObject)
                            .append(".findContainingTreeNode(this).id);")
                            .append(jsObject)
                            .append(".expandTurner(this, '");
                    if (ihl != null) {
                        buff.append(ihl.getClientId(context));
                        iconImage = ihl.getImageFacet();
                    } else {
                        buff.append("null");
                    }
                    buff.append("', '");
                    if (iconImage != null) {
                        buff.append(iconImage.getClientId(context));
                    } else {
                        buff.append("null");
                    }
                    buff.append("', ").append(csFlag).append(", event);");
                    link.setOnClick(buff.toString());
                }
            }
            RenderingUtilities.renderComponent(link, context);
        }
    }
}
