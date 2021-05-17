/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: ScrollerComponent.java,v 1.3 2004/11/14 07:33:13 tcfujii Exp $
 */

package components.components;

import components.renderkit.Util;

import jakarta.faces.component.UICommand;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.event.ActionEvent;

import java.io.IOException;
import java.util.Map;

/**
 * This component produces a search engine style scroller that facilitates
 * easy navigation over results that span across several pages. It
 * demonstrates how a component can do decoding and encoding
 * without delegating it to a renderer.
 */
public class ScrollerComponent extends UICommand {

    public static final String NORTH = "NORTH";
    public static final String SOUTH = "SOUTH";
    public static final String EAST = "EAST";
    public static final String WEST = "WEST";
    public static final String BOTH = "BOTH";

    public static final int ACTION_NEXT = -1;
    public static final int ACTION_PREVIOUS = -2;
    public static final int ACTION_NUMBER = -3;

    public static final String FORM_NUMBER_ATTR = "com.sun.faces.FormNumber";

    /**
     * The component attribute that tells where to put the user supplied
     * markup in relation to the "jump to the Nth page of results"
     * widget.
     */
    public static final String FACET_MARKUP_ORIENTATION_ATTR =
        "navFacetOrientation";


    public ScrollerComponent() {
        super();
        this.setRendererType(null);
    }


    public void decode(FacesContext context) {
        String curPage = null;
        String action = null;
        int actionInt = 0;
        int currentPage = 1;
        int currentRow = 1;
        String clientId = getClientId(context);
        Map requestParameterMap = (Map) context.getExternalContext().
            getRequestParameterMap();
        action = (String) requestParameterMap.get(clientId + "_action");
        if (action == null || action.length() == 0) {
            // nothing to decode
            return;
        }
        MethodBinding mb = Util.createConstantMethodBinding(action);

        this.getAttributes().put("action", mb);
        curPage = (String) requestParameterMap.get(clientId + "_curPage");
        currentPage = Integer.valueOf(curPage).intValue();

        // Assert that action's length is 1.
        switch (actionInt = Integer.valueOf(action).intValue()) {
            case ACTION_NEXT:
                currentPage++;
                break;
            case ACTION_PREVIOUS:
                currentPage--;
                // Assert 1 < currentPage
                break;
            default:
                currentPage = actionInt;
                break;
        }
        // from the currentPage, calculate the current row to scroll to.
        currentRow = (currentPage - 1) * getRowsPerPage(context);
        this.getAttributes().put("currentPage", new Integer(currentPage));
        this.getAttributes().put("currentRow", new Integer(currentRow));
        this.queueEvent(new ActionEvent(this));
    }


    public void encodeBegin(FacesContext context) throws IOException {
        return;
    }


    public void encodeEnd(FacesContext context) throws IOException {
        int currentPage = 1;

        ResponseWriter writer = context.getResponseWriter();

        String clientId = getClientId(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage.intValue();
        }
        int totalPages = getTotalPages(context);

        writer.write("<table border=\"0\" cellpadding=\"0\" align=\"center\">");
        writer.write("<tr align=\"center\" valign=\"top\">");
        writer.write(
            "<td><font size=\"-1\">Result&nbsp;Page:&nbsp;</font></td>");

        // write the Previous link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_PREVIOUS,
                             (1 < currentPage));
        // last arg is true iff we're not the first page
        writer.write("</td>");

        // render the page navigation links
        int i = 0;
        int first = 1;
        int last = totalPages;

        if (10 < currentPage) {
            first = currentPage - 10;
        }
        if ((currentPage + 9) < totalPages) {
            last = currentPage + 9;
        }
        for (i = first; i <= last; i++) {
            writer.write("<td>");
            writeNavWidgetMarkup(context, clientId, i, (i != currentPage));
            writer.write("</td>");
        }

        // write the Next link if necessary
        writer.write("<td>");
        writeNavWidgetMarkup(context, clientId, ACTION_NEXT,
                             (currentPage < totalPages));
        writer.write("</td>");
        writer.write("</tr>");
        writer.write(getHiddenFields(clientId));
        writer.write("</table>");
    }


    public boolean getRendersChildren() {
        return true;
    }


    /**
     * <p>Return the component family for this component.</p>
     */
    public String getFamily() {

        return ("Scroller");

    }

    //
    // Helper methods
    //

    /**
     * Write the markup to render a navigation widget.  Override this to
     * replace the default navigation widget of link with something
     * else.
     */
    protected void writeNavWidgetMarkup(FacesContext context,
                                        String clientId,
                                        int navActionType,
                                        boolean enabled) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String facetOrientation = NORTH;
        String facetName = null;
        String linkText = null;
        String localLinkText = null;
        UIComponent facet = null;
        boolean isCurrentPage = false;
        boolean isPageNumber = false;

        // Assign values for local variables based on the navActionType
        switch (navActionType) {
            case ACTION_NEXT:
                facetName = "next";
                linkText = "Next";
                break;
            case ACTION_PREVIOUS:
                facetName = "previous";
                linkText = "Previous";
                break;
            default:
                facetName = "number";
                linkText = "" + navActionType;
                isPageNumber = true;
                // heuristic: if navActionType is number, and we are not
                // enabled, this must be the current page.
                if (!enabled) {
                    facetName = "current";
                    isCurrentPage = true;
                }
                break;
        }

        // leverage any navigation facets we have
        writer.write("\n&nbsp;");
        if (enabled) {
            writer.write("<a " + getAnchorAttrs(context, clientId,
                                                navActionType) + ">");
        }

        facet = getFacet(facetName);
        // render the facet pertaining to this widget type in the NORTH
        // and WEST cases.
        if (facet != null) {
            // If we're rendering a "go to the Nth page" link
            if (isPageNumber) {
                // See if the user specified an orientation
                String facetO = (String) getAttributes().get(
                    FACET_MARKUP_ORIENTATION_ATTR);
                if (facet != null) {
                    facetOrientation = facetO;
                    // verify that the orientation is valid
                    if (!(facetOrientation.equalsIgnoreCase(NORTH) ||
                        facetOrientation.equalsIgnoreCase(SOUTH) ||
                        facetOrientation.equalsIgnoreCase(EAST) ||
                        facetOrientation.equalsIgnoreCase(WEST))) {
                        facetOrientation = NORTH;
                    }
                }
            }

            // output the facet as specified in facetOrientation
            if (facetOrientation.equalsIgnoreCase(NORTH) ||
                facetOrientation.equalsIgnoreCase(EAST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
            // The difference between NORTH and EAST is that NORTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(NORTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
        }

        // if we have a facet, only output the link text if
        // navActionType is number
        if (null != facet) {
            if (navActionType != ACTION_NEXT &&
                navActionType != ACTION_PREVIOUS) {
                writer.write(linkText);
            }
        } else {
            writer.write(linkText);
        }

        // output the facet in the EAST and SOUTH cases
        if (null != facet) {
            if (facetOrientation.equalsIgnoreCase(SOUTH)) {
                writer.startElement("br", null); // PENDING(craigmcc)
                writer.endElement("br");
            }
            // The difference between SOUTH and WEST is that SOUTH
            // requires a <br>.
            if (facetOrientation.equalsIgnoreCase(SOUTH) ||
                facetOrientation.equalsIgnoreCase(WEST)) {
                facet.encodeBegin(context);
                if (facet.getRendersChildren()) {
                    facet.encodeChildren(context);
                }
                facet.encodeEnd(context);
            }
        }

        if (enabled) {
            writer.write("</a>");
        }

    }


    /**
     * <p>Build and return the string consisting of the attibutes for a
     * result set navigation link anchor.</p>
     *
     * @param context  the FacesContext
     * @param clientId the clientId of the enclosing UIComponent
     * @param action   the value for the rhs of the =
     *
     * @return a String suitable for setting as the value of a navigation
     *         href.
     */
    private String getAnchorAttrs(FacesContext context, String clientId,
                                  int action) {
        int currentPage = 1;
        int formNumber = getFormNumber(context);
        Integer curPage = (Integer) getAttributes().get("currentPage");
        if (curPage != null) {
            currentPage = curPage.intValue();
        }
        String result =
            "href=\"#\" " +
            "onmousedown=\"" +
            "document.forms[" + formNumber + "]['" + clientId +
            "_action'].value='" +
            action +
            "'; " +
            "document.forms[" + formNumber + "]['" + clientId +
            "_curPage'].value='" +
            currentPage +
            "'; " +
            "document.forms[" + formNumber + "].submit()\"";

        return result;
    }


    private String getHiddenFields(String clientId) {
        String result =
            "<input type=\"hidden\" name=\"" + clientId + "_action\"/>\n" +
            "<input type=\"hidden\" name=\"" + clientId + "_curPage\"/>";

        return result;
    }


    // PENDING: avoid doing this each time called.  Perhaps
    // store in our own attr?
    protected UIForm getForm(FacesContext context) {
        UIComponent parent = this.getParent();
        while (parent != null) {
            if (parent instanceof UIForm) {
                break;
            }
            parent = parent.getParent();
        }
        return (UIForm) parent;
    }


    protected int getFormNumber(FacesContext context) {
        Map requestMap = context.getExternalContext().getRequestMap();
        int numForms = 0;
        Integer formsInt = null;
        // find out the current number of forms in the page.
        if (null != (formsInt = (Integer)
            requestMap.get(FORM_NUMBER_ATTR))) {
            numForms = formsInt.intValue();
// since the form index in the document starts from 0.
            numForms--;
        }
        return numForms;
    }


    /**
     * Returns the total number of pages in the result set based on
     * <code>rows</code> and <code>rowCount</code> of <code>UIData</code>
     * component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getTotalPages(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        int rowsPerPage = uiData.getRows();
        int totalRows = 0;
        int result = 0;
        totalRows = uiData.getRowCount();
        result = totalRows / rowsPerPage;
        if (0 != (totalRows % rowsPerPage)) {
            result++;
        }
        return result;
    }


    /**
     * Returns the number of rows to display by looking up the
     * <code>UIData</code> component that this scroller is associated with.
     * For the purposes of this demo, we are assuming the <code>UIData</code> to
     * be child of <code>UIForm</code> component and not nested inside a custom
     * NamingContainer.
     */
    protected int getRowsPerPage(FacesContext context) {
        String forValue = (String) getAttributes().get("for");
        UIData uiData = (UIData) getForm(context).findComponent(forValue);
        if (uiData == null) {
            return 0;
        }
        return uiData.getRows();
    }
}
