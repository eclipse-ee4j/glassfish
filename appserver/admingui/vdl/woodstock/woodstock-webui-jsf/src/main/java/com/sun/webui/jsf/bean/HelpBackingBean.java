/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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
package com.sun.webui.jsf.bean;

import com.sun.webui.jsf.component.Button;
import com.sun.webui.jsf.component.HelpWindow;
import com.sun.webui.jsf.component.Hyperlink;
import com.sun.webui.jsf.component.Markup;
import com.sun.webui.jsf.component.PanelGroup;
import com.sun.webui.jsf.component.StaticText;
import com.sun.webui.jsf.component.TextField;
import com.sun.webui.jsf.component.Tree;
import com.sun.webui.jsf.component.TreeNode;
import com.sun.webui.jsf.theme.ThemeImages;
import com.sun.webui.jsf.theme.ThemeStyles;
import com.sun.webui.jsf.util.ClientSniffer;
import com.sun.webui.jsf.util.HelpUtils;
import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;
import com.sun.webui.theme.Theme;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.help.SearchTOCItem;
import javax.help.TreeItem;
import javax.swing.tree.DefaultMutableTreeNode;

import static com.sun.webui.jsf.util.ConversionUtilities.convertValueToString;

/**
 * This class defines a backing bean required for use by the HelpWindow
 * component.
 */
@RequestScoped
@Named("JavaHelpBean")
public final class HelpBackingBean {

    /**
     * Help content tree id.
     */
    public static final String HELP_CONTENTS_TREE_ID = "helpContentsTree";

    /**
     * Help index tree id.
     */
    public static final String HELP_INDEX_TREE_ID = "helpIndexTree";

    /**
     * content frame name.
     */
    public static final String CONTENT_FRAME_NAME = "contentFrame";

    /**
     * The theme.
     */
    private Theme theme;

    /**
     * The content tree.
     */
    private Tree contentsTree;

    /**
     * The index tree.
     */
    private Tree indexTree;

    /**
     * The search panel.
     */
    private PanelGroup searchPanel;

    /**
     * The search results panel.
     */
    private PanelGroup searchResultsPanel;

    /**
     * The search field.
     */
    private TextField searchField;

    /**
     * The search button.
     */
    private Button searchButton;

    /**
     * The help utils.
     */
    private HelpUtils helpUtils;

    /**
     * Tips link.
     */
    private Hyperlink tipsLink;

    /**
     * Help set path.
     */
    private String helpSetPath = "";

    /**
     * JSP path.
     */
    private String jspPath;

    /**
     * Localized help path.
     */
    private String localizedHelpPath;

    /**
     * HTTP port.
     */
    private int httpPort = -1;

    /**
     * Request scheme.
     */
    private String requestScheme;

    /**
     * Navigator URL.
     */
    private String navigatorUrl;

    /**
     * Bottom frame URL.
     */
    private String bottomFrameUrl;

    /**
     * Button frame URL.
     */
    private String buttonFrameUrl;

    /**
     * Button class name.
     */
    private String buttonClassName;

    /**
     * Inline help class name.
     */
    private String inlineHelpClassName;

    /**
     * Search class name.
     */
    private String searchClassName;

    /**
     * Step tab class name.
     */
    private String stepTabClassName;

    /**
     * Title class name.
     */
    private String titleClassName;

    /**
     * Body class name.
     */
    private String bodyClassName;

    /**
     * Tip head title.
     */
    private String tipsHeadTitle;

    /**
     * Tips title.
     */
    private String tipsTitle;

    /**
     * Tips improve.
     */
    private String tipsImprove;

    /**
     * Tips improve #1.
     */
    private String tipsImprove1;

    /**
     * Tips improve #2.
     */
    private String tipsImprove2;

    /**
     * Tips improve #3.
     */
    private String tipsImprove3;

    /**
     * Tips improve #4.
     */
    private String tipsImprove4;

    /**
     * Tips note.
     */
    private String tipsNote;

    /**
     * Tips note details.
     */
    private String tipsNoteDetails;

    /**
     * Tips search.
     */
    private String tipsSearch;

    /**
     * Tips search #1.
     */
    private String tipsSearch1;

    /**
     * Tips search #2.
     */
    private String tipsSearch2;

    /**
     * Tips search #3.
     */
    private String tipsSearch3;

    /**
     * Tips search #4.
     */
    private String tipsSearch4;

    /**
     * Back button text.
     */
    private String backButtonText;

    /**
     * Forward button text.
     */
    private String forwardButtonText;

    /**
     * Print button text.
     */
    private String printButtonText;

    /**
     * Contents text.
     */
    private String contentsText;

    /**
     * Index text.
     */
    private String indexText;

    /**
     * Search text.
     */
    private String searchText;

    /**
     * Close label.
     */
    private String closeLabel;

    /**
     * Search label.
     */
    private String searchLabel;

    /**
     * Nav frame title.
     */
    private String navFrameTitle;

    /**
     * Button frame title.
     */
    private String buttonFrameTitle;

    /**
     * Content frame title.
     */
    private String contentFrameTitle;

    /**
     * noFrames.
     */
    private String noFrames;

    /**
     * Button nav head title.
     */
    private String buttonNavHeadTitle;

    /**
     * navigator head title.
     */
    private String navigatorHeadTitle;

    /**
     * Button body class name.
     */
    private String buttonBodyClassName;

    /**
     * Creates a new instance of HelpBackingBean.
     */
    public HelpBackingBean() {
    }

    /**
     * Get the navigator URL.
     *
     * @return String
     */
    public String getNavigatorUrl() {
        if (navigatorUrl != null) {
            return navigatorUrl;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder urlBuffer = new StringBuilder();

        // set navigatorUrl
        String zeJspPath = getJspPath();
        if (zeJspPath != null) {
            urlBuffer.append("/").append(zeJspPath);
        }
        urlBuffer.append(HelpWindow.DEFAULT_JSP_PATH)
                .append(HelpWindow.DEFAULT_NAVIGATION_JSP);
        navigatorUrl = context
                .getApplication()
                .getViewHandler()
                .getActionURL(context, urlBuffer.toString());
        return navigatorUrl;
    }

    /**
     * Set the navigator URL.
     *
     * @param url new navigator URL
     */
    public void setNavigatorUrl(final String url) {
        navigatorUrl = url;
    }

    /**
     * Get the bottom frame URL.
     *
     * @return String
     */
    public String getBottomFrameUrl() {
        if (bottomFrameUrl != null) {
            return bottomFrameUrl;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder urlBuffer = new StringBuilder();
        String zeJspPath = getJspPath();
        if (zeJspPath != null) {
            urlBuffer.append("/").append(zeJspPath);
        }
        urlBuffer.append(HelpWindow.DEFAULT_JSP_PATH)
                .append(HelpWindow.DEFAULT_BUTTONFRAME_JSP);
        bottomFrameUrl = context.getApplication().getViewHandler()
                .getActionURL(context, urlBuffer.toString());
        return bottomFrameUrl;
    }

    /**
     * Set the bottom frame URL.
     *
     * @param url new bottom frame URL
     */
    public void setBottomFrameUrl(final String url) {
        bottomFrameUrl = url;
    }

    /**
     * Get the button frame URL.
     *
     * @return String
     */
    public String getButtonFrameUrl() {
        if (buttonFrameUrl != null) {
            return buttonFrameUrl;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder urlBuffer = new StringBuilder();
        String zeJspPath = getJspPath();
        if (zeJspPath != null) {
            urlBuffer.append("/").append(zeJspPath);
        }
        urlBuffer.append(HelpWindow.DEFAULT_JSP_PATH)
                .append(HelpWindow.DEFAULT_BUTTONNAV_JSP);
        buttonFrameUrl = context.getApplication().getViewHandler()
                .getActionURL(context, urlBuffer.toString());
        return buttonFrameUrl;
    }

    /**
     * Set the button frame URL.
     *
     * @param url new button frame URL
     */
    public void setButtonFrameUrl(final String url) {
        buttonFrameUrl = url;
    }

    /**
     * Get the scheme that will be used for help set requests The default is
     * "http".
     *
     * @return The request scheme used for JavaHelp requests.
     */
    public String getRequestScheme() {
        return requestScheme;
    }

    /**
     * Set the scheme that will be used for help set requests.
     *
     * @param scheme The request scheme to use for JavaHelp requests (i.e.
     * {@code "https"}).
     */
    public void setRequestScheme(final String scheme) {
        requestScheme = scheme;
    }

    /**
     * Get the value of the helpSetPath managed bean property.
     *
     * @return The value of the helpSetPath managed bean property.
     */
    public String getHelpSetPath() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        String realPath = ec.getRequestContextPath();

        if (helpSetPath != null && helpSetPath.length() > 0) {
            realPath = realPath.concat(HelpUtils.URL_SEPARATOR)
                    .concat(helpSetPath)
                    .concat(HelpUtils.URL_SEPARATOR);
        }
        return realPath;
    }

    /**
     * Set the value of the helpSetPath managed bean property.
     *
     * @param path The value of the helpSetPath managed bean property.
     */
    public void setHelpSetPath(final String path) {
        helpSetPath = path;
    }

    /**
     * Get the value of the jspPath managed bean property.
     *
     * @return The value of the jspPath managed bean property.
     */
    public String getJspPath() {
        return jspPath;
    }

    /**
     * Set the value of the jspPath managed bean property.
     *
     * @param path The value of the jspPath managed bean property.
     */
    public void setJspPath(final String path) {
        jspPath = path;
    }

    /**
     * Convenience method to get the current instance of HelpUtils, initializing
     * a new instance if necessary.
     *
     * @return The current instance of HelpUtils.
     */
    private HelpUtils getHelpUtils() {
        if (helpUtils == null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) facesContext
                    .getExternalContext()
                    .getRequest();

            String requestHelpSetPath = request.getParameter("helpSetPath");
            if (requestHelpSetPath != null) {
                setHelpSetPath(requestHelpSetPath);
            }

            helpUtils = new HelpUtils(request, getHelpSetPath(), httpPort,
                    getRequestScheme());
        }
        return helpUtils;
    }

    /**
     * This method will initialize the Tree component and populate it with the
     * nodes returned by the help system.
     *
     * @param tree tree to initialize
     * @param treeList initial values
     */
    protected void initTree(final Tree tree, final ArrayList treeList) {

        // create a HashMap to store the nodes we create
        HashMap<String, TreeNode> uiNodeMap = new HashMap<String, TreeNode>();

        // JavaHelp will return swing tree nodes, we must convert to ui nodes
        DefaultMutableTreeNode javaNode;
        javax.swing.tree.TreeNode parentJavaNode;
        TreeNode uiNode;
        TreeNode uiParent;
        TreeItem item;
        int numUiNodes = -1;
        HashMap<String, String> nodeIDMap = new HashMap<String, String>();
        boolean tocTree = tree.getId().equals(HELP_CONTENTS_TREE_ID);

        int nTreeNodes = treeList.size();

        for (int i = 0; i < nTreeNodes; i++) {
            // get the jtree node from the list
            javaNode = (DefaultMutableTreeNode) treeList.get(i);
            if (javaNode == null) {
                // TODO - debug
                continue;
            }

            // get the help data associated with this help jtree node
            item = (TreeItem) javaNode.getUserObject();
            if (item == null) {
                // TODO - debug
                continue;
            }

            // need to add a TreeNode for this help item
            numUiNodes++;

            // assign a unique id to use for the JSF TreeNode component id
            String jsfId = "node" + Integer.toString(numUiNodes);

            // get the java help assigned id
            String javaNodeId = getHelpUtils().getID(javaNode);

            // Save the javaNodeId to jsfID mapping so we can retrieve parent
            // help tree nodes later. Nodes are added to parent objects below
            // based on the parent ID indicated in the node object.
            // store a mapping of the java help node id to our jsf node id
            nodeIDMap.put(javaNodeId, jsfId);

            // Get the parent ID for this tree node. This is used below to get
            // the parent node object from the tree model. The current
            // node will then be added to the existing parent node.
            parentJavaNode = javaNode.getParent();
            String parentJavaNodeId = getHelpUtils().getID(parentJavaNode);

            String label = item.getName();
            uiNode = new com.sun.webui.jsf.component.TreeNode();
            uiNode.setTarget(CONTENT_FRAME_NAME);
            uiNode.setId(jsfId);
            uiNode.setText(label);

            String url = getHelpUtils().getContentURL(item);
            if (getHttpPort() != -1) {
                int portStartIndex = url.indexOf(':', url.indexOf(':') + 1) + 1;
                int portEndIndex = url.indexOf('/', portStartIndex);
                String port = url.substring(portStartIndex, portEndIndex);
                url = url.replaceFirst(port, String.valueOf(getHttpPort()));
            }

            uiNode.setUrl(url);
            uiParent = (com.sun.webui.jsf.component.TreeNode) uiNodeMap
                    .get(nodeIDMap.get(parentJavaNodeId));

            if (uiParent != null) {
                uiParent.getChildren().add(uiNode);
                uiParent.setExpanded(true);
            } else {
                tree.getChildren().add(uiNode);
            }

            // the JSF ui node has been constructed and added to the tree... now
            // save a mapping of the uiNodeId to the actual
            // com.sun.webui.jsf.component.TreeNode itself for retrieving parent
            // nodes later
            uiNodeMap.put(uiNode.getId(), uiNode);
        }
    }

    /**
     * Initialize the search results panel group.
     */
    private void initSearchResultsPanel() {
        searchResultsPanel = new PanelGroup();
        searchResultsPanel.setId("searchResultsPanel");
    }

    /**
     * Convenience method to return the current Theme.
     * @return Theme
     */
    private Theme getTheme() {
        if (theme == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            theme = ThemeUtilities.getTheme(context);
        }
        return theme;
    }

    /**
     * Initialize the search panel that lays out the search tab.
     */
    private void initSearchPanel() {
        searchPanel = new PanelGroup();
        searchPanel.setId("searchPanel");
        searchPanel.setRendered(false);
    }

    /**
     * The action method invoked when the contents tab is clicked.
     */
    public void contentsTabClicked() {
        getContentsTree().setRendered(true);
        getIndexTree().setRendered(false);
        getSearchPanel().setRendered(false);
    }

    /**
     * The action method invoked when the index tab is clicked.
     */
    public void indexTabClicked() {
        getContentsTree().setRendered(false);
        getIndexTree().setRendered(true);
        getSearchPanel().setRendered(false);
    }

    /**
     * The action method invoked when the search tab is clicked.
     */
    public void searchTabClicked() {
        getContentsTree().setRendered(false);
        getIndexTree().setRendered(false);
        getSearchPanel().setRendered(true);
    }

    /**
     * The action method invoked when the search button is clicked.
     */
    public void doSearch() {
        // get the string the user wants to search for
        Markup mu = (Markup) searchPanel.getChildren().get(0);
        TextField f = (TextField) mu.getChildren().get(0);
        String zeSearchText = convertValueToString(f, f.getValue());

        // get the search results panel child list and clear it
        List<UIComponent> resultsKids = getSearchResultsPanel().getChildren();
        resultsKids.clear();
        Theme zeTheme = getTheme();
        Enumeration searchResults = getHelpUtils().doSearch(zeSearchText);

        if (searchResults == null || !searchResults.hasMoreElements()) {
            // search text produced no results
            StaticText text = new StaticText();
            text.setId("noResults");
            text.setStyleClass(
                    zeTheme.getStyleClass(ThemeStyles.HELP_RESULT_DIV));
            text.setText(zeTheme.getMessage("help.noResultsFound"));
            resultsKids.add(text);
            return;
        }

        int linkId = 0;
        Hyperlink resultLink;
        SearchTOCItem item;
        while (searchResults.hasMoreElements()) {
            item = (SearchTOCItem) searchResults.nextElement();
            Markup div = new Markup();
            div.setId("div" + linkId);
            div.setTag("div");
            div.setStyle("padding-top:6px; white-space: nowrap");
            resultLink = new Hyperlink();
            resultLink.setId("searchLink" + linkId++);
            resultLink.setUrl(item.getURL().toString());
            resultLink.setText(item.getName());
            resultLink.setTarget(CONTENT_FRAME_NAME);
            resultLink.setStyleClass(
                    theme.getStyleClass(ThemeStyles.HELP_RESULT_DIV));
            div.getChildren().add(resultLink);
            resultsKids.add(div);
        }
    }

    /**
     * Returns the index tab tree.
     *
     * @return The Tree component for the index tree.
     */
    public Tree getIndexTree() {
        if (indexTree == null) {
            indexTree = new Tree();
            indexTree.setId(HELP_INDEX_TREE_ID);
            ClientSniffer cs = ClientSniffer
                    .getInstance(FacesContext.getCurrentInstance());
            if (cs.isIe6up()) {
                contentsTree.setStyle("width:40em;");
            }
            initTree(indexTree, getHelpUtils().getIndexTreeList());
            indexTree.setRendered(false);
        }
        return indexTree;
    }

    /**
     * Sets the index tab tree.
     *
     * @param tree The Tree to use for the index tab tree.
     */
    public void setIndexTree(final Tree tree) {
        indexTree = tree;
    }

    /**
     * Returns the contents tab tree.
     *
     * @return The Tree component for the contents tab.
     */
    public Tree getContentsTree() {
        if (contentsTree == null) {
            contentsTree = new Tree();
            contentsTree.setId(HELP_CONTENTS_TREE_ID);
            ClientSniffer cs = ClientSniffer
                    .getInstance(FacesContext.getCurrentInstance());
            if (cs.isIe6up()) {
                contentsTree.setStyle("width:40em;");
            }
            initTree(contentsTree, getHelpUtils().getTOCTreeList());
        }
        return contentsTree;
    }

    /**
     * Sets the contents tab tree.
     *
     * @param tree The Tree to use for the contents tab tree.
     */
    public void setContentsTree(final Tree tree) {
        contentsTree = tree;
    }

    /**
     * Get the PanelGroup to use for the search tab content.
     *
     * @return The PanelGroup to use for the search tab content.
     */
    public PanelGroup getSearchPanel() {
        if (searchPanel == null) {
            initSearchPanel();
        }
        return searchPanel;
    }

    /**
     * Set the PanelGroup to use for the search tab content.
     *
     * @param panel The PanelGroup to use for the search tab content.
     */
    public void setSearchPanel(final PanelGroup panel) {
        searchPanel = panel;
    }

    /**
     * Get the PanelGroup to use for the search results.
     *
     * @return The PanelGroup to use for the search results.
     */
    public PanelGroup getSearchResultsPanel() {
        if (searchResultsPanel == null) {
            initSearchResultsPanel();
        }
        return searchResultsPanel;
    }

    /**
     * Set the PanelGroup to use for the search results.
     *
     * @param panel The PanelGroup to use for the search results.
     */
    public void setSearchResultsPanel(final PanelGroup panel) {
        searchResultsPanel = panel;
    }

    /**
     * Get the tips link.
     * @return Hyperlink
     */
    public Hyperlink getTipsLink() {
        if (tipsLink == null) {
            // init the tips on searching link
            tipsLink = new Hyperlink();
            tipsLink.setId("searchTipsLink");
            tipsLink.setText(getTheme().getMessage("help.tips"));
            StringBuilder tipsUrlBuffer = new StringBuilder();
            String zJspPath = getJspPath();
            if (zJspPath != null) {
                tipsUrlBuffer.append(zJspPath);
            }
            tipsUrlBuffer.append(HelpWindow.DEFAULT_JSP_PATH)
                    .append(HelpWindow.DEFAULT_TIPS_FILE);
            FacesContext context = FacesContext.getCurrentInstance();
            tipsLink.setUrl(context.getApplication().getViewHandler()
                    .getActionURL(context, tipsUrlBuffer.toString()));
            tipsLink.setTarget(CONTENT_FRAME_NAME);
        }
        return tipsLink;
    }

    /**
     * Set the tips link.
     * @param link new tips link
     */
    public void setTipsLink(final Hyperlink link) {
        tipsLink = link;
    }

    /**
     * Get the tips title.
     * @return String
     */
    public String getTipsTitle() {
        tipsTitle = getTheme().getMessage("help.tips");
        return tipsTitle;
    }

    /**
     * Get the tips improve.
     * @return String
     */
    public String getTipsImprove() {
        tipsImprove = getTheme().getMessage("help.tipsImprove");
        return tipsImprove;
    }

    /**
     * Get the tips improve #1.
     * @return String
     */
    public String getTipsImprove1() {
        tipsImprove1 = getTheme().getMessage("help.tipsImprove1");
        return tipsImprove1;
    }

    /**
     * Get the tips improve #2.
     * @return String
     */
    public String getTipsImprove2() {
        tipsImprove2 = getTheme().getMessage("help.tipsImprove2");
        return tipsImprove2;
    }

    /**
     * Get the tips improve #3.
     * @return String
     */
    public String getTipsImprove3() {
        tipsImprove3 = getTheme().getMessage("help.tipsImprove3");
        return tipsImprove3;
    }

    /**
     * Get the tips improve #4.
     * @return String
     */
    public String getTipsImprove4() {
        tipsImprove4 = getTheme().getMessage("help.tipsImprove4");
        return tipsImprove4;
    }

    /**
     * Get the tips note.
     * @return String
     */
    public String getTipsNote() {
        tipsNote = getTheme().getMessage("help.tipsNote");
        return tipsNote;
    }

    /**
     * Get the tips note details.
     * @return String
     */
    public String getTipsNoteDetails() {
        tipsNoteDetails = getTheme().getMessage("help.tipsNoteDetails");
        return tipsNoteDetails;
    }

    /**
     * Get the tips search.
     * @return String
     */
    public String getTipsSearch() {
        tipsSearch = getTheme().getMessage("help.tipsSearch");
        return tipsSearch;
    }

    /**
     * Get the tips search #1.
     * @return String
     */
    public String getTipsSearch1() {
        tipsSearch1 = getTheme().getMessage("help.tipsSearch1");
        return tipsSearch1;
    }

    /**
     * Get the tips search #2.
     * @return String
     */
    public String getTipsSearch2() {
        tipsSearch2 = getTheme().getMessage("help.tipsSearch2");
        return tipsSearch2;
    }

    /**
     * Get the tips search #3.
     * @return String
     */
    public String getTipsSearch3() {
        tipsSearch3 = getTheme().getMessage("help.tipsSearch3");
        return tipsSearch3;
    }

    /**
     * Get the tips search #4.
     * @return String
     */
    public String getTipsSearch4() {
        tipsSearch4 = getTheme().getMessage("help.tipsSearch4");
        return tipsSearch4;
    }

    /**
     * Get the back button text.
     * @return String
     */
    public String getBackButtonText() {
        backButtonText = getTheme().getMessage("help.backButtonTitle");
        return backButtonText;
    }

    /**
     * Get the forward button text.
     * @return String
     */
    public String getForwardButtonText() {
        forwardButtonText = getTheme().getMessage("help.forwardButtonTitle");
        return forwardButtonText;
    }

    /**
     * Get the print button text.
     * @return String
     */
    public String getPrintButtonText() {
        printButtonText = getTheme().getMessage("help.printButtonTitle");
        return printButtonText;
    }

    /**
     * Get the contents text.
     * @return String
     */
    public String getContentsText() {
        contentsText = getTheme().getMessage("help.contentsTab");
        return contentsText;
    }

    /**
     * Get the index text.
     * @return String
     */
    public String getIndexText() {
        indexText = getTheme().getMessage("help.indexTab");
        return indexText;
    }

    /**
     * Get the search text.
     * @return String
     */
    public String getSearchText() {
        searchText = getTheme().getMessage("help.searchTab");
        return searchText;
    }

    /**
     * Get the localized help path.
     * @return String
     */
    public String getLocalizedHelpPath() {
        return getHelpUtils().getLocalizedHelpPath();
    }

    /**
     * Set the localized help path.
     * @param path new path
     */
    public void setLocalizedHelpPath(final String path) {
        localizedHelpPath = path;
    }

    /**
     * Get the back button icon.
     * @return String
     */
    public String getBackButtonIcon() {
        return ThemeImages.HELP_BACK;
    }

    /**
     * Get the forward button icon.
     * @return String
     */
    public String getForwardButtonIcon() {
        return ThemeImages.HELP_FORWARD;
    }

    /**
     * Get the print button icon.
     * @return String
     */
    public String getPrintButtonIcon() {
        return ThemeImages.HELP_PRINT;
    }

    /**
     * Get the HTTP port.
     * @return int
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Set the HTTP port.
     * @param port new port
     */
    public void setHttpPort(final int port) {
        httpPort = port;
    }

    /**
     * Get the search label.
     * @return String
     */
    public String getSearchLabel() {
        if (searchLabel == null) {
            searchLabel = getTheme()
                    .getMessage("help.searchButton");
        }
        return searchLabel;
    }

    /**
     * Set the search label.
     * @param label new label
     */
    public void setSearchLabel(final String label) {
        searchLabel = label;
    }

    /**
     * Get the nav frame title.
     * @return String
     */
    public String getNavFrameTitle() {
        if (navFrameTitle == null) {
            navFrameTitle = getTheme()
                    .getMessage("help.navFrameTitle");
        }
        return navFrameTitle;
    }

    /**
     * Set the nav frame title.
     * @param title new title
     */
    public void setNavFrameTitle(final String title) {
        navFrameTitle = title;
    }

    /**
     * Get the button frame title.
     * @return String
     */
    public String getButtonFrameTitle() {
        if (buttonFrameTitle == null) {
            buttonFrameTitle = getTheme()
                    .getMessage("help.buttonFrameTitle");
        }
        return buttonFrameTitle;
    }

    /**
     * Set the button frame title.
     * @param title new title
     */
    public void setButtonFrameTitle(final String title) {
        buttonFrameTitle = title;
    }

    /**
     * Get the content frame title.
     * @return String
     */
    public String getContentFrameTitle() {
        if (contentFrameTitle == null) {
            contentFrameTitle = getTheme()
                    .getMessage("help.contentFrameTitle");
        }
        return contentFrameTitle;
    }

    /**
     * Set the content frame title.
     * @param title new title
     */
    public void setContentFrameTitle(final String title) {
        contentFrameTitle = title;
    }

    /**
     * Get the no frame value.
     * @return String
     */
    public String getNoFrames() {
        if (noFrames == null) {
            noFrames = getTheme().getMessage("help.noframes");
        }
        return noFrames;
    }

    /**
     * Set the no frame value.
     * @param newNoFrames noFrames
     */
    public void setNoFrames(final String newNoFrames) {
        noFrames = newNoFrames;
    }

    /**
     * Get the button nav head title.
     * @return String
     */
    public String getButtonNavHeadTitle() {
        if (buttonNavHeadTitle == null) {
            buttonNavHeadTitle = getTheme()
                    .getMessage("help.buttonNavHeadTitle");
        }
        return buttonNavHeadTitle;
    }

    /**
     * Set the button nav head title.
     * @param title new title
     */
    public void setButtonNavHeadTitle(final String title) {
        buttonNavHeadTitle = title;
    }

    /**
     * Get the navigator head title.
     * @return String
     */
    public String getNavigatorHeadTitle() {
        if (navigatorHeadTitle == null) {
            navigatorHeadTitle = getTheme()
                    .getMessage("help.navigatorHeadTitle");
        }
        return navigatorHeadTitle;
    }

    /**
     * Set the navigator head title.
     * @param title new title
     */
    public void setNavigatorHeadTitle(final String title) {
        navigatorHeadTitle = title;
    }

    /**
     * Get the tips head title.
     * @return String
     */
    public String getTipsHeadTitle() {
        if (tipsHeadTitle == null) {
            tipsHeadTitle = getTheme().getMessage("help.tips");
        }
        return tipsHeadTitle;
    }

    /**
     * Set the tips head title.
     * @param title new title
     */
    public void setTipsHeadTitle(final String title) {
        tipsHeadTitle = title;
    }

    /**
     * Check the specified parameter.
     * @param paramName parameter name
     * @return int
     */
    private int checkParam(final String paramName) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map parms = context.getExternalContext().getRequestParameterMap();
        String paramValue = (String) parms.get(paramName);
        try {
            if (paramValue != null && Integer.parseInt(paramValue) != -1) {
                return Integer.parseInt(paramValue);
            }
        } catch (NumberFormatException nfe) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info(HelpBackingBean.class, "WEBUI0007",
                        new String[]{paramName});
            }
        }
        return -1;
    }

    /**
     * Used in navigator.jsp and tips.jsp.
     * @return String
     */
    public String getBodyClassName() {
        return bodyClassName;
    }

    /**
     * Get button class name.
     * @return String
     */
    public String getButtonClassName() {
        buttonClassName = getTheme()
                .getStyleClass(ThemeStyles.HELP_BUTTON_DIV);
        return buttonClassName;
    }

    /**
     * Get the inline help class name.
     * @return String
     */
    public String getinlineHelpClassName() {
        inlineHelpClassName = getTheme()
                .getStyleClass(ThemeStyles.HELP_FIELD_TEXT);
        return inlineHelpClassName;
    }

    /**
     * Get the search class name.
     * @return String
     */
    public String getSearchClassName() {
        searchClassName = getTheme()
                .getStyleClass(ThemeStyles.HELP_SEARCH_DIV);
        return searchClassName;
    }

    /**
     * Get the step tab class name.
     * @return String
     */
    public String getStepTabClassName() {
        stepTabClassName = getTheme()
                .getStyleClass(ThemeStyles.HELP_STEP_TAB);
        return stepTabClassName;
    }

    /**
     * Get the title class name.
     * @return String
     */
    public String getTitleClassName() {
        titleClassName = getTheme()
                .getStyleClass(ThemeStyles.TITLE_LINE);
        return titleClassName;
    }

    /**
     * Get the button body class name.
     * @return String
     */
    public String getbuttonBodyClassName() {
        bodyClassName = getTheme()
                .getStyleClass(ThemeStyles.HELP_BODY);
        return buttonBodyClassName;
    }
}
