/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

/*
 * Currently hosted in GlassFish; destined for Jersey core-server as
 * org.glassfish.jersey.server.model.ResourceMethodSelector (PR 1).
 *
 * A request-independent, thread-safe selector over a deployed ResourceModel.
 * It answers, at deployment time or at the start of a request (e.g. from a
 * container security valve such as GlassFish's RealmAdapter):
 *
 *   1. Which resource methods exist, as a flat list of endpoints?
 *   2. Which sub-resource locators exist (statically)?
 *   3. For an encoded, application-relative path + HTTP method: which single
 *      resource method would Jersey select?
 *
 * "Selector", not "matcher": many templates MATCH a given path, but Jersey
 * SELECTS exactly one resource method. That singularity is the property the
 * whole design rests on -- it is what lets an authorization permission be
 * compared by equality instead of by pattern matching.
 *
 * PARITY STRATEGY
 * ---------------
 * This class deliberately mirrors, decision for decision, the runtime router
 * construction and selection logic:
 *
 *   - Route table layout        -> RuntimeModelBuilder#buildModel(...)
 *   - Sorting                   -> RuntimeResourceModel / RuntimeResource.COMPARATOR
 *                                  -> PathPattern.COMPARATOR -> UriTemplate.COMPARATOR
 *                                  (JAX-RS 3.7.2: literal chars desc, template vars
 *                                  desc, explicit regexes desc)
 *   - Selection loop            -> PathMatchingRouter#apply(...) including the
 *                                  "matched method route with wrong designator
 *                                  beats a later locator" (405) rule and the
 *                                  HEAD->GET designator fallback
 *   - Input normalization       -> MatchResultInitializerRouter:
 *                                  "/" + ContainerRequest.getPath(false)
 *                                  (i.e. the ENCODED path relative to the base URI)
 *   - Remaining-path extraction -> UriRoutingContext#getFinalMatchingGroup():
 *                                  matchResult.group(groupCount()), null -> ""
 *
 * PATH IDENTITY
 * -------------
 * Both sides of the authorization contract derive their path strings from the
 * declaring Resource, never from the RuntimeResource:
 *
 *   - staging   -> ResourceEndpoint#getTemplatePath() / #getPatternPath()
 *   - selection -> Selection#getTemplatePath() / #getPatternPath()
 *
 * This matters because RuntimeResourceModel groups resources by REGEX, and
 * RuntimeResource then takes its path pattern from resources.get(0) -- an
 * arbitrary member of that group. Two resources declared as /users/{id} and
 * /users/{userId} share one RuntimeResource, so reading the template from it
 * would report an arbitrary one of the two. Walking the selected method's
 * declaring Resource chain instead reports the template that method was
 * actually declared with, which is exactly what staging enumerated.
 *
 * IMPORTANT: construct this from the *enhanced* model that Jersey actually
 * deploys, i.e. the one from ExtendedResourceContext#getResourceModel(),
 * ApplicationHandler#getResourceModel() or ApplicationEvent#getResourceModel()
 * at INITIALIZATION_APP_FINISHED. That model already includes ModelProcessor
 * output (e.g. synthetic OPTIONS methods). A hand-built ResourceModel will not
 * match runtime behaviour.
 */
package com.sun.enterprise.security.ee.authorization;

import jakarta.ws.rs.core.MediaType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.MatchResult;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.RuntimeResource;
import org.glassfish.jersey.server.model.RuntimeResourceModel;
import org.glassfish.jersey.uri.PathPattern;
import org.glassfish.jersey.uri.PathTemplate;
import org.glassfish.jersey.uri.UriTemplate;

import static com.sun.enterprise.security.ee.authorization.ResourceMethodSelector.Outcome.LOCATOR;
import static com.sun.enterprise.security.ee.authorization.ResourceMethodSelector.Outcome.METHOD_NOT_ALLOWED;
import static com.sun.enterprise.security.ee.authorization.ResourceMethodSelector.Outcome.NO_MATCH;
import static com.sun.enterprise.security.ee.authorization.ResourceMethodSelector.Outcome.RESOURCE_METHOD;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Locale.ROOT;
import static org.glassfish.jersey.uri.PathPattern.END_OF_PATH_PATTERN;
import static org.glassfish.jersey.uri.PathPattern.OPEN_ROOT_PATH_PATTERN;

public final class ResourceMethodSelector {

    private static final int MAX_LOCATOR_DEPTH = 32;

    private final ResourceModel resourceModel;
    private final List<StaticRoute> rootRoutes;
    private final boolean expandLocators;
    private final ConcurrentMap<Class<?>, List<StaticRoute>> locatorRouteCache = new ConcurrentHashMap<>();

    /** Outcome classification of a {@link #select(String, String)} call. */
    public enum Outcome {
        /** A concrete resource method was selected: what runtime dispatch would invoke. */
        RESOURCE_METHOD,

        /**
         * The path matched a resource, but no method matches the HTTP designator.
         * Runtime would respond 405; {@link Selection#getAllowedMethods()} lists candidates.
         */
        METHOD_NOT_ALLOWED,

        /**
         * Selection terminated at a sub-resource locator whose result could not be
         * (fully) resolved statically. {@link Selection#getLocatorChain()} identifies
         * the locator(s); {@link Selection#getUnresolvedRemainder()} is the path
         * segment string the locator's sub-model would have to match at runtime.
         *
         * For authorization this is contractually NOT_APPLICABLE: the final endpoint
         * is not statically knowable, so enforcement belongs to request-time components
         * registered for the locator subtree.
         */
        LOCATOR,

        /** No terminal resource method or locator was selected; runtime would respond 404. */
        NO_MATCH
    }

    /**
     * Create a selector for the given (model-processor enhanced) resource model, with
     * no static expansion of sub-resource locators enabled.
     *
     * @param enhancedModel The model to build the selector over. Must be Jersey's fully enhanced,
     * deployment-time model (see class comment); a hand-built ResourceModel will not match
     * runtime behaviour.
     */
    public static ResourceMethodSelector of(ResourceModel enhancedModel) {
        return new ResourceMethodSelector(enhancedModel, false);
    }

    /**
     * @param enhancedModel The model to build the selector over.
     * @param expandLocators Whether to speculatively expand sub-resource locators from their
     * declared return type. Authorization consumers normally pass false: a locator result is
     * not statically knowable, so {@link Outcome#LOCATOR} is the honest answer.
     */
    public static ResourceMethodSelector of(ResourceModel enhancedModel, boolean expandLocators) {
        return new ResourceMethodSelector(enhancedModel, expandLocators);
    }

    /**
     * Canonicalize a raw template path into the same Jersey-native pattern form as
     * {@link Selection#getPatternPath()}. Only needed for path strings that did NOT come from
     * this selector -- hand-authored policy grants, for example. Strings obtained from
     * {@link ResourceEndpoint#getPatternPath()} are already in this form.
     *
     * @param rawTemplatePath The application-relative raw template path to canonicalize.
     */
    public static String canonicalize(String rawTemplatePath) {
        if (rawTemplatePath == null || rawTemplatePath.isEmpty() || "/".equals(rawTemplatePath)) {
            return "/";
        }

        return new PathTemplate(rawTemplatePath).getPattern().toString();
    }

    // ------------------------------------------------------------------
    // Enumeration surface (deployment time)
    // ------------------------------------------------------------------


    /** All root resources of the deployed model (drill down via Resource getters). */
    public List<Resource> getRootResources() {
        return resourceModel.getRootResources();
    }

    public ResourceModel getResourceModel() {
        return resourceModel;
    }

    /** Root path templates relative to the application (mount point owned by the servlet layer). */
    public Set<String> getRootPathTemplates() {
        Set<String> rootPathTemplates = new LinkedHashSet<>();
        for (Resource rootResource : resourceModel.getRootResources()) {
            rootPathTemplates.add(normalizeTemplate(rootResource.getPath()));
        }

        return rootPathTemplates;
    }

    /** All statically declared sub-resource locators, root and child level. */
    public List<LocatorEntry> getSubResourceLocators() {
        List<LocatorEntry> locatorEntries = new ArrayList<>();
        for (Resource rootResource : resourceModel.getRootResources()) {
            collectLocators(rootResource, normalizeTemplate(rootResource.getPath()), locatorEntries);
        }

        return locatorEntries;
    }

    /**
     * Flattened enumeration of all terminal endpoints (root and child resource methods) of the
     * deployed model: one entry per (template, HTTP designator, resource method). Deterministically
     * ordered by pattern path, then designator. This is the deploy-time input for permission
     * staging; consumers typically skip {@link ResourceEndpoint#isExtended() extended} entries.
     */
    public List<ResourceEndpoint> getResourceEndpoints() {
        List<ResourceEndpoint> resourceEndpoints = new ArrayList<>();
        for (Resource rootResource : resourceModel.getRootResources()) {
            UriTemplate rootTemplate = rootResource.getPathPattern().getTemplate();

            collectEndpoints(rootResource, rootTemplate.getTemplate(), rootTemplate.getPattern().toString(), resourceEndpoints);
        }

        resourceEndpoints.sort(
            comparing(ResourceEndpoint::getPatternPath).thenComparing(ResourceEndpoint::getHttpMethod));

        return List.copyOf(resourceEndpoints);
    }

    // ------------------------------------------------------------------
    // Selection surface (request time)
    // ------------------------------------------------------------------


    /**
     * Select the single resource method Jersey would invoke for the given request.
     *
     * @param encodedAppRelativePath The ENCODED request path relative to the application base URI
     * (context path + servlet mapping already stripped) -- exactly what ContainerRequest.getPath(false)
     * would return, with or without a leading slash. Matrix parameters, if present, must be included,
     * as at runtime.
     * @param httpMethod The HTTP method designator, e.g. {@code "GET"}.
     */
    public Selection select(String encodedAppRelativePath, String httpMethod) {
        String normalizedHttpMethod = httpMethod == null ? "" : httpMethod.trim().toUpperCase(ROOT);

        // Mirrors MatchResultInitializerRouter: "/" + request.getPath(false)
        String path = encodedAppRelativePath == null ? "/" : encodedAppRelativePath;
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        State state = new State(normalizedHttpMethod);
        Selection selection = selectLevel(rootRoutes, path, state, 0);
        if (selection != null) {
            return selection;
        }

        return new Selection(
                NO_MATCH,
                normalizedHttpMethod,
                state.templates,
                null, null, null,
                Set.of(),
                state.locatorChain,
                null,
                state.speculative);
    }

    /** One statically declared sub-resource locator. */
    public static final class LocatorEntry {

        private final String fullPathTemplate;
        private final ResourceMethod locator;
        private final Class<?> staticReturnType;

        LocatorEntry(String fullPathTemplate, ResourceMethod locator, Class<?> staticReturnType) {
            this.fullPathTemplate = fullPathTemplate;
            this.locator = locator;
            this.staticReturnType = staticReturnType;
        }

        /** Locator template relative to the application root, e.g. {@code /orders/{id}/items}. */
        public String getFullPathTemplate() {
            return fullPathTemplate;
        }

        public ResourceMethod getLocator() {
            return locator;
        }

        /** Declared (raw) routing return type; the runtime object may be a subtype. */
        public Class<?> getStaticReturnType() {
            return staticReturnType;
        }
    }

    /**
     * One terminal endpoint (resource method) of the deployed model, in flattened form.
     * This is the deploy-time unit for permission staging: pair it with a mount base
     * from the servlet layer to obtain the context-relative identity.
     */
    public static final class ResourceEndpoint {

        private final String templatePath;
        private final String patternPath;
        private final String httpMethod;
        private final Resource resource;
        private final ResourceMethod resourceMethod;
        private final boolean extended;

        ResourceEndpoint(String templatePath, String patternPath, String httpMethod, Resource resource,
                ResourceMethod resourceMethod, boolean extended) {
            this.templatePath = templatePath;
            this.patternPath = patternPath;
            this.httpMethod = httpMethod;
            this.resource = resource;
            this.resourceMethod = resourceMethod;
            this.extended = extended;
        }

        /**
         * Application-relative raw template, e.g.
         * {@code /protectedResource/{id}/users/{username: [A-Z][a-zA-Z_0-9]*}/sayHi}.
         * Derived from the declaring Resource, so variable names and spacing are those this
         * endpoint was actually declared with. Equal by construction to
         * {@link Selection#getTemplatePath()} for a request that selects this endpoint.
         */
        public String getTemplatePath() {
            return templatePath;
        }

        /**
         * Application-relative Jersey-native pattern (regex) form, e.g.
         * {@code /protectedResource/([^/]+)/users/([A-Z][a-zA-Z_0-9]*)/sayHi}.
         * Variable names and template spacing are erased, so resources Jersey considers
         * regex-equal share one value. Equal by construction to
         * {@link Selection#getPatternPath()} for a request that selects this endpoint.
         */
        public String getPatternPath() {
            return patternPath;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public Resource getResource() {
            return resource;
        }

        public ResourceMethod getResourceMethod() {
            return resourceMethod;
        }

        /**
         * The concrete handler (resource) class. Use this for security-annotation
         * resolution -- not the definition method's declaring class, which for
         * interface- or superclass-defined resource methods is the interface/superclass.
         */
        public Class<?> getResourceClass() {
            return resourceMethod.getInvocable().getHandler().getHandlerClass();
        }

        /** The concrete handling method. Use this for security-annotation resolution. */
        public Method getJavaMethod() {
            return resourceMethod.getInvocable().getHandlingMethod();
        }

        /** Media types this endpoint declares it consumes; empty when unconstrained. */
        public List<MediaType> getConsumedTypes() {
            return resourceMethod.getConsumedTypes();
        }

        /** Media types this endpoint declares it produces; empty when unconstrained. */
        public List<MediaType> getProducedTypes() {
            return resourceMethod.getProducedTypes();
        }

        /**
         * True for ModelProcessor-added synthetics (WADL resources, auto-OPTIONS
         * inflectors). Permission staging should skip these: their handling method is
         * {@code Inflector.apply}, which carries no security annotations.
         */
        public boolean isExtended() {
            return extended;
        }

        @Override
        public String toString() {
            return
                "\n" +
                "httpMethod=" + httpMethod + "\n" +
                "templatePath=" + templatePath + "\n" +
                "patternPath=" + patternPath + "\n" +
                "extended=" + extended + "\n\n";
        }
    }

    /** Immutable result of a selection. */
    public static final class Selection {

        private final Outcome outcome;
        private final String httpMethod;
        private final List<UriTemplate> selectedTemplates;   // root -> leaf order
        private final RuntimeResource selectedRuntimeResource;
        private final Resource selectedResource;
        private final ResourceMethod selectedMethod;
        private final Set<String> allowedMethods;
        private final List<ResourceMethod> locatorChain;
        private final String unresolvedRemainder;
        private final boolean speculative;
        private final String templatePath;
        private final String patternPath;

        Selection(Outcome outcome, String httpMethod, List<UriTemplate> selectedTemplates,
                RuntimeResource selectedRuntimeResource, Resource selectedResource, ResourceMethod selectedMethod,
                Set<String> allowedMethods, List<ResourceMethod> locatorChain, String unresolvedRemainder,
                boolean speculative) {
            this.outcome = outcome;
            this.httpMethod = httpMethod;
            this.selectedTemplates = List.copyOf(selectedTemplates);
            this.selectedRuntimeResource = selectedRuntimeResource;
            this.selectedResource = selectedResource;
            this.selectedMethod = selectedMethod;
            this.allowedMethods = Collections.unmodifiableSet(new LinkedHashSet<>(allowedMethods));
            this.locatorChain = List.copyOf(locatorChain);
            this.unresolvedRemainder = unresolvedRemainder;
            this.speculative = speculative;
            this.templatePath = computeTemplatePath(selectedMethod, speculative, this.selectedTemplates);
            this.patternPath = computePatternPath(selectedMethod, speculative, this.selectedTemplates);
        }

        public Outcome getOutcome() {
            return outcome;
        }

        /** The normalized HTTP method the selection was requested for. */
        public String getHttpMethod() {
            return httpMethod;
        }

        /**
         * The selected chain as ONE application-relative raw template string, e.g.
         * {@code /protectedResource/{id}/users/{username: [A-Z][a-zA-Z_0-9]*}/sayHi}.
         *
         * When a resource method was selected, this is derived from that method's declaring
         * Resource chain, so it is exactly the template the method was declared with -- and
         * therefore exactly what {@link ResourceEndpoint#getTemplatePath()} staged for it.
         * For outcomes without a selected method it falls back to the traversed route
         * templates, which for regex-grouped resources report an arbitrary group member.
         * Those outcomes are NOT_APPLICABLE for authorization anyway.
         *
         * Returns "/" when nothing was selected.
         */
        public String getTemplatePath() {
            return templatePath;
        }

        /**
         * The selected chain as ONE Jersey-native pattern (regex) string, e.g.
         * {@code /protectedResource/([^/]+)/users/([A-Z][a-zA-Z_0-9]*)/sayHi}.
         * Same derivation as {@link #getTemplatePath()}; variable names and template
         * whitespace are erased. Resources Jersey considers regex-equal share one value,
         * so this is stable across a merge group where the raw template is not.
         *
         * Returns "/" when nothing was selected.
         */
        public String getPatternPath() {
            return patternPath;
        }

        /**
         * The HTTP designator that will actually serve the request: the selected method's
         * designator when a method was selected -- so a HEAD request served via a GET method
         * reports "GET" -- else the requested method. Use this, not the raw request method,
         * as the effective designator; otherwise stored (template, GET) permissions silently
         * miss HEAD requests.
         */
        public String getEffectiveHttpMethod() {
            if (selectedMethod != null) {
                return selectedMethod.getHttpMethod();
            }
            return httpMethod;
        }

        /** Traversed path templates in request order (root first). Cf. ExtendedUriInfo#getMatchedTemplates (reversed). */
        public List<UriTemplate> getSelectedTemplates() {
            return selectedTemplates;
        }

        public RuntimeResource getSelectedRuntimeResource() {
            return selectedRuntimeResource;
        }

        /** Model resource owning the selected method; first merged resource for other outcomes. */
        public Resource getSelectedResource() {
            return selectedResource;
        }

        /** Non-null only for {@link Outcome#RESOURCE_METHOD}. Handler via getInvocable().getHandler(). */
        public ResourceMethod getSelectedMethod() {
            return selectedMethod;
        }

        /** Declared HTTP methods at the selected level (HEAD additionally served via GET at runtime). */
        public Set<String> getAllowedMethods() {
            return allowedMethods;
        }

        /** Locators traversed to reach this result (empty if none). */
        public List<ResourceMethod> getLocatorChain() {
            return locatorChain;
        }

        /** For {@link Outcome#LOCATOR}: encoded path remainder left for the runtime sub-model. */
        public String getUnresolvedRemainder() {
            return unresolvedRemainder;
        }

        /**
         * True if any hop traversed a locator expanded from its *declared* return type.
         * The runtime object may be a subtype with a different model; treat as best effort.
         */
        public boolean isSpeculative() {
            return speculative;
        }

        @Override
        public String toString() {
            StringBuilder description = new StringBuilder("Selection[").append(outcome).append(' ').append(httpMethod)
                    .append(' ').append(templatePath);
            if (selectedMethod != null) {
                description.append(" method=").append(selectedMethod.getInvocable().getHandlingMethod());
            }
            if (unresolvedRemainder != null) {
                description.append(" remainder='").append(unresolvedRemainder).append('\'');
            }
            if (speculative) {
                description.append(" (speculative)");
            }
            return description.append(']').toString();
        }

        /**
         * The Resource chain a method was declared under, root first. Walking the declaring
         * Resource avoids the RuntimeResource, whose path pattern comes from an arbitrary
         * member of its regex group.
         */
        private static List<Resource> declaringChain(ResourceMethod selectedMethod) {
            List<Resource> chain = new ArrayList<>();
            Resource resource = selectedMethod.getParent();
            while (resource != null) {
                chain.add(resource);
                resource = resource.getParent();
            }
            Collections.reverse(chain);
            return chain;
        }

        private static String computeTemplatePath(ResourceMethod selectedMethod, boolean speculative,
                List<UriTemplate> selectedTemplates) {
            StringBuilder templatePath = new StringBuilder();

            if (selectedMethod != null && !speculative) {
                for (Resource resource : declaringChain(selectedMethod)) {
                    templatePath.append(resource.getPathPattern().getTemplate().getTemplate());
                }
            } else {
                for (UriTemplate template : selectedTemplates) {
                    templatePath.append(template.getTemplate());
                }
            }

            if (templatePath.isEmpty()) {
                return "/";
            }
            return templatePath.toString();
        }

        private static String computePatternPath(ResourceMethod selectedMethod, boolean speculative,
                List<UriTemplate> selectedTemplates) {
            StringBuilder patternPath = new StringBuilder();

            if (selectedMethod != null && !speculative) {
                for (Resource resource : declaringChain(selectedMethod)) {
                    patternPath.append(resource.getPathPattern().getTemplate().getPattern().toString());
                }
            } else {
                for (UriTemplate template : selectedTemplates) {
                    patternPath.append(template.getPattern().toString());
                }
            }

            if (patternPath.isEmpty()) {
                return "/";
            }
            return patternPath.toString();
        }
    }


    // ### Private methods


    private ResourceMethodSelector(ResourceModel enhancedModel, boolean expandLocators) {
        this.resourceModel = enhancedModel;
        this.expandLocators = expandLocators;
        this.rootRoutes = buildRoutes(enhancedModel.getRuntimeResourceModel(), false);
    }

    // ------------------------------------------------------------------
    // Privates for Enumeration surface (deployment time)
    // ------------------------------------------------------------------


    private static void collectEndpoints(Resource resource, String templatePath, String patternPath, List<ResourceEndpoint> endpoints) {
        addEndpoints(resource, templatePath, patternPath, endpoints);

        for (Resource child : resource.getChildResources()) {
            UriTemplate childTemplate = child.getPathPattern().getTemplate();

            collectEndpoints(child, templatePath + childTemplate.getTemplate(), patternPath + childTemplate.getPattern().toString(),
                    endpoints);
        }
    }

    private static void addEndpoints(Resource resource, String templatePath, String patternPath, List<ResourceEndpoint> resourceEndpoints) {
        String effectiveTemplatePath = templatePath.isEmpty() ? "/" : templatePath;
        String effectivePatternPath = patternPath.isEmpty() ? "/" : patternPath;

        for (ResourceMethod resourceMethod : resource.getResourceMethods()) {
            resourceEndpoints.add(new ResourceEndpoint(
                    effectiveTemplatePath,
                    effectivePatternPath,
                    resourceMethod.getHttpMethod(),
                    resource,
                    resourceMethod,
                    resourceMethod.isExtended()));
        }
    }

    // ------------------------------------------------------------------
    // Privates for Selection surface (request time)
    // ------------------------------------------------------------------


    /**
     * Mirrors PathMatchingRouter#apply: first pattern match wins, except that a method route
     * matched with a non-matching HTTP designator is remembered and preferred over any later
     * locator route (so runtime yields 405, not the locator).
     */
    private Selection selectLevel(List<StaticRoute> routes, String path, State state, int depth) {
        StaticRoute candidate = null;
        MatchResult candidateResult = null;

        for (StaticRoute route : routes) {
            MatchResult matchResult = route.pattern.match(path);
            if (matchResult == null) {
                continue;
            }
            if (route.httpMethods == null && candidate != null) {
                return selectRoute(candidate, candidateResult, state, depth);
            }
            if (route.httpMethods == null || designatorMatch(route, state.httpMethod)) {
                return selectRoute(route, matchResult, state, depth);
            }
            if (candidate == null) {
                candidate = route;
                candidateResult = matchResult;
            }
        }

        if (candidate == null) {
            return null;
        }
        return selectRoute(candidate, candidateResult, state, depth);
    }

    private Selection selectRoute(StaticRoute route, MatchResult matchResult, State state, int depth) {
        UriTemplate template = route.pattern.getTemplate();
        if (!template.getTemplate().isEmpty()) {
            state.templates.add(template);
        }

        if (route.inner != null) {
            return selectLevel(route.inner, remainingPath(matchResult), state, depth);
        }
        if (route.locator != null) {
            return followLocator(route, remainingPath(matchResult), state, depth);
        }

        // Terminal method route. Note: full runtime method selection additionally involves
        // @Consumes/@Produces (-> 415/406); path and designator selection -- which is what
        // request-time authorization needs -- ends here.
        ResourceMethod selectedMethod = selectMethod(route.methods, state.httpMethod);
        Resource owningResource;
        if (selectedMethod != null) {
            owningResource = selectedMethod.getParent();
        } else if (route.resource.getResources().isEmpty()) {
            owningResource = null;
        } else {
            owningResource = route.resource.getResources().get(0);
        }

        Outcome outcome = selectedMethod != null ? RESOURCE_METHOD : METHOD_NOT_ALLOWED;

        return new Selection(
            outcome,
            state.httpMethod,
            state.templates,
            route.resource,
            owningResource,
            selectedMethod,
            route.httpMethods,
            state.locatorChain,
            null,
            state.speculative);
    }

    private Selection followLocator(StaticRoute route, String remaining, State state, int depth) {
        state.locatorChain.add(route.locator);

        List<StaticRoute> subRoutes = null;
        if (expandLocators && depth < MAX_LOCATOR_DEPTH) {
            subRoutes = expandableRoutes(route.locator.getInvocable().getRawRoutingResponseType());
        }

        if (subRoutes != null && !subRoutes.isEmpty()) {
            state.speculative = true;
            Selection speculativeSelection = selectLevel(subRoutes, remaining, state, depth + 1);
            if (speculativeSelection != null) {
                return speculativeSelection;
            }
            // Speculative sub-model did not match: fall through and report the locator hop
            // honestly instead of predicting a 404 the real object might not produce.
        }

        Resource owningResource;
        if (route.resource.getResources().isEmpty()) {
            owningResource = null;
        } else {
            owningResource = route.resource.getResources().get(0);
        }

        return new Selection(
            LOCATOR,
            state.httpMethod,
            state.templates,
            route.resource,
            owningResource,
            null,
            Set.of(),
            state.locatorChain,
            remaining,
            state.speculative);
    }

    /**
     * Best-effort static expansion of a locator's declared return type into a sub-resource route
     * table (subResourceMode semantics, cf. RuntimeLocatorModelBuilder). Returns null if not
     * expandable. NOTE: unlike the runtime path, ModelProcessors are NOT applied here, so
     * synthetic methods (e.g. auto-OPTIONS) are absent from speculative sub-models.
     */
    private List<StaticRoute> expandableRoutes(Class<?> type) {
        if (type == null || type == Object.class || type == Class.class || type.isPrimitive()) {
            return null;
        }

        List<StaticRoute> cachedRoutes = locatorRouteCache.computeIfAbsent(type, key -> {
            try {
                Resource speculativeResource = Resource.from(key, true /* disable validation for speculation */);
                if (speculativeResource == null) {
                    return List.of();
                }
                ResourceModel speculativeModel =
                        new ResourceModel.Builder(singletonList(speculativeResource), true /* subResourceModel */).build();
                return buildRoutes(speculativeModel.getRuntimeResourceModel(), true);
            } catch (RuntimeException probeFailure) {
                return List.of();
            }
        });
        if (cachedRoutes.isEmpty()) {
            return null;
        }
        return cachedRoutes;
    }

    private void collectLocators(Resource resource, String prefix, List<LocatorEntry> locatorEntries) {
        if (resource.getResourceLocator() != null) {
            ResourceMethod locator = resource.getResourceLocator();
            Class<?> staticReturnType = locator.getInvocable().getRawRoutingResponseType();
            locatorEntries.add(new LocatorEntry(prefix, locator, staticReturnType));
        }

        for (Resource childResource : resource.getChildResources()) {
            collectLocators(childResource, prefix + normalizeTemplate(childResource.getPath()), locatorEntries);
        }
    }

    // ------------------------------------------------------------------
    // Route table construction -- mirrors RuntimeModelBuilder#buildModel
    // ------------------------------------------------------------------

    private static List<StaticRoute> buildRoutes(RuntimeResourceModel runtimeResourceModel, boolean subResourceMode) {
        List<StaticRoute> routes = new ArrayList<>();

        // RuntimeResourceModel returns resources pre-sorted by RuntimeResource.COMPARATOR.
        for (RuntimeResource runtimeResource : runtimeResourceModel.getRuntimeResources()) {

            // (a) own resource methods -> closed pattern, HTTP designators attached
            if (!runtimeResource.getResourceMethods().isEmpty()) {
                PathPattern methodPattern;
                if (subResourceMode) {
                    methodPattern = END_OF_PATH_PATTERN;
                } else {
                    methodPattern = PathPattern.asClosed(runtimeResource.getPathPattern());
                }
                routes.add(StaticRoute.methods(methodPattern, runtimeResource, runtimeResource.getResourceMethods()));
            }

            // (b) child resources (already sorted) and the resource's own locator,
            //     nested under an open route
            List<StaticRoute> innerRoutes = new ArrayList<>();
            for (RuntimeResource childRuntimeResource : runtimeResource.getChildRuntimeResources()) {
                if (!childRuntimeResource.getResourceMethods().isEmpty()) {
                    innerRoutes.add(StaticRoute.methods(
                            PathPattern.asClosed(childRuntimeResource.getPathPattern()),
                            childRuntimeResource,
                            childRuntimeResource.getResourceMethods()));
                }
                if (childRuntimeResource.getResourceLocator() != null) {
                    innerRoutes.add(StaticRoute.locator(
                            childRuntimeResource.getPathPattern(),
                            childRuntimeResource,
                            childRuntimeResource.getResourceLocator()));
                }
            }

            if (runtimeResource.getResourceLocator() != null) {
                innerRoutes.add(StaticRoute.locator(
                        OPEN_ROOT_PATH_PATTERN,
                        runtimeResource,
                        runtimeResource.getResourceLocator()));
            }

            if (!innerRoutes.isEmpty()) {
                PathPattern hierarchicalPattern;
                if (subResourceMode) {
                    hierarchicalPattern = OPEN_ROOT_PATH_PATTERN;
                } else {
                    hierarchicalPattern = runtimeResource.getPathPattern();
                }
                routes.add(StaticRoute.hierarchical(hierarchicalPattern, runtimeResource, innerRoutes));
            }
        }

        return List.copyOf(routes);
    }

    private static ResourceMethod selectMethod(List<ResourceMethod> methods, String httpMethod) {
        ResourceMethod getMethod = null;
        for (ResourceMethod method : methods) {
            if (httpMethod.equals(method.getHttpMethod())) {
                return method;
            }
            if ("GET".equals(method.getHttpMethod())) {
                getMethod = method;
            }
        }

        // AbstractMethodSelectingRouter serves HEAD via GET when no explicit HEAD exists.
        if ("HEAD".equals(httpMethod)) {
            return getMethod;
        }

        return null;
    }

    /** Mirrors PathMatchingRouter#designatorMatch (exact designator, or HEAD served by GET). */
    private static boolean designatorMatch(StaticRoute route, String httpMethod) {
        if (route.httpMethods.contains(httpMethod)) {
            return true;
        }
        return "HEAD".equals(httpMethod) && route.httpMethods.contains("GET");
    }

    /** Mirrors UriRoutingContext#getFinalMatchingGroup. */
    private static String remainingPath(MatchResult matchResult) {
        String tail = matchResult.group(matchResult.groupCount());
        if (tail == null) {
            return "";
        }
        return tail;
    }

    private static String normalizeTemplate(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (path.startsWith("/")) {
            return path;
        }
        return "/" + path;
    }

    // ------------------------------------------------------------------
    // Static mirror of org.glassfish.jersey.server.internal.routing.Route
    // ------------------------------------------------------------------

    private static final class StaticRoute {

        final PathPattern pattern;
        final Set<String> httpMethods;        // null => locator-like route (Route#getHttpMethods() == null)
        final RuntimeResource resource;
        final List<ResourceMethod> methods;   // terminal method route
        final ResourceMethod locator;         // terminal locator route
        final List<StaticRoute> inner;        // hierarchical (open) route

        private StaticRoute(PathPattern pattern, Set<String> httpMethods, RuntimeResource resource,
                List<ResourceMethod> methods, ResourceMethod locator, List<StaticRoute> inner) {
            this.pattern = pattern;
            this.httpMethods = httpMethods;
            this.resource = resource;
            this.methods = methods;
            this.locator = locator;
            this.inner = inner;
        }

        static StaticRoute methods(PathPattern pattern, RuntimeResource resource, List<ResourceMethod> methods) {
            Set<String> designators = new LinkedHashSet<>();
            for (ResourceMethod method : methods) {
                designators.add(method.getHttpMethod());
            }
            return new StaticRoute(pattern, Collections.unmodifiableSet(designators), resource, List.copyOf(methods), null, null);
        }

        static StaticRoute locator(PathPattern pattern, RuntimeResource resource, ResourceMethod locator) {
            return new StaticRoute(pattern, null, resource, null, locator, null);
        }

        static StaticRoute hierarchical(PathPattern pattern, RuntimeResource resource, List<StaticRoute> inner) {
            return new StaticRoute(pattern, null, resource, null, null, List.copyOf(inner));
        }
    }

    /** Mutable per-selection accumulator (one select() call, single thread). */
    private static final class State {

        final String httpMethod;
        final List<UriTemplate> templates = new ArrayList<>();
        final List<ResourceMethod> locatorChain = new ArrayList<>();
        boolean speculative;

        State(String httpMethod) {
            this.httpMethod = httpMethod;
        }
    }
}