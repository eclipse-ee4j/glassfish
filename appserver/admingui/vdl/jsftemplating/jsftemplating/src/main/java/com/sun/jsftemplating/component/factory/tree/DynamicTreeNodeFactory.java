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

package com.sun.jsftemplating.component.factory.tree;

import com.sun.jsftemplating.annotation.UIComponentFactory;
import com.sun.jsftemplating.component.ComponentUtil;
import com.sun.jsftemplating.component.factory.ComponentFactoryBase;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.handler.Handler;
import com.sun.jsftemplating.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <p>
 * Portions of many trees are non-static, or Dynamic. By this it is meant that some of the nodes can only be determined
 * at Runtime. The goal of this factory is to provide a means for dynamic tree nodes to be defined. This implementation
 * allows a portion or an entire tree to be defined.
 * </p>
 *
 * <p>
 * It relies on the supplied {@link TreeAdaptor} to drive the creation of the "root" tree node. The "root" node is a
 * single Tree or TreeNode that is returned from the {@link #create(FacesContext, LayoutComponent, UIComponent)} method.
 * Since this is a factory class for creating a single UIComponent, it is expected that a single UIComponent is returned
 * from this method. However, the returned UIComponent may contain 0 or more children (other TreeNodes). The
 * {@link TreeAdaptor} facilitates this.
 * </p>
 *
 * <p>
 * The {@link TreeAdaptor} implemenation is respsonsible for traversing the tree data and providing the necessary
 * information about that data to this factory. The tree data can be stored in any format, a specific TreeAdaptor must
 * be written to interpret each unique type of data format. When this factory interacts with the {@link TreeAdaptor}, it
 * passes an <code>Object</code> that represents a tree node in the arbitrary data format. This <code>Object</code> is
 * obtained originally from the {@link TreeAdaptor}, so the developer has control over what object is used to identify
 * tree nodes in their own data format.
 * </p>
 *
 * <p>
 * See {@link TreeAdaptor} to see the necessary methods to implement in order for this factory to be capable of
 * populating <code>TreeNode</code>s based on your data.
 * </p>
 *
 * <p>
 * The {@link com.sun.jsftemplating.layout.descriptors.ComponentType} id for this factory is: "dynamicTreeNode".
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
@UIComponentFactory("dynamicTreeNode")
public class DynamicTreeNodeFactory extends ComponentFactoryBase {

    /**
     * Constructor
     */
    public DynamicTreeNodeFactory() {
    }

    /**
     * <p>
     * This is the factory method responsible for creating the <code>UIComponent</code>.
     * </p>
     *
     * @param context The <code>FacesContext</code>
     * @param descriptor The {@link LayoutComponent} descriptor associated with the requested <code>UIComponent</code>.
     * @param parent The parent <code>UIComponent</code>
     *
     * @return The newly created component.
     */
    @Override
    public UIComponent create(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
        // Get the TreeAdaptor which should be used
        TreeAdaptor treeAdaptor = getTreeAdaptor(context, descriptor, parent);

        // Initialize the TreeAdaptor instance
        treeAdaptor.init();

        // First pull off the root...
        Object currentObj = treeAdaptor.getTreeNodeObject();

        // Return the root TreeNode
        return processNode(context, treeAdaptor, currentObj, parent);
    }

    /**
     * <p>
     * This method gets the <code>TreeAdaptor</code> by looking at the {@link #TREE_ADAPTOR_CLASS} option and invoking
     * <code>getInstance</code> on the specified <code>TreeAdaptor</code> implementation.
     * </p>
     */
    protected TreeAdaptor getTreeAdaptor(FacesContext ctx, LayoutComponent desc, UIComponent parent) {
        TreeAdaptor adaptor = null;
        Object cls = desc.getEvaluatedOption(ctx, TREE_ADAPTOR_CLASS, parent);
        if (cls == null) {
            throw new IllegalArgumentException("'" + TREE_ADAPTOR_CLASS + "' must be specified!");
        }
        try {
            Class adaptorClass = Util.getClass(cls);
            adaptor = (TreeAdaptor) adaptorClass.getMethod("getInstance", new Class[] { FacesContext.class, LayoutComponent.class, UIComponent.class })
                    .invoke((Object) null, new Object[] { ctx, desc, parent });
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        // Return the TreeAdaptor
        return adaptor;
    }

    /**
     * <p>
     * This method is responsible for creating and configuring a <code>TreeNode</code> given its <code>TreeNode</code>
     * object. It then recurses for each child <code>TreeNode</code> object.
     * </p>
     */
    protected UIComponent processNode(FacesContext ctx, TreeAdaptor adaptor, Object currentObj, UIComponent parent) {
        // Pull off the important information...
        String id = adaptor.getId(currentObj);
        String factoryClass = adaptor.getFactoryClass(currentObj);
        // NOTE: Properties specify things such as:
        // URL, Text, Image, Action, Expanded, ActionListener, etc.
        Map<String, Object> props = adaptor.getFactoryOptions(currentObj);
        Properties properties = Util.mapToProperties(props);

        // Create TreeNode
        UIComponent node = ComponentUtil.getInstance(ctx).getChild(parent, id, factoryClass, properties);

        // The above util method defaults to using a facet... change to child
        // NOTE: The above needs "parent" to correctly evaluate ${}
        // NOTE: expressions, in the future find a way to make it store as a
        // NOTE: child vs. facet (possible if I create the LayoutComponent).
        if (parent != null) {
            parent.getFacets().remove(id);
            parent.getChildren().add(node);
        }

        // Configure TreeNode
        configureTreeNode(ctx, adaptor, node, currentObj);

        // Walk TreeAdaptor and Create child TreeNodes
        List<Object> children = adaptor.getChildTreeNodeObjects(currentObj);
        if (children != null) {
            Iterator<Object> it = children.iterator();
            while (it.hasNext()) {
                currentObj = it.next();
                // We can ignore the return value b/c the factory should
                // automatically set the parent
                processNode(ctx, adaptor, currentObj, node);
            }
        }

        // Return the recently created TreeNode (or whatever it is)
        return node;
    }

    /**
     * <p>
     * Adds on facets and handlers.
     * </p>
     */
    protected void configureTreeNode(FacesContext ctx, TreeAdaptor adaptor, UIComponent treeNode, Object currentObj) {
        // Add facets (such as "content" and "image")
        Map<String, UIComponent> facets = adaptor.getFacets(treeNode, currentObj);
        if (facets != null) {
            Map<String, UIComponent> treeNodeFacets = treeNode.getFacets();
            Iterator<String> it = facets.keySet().iterator();
            String facetName;
            UIComponent facetValue;
            while (it.hasNext()) {
                facetName = it.next();
                facetValue = facets.get(facetName);
                if (facetValue != null) {
                    treeNodeFacets.put(facetName, facetValue);
                }
            }
        }

        // Add instance handlers
        Map<String, List<Handler>> handlersByType = adaptor.getHandlersByType(treeNode, currentObj);
        if (handlersByType != null) {
            Iterator<String> it = handlersByType.keySet().iterator();
            if (it.hasNext()) {
                String eventType = null;
                Map<String, Object> compAttrs = treeNode.getAttributes();
                while (it.hasNext()) {
                    // Assign instance handlers to attribute for retrieval later
                    // (Retrieval must be explicit, see LayoutElementBase)
                    eventType = it.next();
                    compAttrs.put(eventType, handlersByType.get(eventType));
                }
            }
        }
    }

    /**
     * <p>
     * This is the option that must be supplied when using this factory in order to specify which TreeAdaptor instance
     * should be used. The value should be a fully qualified class name of a valid TreeAdaptor instance. The TreeAdaptor
     * instance must have a <code>public static TreeAdaptor getInstance(FacesContext,
     *        LayoutComponent, UIComponent)</code> method in order to get access to an instance of the TreeAdaptor instance.
     * </p>
     */
    public static final String TREE_ADAPTOR_CLASS = "treeAdaptorClass";
}
