/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2006, 2022 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jsftemplating.el.PageSessionResolver;
import com.sun.jsftemplating.layout.descriptors.LayoutComponent;
import com.sun.jsftemplating.layout.descriptors.LayoutComposition;
import com.sun.jsftemplating.layout.descriptors.LayoutDefinition;
import com.sun.jsftemplating.layout.descriptors.LayoutElement;
import com.sun.jsftemplating.layout.descriptors.LayoutFacet;
import com.sun.jsftemplating.layout.descriptors.LayoutInsert;
import com.sun.jsftemplating.layout.descriptors.Resource;
import com.sun.jsftemplating.util.LogUtil;
import com.sun.jsftemplating.util.SimplePatternMatcher;
import com.sun.jsftemplating.util.TypeConversion;
import com.sun.jsftemplating.util.TypeConverter;
import com.sun.jsftemplating.util.UIComponentTypeConversion;
import com.sun.jsftemplating.util.fileStreamer.Context;
import com.sun.jsftemplating.util.fileStreamer.FacesStreamerContext;
import com.sun.jsftemplating.util.fileStreamer.FileStreamer;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

// FIXME: Things to consider:
// FIXME:   - Should I attempt to clean up old unused UIComponents?
// FIXME:   - f:view supported setting locale, I should too...

/**
 * This class provides a custom {@link ViewHandler} that is able to create and populate a {@link UIViewRoot} from a
 * {@link LayoutDefinition}. This is often defined by an XML document, the default implementation's DTD is defined in
 * {@code layout.dtd}.
 *
 * <p>
 * Besides the default {@link ViewHandler} behavior, this class is responsible for using the given {@code viewId} as the
 * {@link LayoutDefinition} key and setting it on the {@link UIViewRoot} that is created. It will obtain the
 * {@link LayoutDefinition}, initialize the declared {@link Resource}s, and instantiate {@link UIComponent} tree using
 * the {@link LayoutDefinition}'s declared {@link LayoutComponent} structure. During rendering, it delegates to the
 * {@link LayoutDefinition}.
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutViewHandler extends ViewHandler {

    /**
     * This is the key that may be used to identify the clientId of the UIComponent that is to be updated via an Ajax
     * request.
     */
    public static final String AJAX_REQ_KEY = "ajaxReq";

    public static final String RESTORE_VIEW_ID = "_resViewID";

    /**
     * This is the default prefix that must be included on all requests for resources.
     */
    public static final String DEFAULT_RESOURCE_PREFIX = "/resource";

    /**
     * The name of the {@code context-param} to set the resource prefix.
     */
    public static final String RESOURCE_PREFIX = "com.sun.jsftemplating.RESOURCE_PREFIX";

    /**
     * This key can be used to override the encoding type used in your application.
     */
    public static final String ENCODING_TYPE = "com.sun.jsftemplating.ENCODING";

    static final String AJAX_REQ_TARGET_KEY = "_ajaxReqTarget";

    /**
     * The name of the <code>context-param</code> to set the view mappings.
     */
    // TODO: should these keys be added to a new Class f.e. com.sun.jsftemplating.Keys?
    private static final String VIEW_MAPPINGS = "com.sun.jsftemplating.VIEW_MAPPINGS";

    private static final TypeConversion UICOMPONENT_TYPE_CONVERSION = new UIComponentTypeConversion();

    private final ViewHandler oldViewHandler;
    private Set<String> resourcePrefix;
    private Collection<SimplePatternMatcher> viewMappings;

    /*
     * This is intended to initialize additional type conversions for the {@link TypeConverter}. These additional type
     * conversions are typically specific to JSF or JSFTemplating. If you are reading this and want additional custom type
     * conversions, bug Ken Paulsen to add an init event which will allow you to easily initialize your own type
     * conversions.
     */
    static {
        // Add type conversions by class
        TypeConverter.registerTypeConversion(null, UIComponent.class, UICOMPONENT_TYPE_CONVERSION);
        // Add type conversions by class name
        TypeConverter.registerTypeConversion(null, UIComponent.class.getName(), UICOMPONENT_TYPE_CONVERSION);
    }

    /**
     * Constructor.
     *
     * @param oldViewHandler The old {@link ViewHandler}.
     */
    public LayoutViewHandler(ViewHandler oldViewHandler) {
        this.oldViewHandler = oldViewHandler;

// FIXME: Fire an initializtion event, work out how to listen for this event

        // This is added here to ensure that if the ViewHandler is reloaded in
        // a running application, that handlers, ct's, and resources will get
        // re-read. Ryan added a feature which may introduce this code path.
        LayoutDefinitionManager.clearGlobalComponentTypes(null);
        LayoutDefinitionManager.clearGlobalHandlerDefinitions(null);
        LayoutDefinitionManager.clearGlobalResources(null);
    }

//    /**
//     * Initialize the view for the request processing lifecycle. It is called at the beginning of the Restore View phase.
//     */
//     public void initView(FacesContext context) throws FacesException {
//         // Not used yet... I left this here as a reminder that it is here
//     }

    /**
     * This method is invoked when restoreView does not yield a {@link UIViewRoot} (initial requests and new pages).
     *
     * <p>
     * This implementation should work with both {@link LayoutDefinition}-based pages as well as traditional JSF pages (or
     * other frameworks).
     */
    @Override
    public UIViewRoot createView(FacesContext facesContext, String viewId) {
        // Check to see if this is a resource request
        String resourcePath = getResourcePath(viewId);
        if (resourcePath != null) {
            // Serve Resource
            return serveResource(facesContext, resourcePath);
        }

        // Check to see if jsftemplating should create the view
        if (!this.isMappedView(viewId)) {
            return oldViewHandler.createView(facesContext, viewId);
        }

        Locale locale = null;
        String renderKitId = null;

        // Use the locale from the previous view if is was one which will be
        // the case if this is called from NavigationHandler. There wouldn't be
        // one for the initial case.
        if (facesContext.getViewRoot() != null) {
            UIViewRoot oldViewRoot = facesContext.getViewRoot();
            LayoutDefinition oldLayoutDefinition = ViewRootUtil.getLayoutDefinition(oldViewRoot);
            if (oldLayoutDefinition != null && oldViewRoot.getViewId().equals(viewId)) {
                // If you navigate to the page you are already on, JSF will
                // re-create the UIViewRoot of the current page. The initPage
                // event needs to be reset so that it will re-execute itself.
                oldLayoutDefinition.setInitPageExecuted(facesContext, Boolean.FALSE);
            }
            locale = facesContext.getViewRoot().getLocale();
            renderKitId = facesContext.getViewRoot().getRenderKitId();
        }

        // Create the View Root
        UIViewRoot viewRoot = (UIViewRoot) facesContext.getApplication().createComponent(UIViewRoot.COMPONENT_TYPE);
        viewRoot.setViewId(viewId);
        ViewRootUtil.setLayoutDefinitionKey(viewRoot, viewId);

        // If there was no locale from the previous view, calculate the locale
        // for this view.
        if (locale == null) {
            locale = calculateLocale(facesContext);
        }
        viewRoot.setLocale(locale);

        // Set the renderKit
        if (renderKitId == null) {
            renderKitId = calculateRenderKitId(facesContext);
        }
        viewRoot.setRenderKitId(renderKitId);

        // Save the current View Root, temporarily set the new UIViewRoot so
        // beforeCreate, afterCreate will function correctly
        UIViewRoot currentViewRoot = facesContext.getViewRoot();

        // Set the View Root to the new View Root
        // NOTE: This must happen after return oldViewHandler.createView(...)
        // NOTE2: However, we really want the UIViewRoot available during
        // initPage events which are fired during
        // getLayoutDefinition()... so we need to set this, then unset
        // it if we go through oldViewHandler.createView(...)
        facesContext.setViewRoot(viewRoot);

        // Initialize Resources / Create Tree
        LayoutDefinition layoutDefinition;
        try {
            layoutDefinition = ViewRootUtil.getLayoutDefinition(viewRoot);
        } catch (LayoutDefinitionException ex) {
            if (LogUtil.configEnabled()) {
                LogUtil.config("JSFT0005", (Object) viewId);
                if (LogUtil.finestEnabled()) {
                    LogUtil.finest("File (" + viewId + ") not found!", ex);
                }
            }

            // Restore original View Root, we set it prematurely
            if (currentViewRoot != null) {
// FIXME: Talk to Ryan about restoring the ViewRoot to null!!
                facesContext.setViewRoot(currentViewRoot);
            }

// FIXME: Provide better feedback when no .jsf & no .jsp
// FIXME: Difficult to tell at this stage if no .jsp is present

            // Not found, delegate to old ViewHandler
            return oldViewHandler.createView(facesContext, viewId);
        } catch (RuntimeException ex) {
            // Restore original View Root, we set it prematurely
            if (currentViewRoot != null) {
// FIXME: Talk to Ryan about restoring the ViewRoot to null!!
                facesContext.setViewRoot(currentViewRoot);
            }

            // Allow error to be thrown (this isn't the normal code path)
            throw ex;
        }

        // We need to do this again b/c an initPage handler may have changed
        // the viewRoot
        viewRoot = facesContext.getViewRoot();

        // Check to make sure we found a LayoutDefinition and that the response isn't
        // already finished (initPage could complete the response...
        // i.e. during a redirect).
        if (layoutDefinition != null && !facesContext.getResponseComplete()) {
            // Ensure that our Resources are available
            for (Resource resource : layoutDefinition.getResources()) {
                // Just calling getResource() puts it in the Request scope
                resource.getFactory().getResource(facesContext, resource);
            }

            // Get the Tree and pre-walk it
            if (LayoutDefinitionManager.isDebug(facesContext)) {
                // Make sure to reset all the client ids we're about to check
                getClientIdMap(facesContext).clear();
            }
            buildUIComponentTree(facesContext, viewRoot, layoutDefinition);
        }

        // Restore the current UIViewRoot.
        if (currentViewRoot != null) {
            facesContext.setViewRoot(currentViewRoot);
        }

        // Return the populated UIViewRoot
        return viewRoot;
    }

    /**
     * Tests if the provided {@code viewId} matches one of the configured view-mappings. If no view-mappings are defined,
     * all {@code viewId}s will match.
     *
     * @param viewId The {@code viewId} to be tested.
     *
     * @return {@code true} if the viewId matched or no view-mappings are defined, {@code false} otherwise.
     * @since 1.2
     */
    private boolean isMappedView(String viewId) {
        if (viewId == null) {
            return false;
        }
        if (viewMappings == null) {
            String initParam = FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get(VIEW_MAPPINGS);
            viewMappings = SimplePatternMatcher.parseMultiPatternString(initParam, ";");
        }
        if (viewMappings.isEmpty()) {
            return true;
        }
        for (SimplePatternMatcher mapping : viewMappings) {
            if (mapping.matches(viewId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If this is a resource request, this method will handle the request.
     */
    public static UIViewRoot serveResource(FacesContext facesContext, String resourcePath) {
        // Mark the response complete so no more processing occurs
        facesContext.responseComplete();

        // Create dummy UIViewRoot
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setRenderKitId("dummy");

        // Setup the FacesStreamerContext
        Context facesStreamerContext = new FacesStreamerContext(facesContext);
        facesStreamerContext.setAttribute(Context.FILE_PATH, resourcePath);

        // Get the HttpServletResponse
        Object response = facesContext.getExternalContext().getResponse();
        HttpServletResponse httpServletResponse = null;
        if (response instanceof HttpServletResponse) {
            httpServletResponse = (HttpServletResponse) response;

            // We have an HttpServlet response, do some extra stuff...
            // Check the last modified time to see if we need to serve the resource
            long lastModified = facesStreamerContext.getContentSource().getLastModified(facesStreamerContext);
            if (lastModified != -1) {
                long ifModifiedSince = ((HttpServletRequest) facesContext.getExternalContext().getRequest())
                        .getDateHeader("If-Modified-Since");
                // Round down to the nearest second for a proper compare
                if (ifModifiedSince < lastModified / 1000 * 1000) {
                    // A ifModifiedSince of -1 will always be less
                    httpServletResponse.setDateHeader("Last-Modified", lastModified);
                } else {
                    // Set not modified header and complete response
                    httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return viewRoot;
                }
            }
        }

        // Stream the content
        try {
            FileStreamer.getFileStreamer(facesContext).streamContent(facesStreamerContext);
        } catch (FileNotFoundException ex) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info("JSFT0004", (Object) resourcePath);
            }
            if (httpServletResponse != null) {
                try {
                    httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (IOException ioEx) {
                    // Ignore
                }
            }
        } catch (IOException ex) {
            if (LogUtil.infoEnabled()) {
                LogUtil.info("JSFT0004", (Object) resourcePath);
                if (LogUtil.fineEnabled()) {
                    LogUtil.fine("Resource (" + resourcePath + ") not available!", ex);
                }
            }
// FIXME: send 404?
        }

        // Return dummy UIViewRoot to avoid NPE
        return viewRoot;
    }

    /**
     * Returns the current encoding type.
     *
     * @param facesContext The {@link FacesContext}.
     */
    public static String getEncoding(FacesContext facesContext) {
        // Sanity check
        if (facesContext == null) {
            return null;
        }

        String encodingType = null;
        UIViewRoot viewRoot = facesContext.getViewRoot();
        Map<String, Serializable> pageSession = PageSessionResolver.getPageSession(facesContext, viewRoot);
        if (pageSession != null) {
            // Check for page session
            encodingType = (String) pageSession.get(ENCODING_TYPE);
        }
        if (encodingType == null || encodingType.isEmpty()) {
            // Check for application level
            encodingType = facesContext.getExternalContext().getInitParameter(ENCODING_TYPE);
        }
        if (encodingType == null || encodingType.isEmpty()) {
            ExternalContext externalContext = facesContext.getExternalContext();
            try {
                ServletRequest request = (ServletRequest) externalContext.getRequest();
                encodingType = request.getCharacterEncoding();
            } catch (Exception ex) {
                // Ignore
            }
            if (encodingType == null || encodingType.isEmpty()) {
                // Default encoding type
                encodingType = StandardCharsets.UTF_8.name();
            }
        }
        return encodingType;
    }

    /**
     * This method checks the given viewId and returns a the path to the requested resource if it refers to a resource.
     * Resources are things like JavaScript files, images, etc. Basically anything that is not a JSF page that you'd like to
     * serve up via the FacesServlet. Serving resources this way allows you to bundle the resources in a jar file, this is
     * useful if you want to package up part of an app (or a JSF component) in a single file.
     *
     * <p>
     * A request for a resource must be prefixed by the resource prefix, see @{link #getResourcePrefixes}. This prefix must
     * also be mapped to the <code>FacesServlet</code> in order for this class to handle the request.
     */
    public String getResourcePath(String viewId) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String servletPath = externalContext.getRequestServletPath();
        for (String resourcePrefix : getResourcePrefixes()) {
            if (servletPath.equals(resourcePrefix)) {
                return externalContext.getRequestPathInfo();
            }
        }
        return null;
    }

    /**
     * This method returns the prefix that a URL must contain in order to retrieve a "resource" through this
     * {@link ViewHandler}.
     *
     * <p>
     * The prefix itself does not manifest itself in the file system / classpath.
     *
     * <p>
     * If the prefix is not set, then an init parameter (see {@link #RESOURCE_PREFIX}) will be checked. If that is still not
     * specified, then the {@link #DEFAULT_RESOURCE_PREFIX} will be used.
     */
    public Set<String> getResourcePrefixes() {
        if (resourcePrefix == null) {
            HashSet<String> prefixes = new HashSet<>();
            // Check to see if it's specified by a context param
            // Get context parameter map (initParams in JSF are context params)
            String initParam = FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get(RESOURCE_PREFIX);
            if (initParam != null) {
                for (String prefix : initParam.split(",")) {
                    prefixes.add(prefix.trim());
                }
            }
            // Add default...
            prefixes.add(DEFAULT_RESOURCE_PREFIX);
            resourcePrefix = prefixes;
        }
        return resourcePrefix;
    }

    /**
     * This method allows a user to set the resource prefix which will be checked to obtain a resource via this
     * {@link ViewHandler}. Currently, only one prefix is supported. The prefix itself does not manifest itself in the file
     * system / classpath.
     */
    public void setResourcePrefixes(Set<String> prefixes) {
        resourcePrefix = prefixes;
    }

    /**
     * This method iterates over the child {@link LayoutElement}s of the given {@code element} to create
     * {@link UIComponent}s for each {@link LayoutComponent}.
     *
     * @param facesContext The {@link FacesContext}.
     * @param parentComponent The parent {@link UIComponent} of the {@link UIComponent} to be found or created.
     * @param layoutElement The {@link LayoutElement} driving everything.
     */
    public static void buildUIComponentTree(FacesContext facesContext, UIComponent parentComponent, LayoutElement layoutElement) {
// FIXME: Consider processing *ALL* LayoutElements so that <if> and others
// FIXME: have meaning when inside other components.
        for (LayoutElement childLayoutElement : layoutElement.getChildLayoutElements()) {
            if (childLayoutElement instanceof LayoutFacet) {
                if (!((LayoutFacet) childLayoutElement).isRendered()) {
                    // The contents of this should be a single UIComponent
                    buildUIComponentTree(facesContext, parentComponent, childLayoutElement);
                }
                // NOTE: LayoutFacets that aren't JSF facets aren't
                // NOTE: meaningful in this context
            } else if (childLayoutElement instanceof LayoutComposition) {
                LayoutComposition layoutComposition = (LayoutComposition) childLayoutElement;
                String template = layoutComposition.getTemplate();
                if (template != null) {
                    // Add LayoutComposition to the stack
                    LayoutComposition.push(facesContext, childLayoutElement);

                    try {
                        // Add the template here.
                        buildUIComponentTree(facesContext, parentComponent,
                                LayoutDefinitionManager.getLayoutDefinition(facesContext, template));
                    } catch (LayoutDefinitionException ex) {
                        if (((LayoutComposition) childLayoutElement).isRequired()) {
                            throw ex;
                        }
                    }

                    // Remove the LayoutComposition from the stack
                    LayoutComposition.pop(facesContext);
                } else {
                    // In this case we don't have a template, so instead we
                    // render the body
                    buildUIComponentTree(facesContext, parentComponent, childLayoutElement);
                }
            } else if (childLayoutElement instanceof LayoutInsert) {
                Stack<LayoutElement> compositionStack = LayoutComposition.getCompositionStack(facesContext);
                if (compositionStack.empty()) {
                    // No template-client found...
                    // Is this supposed to do nothing? Or throw an exception?
                    throw new IllegalArgumentException("'ui:insert' encountered, however, no " + "'ui:composition' was used!");
                }

                // Get associated UIComposition
                String insertName = ((LayoutInsert) childLayoutElement).getName();
                if (insertName == null) {
                    // Include everything
                    buildUIComponentTree(facesContext, parentComponent, compositionStack.get(0));
                } else {
                    // First resolve any EL in the insertName
                    insertName = "" + ((LayoutInsert) childLayoutElement).resolveValue(facesContext, parentComponent, insertName);

                    // Search for specific LayoutDefine
                    LayoutElement layoutDefine = LayoutInsert.findLayoutDefine(facesContext, parentComponent, compositionStack, insertName);
                    if (layoutDefine == null) {
                        // Not found include the body-content of the insert
                        buildUIComponentTree(facesContext, parentComponent, childLayoutElement);
                    } else {
                        // Found, include the ui:define content
                        buildUIComponentTree(facesContext, parentComponent, layoutDefine);
                    }
                }
            } else if (childLayoutElement instanceof LayoutComponent) {
                // Calling getChild will add the child UIComponent to tree
                UIComponent childComponent = ((LayoutComponent) childLayoutElement).getChild(facesContext, parentComponent);

                if (LayoutDefinitionManager.isDebug(facesContext)) {
                    // To help developer avoid duplicate ids, we'll check the ids here.
                    Map<String, String> clientIdMap = getClientIdMap(facesContext);
                    String clientId = childComponent.getClientId(facesContext);
                    if (clientIdMap.containsKey(clientId)) {
                        if (!((LayoutComponent) childLayoutElement).containsOption(LayoutComponent.SKIP_ID_CHECK)
                                && LogUtil.warningEnabled()) {
                            LogUtil.warning("JSFT0011", (Object) clientId);
                        }

                        // Clear the map as a way to prevent this message from
                        // being shown tons of times as may occur in some
                        // valid use cases. Remember this is just a debug-time
                        // only helpful message anyway...
                        clientIdMap.clear();
                    }
                    clientIdMap.put(clientId, clientId);
                }

                // Check for events
                // NOTE: For now I am only supporting "action" and
                // NOTE: "actionListener" event types. In the future it
                // NOTE: may be desirable to support beforeEncode /
                // NOTE: afterEncode as well. At this time, those events
                // NOTE: are supported by the "Event" UIComponent. That
                // NOTE: component can wrap non-layout-based components to
                // NOTE: achieve this functionality (supporting that
                // NOTE: functionality here will simply do the same thing
                // NOTE: automatically).

                // Recurse
                buildUIComponentTree(facesContext, childComponent, childLayoutElement);
            } else {
                buildUIComponentTree(facesContext, parentComponent, childLayoutElement);
            }
        }
    }

    /**
     * This method provides access to a {@link Map} of clientIds that have been used in this page.
     */
    private static Map<String, String> getClientIdMap(FacesContext facesContext) {
        Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
        @SuppressWarnings("unchecked")
        Map<String, String> debugIdMap = (Map<String, String>) requestMap.get("__debugIdMap");
        if (debugIdMap == null) {
            debugIdMap = new HashMap<>();
            requestMap.put("__debugIdMap", debugIdMap);
        }
        return debugIdMap;
    }

    /**
     * Reconstructs the UIViewRoot.
     */
    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId) {
        Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
        if (requestMap.get(RESTORE_VIEW_ID) == null) {
            requestMap.put(RESTORE_VIEW_ID, viewId);
        } else {
            // This request has already been processed, it must be a forward()
            return createView(facesContext, viewId);
        }

        // Perform default behavior...
        if (!isMappedView(viewId)) {
            return oldViewHandler.restoreView(facesContext, viewId);
        }

        UIViewRoot viewRoot = StateManagerUtil.restoreView(facesContext, viewId,
                facesContext.getApplication().getViewHandler().calculateRenderKitId(facesContext));

        // We can check for JSFT UIViewRoots by calling
        // getLayoutDefinitionKey(root) as this will return null if not JSFT
        if (viewRoot != null) {
            String layoutDefinitionKey = ViewRootUtil.getLayoutDefinitionKey(viewRoot);
            if (layoutDefinitionKey != null) {
                // Set the View Root to the new viewRoot (needed for initPage)
                // NOTE: See createView note about saving / restoring the
                // NOTE: original UIViewRoot and issue with setting it to
                // NOTE: (null). restoreView is less important b/c it is not
                // NOTE: normally called by a developer or framework as
                // NOTE: navigation rules will call createView. For this
                // NOTE: reason, I am not resetting the UIViewRoot for now.
                facesContext.setViewRoot(viewRoot);

                // Call getLayoutDefinition() to ensure initPage events are
                // fired, only do this for JSFT ViewRoots. Its good to call
                // this after restoreView as we need the UIViewRoot available
                // during initPage events. Formerly this was done during the
                // ApplyRequestValuesPhase, however, I no longer have a custom
                // UIViewRoot to use for this purpose, so I will do it here,
                // which should be just as good.
                LayoutDefinition layoutDefinition = ViewRootUtil.getLayoutDefinition(layoutDefinitionKey);

                // While we're at it, we should call the LD decode() event so
                // we can provide a page-level decode() functionality. This
                // won't effect components in the page, or JSFT-based
                // components.
                layoutDefinition.decode(facesContext, viewRoot);
            }
        }

        // Return the UIViewRoot
        return viewRoot;
    }

    /**
     * Perform whatever actions are required to render the response view to the response object associated with the current
     * {@link FacesContext}.
     */
    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewToRender) throws IOException {
        // Make sure we have a def
        LayoutDefinition layoutDefinition = ViewRootUtil.getLayoutDefinition(viewToRender);
        if (layoutDefinition == null) {
            // PartialRequest or No def, fall back to default behavior
            oldViewHandler.renderView(facesContext, viewToRender);
        } else {
            // Start document
            if (!facesContext.getPartialViewContext().isPartialRequest() || facesContext.getPartialViewContext().isRenderAll()) {
                ResponseWriter responseWriter = setupResponseWriter(facesContext);
                responseWriter.startDocument();

                // Render content
                layoutDefinition.encode(facesContext, viewToRender);

                // End document
                responseWriter.endDocument();
            } else {
                // NOTE: This "if" branch has been added to avoid the
                // NOTE: start/endDocument calls being called 2x on PartialView
                // NOTE: requests. JSF Issue #1307 has been filed to resolve
                // NOTE: this correctly (assuming checking here is not
                // NOTE: correct... which I do not feel that it is).
                //
                // Render content
                layoutDefinition.encode(facesContext, viewToRender);
            }
        }
    }

    private static void renderComponent(FacesContext facesContext, UIComponent component) throws IOException {
        if (!component.isRendered()) {
            return;
        }

        component.encodeBegin(facesContext);
        if (component.getRendersChildren()) {
            component.encodeChildren(facesContext);
        } else {
            for (UIComponent childComponent : component.getChildren()) {
                renderComponent(facesContext, childComponent);
            }
        }
        component.encodeEnd(facesContext);
    }

    /**
     *
     */
    private ResponseWriter setupResponseWriter(FacesContext facesContext) throws IOException {
        ResponseWriter responseWriter = facesContext.getResponseWriter();
        if (responseWriter != null) {
            // It is already setup
            return responseWriter;
        }

        ExternalContext externalContext = facesContext.getExternalContext();
        ServletResponse response = (ServletResponse) externalContext.getResponse();

        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = renderKitFactory.getRenderKit(facesContext, facesContext.getViewRoot().getRenderKitId());

        // See if the user (page author) specified a ContentType...
        String contentTypeList = null;
// FIXME: Provide a way for the user to specify this...
// FIXME: Test multiple browsers against this code!!
        String userContentType = "text/html";
        if (userContentType != null && !userContentType.isEmpty()) {
            // User picked this, use it...
            response.setContentType(userContentType);
        } else {
            // No explicit Content-type, find best match...
            contentTypeList = externalContext.getRequestHeaderMap().get("Accept");
            if (contentTypeList == null) {
                contentTypeList = "text/html;q=1.0";
            }
        }
        String encodingType = getEncoding(facesContext);

        externalContext.getSessionMap().put(ViewHandler.CHARACTER_ENCODING_KEY, encodingType);
// FIXME: use the external context to set the character encoding, it is supported
        response.setCharacterEncoding(encodingType);

        responseWriter = renderKit.createResponseWriter(new OutputStreamWriter(response.getOutputStream(), encodingType), contentTypeList,
                encodingType);
        facesContext.setResponseWriter(responseWriter);
        // Not setting the contentType here results in XHTML which formats differently
        // than text/html in Mozilla.. even though the documentation claims this
        // works, it doesn't (try viewing the Tree)
        // response.setContentType("text/html");

        // As far as I can tell JSF doesn't ever set the Content-type that it
        // works so hard to calculate... This is the code we should be
        // calling, however we can't do this yet
        response.setContentType(responseWriter.getContentType());

        return responseWriter;
    }

    /**
     * Take any appropriate action to either immediately write out the current state information (by calling
     * {@link StateManager#writeState}, or noting where state information should later be written.
     *
     * @param facesContext {@link FacesContext} for the current request
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void writeState(FacesContext facesContext) throws IOException {
        // Check to see if we should delegate back to the legacy ViewHandler
        UIViewRoot viewRoot = facesContext.getViewRoot();
// FIXME: For now I am treating "@all" Ajax requests as normal requests...
// FIXME: Otherwise the view state is not written.
        if (viewRoot == null
                || facesContext.getPartialViewContext().isPartialRequest() && !facesContext.getPartialViewContext().isRenderAll()
                || ViewRootUtil.getLayoutDefinition(viewRoot) == null) {
            // Use old behavior...
            oldViewHandler.writeState(facesContext);
        } else {
            // Because we pre-processed the ViewTree, we can just add it...
            StateManager stateManager = facesContext.getApplication().getStateManager();

            // New versions of JSF 1.2 changed the contract so that state is
            // always written (client and server state saving)
            Object savedView = StateManagerUtil.saveView(facesContext, facesContext.getViewRoot().getViewId());
            if (savedView != null) {
                stateManager.writeState(facesContext, savedView);
            }
        }
    }

    /**
     * Return a URL suitable for rendering (after optional encoding performed by the {@code encodeResourceURL()} method of
     * {@code ExternalContext} that selects the specified web application resource. If the specified path starts with a
     * slash, it must be treated as context relative; otherwise, it must be treated as relative to the action URL of the
     * current view.
     *
     * @param facesContext {@link FacesContext} for the current request
     * @param resourcePath Resource path to convert to a URL
     * @exception IllegalArgumentException If {@code viewId} is not valid for this {@link ViewHandler}.
     */
    @Override
    public String getResourceURL(FacesContext facesContext, String resourcePath) {
        return oldViewHandler.getResourceURL(facesContext, resourcePath);
    }

    @Override
    public String getWebsocketURL(FacesContext facesContext, String channel) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Return a URL suitable for rendering (after optional encoding performed by the {@code encodeActionURL()} method of
     * {@code ExternalContext} that selects the specified view identifier.
     *
     * @param facesContext {@link FacesContext} for this request
     * @param viewId View identifier of the desired view
     * @exception IllegalArgumentException If {@code viewId} is not valid for this {@link ViewHandler}.
     */
    @Override
    public String getActionURL(FacesContext facesContext, String viewId) {
        return oldViewHandler.getActionURL(facesContext, viewId);
    }

    /**
     * Returns an appropriate {@link Locale} to use for this and subsequent requests for the current client.
     *
     * @param facesContext {@link FacesContext} for the current request
     * @exception NullPointerException if {@code context} is {@code null}
     */
    @Override
    public Locale calculateLocale(FacesContext facesContext) {
        return oldViewHandler.calculateLocale(facesContext);
    }

    /**
     * Return an appropriate {@code renderKitId} for this and subsequent requests from the current client.
     *
     * <p>
     * The default return value is {@link jakarta.faces.render.RenderKitFactory#HTML_BASIC_RENDER_KIT}.
     *
     * @param facesContext {@link FacesContext} for the current request.
     */
    @Override
    public String calculateRenderKitId(FacesContext facesContext) {
        return oldViewHandler.calculateRenderKitId(facesContext);
    }
}
