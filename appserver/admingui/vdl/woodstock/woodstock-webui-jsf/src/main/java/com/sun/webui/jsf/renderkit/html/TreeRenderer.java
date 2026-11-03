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
import com.sun.webui.jsf.component.Tree;
import com.sun.webui.jsf.component.TreeNode;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.theme.Theme;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.json.JsonObject;

import java.io.IOException;
import java.util.Iterator;

import static com.sun.webui.jsf.util.JavaScriptUtilities.renderCall;
import static com.sun.webui.jsf.util.JavaScriptUtilities.renderScripTag;
import static com.sun.webui.jsf.util.JsonUtilities.JSON_BUILDER_FACTORY;
import static com.sun.webui.jsf.util.RenderingUtilities.renderAnchor;
import static com.sun.webui.jsf.util.RenderingUtilities.renderComponent;
import static com.sun.webui.jsf.util.RenderingUtilities.renderSkipLink;
import static com.sun.webui.jsf.util.ThemeUtilities.getTheme;

/**
 * Renderer for a {@link Tree} component.
 */
@Renderer(
        @Renderer.Renders(componentFamily = "com.sun.webui.jsf.Tree"))
public final class TreeRenderer extends TreeNodeRenderer {

    /**
     * Skip tree link property.
     */
    private static final String SKIPTREE_LINK = "skipTreeLink";

    @Override
    public void decode(final FacesContext context,
            final UIComponent component) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public void encodeEnd(final FacesContext context,
            final UIComponent component) throws IOException {

        Iterator messages = context.getMessages();
        if (messages != null) {
            while (messages.hasNext()) {
                FacesMessage fm = (FacesMessage) messages.next();
                LogUtil.fine(fm.getSummary());
                LogUtil.fine(fm.getDetail());
            }
        }

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (!component.isRendered()) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        Tree node = (Tree) component;
        Theme theme = getTheme(context);

        // The title bar can be defined with either ui:tree tag attributes or
        // facets.
        // The title bar is rendered if the tree component includes imageURL
        // property
        // for the graphic, the text property for the title text, the content
        // facet,
        // or the image facet.
        // render outermost div of the tree.
        writer.write("\n\n\n");
        writer.startElement(HTMLElements.DIV, node);
        writer.writeAttribute(HTMLAttributes.ID, node.getClientId(context),
                null);
        String nodeStyleClass = theme.getStyleClass(ThemeStyles.TREE);
        if (!node.isVisible()) {
            nodeStyleClass = theme.getStyleClass(ThemeStyles.HIDDEN);
        } else if (node.getStyleClass() != null) {
            nodeStyleClass = node.getStyleClass();
        }
        writer.writeAttribute(HTMLAttributes.CLASS, nodeStyleClass, null);
        if (node.getStyle() != null) {
            writer.writeAttribute(HTMLAttributes.STYLE, node.getStyle(),
                    null);
        }
        writer.write("\n");

        // render the skip hyper link to support A11Y
        renderSkipLink(SKIPTREE_LINK,
                theme.getStyleClass(ThemeStyles.SKIP_WHITE), null,
                theme.getMessage("tree.skipTagAltText"),
                null, node, context);
        writer.write("\n");

        // add the spacer
        writer.write("\n");
        String rootText = node.getText();
        String rootImageURL = node.getImageURL();
        boolean hasRootContentFacet
                = (node.getFacet(Tree.TREE_CONTENT_FACET_NAME) != null);
        boolean hasRootImageFacet
                = (node.getFacet(Tree.TREE_IMAGE_FACET_NAME) != null);

        if ((rootText != null && rootText.length() > 0)
                || rootImageURL != null
                || hasRootImageFacet
                || hasRootContentFacet) {

            String titlebarSpacerDivID = node.getClientId(context)
                    + "TitleBarSpacer";
            String titlebarDivID = node.getClientId(context) + "TitleBar";
            String lineImageDivID = node.getClientId(context) + "LineImages";
            String lineTxtDivID = node.getClientId(context) + "LineText";

            // title bar spacer
            writer.startElement(HTMLElements.DIV, node);
            writer.writeAttribute(HTMLAttributes.ID, titlebarSpacerDivID,
                    null);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.TREE_ROOT_ROW_HEADER),
                    null);
            writer.endElement(HTMLElements.DIV);
            writer.write("\n");

            writer.startElement(HTMLElements.DIV, node); // tree root row start
            writer.writeAttribute(HTMLAttributes.ID, titlebarDivID, null);
            writer.writeAttribute(HTMLAttributes.CLASS,
                    theme.getStyleClass(ThemeStyles.TREE_ROOT_ROW), null);
            writer.write("\n");
            Iterator imageIter = node.getImageKeys().iterator();
            if (((node.getUrl() != null) && (node.getUrl().length() > 0))
                    || hasRootContentFacet) {

                renderTreeRow(node, imageIter, context, writer);

            } else {

                writer.write("\n");
                writer.startElement(HTMLElements.SPAN, node);
                writer.writeAttribute(HTMLAttributes.CLASS,
                        theme.getStyleClass(ThemeStyles.TREE_TITLE), null);
                writer.write("\n");
                renderTreeRow(node, imageIter, context, writer);
                writer.endElement(HTMLElements.SPAN);
            }
            writer.endElement(HTMLElements.DIV);

        }

        // Check if the TreeNode has children. If so, render each child which
        // in turn would cause each of the descendent nodes to get rendered.
        Iterator<UIComponent> iter = node.getChildren().iterator();

        //writer.writeText("\n", null);
        String clientId = node.getClientId(context);
        writer.startElement(HTMLElements.DIV, node);
        writer.writeAttribute(HTMLAttributes.ID,
                clientId + "_children", null);
        while (iter.hasNext()) {
            renderComponent(iter.next(), context);
        }

        writer.endElement(HTMLElements.DIV);
        //writer.writeText("\n", null);

        String nodeId = null;
        if (node.getSelected() != null) {
            String childID = (String) node.getSelected();
            TreeNode childNode = node.getChildNode(childID);
            if (childNode != null) {
                nodeId = childNode.getClientId(context);
            }
        }

        JsonObject initProps = JSON_BUILDER_FACTORY
                .createObjectBuilder()
                .add("id", clientId)
                .build();

        renderScripTag(writer,
                // ws_init_tree
                renderCall("init_tree", initProps, clientId, nodeId));

        // Render skip anchor.
        renderAnchor(SKIPTREE_LINK, node, context);
        writer.write("\n");
        writer.endElement(HTMLElements.DIV);
    }

    @Override
    public void encodeChildren(final FacesContext context,
            final UIComponent component)
            throws IOException {
    }
}
