/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.layout;

import com.sun.jsftemplating.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.StateManagementStrategy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The state management strategy for the JSFT.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
class LayoutStateManagementStrategy extends StateManagementStrategy {

    /**
     * Stores the class map.
     */
    private final Map<String, Class<?>> classMap;

    /**
     * Constructor.
     */
    public LayoutStateManagementStrategy() {
        this.classMap = new ConcurrentHashMap<>(32);
    }

    /**
     * Capture the child.
     *
     * @param tree the tree
     * @param parent the parent
     * @param component the component
     */
    private void captureChild(List<TreeNode> tree, int parent, UIComponent component) {
        if (!component.isTransient()) {
            TreeNode treeNode = new TreeNode(parent, component);
            int position = tree.size();
            tree.add(treeNode);
            captureRest(tree, position, component);
        }
    }

    /**
     * Capture the facet.
     *
     * @param tree the tree
     * @param parent the parent
     * @param name the facet name
     * @param component the component
     */
    private void captureFacet(List<TreeNode> tree, int parent, String name, UIComponent component) {
        if (!component.isTransient()) {
            FacetNode facetNode = new FacetNode(parent, name, component);
            int position = tree.size();
            tree.add(facetNode);
            captureRest(tree, position, component);
        }
    }

    /**
     * Capture the rest.
     *
     * @param tree the tree
     * @param position the position
     * @param component the component
     */
    private void captureRest(List<TreeNode> tree, int position, UIComponent component) {
        int size = component.getChildCount();
        if (size > 0) {
            List<UIComponent> children = component.getChildren();
            for (int i = 0; i < size; i++) {
                captureChild(tree, position, children.get(i));
            }
        }

        size = component.getFacetCount();
        if (size > 0) {
            for (Map.Entry<String, UIComponent> facetEntry : component.getFacets().entrySet()) {
                captureFacet(tree, position, facetEntry.getKey(), facetEntry.getValue());
            }
        }
    }

    /**
     * Create a new component instance.
     *
     * @param treeNode the tree node
     * @return the UI component
     * @throws FacesException when a serious error occurs
     */
    private UIComponent createComponent(TreeNode treeNode) throws FacesException {
        try {
            Class<?> componentClass = classMap.get(treeNode.componentType);
            if (componentClass == null) {
                componentClass = Util.loadClass(treeNode.componentType, treeNode);
                classMap.put(treeNode.componentType, componentClass);
            }

            UIComponent component = (UIComponent) componentClass.getDeclaredConstructor().newInstance();
            component.setId(treeNode.id);

            return component;
        } catch (ReflectiveOperationException e) {
            throw new FacesException(e);
        }
    }

    /**
     * Restore the component tree.
     *
     * @param renderKitId the render kit identifier
     * @param tree the saved tree
     * @return the view root
     * @throws FacesException when a serious error occurs
     */
    private UIViewRoot restoreTree(FacesContext facesContext, String renderKitId, Object[] tree) throws FacesException {
        UIComponent component;
        FacetNode facetNode;
        TreeNode treeNode;
        for (int i = 0; i < tree.length; i++) {
            if (tree[i] instanceof FacetNode) {
                facetNode = (FacetNode) tree[i];
                component = createComponent(facetNode);
                tree[i] = component;
                if (i != facetNode.getParent()) {
                    ((UIComponent) tree[facetNode.getParent()]).getFacets().put(facetNode.facetName, component);
                }

            } else {
                treeNode = (TreeNode) tree[i];
                component = createComponent(treeNode);
                tree[i] = component;
                if (i != treeNode.parent) {
                    ((UIComponent) tree[treeNode.parent]).getChildren().add(component);
                } else {
                    UIViewRoot viewRoot = (UIViewRoot) component;
                    facesContext.setViewRoot(viewRoot);
                    viewRoot.setRenderKitId(renderKitId);
                }
            }
        }
        return (UIViewRoot) tree[0];
    }

    /**
     * Restore the view.
     *
     * @param facesContext the Faces context
     * @param viewId the view id
     * @param renderKitId the render kit identifier
     * @return the view root
     */
    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId, String renderKitId) {
        UIViewRoot viewRoot = null;

        ResponseStateManager responseStateManager = RenderKitUtil.getRenderKit(facesContext, renderKitId).getResponseStateManager();
        Object[] state = (Object[]) responseStateManager.getState(facesContext, viewId);

        if (state != null && state.length >= 2) {
            /*
             * Restore the component tree.
             */
            if (state[0] != null) {
                viewRoot = restoreTree(facesContext, renderKitId, ((Object[]) state[0]).clone());
                facesContext.setViewRoot(viewRoot);
            }
            /*
             * Restore the component state.
             */
            if (viewRoot != null && state[1] != null) {
                viewRoot.processRestoreState(facesContext, state[1]);
            }
        }

        return viewRoot;
    }

    /**
     * Save the view.
     *
     * @param facesContext the Faces context
     * @return the saved view
     */
    @Override
    public Object saveView(FacesContext facesContext) {
        UIViewRoot viewRoot = facesContext.getViewRoot();

        /*
         * Check uniqueness.
         */
        checkIdUniqueness(facesContext, viewRoot, new HashSet<>(viewRoot.getChildCount() << 1));

        /*
         * Save the component state.
         */
        Object state = viewRoot.processSaveState(facesContext);

        /*
         * Save the tree structure.
         */
        List<TreeNode> treeList = new ArrayList<>(32);
        captureChild(treeList, 0, viewRoot);
        Object[] tree = treeList.toArray();

        return new Object[] { tree, state };
    }

    /**
     * Utility method to validate id uniqueness for the tree represented by {@code component}.
     *
     * @param facesContext the Faces context
     * @param component the component represents a tree
     * @param componentIds the component ids to be validated
     */
    private static void checkIdUniqueness(FacesContext facesContext, UIComponent component, Set<String> componentIds) {
        // Deal with children/facets that are marked transient.
        for (Iterator<UIComponent> children = component.getFacetsAndChildren(); children.hasNext();) {
            UIComponent child = children.next();
            // Check for id uniqueness
            String componentId = child.getClientId(facesContext);
            if (componentIds.add(componentId)) {
                checkIdUniqueness(facesContext, child, componentIds);
            } else {
                throw new IllegalStateException("Duplicate component id found in view: " + componentId);
            }
        }
    }

    /**
     * Inner class used to store a facet in the saved component tree.
     */
    private static final class FacetNode extends TreeNode {

        /**
         * Stores the serial version UID.
         */
        private static final long serialVersionUID = -3777170310958005106L;

        /**
         * Stores the facet name.
         */
        private String facetName;

        /**
         * Constructor.
         */
        public FacetNode() {
        }

        /**
         * Constructor.
         *
         * @param parent the parent
         * @param name the facet name
         * @param component the component
         */
        public FacetNode(int parent, String name, UIComponent component) {
            super(parent, component);
            facetName = name;
        }

        /**
         * Read the facet node in.
         *
         * @param in the object input
         * @throws IOException when an I/O error occurs
         * @throws ClassNotFoundException when the class could not be found
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            facetName = in.readUTF();
        }

        /**
         * Write the facet node out.
         *
         * @param out the object output
         * @throws IOException when an I/O error occurs
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeUTF(facetName);
        }
    }

    /**
     * Inner class used to store a node in the saved component tree.
     */
    private static class TreeNode implements Externalizable {

        /**
         * Stores the serial version UID.
         */
        private static final long serialVersionUID = -835775352718473281L;
        /**
         * Stores the NULL_ID constant.
         */
        private static final String NULL_ID = "";

        /**
         * Stores the component type.
         */
        private String componentType;

        /**
         * Stores the id.
         */
        private String id;

        /**
         * Stores the parent.
         */
        private int parent;

        /**
         * Constructor.
         */
        public TreeNode() {
        }

        /**
         * Constructor.
         *
         * @param parent the parent
         * @param component the component
         */
        public TreeNode(int parent, UIComponent component) {
            this.parent = parent;
            this.id = component.getId();
            this.componentType = component.getClass().getName();

        }

        /**
         * Read the tree node in.
         *
         * @param in the object input
         * @throws IOException when an I/O error occurs
         * @throws ClassNotFoundException when the class could not be found
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            parent = in.readInt();
            componentType = in.readUTF();
            id = in.readUTF();
            if (id.length() == 0) {
                id = null;
            }
        }

        /**
         * Write the tree node out.
         *
         * @param out the object output
         * @throws IOException when an I/O error occurs
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(parent);
            out.writeUTF(componentType);
            out.writeUTF(Objects.requireNonNullElse(id, NULL_ID));
        }

        public int getParent() {
            return parent;
        }
    }
}
