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

import com.sun.data.provider.RowKey;
import com.sun.faces.annotation.Component;
import com.sun.faces.annotation.Property;
import com.sun.webui.jsf.component.TableRowGroup.WrapperEvent;
import com.sun.webui.jsf.util.MessageUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.UIForm;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.FacesEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * The Form component is used to create a form element.
 */
@Component(type = "com.sun.webui.jsf.Form",
        family = "com.sun.webui.jsf.Form",
        displayName = "Form", tagName = "form",
        helpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_form",
        //CHECKSTYLE:OFF
        propertiesHelpKey = "projrave_ui_elements_palette_wdstk-jsf1.2_propsheets_form_props")
        //CHECKSTYLE:ON
public final class Form extends UIForm {

    /**
     * The virtual form that was submitted.
     */
    private VirtualFormDescriptor submittedVirtualForm;

    /**
     * Virtual form delimiter #1.
     */
    private static final String VF_DELIM_1 = ",";

    /**
     * Virtual format delimiter #2.
     */
    private static final String VF_DELIM_2 = "|";

    /**
     * Id separator.
     */
    private static final String ID_SEP
            = String.valueOf(NamingContainer.SEPARATOR_CHAR);

    /**
     * Id wildcard character.
     */
    public static final char ID_WILD_CHAR = '*';

    /**
     * Id wildcard string.
     */
    private static final String ID_WILD = String.valueOf(ID_WILD_CHAR);

    /**
     * Has an EditableValueHolder as the key, and an Object[] value pair or a
     * TableValues as the value.
     */
    private transient Map<EditableValueHolder, Object> erasedMap
            = new HashMap<EditableValueHolder, Object>();

    /**
     * Contains EditableValueHolders with a retain status different from the
     * default.
     */
    private transient Set<EditableValueHolder> nonDefaultRetainStatusEvhs
            = new HashSet<EditableValueHolder>();

    /**
     * Default for whether non-participating submitted values are retained.
     */
    private static final boolean DEFAULT_RETAIN_STATUS = true;

    /**
     * Use this non-XHTML compliant {@code boolean} attribute to turn off auto
     * completion feature of Internet Explorer and Firefox browsers. Set to
     * "false" to turn off completion. The default is "true".
     */
    @Property(name = "autoComplete",
            displayName = "Auto Complete",
            category = "Behavior")
    private boolean autoComplete;

    /**
     * autocomplete set flag.
     */
    private boolean autoCompleteSet;

    /**
     * Use this attribute to set the content-type of the HTTP request generated
     * by this form. You do not normally need to set this attribute. Its default
     * value is application/x-www-form-urlencoded. If there is an upload tag
     * inside the form, the upload tag will modify the form's enctype attribute
     * to multipart/form-data.
     */
    @Property(name = "enctype",
            displayName = "Content Type of the Request",
            category = "Advanced")
    private String enctype;

    /**
     * The virtual forms used "internally" by components (such as Table).
     * Component authors can manipulate this set of virtual forms independent of
     * the set exposed to developers. This set is only consulted after the set
     * exposed to developers is consulted. A participating or submitting id can
     * end in ":*" to indicate descendants. For example, table1:* can be used as
     * a participating or submitting id to indicate all the descendants of
     * table1.
     */
    @Property(name = "internalVirtualForms",
            displayName = "Internal Virtual Form Descriptors",
            category = "Advanced",
            isHidden = false,
            isAttribute = false)
    private VirtualFormDescriptor[] internalVirtualForms;

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     */
    @Property(name = "onClick",
            displayName = "Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onClick;

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     */
    @Property(name = "onDblClick",
            displayName = "Double Click Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onDblClick;

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     */
    @Property(name = "onKeyDown",
            displayName = "Key Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyDown;

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     */
    @Property(name = "onKeyPress",
            displayName = "Key Press Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyPress;

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     */
    @Property(name = "onKeyUp",
            displayName = "Key Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onKeyUp;

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseDown",
            displayName = "Mouse Down Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseDown;

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     */
    @Property(name = "onMouseMove",
            displayName = "Mouse Move Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseMove;

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     */
    @Property(name = "onMouseOut",
            displayName = "Mouse Out Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOut;

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     */
    @Property(name = "onMouseOver",
            displayName = "Mouse In Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseOver;

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     */
    @Property(name = "onMouseUp",
            displayName = "Mouse Up Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onMouseUp;

    /**
     * Scripting code executed when this form is reset.
     */
    @Property(name = "onReset",
            displayName = "Form Reset Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onReset;

    /**
     * Scripting code executed when this form is submitted.
     */
    @Property(name = "onSubmit",
            displayName = "Form Submit Script",
            category = "Javascript",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.JavaScriptPropertyEditor")
            //CHECKSTYLE:ON
    private String onSubmit;

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
     * Use this attribute to set the target of the XHTML form tag.
     */
    @Property(name = "target",
            displayName = "Target",
            category = "Behavior",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.webui.jsf.component.propertyeditors.FrameTargetsEditor")
            //CHECKSTYLE:ON
    private String target;

    /**
     * The virtual forms within this literal form, represented as an array of
     * Form.VirtualFormDescriptor objects. This property and the
     * "virtualFormsConfig" property are automatically kept in-sync.
     */
    @Property(name = "virtualForms",
            displayName = "Virtual Form Descriptors",
            category = "Advanced",
            isHidden = true,
            isAttribute = false)
    private VirtualFormDescriptor[] virtualForms;

    /**
     * The configuration of the virtual forms within this literal form,
     * represented as a String. Each virtual form is described by three parts,
     * separated with pipe ("|") characters: the virtual form name, a
     * space-separated list of component ids that participate in the virtual
     * form, and a space-separated list of component ids that submit the virtual
     * form. Multiple such virtual form "descriptors" are separated by commas.
     * The component ids may be qualified (for instance,
     * "table1:tableRowGroup1:tableColumn1:textField1").
     */
    @Property(name = "virtualFormsConfig",
            displayName = "Virtual Forms Configuration",
            category = "Advanced",
            //CHECKSTYLE:OFF
            editorClassName = "com.sun.rave.propertyeditors.StringPropertyEditor")
            //CHECKSTYLE:ON
    private String virtualFormsConfig;

    /**
     * Use the visible attribute to indicate whether the component should be
     * viewable by the user in the rendered HTML page.
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
     * Default constructor.
     */
    public Form() {
        super();
        setRendererType("com.sun.webui.jsf.Form");
    }

    @Override
    public String getFamily() {
        return "com.sun.webui.jsf.Form";
    }

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
     */
    @Property(name = "rendered")
    @Override
    public void setRendered(final boolean rendered) {
        super.setRendered(rendered);
    }

    /**
     * Override {@code UIForm.processDecodes(FacesContext)} to ensure correct
     * virtual form processing.
     *
     * @param context {@code FacesContext} for the current request
     *
     * @exception NullPointerException Thrown when {@code context} is null
     */
    @Override
    public void processDecodes(final FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        submittedVirtualForm = null;
        erasedMap.clear();
        //clearing out nonDefaultRetainStatusEvhs occurs in
        // restoreNonParticipatingSubmittedValues
        //(which is called during renderering)
        //so that application code can muck with the retain statuses in
        // preprocess

        // Process this component itself
        // may set submittedVirtualForm
        decode(context);

        // if we're not the submitted form, don't process children.
        if (!isSubmitted()) {
            return;
        }

        // Process all facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            kid.processDecodes(context);
        }

        if (submittedVirtualForm != null) {
            //if the children of the Form are known to participate in
            // submittedVirtualForm,
            //then don't bother erasing
            if (!childrenAreKnownToParticipate(this, submittedVirtualForm)) {
                eraseVirtualFormNonParticipants(this, null, null);
            }
        }
    }

    @Override
    public void queueEvent(final FacesEvent event) {
        FacesEvent relevantEvent = event;
        if (event instanceof WrapperEvent) {
            WrapperEvent wrapperEvent = (WrapperEvent) event;
            relevantEvent = wrapperEvent.getFacesEvent();
        }
        if (relevantEvent instanceof ActionEvent
                && submittedVirtualForm == null) {
            UIComponent sourceComp = relevantEvent.getComponent();
            VirtualFormDescriptor virtualFormComponentSubmits =
                    Form.this.getVFormCompSubmits(sourceComp);
            if (virtualFormComponentSubmits != null) {
                // if the source component does in fact submit a virtual form
                // then set that virtual form as having been submitted
                submittedVirtualForm = virtualFormComponentSubmits;
            }
        }
        super.queueEvent(event);
    }

    /**
     * Set the submitted virtual form.
     * @param vfd virtual format descriptor
     */
    public void setSubmittedVirtualForm(final VirtualFormDescriptor vfd) {
        if (submittedVirtualForm == null && vfd != null) {
            submittedVirtualForm = vfd;
        }
    }

    /**
     * Set the specified virtual forms.
     * @param vfds virtual form descriptors
     */
    public void setVirtualForms(final VirtualFormDescriptor[] vfds) {
        setVirtualForms(vfds, true);
    }

    /**
     * Set and synchronize the specified virtual forms.
     * @param vfds virtual form descriptors
     * @param sync sync flag
     */
    private void setVirtualForms(final VirtualFormDescriptor[] vfds,
            final boolean sync) {

        doSetVirtualForms(vfds);
        if (sync) {
            String configStr = generateVFormsConfig(vfds);
            setVirtualFormsConfig(configStr, false);
        }
    }

    /**
     * Set the virtual forms config.
     * @param configStr config string
     */
    public void setVirtualFormsConfig(final String configStr) {
        setVirtualFormsConfig(configStr, true);
    }

    /**
     * Set and synchronize the virtual form config.
     * @param configStr config string
     * @param sync sync flag
     */
    private void setVirtualFormsConfig(final String configStr,
            final boolean sync) {

        doSetVirtualFormsConfig(configStr);
        if (sync) {
            VirtualFormDescriptor[] vfds = generateVirtualForms(configStr);
            setVirtualForms(vfds, false);
        }
    }

    /**
     * Get the virtual form submits by fully qualified Id.
     * @param fqId fully qualified id
     * @return VirtualFormDescriptor
     */
    private VirtualFormDescriptor getVFormCompSubmitsByFQId(final String fqId) {

        if (!isValidFullyQualifiedId(fqId)) {
            return null;
        }

        //first try regular configuration
        VirtualFormDescriptor vfd = getVFormCompSubmitsByFQId(fqId,
                getVirtualForms());
        if (vfd != null) {
            return vfd;
        }

        //try internal configuration
        vfd = getVFormCompSubmitsByFQId(fqId, getInternalVirtualForms());
        return vfd;
    }

    /**
     * Get the virtual form submits by fully qualified Id.
     * @param fqId fully qualified id
     * @param vfds virtual form descriptors
     * @return VirtualFormDescriptor
     */
    private VirtualFormDescriptor getVFormCompSubmitsByFQId(final String fqId,
            final VirtualFormDescriptor[] vfds) {

        if (vfds == null || vfds.length < 1) {
            return null;
        }

        //look for matches of fqId against all of vfds's submitters--without
        // any trailing wilds.
        // if no match found, try the parent fqId.
        // this technique ensures that the most appropriate submitter is found
        // first and its vf is returned
        String currentFqId = fqId;
        while (currentFqId.length() > 0) {
            for (int v = 0; v < vfds.length; v++) {
                VirtualFormDescriptor vfd = vfds[v];
                String[] submitters = vfd.getSubmittingIds();
                for (int s = 0; submitters != null
                        && s < submitters.length; s++) {
                    String submitter = submitters[s];
                    if (submitter == null) {
                        continue;
                    }
                    String wildSuffix = ID_SEP + ID_WILD;
                    if (submitter.endsWith(wildSuffix)) {
                        submitter = submitter.substring(0, submitter.length()
                                - wildSuffix.length());
                    }
                    if (submitter.length() < 1) {
                        continue;
                    }
                    boolean fqIdMatches = fQIdMatchesPattern(
                            currentFqId, submitter);
                    if (fqIdMatches) {
                        return vfd;
                    }
                }
            }
            int lastIndexOfSep = currentFqId.lastIndexOf(ID_SEP);
            currentFqId = currentFqId.substring(0, lastIndexOfSep);
        }
        return null;
    }

    /**
     * Get the virtual form count.
     * @return int
     */
    private int getVirtualFormCount() {
        VirtualFormDescriptor[] vfds = getVirtualForms();
        VirtualFormDescriptor[] ivfds = getInternalVirtualForms();
        int count;
        if (vfds == null) {
            count = 0;
        } else {
            count = vfds.length;
        }
        if (ivfds == null) {
            return count;
        } else {
            return count + ivfds.length;
        }
    }

    /**
     * Get virtual form component submits.
     * @param component UI component
     * @return VirtualFormDescriptor
     */
    private VirtualFormDescriptor getVFormCompSubmits(
            final UIComponent component) {

        if (getVirtualFormCount() < 1) {
            return null;
        }
        String fqId = getFullyQualifiedId(component);
        VirtualFormDescriptor vfd = getVFormCompSubmitsByFQId(fqId);
        return vfd;
    }

    /**
     * Get the virtual form submitted by the component whose id is provided or
     * null if the component does not submit a virtual form.
     * @param id fully qualified id
     * @return VirtualFormDescriptor
     */
    public VirtualFormDescriptor getVFormCompSubmits(final String id) {
        if (getVirtualFormCount() < 1) {
            return null;
        }
        if (isValidFullyQualifiedId(id)) {
            return getVFormCompSubmitsByFQId(id);
        }
        UIComponent component = findComponentById(id);
        return Form.this.getVFormCompSubmits(component);
    }

    /**
     * Given a bare, partially qualified, or fully qualified id, find the
     * component. Unlike the inherited {@code findComponent} method, this method
     * does recursively search NamingContainers.
     * @param id component Id
     * @return UIComponent
     */
    public UIComponent findComponentById(final String id) {
        if (id == null) {
            return null;
        }
        if (id.length() == 0
                || id.endsWith(ID_WILD)
                || (!id.equals(ID_SEP)
                && id.endsWith(ID_SEP))) {
            return null;
        }
        // see if id indicates the Form itself
        String fqId = getFullyQualifiedId(this);
        if (fQIdMatchesPattern(fqId, id)) {
            return this;
        }
        return searchKidsRecursivelyForId(this, id);
    }

    /**
     * Search children recursively.
     * @param parent parent to traverse.
     * @param id component id
     * @return UIComponent
     */
    private UIComponent searchKidsRecursivelyForId(
            final UIComponent parent, final String id) {

        Iterator kids = parent.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            String fqId = getFullyQualifiedId(kid);
            //see if id indicates kid
            boolean fqIdMatches = fQIdMatchesPattern(fqId, id);
            if (fqIdMatches) {
                return kid;
            }
            UIComponent match = searchKidsRecursivelyForId(kid, id);
            if (match != null) {
                return match;
            }
        }
        return null;
    }

    /**
     * Return true if a virtual form has been submitted and this component
     * participates in that virtual form.
     * @param component UI component
     * @return {@code boolean}
     */
    private boolean participatesInSubmittedVirtualForm(
            final UIComponent component) {

        if (submittedVirtualForm == null) {
            return false;
        }
        String fqId = getFullyQualifiedId(component);
        return submittedVirtualForm.hasParticipant(fqId);
    }

    // Be sure to keep this method in sync with the version in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl).
    /**
     * Generate an array of {@code VirtualFormDescriptor}s based on a virtual
     * form configuration {@code String}.
     * @param configStr config string
     * @return VirtualFormDescriptor[]
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static VirtualFormDescriptor[] generateVirtualForms(
            final String configStr) {

        // formname1|pid1 pid2 pid3|sid1 sid2 sid3, formname2|pid4 pid5 pid6
        // |sid4 sid5 sid6
        if (configStr == null) {
            return null;
        }
        String cfg = configStr;
        cfg = cfg.trim();
        if (cfg.length() < 1) {
            return new VirtualFormDescriptor[0];
        }
        //cfg now can't be null, blank, or just ws

        StringTokenizer st = new StringTokenizer(cfg, VF_DELIM_1);
        // list of marshalled vfs
        List<String> vfs = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            //not null, but could be just whitespace or blank
            String vf = st.nextToken();
            vf = vf.trim();
            //vf could be a blank string
            if (vf.length() > 0) {
                vfs.add(vf);
            }
        }

        // a list of VirtualFormDescriptors
        List<VirtualFormDescriptor> descriptors =
                new ArrayList<VirtualFormDescriptor>();
        // go through each marshalled vf
        for (int i = 0; i < vfs.size(); i++) {
            // get the marshalled vf. not mere ws, blank, or null.
            String vf = (String) vfs.get(i);
            st = new StringTokenizer(vf, VF_DELIM_2);
            // part1 is vf name, part2 is participating ids, part3 is
            // submitting ids
            String[] parts = new String[3];
            int partIndex = 0;
            while (partIndex < parts.length && st.hasMoreTokens()) {
                // not null, but could be whitespace or blank
                String part = st.nextToken();
                // now can't be whitespace, but could be blank
                part = part.trim();
                if (part.length() > 0) {
                    // part is not null, whitespace, or blank
                    parts[partIndex] = part;
                }
                partIndex++;
            }

            VirtualFormDescriptor vfd;
            if (parts[0] != null) {
                vfd = new VirtualFormDescriptor();
                // won't be null, blank, or just ws
                vfd.setName(parts[0]);
                descriptors.add(vfd);
            } else {
                // this marshalled vf has no name. can't create a descriptor
                // for it. go to next marshalled vf
                continue;
            }

            if (parts[1] != null) {
                // not null, blank, or just ws
                String pidString = parts[1];
                st = new StringTokenizer(pidString);
                List<String> pidList = new ArrayList<String>();
                while (st.hasMoreTokens()) {
                    String pid = st.nextToken();
                    pidList.add(pid.trim());
                }
                // size guaranteed to be at least 1
                String[] pids = (String[]) pidList
                        .toArray(new String[pidList.size()]);
                vfd.setParticipatingIds(pids);
            }

            if (parts[2] != null) {
                // not null, blank, or just ws
                String sidString = parts[2];
                st = new StringTokenizer(sidString);
                List<String> sidList = new ArrayList<String>();
                while (st.hasMoreTokens()) {
                    String sid = st.nextToken();
                    sidList.add(sid.trim());
                }
                // size guaranteed to be at least 1
                String[] sids = (String[]) sidList
                        .toArray(new String[sidList.size()]);
                vfd.setSubmittingIds(sids);
            }
        }
        // might be of size 0, but won't be null
        return (VirtualFormDescriptor[]) descriptors
                .toArray(new VirtualFormDescriptor[descriptors.size()]);
    }

    // Be sure to keep this method in sync with the version in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl).
    /**
     * Generate a virtual form configuration {@code String} based on an array of
     * {@code VirtualFormDescriptor}s.
     * @param descs virtual form descriptors
     * @return String
     */
    public static String generateVFormsConfig(
            final VirtualFormDescriptor[] descs) {

        if (descs == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (VirtualFormDescriptor desc : descs) {
            if (desc != null) {
                String vf = desc.toString();
                if (vf.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append(" , ");
                    }
                    sb.append(vf);
                }
            }
        }
        return sb.toString();
    }

    // Be sure to keep this method in sync with the versions in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl) and  {@code com.sun.webui.jsf.component.FormDesignInfo}
    // (in webui).
    /**
     * Obtain the virtual form compatible fully-qualified id for the supplied
     * component. A fully-qualified id begins with the
     * {@code NamingContainer.SEPARATOR_CHAR} (representing the {@code Form}
     * itself), contains component ids of the component's ancestors separated by
     * {@code NamingContainer.SEPARATOR_CHAR}, and ends with the component's id.
     * @param component UI component
     * @return String
     */
    public static String getFullyQualifiedId(final UIComponent component) {
        if (component == null) {
            return null;
        }
        if (component instanceof Form) {
            return ID_SEP;
        }
        String compId = component.getId();
        if (compId == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(compId);
        UIComponent currentComp = component.getParent();
        boolean formEncountered = false;
        while (currentComp != null) {
            sb.insert(0, ID_SEP);
            if (currentComp instanceof Form) {
                formEncountered = true;
                break;
            } else {
                String currentCompId = currentComp.getId();
                if (currentCompId == null) {
                    return null;
                }
                sb.insert(0, currentCompId);
            }
            currentComp = currentComp.getParent();
        }
        if (formEncountered) {
            return sb.toString();
        } else {
            return null;
        }
    }

    // Be sure to keep this method in sync with the version in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl).
    /**
     * Determine if the id provided is non-null and exhibits the traits of a
     * fully qualified id. This includes beginning with
     * {@code NamingContainer.SEPARATOR_CHAR}, not ending with that character
     * unless it is the only character, not ending in {@code Form.ID_WILD_CHAR},
     * and not containing spaces.
     * @param id id to validate
     * @return {@code boolean}
     */
    public static boolean isValidFullyQualifiedId(final String id) {
        return id != null
                && id.startsWith(ID_SEP)
                && (id.length() == 1 || !id.endsWith(ID_SEP))
                && !id.endsWith(ID_WILD)
                && id.indexOf(' ') == -1;
    }

    // Be sure to keep this method in sync with the version in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl).
    /**
     * Determine if the fully qualified id provided matches the supplied
     * pattern. The pattern may be a bare, partially qualified, or fully
     * qualified id. The pattern may also end with the
     * {@code NamingContainer.SEPARATOR_CHAR} followed by the
     * {@code Form.ID_WILD_CHAR}, in which case the children of the component
     * indicated by the pattern will be considered a match.
     * @param fqId fully qualified id
     * @param pattern pattern to match
     * @return {@code boolean}
     */
    public static boolean fQIdMatchesPattern(final String fqId,
            final String pattern) {

        if (!isValidFullyQualifiedId(fqId)) {
            return false;
        }
        if (pattern == null
                || pattern.length() < 1
                || pattern.indexOf(' ') != -1) {
            return false;
        }
        // unless pattern is ":", it should not end with ":"
        if (pattern.endsWith(ID_SEP) && !pattern.equals(ID_SEP)) {
            return false;
        }

        String wildSuffix = ID_SEP + ID_WILD;

        // if ID_WILD appears in pattern, it must be the last character,
        // and preceded by ID_SEP
        int indexOfWildInPattern = pattern.indexOf(ID_WILD);
        if (indexOfWildInPattern != -1) {
            if (indexOfWildInPattern != pattern.length() - 1) {
                return false;
            }
            if (!pattern.endsWith(wildSuffix)) {
                return false;
            }
        }

        if (pattern.equals(wildSuffix)) {
            // if pattern was ":*", then any valid fqId is a match
            return true;
        } else if (pattern.endsWith(wildSuffix)) {
            String patternPrefix = pattern
                    .substring(0, pattern.length() - wildSuffix.length());
            if (patternPrefix.startsWith(ID_SEP)) {
                return fqId.equals(patternPrefix)
                        || fqId.startsWith(patternPrefix + ID_SEP);
            } else {
                return fqId.endsWith(ID_SEP + patternPrefix)
                        || fqId.contains(ID_SEP + patternPrefix + ID_SEP);
            }
        } else {
            if (pattern.startsWith(ID_SEP)) {
                return fqId.equals(pattern);
            } else {
                return fqId.endsWith(ID_SEP + pattern);
            }
        }
    }

    /**
     * Add a {@code VirtualFormDescriptor} to the internal virtual forms. If an
     * existing VirtualFormDescriptor object is found with the same name, the
     * object is replaced.
     *
     * @param descriptor The {@code VirtualFormDescriptor} to add.
     */
    public void addInternalVirtualForm(final VirtualFormDescriptor descriptor) {
        if (descriptor == null) {
            return;
        }

        // Get current descriptors.
        VirtualFormDescriptor[] oldDescriptors = getInternalVirtualForms();

        // Iterate over each VirtualFormDescriptor object and check for a match.
        if (oldDescriptors != null) {
            for (int i = 0; i < oldDescriptors.length; i++) {
                if (oldDescriptors[i] == null) {
                    continue;
                }
                String name = oldDescriptors[i].getName();
                if (name != null && name.equals(descriptor.getName())) {
                    oldDescriptors[i] = descriptor;
                    return; // No further processing is required.
                }
            }
        }

        // Create array to hold new descriptors.
        int oldLength;
        if (oldDescriptors != null) {
            oldLength = oldDescriptors.length;
        } else {
            oldLength = 0;
        }
        VirtualFormDescriptor[] newDescriptors =
                new VirtualFormDescriptor[oldLength + 1];
        if (oldLength > 0) {
            System.arraycopy(oldDescriptors, 0, newDescriptors, 0, oldLength);
        }

        // Add new VirtualFormDescriptor object.
        newDescriptors[oldLength] = descriptor;
        setInternalVirtualForms(newDescriptors);
    }

    // Be sure to keep this method in sync with the version in
    // {@code jakarta.faces.component.html.HtmlFormDesignInfo}
    // (in jsfcl).
    /**
     * Virtual form descriptor.
     */
    public static final class VirtualFormDescriptor implements Serializable {

        /**
         * Serialization UID.
         */
        private static final long serialVersionUID = 7348674581125090187L;

        /**
         * name of the virtual form.
         */
        private String name;

        /**
         * ids of components that participate.
         */
        private String[] participatingIds;

        /**
         * ids of components that submit.
         */
        private String[] submittingIds;

        /**
         * Create a new instance.
         */
        public VirtualFormDescriptor() {
        }

        /**
         * Create a new instance.
         * @param newName name
         */
        public VirtualFormDescriptor(final String newName) {
            setName(newName);
        }

        /**
         * Get the virtual form name.
         * @return String
         */
        public String getName() {
            return name;
        }

        /**
         * Set the virtual form name.
         * @param newName name
         */
        public void setName(final String newName) {
            if (newName == null) {
                throw new IllegalArgumentException(
                        getMessage("nullVfName", null));
            }
            String zName = newName.trim();
            if (zName.length() < 1) {
                throw new IllegalArgumentException(
                        getMessage("vfNameWhitespaceOnly", null));
            }
            this.name = zName;
        }

        /**
         * Get the participating ids.
         * @return String[]
         */
        public String[] getParticipatingIds() {
            return participatingIds;
        }

        /**
         * Set the participating ids.
         * @param newParticipatingIds participatingIds
         */
        public void setParticipatingIds(final String[] newParticipatingIds) {
            for (int i = 0; newParticipatingIds != null
                    && i < newParticipatingIds.length; i++) {

                if (newParticipatingIds[i] == null) {
                    throw new IllegalArgumentException(
                            getMessage("nullParticipatingIdAtIndex",
                                    new Object[]{i}));
                }
                newParticipatingIds[i] = newParticipatingIds[i].trim();
                if (newParticipatingIds[i].length() < 1) {
                    throw new IllegalArgumentException(
                            getMessage("whitespaceOnlyParticipatingIdAtIndex",
                                    new Object[]{i}));
                }
            }
            this.participatingIds = newParticipatingIds;
        }

        /**
         * Get the submitting ids.
         * @return String[]
         */
        public String[] getSubmittingIds() {
            return submittingIds;
        }

        /**
         * Set the submitting ids.
         * @param newSubmittingIds submittingIds
         */
        public void setSubmittingIds(final String[] newSubmittingIds) {
            for (int i = 0; newSubmittingIds != null
                    && i < newSubmittingIds.length; i++) {

                if (newSubmittingIds[i] == null) {
                    throw new IllegalArgumentException(
                            getMessage("nullSubmittingIdAtIndex",
                                    new Object[]{i}));
                }
                newSubmittingIds[i] = newSubmittingIds[i].trim();
                if (newSubmittingIds[i].length() < 1) {
                    throw new IllegalArgumentException(
                            getMessage("whitespaceOnlySubmittingIdAtIndex",
                                    new Object[]{i}));
                }
            }
            this.submittingIds = newSubmittingIds;
        }

        /**
         * return true if the component id provided submits this virtual form.
         * @param fqId fully qualified id
         * @return {@code boolean}
         */
        public boolean isSubmittedBy(final String fqId) {
            if (!isValidFullyQualifiedId(fqId)) {
                return false;
            }
            for (int i = 0; submittingIds != null
                    && i < submittingIds.length; i++) {

                if (Form.fQIdMatchesPattern(fqId, submittingIds[i])) {
                    return true;
                }
            }
            return false;
        }

        /**
         * return true if the component id provided participates in this virtual
         * form.
         * @param fqId fully qualified id
         * @return {@code boolean}
         */
        public boolean hasParticipant(final String fqId) {
            if (!isValidFullyQualifiedId(fqId)) {
                return false;
            }
            for (int i = 0; participatingIds != null
                    && i < participatingIds.length; i++) {
                if (Form.fQIdMatchesPattern(fqId, participatingIds[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            if (name == null) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append(" | ");
            for (int i = 0; participatingIds != null
                    && i < participatingIds.length; i++) {
                sb.append(participatingIds[i]);
                sb.append(' ');
            }
            sb.append("| ");
            for (int i = 0; submittingIds != null
                    && i < submittingIds.length; i++) {
                sb.append(submittingIds[i]);
                sb.append(' ');
            }
            return sb.toString().trim();
        }
    }

    /**
     * Examine the participating ids that end in ID_WILD. If any of them match
     * the component's fully qualified id, then the component's children are
     * known to participate in the virtual form descriptor.
     *
     * @param component UI component
     * @param vfd virtual form descriptor
     * @return {@code boolean}
     */
    private static boolean childrenAreKnownToParticipate(
            final UIComponent component, final VirtualFormDescriptor vfd) {

        if (vfd == null) {
            return false;
        }

        String fqId = getFullyQualifiedId(component);
        if (fqId == null) {
            return false;
        }

        String[] participants = vfd.getParticipatingIds();
        for (int i = 0; participants != null && i < participants.length; i++) {
            String participant = participants[i];
            String wildSuffix = ID_SEP + ID_WILD;
            if (participant == null || !participant.endsWith(wildSuffix)) {
                continue;
            }
            if (fQIdMatchesPattern(fqId, participant)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Recursively erase virtual form non-participants by setting their
     * submitted values to {@code null}. This method caches the submitted values
     * in the {@code erasedMap} before actually erasing. If the {@code parent}
     * is embedded in one or more tables, the {@code contextualTables} and
     * {@code contextualRows} are used to record the table- and row-based
     * context of the submitted value, so that such context can be stored in the
     * {@code erasedMap}.
     * <p>
     * <b>Note:</b> Restoring of submitted values works for the brave heart
     * table but not the standard jsf table. However, submitted values inside a
     * standard jsf table are still cached and an attempt is made to restore
     * them, in case a third-party component extends {@code UIData} and, unlike
     * {@code UIData}, does not discard its saved state during rendering.</p>
     *
     * @param parent A parent component whose children will be examined and
     * possibly erased
     * @param contextualTables an array of UIData or TableRowGroup components in
     * the parent's ancestry (with the most distant ancestor as the first member
     * of the array), or {@code null} if the parent is not embedded within any
     * tables
     * @param contextualRows a parallel array of {@code Integer} or
     * {@code RowKey} objects representing a row in the corresponding contextual
     * table, or {@code null} if the parent is not embedded within any tables
     */
    private void eraseVirtualFormNonParticipants(
            final UIComponent parent, final Object[] contextualTables,
            final Object[] contextualRows) {

        // Process all facets and children of this component
        // FIXME check the synchronization issue here
        synchronized (erasedMap) {
            // prevent multiple threads from the same session simultaneously
            // accessing erasedMap, nonDefaultRetainStatusEvhs
            Iterator kids = parent.getFacetsAndChildren();
            while (kids.hasNext()) {
                UIComponent kid = (UIComponent) kids.next();
                // if this kid is an EditableValueHolder, and it does not
                // participate, set submitted value to null
                if (kid instanceof EditableValueHolder
                        && !participatesInSubmittedVirtualForm(kid)) {
                    EditableValueHolder kidEvh = (EditableValueHolder) kid;
                    //cache the submitted value to be erased in eraseMap
                    Object submittedValueToErase = kidEvh.getSubmittedValue();
                    if (contextualTables == null) {
                        erasedMap.put(kidEvh, submittedValueToErase);
                    } else {
                        addTableValuesEntry(erasedMap, kidEvh, 0,
                                contextualTables, contextualRows,
                                submittedValueToErase);
                    }
                    kidEvh.setSubmittedValue(null);
                }

                // if children of kid are known to participate in
                // submittedVirtualForm,
                // then no need to recurse on kid
                if (childrenAreKnownToParticipate(kid, submittedVirtualForm)) {
                    continue;   //continue to next kid
                }

                // recurse. if kid is a UIData or TableRowGroup, perform a
                // recursive call once per row.
                // if kid is not a UIData or TableRowGroup, simply perform
                // a recursive call once.
                if (kid instanceof UIData) {
                    UIData kidTable = (UIData) kid;
                    int originalRowIndex = kidTable.getRowIndex();
                    int rowIndex = 0;
                    kidTable.setRowIndex(rowIndex);
                    while (kidTable.isRowAvailable()) {
                        Object[] localContextualTables =
                                appendToArray(contextualTables, kidTable);
                        Object[] localContextualRows =
                                appendToArray(contextualRows, rowIndex);
                        eraseVirtualFormNonParticipants(kidTable,
                                localContextualTables, localContextualRows);
                        kidTable.setRowIndex(++rowIndex);
                    }
                    kidTable.setRowIndex(originalRowIndex);
                } else if (kid instanceof TableRowGroup) {
                    TableRowGroup group = (TableRowGroup) kid;
                    RowKey[] rowKeys = group.getRowKeys();
                    // Save RowKey.
                    RowKey oldRowKey = group.getRowKey();

                    // Check for null TableDataProvider.
                    if (rowKeys != null) {
                        for (RowKey rowKey : rowKeys) {
                            group.setRowKey(rowKey);
                            if (!group.isRowAvailable()) {
                                continue;
                            }
                            Object[] localContextualTables =
                                    appendToArray(contextualTables, group);
                            Object[] localContextualRows =
                                    appendToArray(contextualRows, rowKey);
                            eraseVirtualFormNonParticipants(group,
                                    localContextualTables, localContextualRows);
                        }
                    }
                    // Restore RowKey.
                    group.setRowKey(oldRowKey);
                } else {
                    eraseVirtualFormNonParticipants(kid, contextualTables,
                            contextualRows);
                }
            }
        }
    }

    /**
     * Recursively add an entry to {@code erasedMap}, whose value is a
     * {@code TableValues} (and whose key is an {@code EditableValueHolder}).
     *
     * @param map input map containing table values
     * @param mapKey key of the table values to process
     * @param c table values index
     * @param contextualTables contextual tables
     * @param contextualRows contextual rows
     * @param submittedValueToErase values to erase
     */
    @SuppressWarnings("unchecked")
    private void addTableValuesEntry(final Map map, final Object mapKey,
            final int c, final Object[] contextualTables,
            final Object[] contextualRows,
            final Object submittedValueToErase) {

        // use the map and mapKey to get a TableValues, using
        // contextualTables[c] to create tsv if necessary
        TableValues tv = (TableValues) map.get(mapKey);
        if (tv == null) {
            tv = new TableValues(contextualTables[c]);
            map.put(mapKey, tv);
        }

        // ensure an entry is populated in tv.values with contextualRows[c]
        // as the key
        Map values = tv.getValues();
        // if last index in contextualTables
        if (c == contextualTables.length - 1) {
            values.put(contextualRows[c], submittedValueToErase);
        } else {
            addTableValuesEntry(values, contextualRows[c], c + 1,
                    contextualTables, contextualRows, submittedValueToErase);
        }
    }

    /**
     * Append the item to the array.
     * @param array destination array
     * @param item item to append
     * @return Object[]
     */
    private static Object[] appendToArray(final Object[] array,
            final Object item) {

        Object[] result;
        if (array == null) {
            result = new Object[]{item};
        } else {
            result = new Object[array.length + 1];
            System.arraycopy(array, 0, result, 0, array.length);
            result[array.length] = item;
        }
        return result;
    }

    /**
     * Restore the submitted values erased by the virtual form mechanism where
     * appropriate. This method is called in {@code FormRenderer.renderStart}.
     * It should not be called by developer code.
     * <p>
     * <b>Note:</b> Restoring of submitted values works on {@code TableRowGroup}
     * components, but does not work on the standard faces data table component.
     * This is because in {@code UIData.encodeBegin}, the table's per-row saved
     * state is typically discarded. The result is that upon exiting
     * {@code FormRenderer.renderStart}, the submitted values will be restored;
     * however, they will subsequently be discarded. Nonetheless, we still cache
     * and restore those submitted values, in case a third-party component
     * extends {@code UIData} and, unlike {@code UIData}, does not discard its
     * saved state during rendering.</p>
     */
    public void restoreNonParticipatingSubmittedValues() {
        // FIXME check the synchronization issue here
        synchronized (erasedMap) {
            // prevent multiple threads from the same session simultaneously
            // 1accessing erasedMap, nonDefaultRetainStatusEvhs
            for (Map.Entry entry : erasedMap.entrySet()) {
                EditableValueHolder evh = (EditableValueHolder) entry.getKey();
                // if evh is designated as discarding submitted values, do not
                // restore
                // if DEFAULT_RETAIN_STATUS==true (retain by default), then
                // nonDefaultRetainStatusEvhs contains evhs that discard
                // if DEFAULT_RETAIN_STATUS==false (discard by default), then
                // nonDefaultRetainStatusEvhs contains evhs that retain
                boolean evhAppearsInSet = nonDefaultRetainStatusEvhs
                        .contains(evh);
                boolean discards;
                if (DEFAULT_RETAIN_STATUS) {
                    discards = evhAppearsInSet;
                } else {
                    discards = !evhAppearsInSet;
                }
                if (discards) {
                    continue;
                }
                //restore
                Object erasedMapValue = entry.getValue();
                if (erasedMapValue instanceof TableValues) {
                    TableValues tv = (TableValues) erasedMapValue;
                    restoreTableValues(tv, evh);
                } else {
                    evh.setSubmittedValue(erasedMapValue);
                }
            }
            // after restoring, clear out the retain status data
            nonDefaultRetainStatusEvhs.clear();
        }
    }

    /**
     * Helper method to restore submitted values for an
     * {@code EditableValueHolder} component from a {@code TableValues} object,
     * which contains a value for each row of each table in the
     * {@code EditableValueHolder}'s ancestry.
     * @param tv table values
     * @param evh editable value holder
     */
    @SuppressWarnings("unchecked")
    private void restoreTableValues(final TableValues tv,
            final EditableValueHolder evh) {

        // capture the old row of the tv and
        // iterate through the tv rows
        Object oldRow;  //an Integer or RowKey
        Iterator rowIterator;
        Object table = tv.getTable();
        Map values = tv.getValues();
        if (table instanceof UIData) {
            //capture the old row
            UIData uidata = (UIData) table;
            int iOldRow = uidata.getRowIndex();
            oldRow = iOldRow;

            //get rowIterator
            List rowList = new ArrayList();
            // add Set of Integers to List
            rowList.addAll(values.keySet());
            Collections.sort(rowList);
            rowIterator = rowList.iterator();
        } else {
            //capture the old row
            TableRowGroup rowGroup = (TableRowGroup) table;
            oldRow = rowGroup.getRowKey();

            // get rowIterator
            rowIterator = values.keySet().iterator();
        }

        while (rowIterator.hasNext()) {
            // row is an Integer or RowKey
            Object row = rowIterator.next();

            //set the table to that row
            if (table instanceof UIData) {
                UIData uidata = (UIData) table;
                Integer rowInt = (Integer) row;
                int iRow = rowInt;
                uidata.setRowIndex(iRow);
            } else {
                TableRowGroup rowGroup = (TableRowGroup) table;
                RowKey rowKey = (RowKey) row;
                rowGroup.setRowKey(rowKey);
            }

            // get the rowValue for that row, which can be an Object or a
            // TableValues
            // and restore that rowValue
            Object rowValue = values.get(row);
            if (rowValue instanceof TableValues) {
                TableValues rowValueTv = (TableValues) rowValue;
                restoreTableValues(rowValueTv, evh);
            } else {
                evh.setSubmittedValue(rowValue);
            }
        }

        // set table back to old row
        if (table instanceof UIData) {
            UIData uidata = (UIData) table;
            Integer oldRowInt = (Integer) oldRow;
            int iOldRow = oldRowInt;
            uidata.setRowIndex(iOldRow);
        } else {
            TableRowGroup rowGroup = (TableRowGroup) table;
            RowKey oldRowKey = (RowKey) oldRow;
            rowGroup.setRowKey(oldRowKey);
        }
    }

    /**
     * Structure that stores a component's submitted value for each row of a
     * table ({@code UIData} or {@code TableRowGroup}), either as an
     * {@code Object} or as a nested {@code TableValues}. This permits storing
     * of all the submitted values for a component in the case where that
     * component has more than one table in its ancestry.
     */
    private static final class TableValues {

        /**
         * Store a UIData or TableRowGroup.
         */
        private final Object table;

        /**
         * has a RowKey or Integer as the key, and a submitted value Object or a
         * TableValues as the value.
         */
        private final Map values;

        /**
         * Create a new instance.
         * @param newTable UIData or TableRowGroup
         */
        TableValues(final Object newTable) {
            this.table = newTable;
            values = new HashMap();
        }

        /**
         * Get the {@code UIData} or {@code TableRowGroup} for this instance.
         *
         * @return Object
         */
        public Object getTable() {
            return this.table;
        }

        /**
         * Get a {@code Map} of the values for each row of the table in
         * question, where the key is an {@code Integer} or {@code RowKey}, and
         * the value is a submitted value {@code Object} or a
         * {@code TableValues} instance.
         *
         * @return Map
         */
        public Map getValues() {
            return this.values;
        }
    }

    /**
     * Ensure that the supplied {@code EditableValueHolder} component will
     * discard (rather than retain) its submitted value when a virtual form is
     * submitted in which the component does not participate.
     *
     * @param inputField An {@code EditableValueHolder} component that is
     * <strong>not</strong> a participant in the virtual form that was submitted
     * on this request.
     * @throws IllegalArgumentException if inputField is null.
     * @throws IllegalArgumentException if a virtual form has been submitted and
     * the supplied inputField participates in it.
     */
    public void discardSubmittedValue(final EditableValueHolder inputField) {
        if (inputField == null) {
            throw new IllegalArgumentException(
                    getMessage("nullInputField", null));
        }
        // just defensive
        if (inputField instanceof UIComponent) {
            UIComponent uicInputField = (UIComponent) inputField;
            if (participatesInSubmittedVirtualForm(uicInputField)) {
                throw new IllegalArgumentException(
                        getMessage("supplyNonParticipatingInputField",
                                new Object[]{
                                    uicInputField.getId(),
                                    "discardSubmittedValue",
                                }));
            }
        }
        List<EditableValueHolder> evhCollection =
                new ArrayList<EditableValueHolder>();
        evhCollection.add(inputField);
        setRetainWhenNonParticipating(evhCollection, false);
    }

    /**
     * Ensure that the participants in the supplied virtual form will discard
     * (rather than retain) their submitted values when a different virtual form
     * is submitted.
     *
     * @param virtualFormName The name of a virtual form on this page which has
     * not been submitted.
     * @throws IllegalArgumentException if no virtual form exists with the
     * supplied name.
     * @throws IllegalArgumentException if the supplied virtual form has been
     * submitted on this request.
     */
    public void discardSubmittedValues(final String virtualFormName) {
        VirtualFormDescriptor vfd = getVFormByName(virtualFormName);
        if (vfd == null) {
            throw new IllegalArgumentException(
                    getMessage("unrecognizedVfName", new Object[]{
                        virtualFormName,
                    }));
        }
        if (vfd == submittedVirtualForm) {
            throw new IllegalArgumentException(
                    getMessage("supplyUnsubmittedVirtualForm",
                            new Object[]{
                                virtualFormName,
                                "discardSubmittedValues"}));
        }
        setRetainWhenNonParticipating(vfd, false);
    }

    /**
     * Set the retain collections.
     * @param vfd virtual forms
     * @param retain retain flag
     */
    private void setRetainWhenNonParticipating(final VirtualFormDescriptor vfd,
            final boolean retain) {

        String[] pids = vfd.getParticipatingIds();
        if (pids == null || pids.length < 1) {
            return;
        }
        List<EditableValueHolder> evhList =
                new ArrayList<EditableValueHolder>();
        for (String pid : pids) {
            UIComponent uic = findComponentById(pid);
            if (uic instanceof EditableValueHolder) {
                evhList.add((EditableValueHolder) uic);
            }
        }
        setRetainWhenNonParticipating(evhList, retain);
    }

    /**
     * Set the retain collections.
     * @param evhs editable value holders
     * @param retain retain flag
     */
    private void setRetainWhenNonParticipating(
            final List<EditableValueHolder> evhs, final boolean retain) {

        if (evhs == null || evhs.size() < 1) {
            return;
        }
        //FIXME check the synchronization issue here
        synchronized (erasedMap) {
            // prevent multiple threads from the same session simultaneously
            // accessing erasedMap, nonDefaultRetainStatusEvhs
            // if we retain by default, then nonDefaultRetainStatusEvhs should
            // contain evhs only if we want evhs to discard
            // if we discard by default, then nonDefaultRetainStatusEvhs
            // should contain evhs only if we want evhs to retain
            boolean shouldContain;
            if (DEFAULT_RETAIN_STATUS) {
                shouldContain = !retain;
            } else {
                shouldContain = retain;
            }
            if (shouldContain) {
                nonDefaultRetainStatusEvhs.addAll(evhs);
            } else {
                nonDefaultRetainStatusEvhs.removeAll(evhs);
            }
        }
    }

    /**
     * Get a virtual form by name.
     * @param vFormName form name
     * @return String
     */
    private VirtualFormDescriptor getVFormByName(final String vFormName) {
        if (vFormName == null) {
            return null;
        }
        VirtualFormDescriptor[] vfds = getVirtualForms();
        for (int i = 0; vfds != null && i < vfds.length; i++) {
            if (vFormName.equals(vfds[i].getName())) {
                return vfds[i];
            }
        }
        return null;
    }

    /**
     * Get the specified message.
     *
     * @param key message key
     * @param args message arguments
     * @return String
     */
    private static String getMessage(final String key, final Object[] args) {
        String baseName = Form.class.getPackage().getName() + ".Bundle";
        return MessageUtil.getMessage(FacesContext.getCurrentInstance(),
                baseName, key, args);
    }

    /**
     * Get the encoding type.
     *
     * @return String
     */
    public String getEnctype() {
        String encType = doGetEnctype();
        if (encType == null || encType.length() == 0) {
            encType = "application/x-www-form-urlencoded";
            setEnctype(encType);
        }
        return encType;
    }

    /**
     * Get the {@code autocomplete} flag value.
     * @return {@code boolean}
     */
    public boolean isAutoComplete() {
        if (this.autoCompleteSet) {
            return this.autoComplete;
        }
        ValueExpression vb = getValueExpression("autoComplete");
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
     * Use this non-XHTML compliant boolean attribute to turn off autocompletion
     * feature of Internet Explorer and Firefox browsers. Set to "false" to turn
     * off completion. The default is "true".
     *
     * @see #isAutoComplete()
     * @param newAutoComplete autoComplete
     */
    public void setAutoComplete(final boolean newAutoComplete) {
        this.autoComplete = newAutoComplete;
        this.autoCompleteSet = true;
    }

    /**
     * Get the encoding type.
     *
     * @return String
     */
    private String doGetEnctype() {
        if (this.enctype != null) {
            return this.enctype;
        }
        ValueExpression vb = getValueExpression("enctype");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return "application/x-www-form-urlencoded";
    }

    /**
     * Use this attribute to set the content-type of the HTTP request generated
     * by this form. You do not normally need to set this attribute. Its default
     * value is application/x-www-form-urlencoded. If there is an upload tag
     * inside the form, the upload tag will modify the form's enctype attribute
     * to multipart/form-data.
     *
     * @see #getEnctype()
     * @param newEnctype enctype
     */
    public void setEnctype(final String newEnctype) {
        this.enctype = newEnctype;
    }

    /**
     * Get the internal virtual forms.
     * @return VirtualFormDescriptor[]
     */
    public VirtualFormDescriptor[] getInternalVirtualForms() {
        if (this.internalVirtualForms != null) {
            return this.internalVirtualForms;
        }
        ValueExpression vb = getValueExpression("internalVirtualForms");
        if (vb != null) {
            return (VirtualFormDescriptor[]) vb.getValue(getFacesContext()
                    .getELContext());
        }
        return null;
    }

    /**
     * The virtual forms used "internally" by components (such as Table).
     * Component authors can manipulate this set of virtual forms independent of
     * the set exposed to developers. This set is only consulted after the set
     * exposed to developers is consulted. A participating or submitting id can
     * end in ":*" to indicate descendants. For example, table1:* can be used as
     * a participating or submitting id to indicate all the descendants of
     * table1.
     *
     * @see #getInternalVirtualForms()
     * @param newInternalVirtualForms internalVirtualForms
     */
    public void setInternalVirtualForms(
            final VirtualFormDescriptor[] newInternalVirtualForms) {

        this.internalVirtualForms = newInternalVirtualForms;
    }

    /**
     * Get the {@code onClick} value.
     * @return String
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueExpression vb = getValueExpression("onClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse click occurs over this
     * component.
     *
     * @see #getOnClick()
     * @param newOnClick onClick
     */
    public void setOnClick(final String newOnClick) {
        this.onClick = newOnClick;
    }

    /**
     * Get the {@code onDblClick} value.
     * @return String
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueExpression vb = getValueExpression("onDblClick");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse double click occurs over this
     * component.
     *
     * @see #getOnDblClick()
     * @param newOnDblClick onDblClick
     */
    public void setOnDblClick(final String newOnDblClick) {
        this.onDblClick = newOnDblClick;
    }

    /**
     * Get the {@code onKeyDown} value.
     * @return String
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueExpression vb = getValueExpression("onKeyDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses down on a key while the
     * component has focus.
     *
     * @see #getOnKeyDown()
     * @param newOnKeyDown onKeyDown
     */
    public void setOnKeyDown(final String newOnKeyDown) {
        this.onKeyDown = newOnKeyDown;
    }

    /**
     * Get the {@code onKeyPress} value.
     * @return String
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueExpression vb = getValueExpression("onKeyPress");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses and releases a key while
     * the component has focus.
     *
     * @see #getOnKeyPress()
     * @param newOnKeyPress onKeyPress
     */
    public void setOnKeyPress(final String newOnKeyPress) {
        this.onKeyPress = newOnKeyPress;
    }

    /**
     * Get the {@code onKeyUp} value.
     * @return String
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueExpression vb = getValueExpression("onKeyUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a key while the component
     * has focus.
     *
     * @see #getOnKeyUp()
     * @param newOnKeyUp onKeyUp
     */
    public void setOnKeyUp(final String newOnKeyUp) {
        this.onKeyUp = newOnKeyUp;
    }

    /**
     * Get the {@code onMouseDown} value.
     * @return String
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueExpression vb = getValueExpression("onMouseDown");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user presses a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseDown()
     * @param newOnMouseDown onMouseDown
     */
    public void setOnMouseDown(final String newOnMouseDown) {
        this.onMouseDown = newOnMouseDown;
    }

    /**
     * Get the {@code onMouseMove} value.
     * @return String
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueExpression vb = getValueExpression("onMouseMove");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer while over
     * the component.
     *
     * @see #getOnMouseMove()
     * @param newOnMouseMove onMouseMove
     */
    public void setOnMouseMove(final String newOnMouseMove) {
        this.onMouseMove = newOnMouseMove;
    }

    /**
     * Get the {@code onMouseOut} value.
     * @return String
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueExpression vb = getValueExpression("onMouseOut");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when a mouse out movement occurs over this
     * component.
     *
     * @see #getOnMouseOut()
     * @param newOnMouseOut onMouseOut
     */
    public void setOnMouseOut(final String newOnMouseOut) {
        this.onMouseOut = newOnMouseOut;
    }

    /**
     * Get the {@code onMouseOver} value.
     * @return String
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueExpression vb = getValueExpression("onMouseOver");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user moves the mouse pointer into the
     * boundary of this component.
     *
     * @see #getOnMouseOver()
     * @param newOnMouseOver onMouseOver
     */
    public void setOnMouseOver(final String newOnMouseOver) {
        this.onMouseOver = newOnMouseOver;
    }

    /**
     * Get the {@code onMouseUp} value.
     * @return String
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueExpression vb = getValueExpression("onMouseUp");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when the user releases a mouse button while the
     * mouse pointer is on the component.
     *
     * @see #getOnMouseUp()
     * @param newOnMouseUp onMouseUp
     */
    public void setOnMouseUp(final String newOnMouseUp) {
        this.onMouseUp = newOnMouseUp;
    }

    /**
     * Get the {@code onReset} value.
     * @return String
     */
    public String getOnReset() {
        if (this.onReset != null) {
            return this.onReset;
        }
        ValueExpression vb = getValueExpression("onReset");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this form is reset.
     *
     * @see #getOnReset()
     * @param newOnReset onReset
     */
    public void setOnReset(final String newOnReset) {
        this.onReset = newOnReset;
    }

    /**
     * Get the {@code onSubmit} value.
     * @return String
     */
    public String getOnSubmit() {
        if (this.onSubmit != null) {
            return this.onSubmit;
        }
        ValueExpression vb = getValueExpression("onSubmit");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * Scripting code executed when this form is submitted.
     *
     * @see #getOnSubmit()
     * @param newOnSubmit onSubmit
     */
    public void setOnSubmit(final String newOnSubmit) {
        this.onSubmit = newOnSubmit;
    }

    /**
     * Get the style.
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
     * Get the style class.
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
     * Get the form target.
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
     * Use this attribute to set the target of the XHTML form tag.
     *
     * @see #getTarget()
     * @param newTarget target
     */
    public void setTarget(final String newTarget) {
        this.target = newTarget;
    }

    /**
     * Get the virtual forms.
     * @return VirtualFormDescriptor[]
     */
    public VirtualFormDescriptor[] getVirtualForms() {
        if (this.virtualForms != null) {
            return this.virtualForms;
        }
        ValueExpression vb = getValueExpression("virtualForms");
        if (vb != null) {
            return (VirtualFormDescriptor[]) vb
                    .getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The virtual forms within this literal form, represented as an array of
     * Form.VirtualFormDescriptor objects. This property and the
     * "virtualFormsConfig" property are automatically kept in-sync.
     *
     * @see #getVirtualForms()
     * @param newVirtualForms virtualForms
     */
    private void doSetVirtualForms(
            final VirtualFormDescriptor[] newVirtualForms) {

        this.virtualForms = newVirtualForms;
    }

    /**
     * Get the virtual forms config.
     * @return String
     */
    public String getVirtualFormsConfig() {
        if (this.virtualFormsConfig != null) {
            return this.virtualFormsConfig;
        }
        ValueExpression vb = getValueExpression("virtualFormsConfig");
        if (vb != null) {
            return (String) vb.getValue(getFacesContext().getELContext());
        }
        return null;
    }

    /**
     * The configuration of the virtual forms within this literal form,
     * represented as a String. Each virtual form is described by three parts,
     * separated with pipe ("|") characters: the virtual form name, a
     * space-separated list of component ids that participate in the virtual
     * form, and a space-separated list of component ids that submit the virtual
     * form. Multiple such virtual form "descriptors" are separated by commas.
     * The component ids may be qualified (for instance,
     * "table1:tableRowGroup1:tableColumn1:textField1").
     *
     * @see #getVirtualFormsConfig()
     * @param newVirtualFormsConfig virtualFormsConfig
     */
    private void doSetVirtualFormsConfig(final String newVirtualFormsConfig) {
        this.virtualFormsConfig = newVirtualFormsConfig;
    }

    /**
     * Get the visible attribute value.
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
     * viewable by the user in the rendered HTML page.
     *
     * @see #isVisible()
     * @param newVisible visible
     */
    public void setVisible(final boolean newVisible) {
        this.visible = newVisible;
        this.visibleSet = true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public void restoreState(final FacesContext context, final Object state) {
        Object[] values = (Object[]) state;
        super.restoreState(context, values[0]);
        this.autoComplete = ((Boolean) values[1]);
        this.autoCompleteSet = ((Boolean) values[2]);
        this.enctype = (String) values[3];
        this.internalVirtualForms = (VirtualFormDescriptor[]) values[4];
        this.onClick = (String) values[5];
        this.onDblClick = (String) values[6];
        this.onKeyDown = (String) values[7];
        this.onKeyPress = (String) values[8];
        this.onKeyUp = (String) values[9];
        this.onMouseDown = (String) values[10];
        this.onMouseMove = (String) values[11];
        this.onMouseOut = (String) values[12];
        this.onMouseOver = (String) values[13];
        this.onMouseUp = (String) values[14];
        this.onReset = (String) values[15];
        this.onSubmit = (String) values[16];
        this.style = (String) values[17];
        this.styleClass = (String) values[18];
        this.target = (String) values[19];
        this.virtualForms = (VirtualFormDescriptor[]) values[20];
        this.virtualFormsConfig = (String) values[21];
        this.visible = ((Boolean) values[22]);
        this.visibleSet = ((Boolean) values[23]);
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public Object saveState(final FacesContext context) {
        Object[] values = new Object[24];
        values[0] = super.saveState(context);
        if (this.autoComplete) {
            values[1] = Boolean.TRUE;
        } else {
            values[1] = Boolean.FALSE;
        }
        if (this.autoCompleteSet) {
            values[2] = Boolean.TRUE;
        } else {
            values[2] = Boolean.FALSE;
        }
        values[3] = this.enctype;
        values[4] = this.internalVirtualForms;
        values[5] = this.onClick;
        values[6] = this.onDblClick;
        values[7] = this.onKeyDown;
        values[8] = this.onKeyPress;
        values[9] = this.onKeyUp;
        values[10] = this.onMouseDown;
        values[11] = this.onMouseMove;
        values[12] = this.onMouseOut;
        values[13] = this.onMouseOver;
        values[14] = this.onMouseUp;
        values[15] = this.onReset;
        values[16] = this.onSubmit;
        values[17] = this.style;
        values[18] = this.styleClass;
        values[19] = this.target;
        values[20] = this.virtualForms;
        values[21] = this.virtualFormsConfig;
        if (this.visible) {
            values[22] = Boolean.TRUE;
        } else {
            values[22] = Boolean.FALSE;
        }
        if (this.visibleSet) {
            values[23] = Boolean.TRUE;
        } else {
            values[23] = Boolean.FALSE;
        }
        return values;
    }
}
