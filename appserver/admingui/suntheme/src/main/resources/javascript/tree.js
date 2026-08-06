/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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

define([
    "webui/suntheme/props",
    "webui/suntheme/common"
], function (props, common) {

    return {
        addOnInitCallback: common.addOnInitCallback,
        /**
         * This function is used to initialize HTML element properties with the
         * following Object literals.
         *getValidCookieName
         * <ul>
         *  <li>id</li>
         * </ul>getValidCookieNamegetValidCookieName
         *
         * Note: This is considered a private API, do not use.
         *
         * @param props Key-Value pairs of properties.
         */
        init: function (props) {
            if (props === null || props.id === null) {
                return false;
            }
            var domNode = document.getElementById(props.id);
            if (domNode === null) {
                return false;
            }
            if(common.fireInitCallBacks(domNode)){
                return true;
            }

            // Set given properties on domNode.
            Object.extend(domNode, props);

            // Set functions.
            domNode.clearAllHighlight = this.clearAllHighlight;
            domNode.clearHighlight = this.clearHighlight;
            domNode.expandCollapse = this.expandCollapse;
            domNode.expandTurner = this.expandTurner;
            domNode.findContainingTreeNode = this.findContainingTreeNode;
            domNode.findNodeByTypeAndProp = this.findNodeByTypeAndProp;
            domNode.badCookieChars = this.badCookieChars;
            domNode.getValidCookieName = this.getValidCookieName;
            domNode.getCookieValue = this.getCookieValue;
            domNode.getHighlightTreeBgColor = this.getHighlightTreeBgColor;
            domNode.getHighlightTreeTextColor = this.getHighlightTreeTextColor;
            domNode.getNormalTreeTextColor = this.getNormalTreeTextColor;
            domNode.getParentTreeNode = this.getParentTreeNode;
            domNode.getSelectedTreeNode = this.getSelectedTreeNode;
            domNode.getTree = this.getTree;
            domNode.highlight = this.highlight;
            domNode.highlightParent = this.highlightParent;
            domNode.isAnHref = this.isAnHref;
            domNode.isTreeHandle = this.isTreeHandle;
            domNode.onTreeNodeClick = this.onTreeNodeClick;
            domNode.selectTreeNode = this.selectTreeNode;
            domNode.setCookieValue = this.setCookieValue;
            domNode.treecontent_submit = this.treecontent_submit;
            domNode.treeNodeIsExpanded = this.treeNodeIsExpanded;
            domNode.unhighlightParent = this.unhighlightParent;
            domNode.updateHighlight = this.updateHighlight;
            common.setInitialized(domNode);
        },

        badCookieChars: ["(", ")", "<", ">", "@", ",", ";", ":", "\\", "\"", "/", "[", "]", "?", "=", "{", "}", " ", "\t"],

        /**
         * Ensure we use a RFC 2109 compliant cookie name.
         */
        getValidCookieName: function (name) {
            for (var idx = 0; idx < this.badCookieChars.length; idx++) {
                name = name.replace(this.badCookieChars[idx], "_");
            }
            return name;
        },

        setCookieValue: function (cookieName, val) {

            /*
             Fix for bug 6476859 :On initial visit to page, 
             getting selected node returns value from previous 
             session.
             The cookie value should be stored with the global path 
             so that it is applicable for all pages on site.
             */

            cookieName = this.getValidCookieName(cookieName);
            document.cookie = cookieName + "=" + val + ";path=/;";
        },

        getCookieValue: function (cookieName) {
            cookieName = this.getValidCookieName(cookieName);
            var docCookie = document.cookie;
            var pos = docCookie.indexOf(cookieName + "=");

            if (pos === -1) {
                return null;
            }

            var start = pos + cookieName.length + 1;
            var end = docCookie.indexOf(";", start);

            if (end === -1) {
                end = docCookie.length;
            }
            return docCookie.substring(start, end);
        },

        /**
         *  This function expands or collapses the given tree node.  It expects the
         * source of the given event object (if supplied) to be a tree handle
         * image.  It will change this image to point in the correct direction
         * (right or down).  This implementation depends on the tree handle image
         * names including "tree_handleright" and "tree_handledown" in them.
         * Swapping "right" and "down" in these names must change the handle
         * direction to right and down respectively.
         */
        expandCollapse: function (treeNode, imageId, event) {
            var tree = this.getTree(treeNode);
            var childNodes = document.getElementById(treeNode.id + "_children");
            if (childNodes) {
                // Get the event source
                if (!event) {
                    event = window.event;
                }
                var elt = document.getElementById(imageId);

                // First, unhighlight the parent if applicable
                this.unhighlightParent(this.getSelectedTreeNode(tree.id));

                // Change the style to cause the expand / collapse & switch the image
                var display = childNodes.style.display;
                if (display === "none") {
                    childNodes.style.display = "block";
                    if (elt && elt.src) {
                        elt.src = elt.src.replace("tree_handleright",
                                "tree_handledown");
                    }
                } else {
                    childNodes.style.display = "none";
                    if (elt && elt.src) {
                        elt.src = elt.src.replace("tree_handledown",
                                "tree_handleright");
                    }
                }

                // Last, update the visible parent of the selected node if now hidden
                this.highlightParent(this.getSelectedTreeNode(tree.id));
            }
        },

        /**
         * This function returns the Tree for the given TreeNode.  From
         * a DOM point of view, the tree directly contains all its children
         * (excluding *_children div tags.  This will return the first
         * parentNode that is a div w/ an id != "*_children".
         */
        getTree: function (treeNode) {
            var tree = treeNode.parentNode;
            var SUFFIX = "_children";

            while (tree) {
                // Ignore all div's ending w/ SUFFIX
                if ((tree.nodeName === "DIV")
                        && (tree.id.substr(tree.id.length - SUFFIX.length) !== SUFFIX)) {
                    break;
                }
                tree = tree.parentNode;
            }
            return tree;
        },

        /**
         * This method handles TreeNode onClick events.  It takes the TreeNode
         * &lt;div&gt; object that was clicked on in order to process the
         * highlighting changes that are necessary.  This object may be obtained by
         * calling <code>getElementById("&lt;TreeNode.getClientId()&gt;")</code>.
         * If this function is invoked from the TreeNode &lt;div&gt; object itself
         * (as is the case when this method is implicitly called), the TreeNode
         * object is simply the <code>this</code> variable.
         */
        onTreeNodeClick: function (treeNode, imageId, event) {
            // Check for Tree Handles
            if (this.isTreeHandle(event)) {
                this.expandCollapse(treeNode, imageId, event);
                return true;
            }

            // Make sure they clicked on an href
            if (!this.isAnHref(event)) {
                // Do nothing
                return true;
            }

            // If we're here, we should select the TreeNode
            return this.selectTreeNode(treeNode.id);
        },

        /**
         * This function may be used to select the given TreeNode. 
         * It will clear the previous TreeNode and select the 
         * given one. The parameter passed to the function is
         * the id of the currently selected treeNode - the
         * value of the tree component from a JSF perspective.
         * This node may not be availbe in the DOM tree as the
         * user may have clicked on the expanded parent node
         * and closed it. In this case the parent node needs
         * to show up in bold.
         */
        selectTreeNode: function (treeNodeId) {
            // Find the top of the tree
            var treeNode = document.getElementById(treeNodeId);

            if (treeNode) {
                var tree = this.getTree(treeNode);

                // Clear the old highlighting
                this.clearAllHighlight(tree.id);

                // Mark the node as selected
                this.setCookieValue(tree.id + "-hi", treeNode.id);

                // first highlight is as a parent
                // when the left frame loads the nodes will 
                // be hightlighted correctly
                this.highlightParent(treeNode);
                this.highlight(treeNode);

                // onClick handler should proceed with hyperlink
                return true;

            } else {

                // get the parent node ID and highlight the parent.
                // var arr = treeNodeId.split(":");
                // var lastBitOfId = arr[arr.length - 1].length;
                // var lastIndex = treeNodeId.length - lastBitOfId.length;
                var lastIndex = treeNodeId.lastIndexOf(":");
                parentNodeId = treeNodeId.substr(0, lastIndex);
                var parentNode =
                        document.getElementById(parentNodeId);
                if (parentNode) {
                    parentNode.style.fontWeight = "bold";
                }
                return false;
            }
        },

        /**
         * This function returns the selected TreeNode given the treeId of the
         * Tree.
         */
        getSelectedTreeNode: function (treeId) {
            var id = this.getCookieValue(treeId + "-hi");
            if (id) {
                return document.getElementById(id);
            }
            return null;
        },

        clearAllHighlight: function (cookieId) {
            // Clear
            var selectedNode = this.getSelectedTreeNode(cookieId);
            this.clearHighlight(selectedNode);
            this.setCookieValue(cookieId + "-hi", "");

            // FIXME: Fix this...
            // this.clearHighlight(document.getElementById(currentHighlightParent));
            return true;
        },

        clearHighlight: function (node) {
            if (node) {
                node.className = "TreeRow_sun4";
            }
            return true;
        },

        /**
         * This function determines if the event source was a tree handle image.
         * This implementation depends on the tree handle image file name
         * containing "tree_handle" and no other images containing this
         * string.
         */
        isTreeHandle: function (event) {
            if (!event) {
                event = window.event;
                if (!event) {
                    return false;
                }
            }
            var elt = (event.target) ? event.target : event.srcElement;

            // Ignore Tree Handles b/c they should not update highlighting
            if (elt.nodeName === "IMG") {
                var url = new String(elt.src).valueOf();
                if ((url.indexOf("tree_handle") > 0) && (url.indexOf("theme") > 0)) {
                    // This is a tree handle
                    return true;
                }
            } else if (elt.nodeName === "A") {
                // might have been user pressing enter on a around image
                if (elt.innerHTML.toLowerCase().indexOf("<img") === 0) {
                    // This is a tree handle
                    return true;
                }
            }
            // Not a tree handle
            return false;
        },

        /**
         * This method checks to see if the event.target is an href, or if any of
         * the parent nodes which contain it is an href.  To be an href, it must be
         * an "A" tag with an "href" attribute containing atleast 4 characters.
         * (Note: Browsers will add on the protocol if you supply a relative URL
         *     such as one starting with a '#', '/', or filename).
         */
        isAnHref: function (event) {
            if (!event) {
                event = window.event;
                if (!event) {
                    return false;
                }
            }
            var elt = (event.target) ? event.target : event.srcElement;

            // Look for parent href
            while (elt !== null) {
                if (elt.nodeName === "A") {
                    // Creates a String containing the url
                    var url = new String(elt).valueOf();
                    if (url.length > 4) {
                        // All URLs are atleast this long
                        return true;
                    }
                }
                elt = elt.parentNode;
            }
            // Not an href
            return false;
        },

        /**
         * This function updates the highlighting for the given Tree client id.
         * This function provides a way to restore the highlighting when a Tree is
         * reloaded in a window (necessary each page load).
         */
        updateHighlight: function (cookieId) {
            var selNode = this.getSelectedTreeNode(cookieId)
            this.highlight(selNode);

            // FIXME: This doesn't work if the TreeNode element doesn't exist 
            // (which is the case for the server-side tree)
            this.highlightParent(selNode);
        },

        /**
         * This function highlights the given <code>TreeNode</code>.  The
         * <code>obj</code> passed in is actually the &lt;div&gt; around the html
         * for the <code>TreeNode</code> and may be obtained by calling
         * <code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
         */
        highlight: function (node) {
            if (node) {
                node.className = "TreeSelRow_sun4";
                return true;
            }
            return false;
        },

        /**
         * This function finds a node of the given type w/ matching property name
         * and value by looking recursively deep at the children of the given
         * node.  The type is the type of node to find (i.e. "IMG").  The propName
         * is the name of the property to match (i.e. "src" on an "IMG" node).
         * The propVal is the value that must be contained in propName; this value
         * does not have to match exactly, it only needs to exist within the
         * property.
         */
        findNodeByTypeAndProp: function (node, type, propName, propVal) {
            if (node === null) {
                return null;
            }
            // First check to see if node is what we are looking for...
            if (node.nodeName === type) {
                if (node[propName].indexOf(propVal) > -1) {
                    return node;
                }
            }
            // Not what we want, walk its children if any
            var nodeList = node.childNodes;
            if (!nodeList || (nodeList.length === 0)) {
                return null;
            }
            var result;
            for (var count = 0; count < nodeList.length; count++) {
                // Recurse
                result = this.findNodeByTypeAndProp(nodeList[count], type, propName, propVal);
                if (result) {
                    // Propagate the result
                    return result;
                }
            }
            // Not found
            return null;
        },

        /**
         * This function determines if the given TreeNode is expanded.  It returns
         * <code>true</code> if it is, <code>false</code> otherwise.
         */
        treeNodeIsExpanded: function (treeNode) {
            // Find the div containing the tree images for this TreeNode row
            var node = document.getElementById(treeNode.id + "LineImages");
            node = this.findNodeByTypeAndProp(node, "IMG", "src", "tree_handle");
            if (!node) {
                // This shouldn't happen, but if it does return true b/c nothing
                // happens in this case
                return true;
            }
            // If the image contains this string, it is not expanded
            return (node.src.indexOf("tree_handleright") === -1);
        },

        /**
         * This function returns the parent TreeNode of the given TreeNode.
         */
        getParentTreeNode: function (treeNode) {
            // Get the parent id
            var parentId = treeNode.parentNode.id;
            var childrenIdx = parentId.indexOf("_children");
            if (childrenIdx === -1) {
                return null;
            }
            // This is really a peer div id to what we really want... remove _children
            parentId = parentId.substring(0, childrenIdx);
            // Return the parent TreeNode
            return document.getElementById(parentId);
        },

        unhighlightParent: function (childNode) {
            if (!childNode) {
                return false;
            }

            // First find the parent node and make sure it is collapsed (we don't
            // highlight parent nodes when the selected node is visible)
            var parentNode = this.getParentTreeNode(childNode);
            var highlight = null;
            while (parentNode !== null) {
                if (!this.treeNodeIsExpanded(parentNode)) {
                    highlight = parentNode;
                }
                parentNode = this.getParentTreeNode(parentNode);
            }
            if (highlight) {
                highlight.style.fontWeight = "normal";
            }
            return true;
        },

        highlightParent: function (childNode) {
            if (!childNode) {
                return false;
            }

            // First find the parent node and make sure it is collapsed (we don't
            // highlight parent nodes when the selected node is visible)
            var parentNode = this.getParentTreeNode(childNode);
            var highlight = null;
            while (parentNode !== null) {
                if (!this.treeNodeIsExpanded(parentNode)) {
                    highlight = parentNode;
                }
                parentNode = this.getParentTreeNode(parentNode);
            }
            if (highlight) {
                highlight.style.fontWeight = "bold";
            }
            return true;
        },

        getNormalTreeTextColor: function () {
            return "#003399";
        },

        getHighlightTreeBgColor: function () {
            return "#CBDCAF";  // ~greenish color
        },

        getHighlightTreeTextColor: function () {
            return "#000000";  // black
        },

        /**
         * Returns the TreeNode that contains the given link. This assumes the link
         * is a direct child of the node.
         */
        findContainingTreeNode: function (link) {
            var linkId = link.id;
            var nodeId = linkId.substring(0, linkId.lastIndexOf(":"));
            return document.getElementById(nodeId);
        },

        /**
         * If the Tree's expandOnSelect property is true, this method is called to 
         * expand the turner of the tree node with the given labelLink.
         */
        expandTurner: function (labelLink, turnerId, imageId, isClientSide, event) {
            var labelLinkId = labelLink.id;
            var formId = labelLinkId.substring(0, labelLinkId.indexOf(":"));
            var node = this.findContainingTreeNode(labelLink);
            var turnerLink = document.getElementById(turnerId);

            if (turnerLink === null) {
                return;
            }
            if (!this.treeNodeIsExpanded(node)) {
                // folder is currently closed, expand it
                if (isClientSide) {
                    this.expandCollapse(node, imageId, event);
                } else {
                    turnerLink.onclick();
                }
            }
        },

        /**
         * When the tree node link has an associated action, this method 
         * should
         * be called to ensure selection highlighting and (if necessary) node expansion
         * occurs.
         *
         * If the developer specifies the content facet for a given TreeNode, he should
         * call this function from his facet hyperlink's onClick.
         */
        treecontent_submit: function (nodeId) {
            if (nodeId === null) {
                return false;
            }
            var node = document.getElementById(nodeId);
            var tree = this.getTree(node);

            // update the current selection
            this.selectTreeNode(node.id);

            // set a cookie that the Tree's decode method will 
            // inspect and expand the corresponding node if necessary
            this.setCookieValue(tree.id + "-expand", nodeId);
        }
    };
});
