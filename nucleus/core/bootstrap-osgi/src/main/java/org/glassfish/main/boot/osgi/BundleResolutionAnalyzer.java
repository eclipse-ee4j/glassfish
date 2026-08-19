/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.main.boot.osgi;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;

import static org.osgi.framework.namespace.PackageNamespace.RESOLUTION_DYNAMIC;
import static org.osgi.resource.Namespace.EFFECTIVE_RESOLVE;
import static org.osgi.resource.Namespace.REQUIREMENT_EFFECTIVE_DIRECTIVE;
import static org.osgi.resource.Namespace.REQUIREMENT_FILTER_DIRECTIVE;
import static org.osgi.resource.Namespace.REQUIREMENT_RESOLUTION_DIRECTIVE;
import static org.osgi.resource.Namespace.RESOLUTION_MANDATORY;
import static org.osgi.resource.Namespace.RESOLUTION_OPTIONAL;

/**
 * Explains why a bundle cannot be resolved by asking the OSGi resolver model instead of reading the prose of a {@link BundleException}.
 * <p>
 * One missing package cascades: in a GlassFish installation a single absent export leaves a hundred bundles unresolved, and every one of
 * them is unresolved for the same reason. This class therefore reports the shortest path from the bundle that failed to the requirement
 * that nothing can satisfy, and stops there. It does not describe the cascade, because the cascade is not the answer.
 * <p>
 * A requirement is only a root cause when it is {@link Reason#NOT_DECLARED} - nothing in the framework offers the capability - or
 * {@link Reason#DECLARATIONS_REJECTED} - the capability is offered but no declaration satisfies the filter, which is nearly always a
 * version range. A requirement whose provider merely happens to be unresolved as well is a step along the path, never the destination.
 * <p>
 * Two things this cannot see:
 * <ul>
 * <li>{@link BundleRequirement#matches(BundleCapability)} compares attributes against the filter and ignores {@code uses} constraints. A
 * bundle whose every requirement is satisfiable and which still does not resolve is reported as
 * {@link Reason#NO_UNSATISFIED_REQUIREMENT}, which in practice means a class space conflict.</li>
 * <li>Declared capabilities and requirements are used rather than the wiring, because an unresolved bundle has no wiring. Fragments
 * attached at resolve time therefore contribute nothing here.</li>
 * </ul>
 */
public final class BundleResolutionAnalyzer {

    private static final int MAX_ROOT_CAUSES = 5;
    private static final int MAX_CANDIDATES_SHOWN = 5;
    private static final int MAX_BUNDLES_PER_CAUSE = 5;
    private static final int MAX_CAUSES_SHOWN = 10;
    private static final int MAX_NEAREST_SHOWN = 3;

    private static final char SEGMENT_SEPARATOR = '.';
    private static final char PATH_SEPARATOR = '/';

    /** How many jars are worth opening for one missing package, and how many hits are worth printing. */
    private static final int MAX_SUSPECTS_PROBED = 8;
    private static final int MAX_SUSPECTS_SHOWN = 3;
    private static final int MIN_SHARED_SEGMENTS = 2;
    private static final int INDENT_WIDTH = 4;

    private BundleResolutionAnalyzer() {
    }

    /**
     * Why a requirement cannot be met.
     */
    public enum Reason {

        /** No bundle in the framework declares a capability under the name the requirement asks for. */
        NOT_DECLARED,

        /** The name is declared, but no declaration satisfies the filter. Nearly always a version range that does not overlap. */
        DECLARATIONS_REJECTED,

        /** A declaration satisfies the filter, but every bundle providing it is itself unresolved. A step, never a root cause. */
        WAITING_ON_PROVIDER,

        /** Every mandatory requirement can be satisfied, yet the bundle is not resolved. Look for a uses constraint conflict. */
        NO_UNSATISFIED_REQUIREMENT
    }

    /**
     * How a declared name relates to the one a requirement asked for and could not find.
     */
    public enum Relation {

        /** The declared name is a parent of the wanted one, so the requirement is for a subpackage nobody exports. */
        ANCESTOR,

        /** The declared name sits below the wanted one. */
        DESCENDANT,

        /** The declared name shares a parent with the wanted one. */
        SIBLING,

        /** Nothing is related by name, and the namespace is small enough to simply list what it does hold. */
        SAME_NAMESPACE
    }

    /**
     * A name that is declared, offered when the one asked for is not. The point is to distinguish a requirement on something that never
     * existed from a requirement on an unexported corner of something that does.
     */
    public record NearestName(String name, Relation relation, int distance, List<Candidate> declaredBy) {

        @Override
        public String toString() {
            return name + " (" + relation + ")";
        }
    }

    /**
     * A capability that was considered for a requirement, with the bundle declaring it.
     */
    public record Candidate(Bundle bundle, BundleCapability capability) {

        @Override
        public String toString() {
            return describe(bundle) + " declares " + attributesOf(capability);
        }
    }

    /**
     * One hop along the path to a root cause: a bundle and the requirement of it that could not be met.
     */
    public record Step(Bundle bundle, BundleRequirement requirement) {

        @Override
        public String toString() {
            if (requirement == null) {
                return describe(bundle);
            }

            return describe(bundle) + " requires " + describe(requirement);
        }
    }

    /**
     * A requirement nothing can satisfy, and the shortest path from the bundle that was asked about to the bundle declaring it. The last
     * step of the path is the failing one; the steps before it are the bundles that are only waiting on it.
     */
    public record RootCause(List<Step> path, Reason reason, List<Candidate> candidates, List<NearestName> nearest,
            List<Bundle> privateIn) {

        public Bundle bundle() {
            return path.get(path.size() - 1).bundle();
        }

        public BundleRequirement requirement() {
            return path.get(path.size() - 1).requirement();
        }

        /**
         * One line, so that printing a list of these stays readable. {@link #explain(BundleContext, Bundle)} is the rendering meant for a
         * person to read.
         */
        @Override
        public String toString() {
            StringBuilder text = new StringBuilder(160);

            for (Step step : path) {
                if (!text.isEmpty()) {
                    text.append(" -> ");
                }
                text.append(step.bundle().getSymbolicName()).append(" [").append(step.bundle().getBundleId()).append(']');
            }

            if (requirement() != null) {
                text.append(" requires ").append(describe(requirement()));
            }

            return text.append(" - ").append(summaryOf(reason)).toString();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Explains a single bundle, which is what belongs in the message of a failure to start it.
     *
     * @param bundleContext Any valid context, used only to enumerate the installed bundles.
     * @param bundle The bundle that would not resolve.
     * @return A short report, a handful of lines, naming the requirement that nothing can satisfy.
     */
    public static String explain(BundleContext bundleContext, Bundle bundle) {
        if (isResolved(bundle)) {
            return describe(bundle) + " is resolved.\n";
        }

        List<RootCause> rootCauses = findRootCauses(bundleContext, bundle);
        if (rootCauses.isEmpty()) {
            return describe(bundle) + " declares no mandatory requirement that cannot be met.\n";
        }

        StringBuilder reportBuilder = new StringBuilder(1024);
        reportBuilder.append(describe(bundle)).append(" cannot resolve.\n");

        for (RootCause rootCause : rootCauses) {
            reportBuilder.append('\n');
            append(rootCause, 1, reportBuilder);
        }

        return reportBuilder.toString();
    }

    /**
     * Answers whether a bundle's jar holds a package without exporting it - a private package, in bnd's terms. That is the difference
     * between a dependency on something that was never shipped and a dependency on an unexported corner of something that was, and the
     * two have completely different fixes.
     *
     * Only the bundle's own jar is searched. A package embedded in a nested jar on the Bundle-ClassPath is not found, so a false answer
     * means "not seen", never "definitely absent".
     *
     * @param bundle The bundle to look inside.
     * @param packageName The package to look for, in dotted form.
     * @return True when the jar holds at least one entry directly in that package. False on anything unreadable, because a guess here
     *             would be worse than silence.
     */
    public static boolean containsPackage(Bundle bundle, String packageName) {
        String path = packageName.replace(SEGMENT_SEPARATOR, PATH_SEPARATOR);

        try {
            // getEntryPaths does not attempt to resolve the bundle the way findEntries does. We are reporting a resolution failure
            // already, so provoking another one on the side would be careless.
            Enumeration<String> entries = bundle.getEntryPaths(path);
            if (entries != null && entries.hasMoreElements()) {
                return true;
            }

            // getEntryPaths works off the file entries, so it finds the package even in a jar that carries no directory entries. The
            // directory lookup only adds the case of a package directory that exists but holds nothing.
            return bundle.getEntry(path + PATH_SEPARATOR) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Finds the requirements that nothing in the framework can satisfy, reachable from a bundle, nearest first.
     * <p>
     * The search is breadth first over the bundles that a failing requirement leads to, and it stops descending as soon as a bundle
     * explains itself, so the path returned is the shortest one. Every bundle is examined at most once.
     *
     * @param bundleContext Any valid context, used only to enumerate the installed bundles.
     * @param bundle The bundle to start from.
     * @return The root causes, nearest first, never more than a handful. Empty when the bundle has no current revision.
     */
    public static List<RootCause> findRootCauses(BundleContext bundleContext, Bundle bundle) {
        return findRootCauses(bundle, CapabilityIndex.of(bundleContext));
    }

    /**
     * Summarises the whole framework: the distinct requirements that nothing can satisfy, the bundles blocked by each of them, and a count
     * of the bundles that are only waiting on those. This replaces describing every unresolved bundle, which says the same thing once per
     * bundle.
     *
     * @param bundleContext Any valid context, used only to enumerate the installed bundles.
     * @return A report of at most a few dozen lines however large the cascade is.
     */
    public static String explainUnresolvedBundles(BundleContext bundleContext) {
        CapabilityIndex index = CapabilityIndex.of(bundleContext);

        List<Bundle> unresolved = new ArrayList<>();
        for (Bundle bundle : bundleContext.getBundles()) {
            if (!isResolved(bundle) && bundle.getState() != Bundle.UNINSTALLED) {
                unresolved.add(bundle);
            }
        }

        if (unresolved.isEmpty()) {
            return "All installed bundles are resolved.\n";
        }

        // Bundles that fail on their own are the causes. The rest are either waiting on one of those, or have nothing wrong with them.
        Map<String, List<RootCause>> causes = new LinkedHashMap<>();
        int waiting = 0;
        int satisfiable = 0;

        for (Bundle bundle : unresolved) {
            List<Verdict> verdicts = verdicts(bundle, index);
            if (verdicts.isEmpty()) {
                satisfiable++;
                continue;
            }

            List<Verdict> terminal = verdicts.stream().filter(verdict -> verdict.reason() != Reason.WAITING_ON_PROVIDER).toList();
            if (terminal.isEmpty()) {
                waiting++;
                continue;
            }

            for (Verdict verdict : terminal) {
                RootCause rootCause = new RootCause(List.of(new Step(bundle, verdict.requirement())), verdict.reason(),
                    verdict.candidates(), verdict.nearest(), verdict.privateIn());
                causes.computeIfAbsent(headline(rootCause), headline -> new ArrayList<>()).add(rootCause);
            }
        }

        return summarise(bundleContext, unresolved.size(), causes, waiting, satisfiable);
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Search
    // -----------------------------------------------------------------------------------------------------------------------------------

    /** A requirement of one bundle that could not be met, and the capabilities that were considered for it. */
    private record Verdict(BundleRequirement requirement, Reason reason, List<Candidate> candidates, List<NearestName> nearest,
            List<Bundle> privateIn) {
    }

    /** A bundle still to examine, with the path that led to it. */
    private record Frontier(List<Step> path, Bundle bundle) {
    }

    private static List<RootCause> findRootCauses(Bundle bundle, CapabilityIndex index) {
        List<RootCause> rootCauses = new ArrayList<>();

        Set<Long> visited = new LinkedHashSet<>();
        visited.add(bundle.getBundleId());

        Deque<Frontier> frontier = new ArrayDeque<>();
        frontier.add(new Frontier(List.of(), bundle));

        while (!frontier.isEmpty() && rootCauses.size() < MAX_ROOT_CAUSES) {
            Frontier current = frontier.removeFirst();

            if (current.bundle().adapt(BundleRevision.class) == null) {
                continue;
            }

            List<Verdict> verdicts = verdicts(current.bundle(), index);
            if (verdicts.isEmpty()) {
                rootCauses.add(new RootCause(extend(current, null), Reason.NO_UNSATISFIED_REQUIREMENT, List.of(), List.of(), List.of()));
                continue;
            }

            List<Verdict> terminal = verdicts.stream().filter(verdict -> verdict.reason() != Reason.WAITING_ON_PROVIDER).toList();
            if (!terminal.isEmpty()) {
                // This bundle explains itself, so there is nothing to gain by walking past it.
                for (Verdict verdict : terminal) {
                    rootCauses.add(new RootCause(extend(current, verdict.requirement()), verdict.reason(), verdict.candidates(),
                        verdict.nearest(), verdict.privateIn()));
                }
                continue;
            }

            for (Verdict verdict : verdicts) {
                for (Candidate candidate : verdict.candidates()) {
                    if (visited.add(candidate.bundle().getBundleId())) {
                        frontier.addLast(new Frontier(extend(current, verdict.requirement()), candidate.bundle()));
                    }
                }
            }
        }

        return rootCauses;
    }

    private static List<Step> extend(Frontier frontier, BundleRequirement requirement) {
        List<Step> path = new ArrayList<>(frontier.path());
        path.add(new Step(frontier.bundle(), requirement));

        return List.copyOf(path);
    }

    private static List<Verdict> verdicts(Bundle bundle, CapabilityIndex index) {
        BundleRevision revision = bundle.adapt(BundleRevision.class);
        if (revision == null) {
            return List.of();
        }

        List<Verdict> verdicts = new ArrayList<>();
        for (BundleRequirement requirement : revision.getDeclaredRequirements(null)) {
            if (!isMandatory(requirement)) {
                continue;
            }

            Verdict verdict = judge(bundle, requirement, index);
            if (verdict != null) {
                verdicts.add(verdict);
            }
        }

        return verdicts;
    }

    /**
     * @return Null when the requirement can be met, otherwise why it cannot.
     */
    private static Verdict judge(Bundle bundle, BundleRequirement requirement, CapabilityIndex index) {
        CapabilityIndex.Candidates candidates = index.candidatesFor(requirement);

        List<Candidate> matching = new ArrayList<>();
        List<Candidate> rejected = new ArrayList<>();

        for (BundleCapability capability : candidates.capabilities()) {
            Bundle provider = capability.getRevision().getBundle();

            if (requirement.matches(capability)) {
                // A bundle that exports what it imports - the substitutable export bnd generates for every export - satisfies itself.
                if (isResolved(provider) || provider.getBundleId() == bundle.getBundleId()) {
                    return null;
                }
                matching.add(new Candidate(provider, capability));
            } else if (candidates.sameName()) {
                rejected.add(new Candidate(provider, capability));
            }
        }

        if (!matching.isEmpty()) {
            return new Verdict(requirement, Reason.WAITING_ON_PROVIDER, matching, List.of(), List.of());
        }

        if (!rejected.isEmpty()) {
            return new Verdict(requirement, Reason.DECLARATIONS_REJECTED, rejected, List.of(), List.of());
        }

        List<NearestName> nearest = index.nearestTo(requirement);

        return new Verdict(requirement, Reason.NOT_DECLARED, List.of(), nearest, index.findPrivateHolders(requirement, nearest));
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Capability index
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Every declared capability in the framework, indexed by namespace and by the name it carries, so that a requirement is only ever
     * compared against the handful of capabilities that could conceivably match it.
     * <p>
     * Declared rather than wired, so that unresolved bundles - the whole point of this class - still contribute.
     */
    private static final class CapabilityIndex {

        record Candidates(List<BundleCapability> capabilities, boolean sameName) {
        }

        private final Map<String, Map<String, List<BundleCapability>>> byName = new LinkedHashMap<>();
        private final Map<String, List<BundleCapability>> byNamespace = new LinkedHashMap<>();
        private final List<Bundle> bundles = new ArrayList<>();

        static CapabilityIndex of(BundleContext bundleContext) {
            CapabilityIndex index = new CapabilityIndex();

            for (Bundle bundle : bundleContext.getBundles()) {
                index.bundles.add(bundle);

                BundleRevision revision = bundle.adapt(BundleRevision.class);
                if (revision == null) {
                    continue;
                }

                for (BundleCapability capability : revision.getDeclaredCapabilities(null)) {
                    index.add(capability);
                }
            }

            return index;
        }

        private void add(BundleCapability capability) {
            String namespace = capability.getNamespace();
            byNamespace.computeIfAbsent(namespace, key -> new ArrayList<>()).add(capability);

            Object name = capability.getAttributes().get(namespace);
            if (name != null) {
                byName.computeIfAbsent(namespace, key -> new LinkedHashMap<>())
                      .computeIfAbsent(name.toString(), key -> new ArrayList<>())
                      .add(capability);
            }
        }

        /**
         * @return The capabilities worth comparing against the requirement, and whether they are narrowed to the name it asks for. When
         *             they are not, a capability that fails to match says nothing about how close it came.
         */
        Candidates candidatesFor(BundleRequirement requirement) {
            String name = requiredName(requirement);
            if (name == null) {
                return new Candidates(byNamespace.getOrDefault(requirement.getNamespace(), List.of()), false);
            }

            Map<String, List<BundleCapability>> namespace = byName.getOrDefault(requirement.getNamespace(), Map.of());

            return new Candidates(namespace.getOrDefault(name, List.of()), true);
        }

        /**
         * Opens the jars most likely to hold the missing package and reports the ones that actually do.
         *
         * @param requirement The requirement whose name is declared nowhere.
         * @param nearest What the name search turned up, whose declaring bundles are the first worth opening.
         * @return The bundles holding the package without exporting it, at most a few.
         */
        List<Bundle> findPrivateHolders(BundleRequirement requirement, List<NearestName> nearest) {
            String name = requiredName(requirement);
            if (name == null) {
                return List.of();
            }

            List<Bundle> holders = new ArrayList<>();
            for (Bundle suspect : suspects(name, nearest)) {
                if (holders.size() == MAX_SUSPECTS_SHOWN) {
                    break;
                }

                if (containsPackage(suspect, name)) {
                    holders.add(suspect);
                }
            }

            return List.copyOf(holders);
        }

        /**
         * Whoever declares the nearest name is the obvious jar to open. After that, any bundle whose symbolic name shares a long prefix
         * with the package, which is the only lead left when a whole subtree is exported nowhere.
         */
        private List<Bundle> suspects(String name, List<NearestName> nearest) {
            Map<Long, Bundle> suspects = new LinkedHashMap<>();

            for (NearestName nearestName : nearest) {
                for (Candidate candidate : nearestName.declaredBy()) {
                    suspects.putIfAbsent(candidate.bundle().getBundleId(), candidate.bundle());
                }
            }

            List<Bundle> byPrefix = new ArrayList<>();
            for (Bundle bundle : bundles) {
                if (bundle.getSymbolicName() != null && sharedSegments(bundle.getSymbolicName(), name) >= MIN_SHARED_SEGMENTS) {
                    byPrefix.add(bundle);
                }
            }
            byPrefix.sort(Comparator.comparingInt((Bundle bundle) -> sharedSegments(bundle.getSymbolicName(), name)).reversed());

            for (Bundle bundle : byPrefix) {
                if (suspects.size() == MAX_SUSPECTS_PROBED) {
                    break;
                }

                suspects.putIfAbsent(bundle.getBundleId(), bundle);
            }

            return new ArrayList<>(suspects.values());
        }

        private static int sharedSegments(String left, String right) {
            String[] leftSegments = left.split("\\.");
            String[] rightSegments = right.split("\\.");

            int shared = 0;
            while (shared < leftSegments.length && shared < rightSegments.length && leftSegments[shared].equals(rightSegments[shared])) {
                shared++;
            }

            return shared;
        }

        /**
         * Looks for a declared name close to the one a requirement asked for and did not find. A declared ancestor is the strongest
         * signal there is: it means the bundle imports a corner of something that is exported, which is what an import generated for an
         * unexported internal package looks like.
         *
         * @param requirement The requirement whose name is declared nowhere.
         * @return At most a few names, all related the same way, nearest relation first. Empty when nothing is close.
         */
        List<NearestName> nearestTo(BundleRequirement requirement) {
            String name = requiredName(requirement);
            Map<String, List<BundleCapability>> declared = byName.getOrDefault(requirement.getNamespace(), Map.of());

            if (name == null || declared.isEmpty()) {
                return List.of();
            }

            for (int dot = name.lastIndexOf(SEGMENT_SEPARATOR); dot > 0; dot = name.lastIndexOf(SEGMENT_SEPARATOR, dot - 1)) {
                String ancestor = name.substring(0, dot);
                if (declared.containsKey(ancestor)) {
                    return List.of(toNearestName(ancestor, Relation.ANCESTOR, segments(name) - segments(ancestor),
                        declared.get(ancestor)));
                }
            }

            String below = name + SEGMENT_SEPARATOR;
            List<NearestName> descendants = collect(declared, Relation.DESCENDANT, name, candidate -> candidate.startsWith(below));
            if (!descendants.isEmpty()) {
                return descendants;
            }

            int lastDot = name.lastIndexOf(SEGMENT_SEPARATOR);
            if (lastDot > 0) {
                String parent = name.substring(0, lastDot + 1);
                List<NearestName> siblings = collect(declared, Relation.SIBLING, name,
                    candidate -> candidate.startsWith(parent) && candidate.indexOf(SEGMENT_SEPARATOR, parent.length()) < 0);
                if (!siblings.isEmpty()) {
                    return siblings;
                }
            }

            // Namespaces like osgi.ee hold a handful of names with no hierarchy at all, so listing them is the useful answer.
            if (declared.size() <= MAX_NEAREST_SHOWN) {
                return collect(declared, Relation.SAME_NAMESPACE, name, candidate -> true);
            }

            return List.of();
        }

        private static int segments(String name) {
            int count = 1;
            for (int index = name.indexOf(SEGMENT_SEPARATOR); index >= 0; index = name.indexOf(SEGMENT_SEPARATOR, index + 1)) {
                count++;
            }

            return count;
        }

        private static List<NearestName> collect(Map<String, List<BundleCapability>> declared, Relation relation, String name,
                Predicate<String> matches) {

            List<NearestName> nearest = new ArrayList<>();
            for (Map.Entry<String, List<BundleCapability>> entry : declared.entrySet()) {
                if (nearest.size() == MAX_NEAREST_SHOWN) {
                    return nearest;
                }

                if (matches.test(entry.getKey())) {
                    nearest.add(toNearestName(entry.getKey(), relation, Math.abs(segments(entry.getKey()) - segments(name)),
                        entry.getValue()));
                }
            }

            return nearest;
        }

        private static NearestName toNearestName(String name, Relation relation, int distance, List<BundleCapability> capabilities) {
            List<Candidate> declaredBy = new ArrayList<>();
            for (BundleCapability capability : capabilities) {
                if (declaredBy.size() == MAX_NEAREST_SHOWN) {
                    break;
                }
                declaredBy.add(new Candidate(capability.getRevision().getBundle(), capability));
            }

            return new NearestName(name, relation, distance, List.copyOf(declaredBy));
        }

        /**
         * Reads the name a requirement asks for out of the equality assertion in its filter directive.
         * <p>
         * The assertion is matched whole, parentheses included. Matching it loosely is what made an export of org.jboss.weld.annotated look
         * like a near miss for a requirement on org.jboss.weld.annotated.enhanced, and with a prefix of the wanted name almost always
         * present in a package hierarchy, that hid every genuinely absent capability behind a bundle that had nothing to do with it.
         *
         * @return The name, or null when the filter does not assert exactly one, in which case the caller falls back to the whole
         *             namespace. A disjunction over several names lands here.
         */
        private static String requiredName(BundleRequirement requirement) {
            String filter = requirement.getDirectives().get(REQUIREMENT_FILTER_DIRECTIVE);
            if (filter == null) {
                return null;
            }

            String assertion = "(" + requirement.getNamespace() + "=";

            int start = filter.indexOf(assertion);
            if (start < 0 || filter.indexOf(assertion, start + 1) >= 0) {
                return null;
            }

            int end = filter.indexOf(')', start + assertion.length());
            if (end < 0) {
                return null;
            }

            return filter.substring(start + assertion.length(), end);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------------------------------------------------------------------

    private static void append(RootCause rootCause, int indent, StringBuilder reportBuilder) {
        List<Step> path = rootCause.path();

        for (int index = 0; index < path.size(); index++) {
            Step step = path.get(index);
            String arrow = index == 0 ? "" : "-> ";

            printLine(reportBuilder, indent, arrow + describe(step.bundle()));
            if (step.requirement() != null) {
                printLine(reportBuilder, indent + 1, "requires " + describe(step.requirement()));
            }
        }

        printLine(reportBuilder, indent + 1, describe(rootCause.reason()));
        appendCandidates(rootCause, indent + 2, reportBuilder);
        appendPresence(rootCause, indent + 1, reportBuilder);
    }

    /**
     * Finding the package inside a jar answers the question outright, so the nearest name is not worth printing alongside it.
     */
    private static void appendPresence(RootCause rootCause, int indent, StringBuilder reportBuilder) {
        if (rootCause.privateIn().isEmpty()) {
            appendNearest(rootCause, indent, reportBuilder);
            return;
        }

        printLine(reportBuilder, indent, "but it is present, unexported, in:");
        for (Bundle bundle : rootCause.privateIn()) {
            printLine(reportBuilder, indent + 1, describe(bundle));
        }
    }

    private static void appendNearest(RootCause rootCause, int indent, StringBuilder reportBuilder) {
        for (NearestName nearest : rootCause.nearest()) {
            printLine(reportBuilder, indent, describe(nearest) + " " + nearest.name());

            for (Candidate candidate : nearest.declaredBy()) {
                printLine(reportBuilder, indent + 1, "declared by " + describe(candidate.bundle()));
            }
        }
    }

    private static void appendCandidates(RootCause rootCause, int indent, StringBuilder reportBuilder) {
        int shown = 0;
        for (Candidate candidate : rootCause.candidates()) {
            if (shown == MAX_CANDIDATES_SHOWN) {
                printLine(reportBuilder, indent, (rootCause.candidates().size() - shown) + " further declarations not shown");
                return;
            }

            printLine(reportBuilder, indent, candidate.toString());
            shown++;
        }
    }

    private static String summarise(BundleContext bundleContext, int unresolved, Map<String, List<RootCause>> causes, int waiting,
            int satisfiable) {
        StringBuilder reportBuilder = new StringBuilder(2048);
        reportBuilder.append(unresolved).append(" of ").append(bundleContext.getBundles().length)
                     .append(" installed bundles are unresolved.\n");

        if (causes.isEmpty()) {
            reportBuilder.append("\nNo bundle carries a requirement that nothing can satisfy, "
                + "which points at a uses constraint rather than a missing capability.\n");

            return reportBuilder.toString();
        }

        reportBuilder.append('\n').append(causes.size()).append(causes.size() == 1 ? " root cause:\n" : " distinct root causes:\n");

        int causesShown = 0;
        for (Map.Entry<String, List<RootCause>> cause : causes.entrySet()) {
            if (causesShown == MAX_CAUSES_SHOWN) {
                reportBuilder.append('\n').append(causes.size() - causesShown).append(" further root causes not shown.\n");
                break;
            }
            causesShown++;

            List<RootCause> blocked = cause.getValue();

            reportBuilder.append('\n');
            printLine(reportBuilder, 1, cause.getKey());
            appendCandidates(blocked.get(0), 2, reportBuilder);
            appendPresence(blocked.get(0), 2, reportBuilder);

            int shown = 0;
            for (RootCause rootCause : blocked) {
                if (shown == MAX_BUNDLES_PER_CAUSE) {
                    printLine(reportBuilder, 2, "and " + (blocked.size() - shown) + " further bundles");
                    break;
                }

                printLine(reportBuilder, 2, "blocks " + describe(rootCause.bundle()));
                shown++;
            }
        }

        if (waiting > 0) {
            reportBuilder.append('\n').append(waiting)
                         .append(waiting == 1 ? " further bundle is unresolved only because it needs one of the bundles above.\n"
                             : " further bundles are unresolved only because they need one of the bundles above.\n");
        }

        if (satisfiable > 0) {
            reportBuilder.append('\n').append(satisfiable)
                         .append(satisfiable == 1 ? " further bundle has every mandatory requirement satisfiable, so it is"
                             : " further bundles have every mandatory requirement satisfiable, so they are")
                         .append(" unresolved only because nothing has needed them yet, or because of a uses constraint.\n");
        }

        return reportBuilder.toString();
    }

    private static String headline(RootCause rootCause) {
        return describe(rootCause.requirement()) + " - " + describe(rootCause.reason());
    }

    private static String describe(Bundle bundle) {
        return bundle.getSymbolicName() + " " + bundle.getVersion() + " [" + bundle.getBundleId() + "] " + toStateName(bundle);
    }

    /**
     * Describes a requirement by the filter directive the framework stored, rendered the same way {@link FelixPrettyPrinter} renders the
     * one in the resolver message, so that the two reports agree rather than each inventing its own notation.
     */
    private static String describe(BundleRequirement requirement) {
        String filter = requirement.getDirectives().get(REQUIREMENT_FILTER_DIRECTIVE);
        if (filter == null) {
            return requirement.getNamespace() + " " + requirement.getAttributes();
        }

        return FelixPrettyPrinter.formatFilter(filter);
    }

    /** The compact wording, for a reason that has to share a line with everything else. */
    private static String summaryOf(Reason reason) {
        return switch (reason) {
            case NOT_DECLARED -> "not declared by any bundle";
            case DECLARATIONS_REJECTED -> "declared, but no declaration satisfies it";
            case WAITING_ON_PROVIDER -> "every bundle providing it is unresolved";
            case NO_UNSATISFIED_REQUIREMENT -> "every requirement satisfiable, look for a uses constraint";
        };
    }

    /**
     * How far away the declared name is decides how much it is worth. A parent one level up says the requirement is for an unexported
     * corner of a bundle that is right there; an ancestor three levels up says little more than that the tree exists.
     */
    private static String describe(NearestName nearest) {
        return switch (nearest.relation()) {
            case ANCESTOR -> nearest.distance() == 1 ? "but its parent is declared:"
                : "but an ancestor " + nearest.distance() + " levels up is declared:";
            case DESCENDANT -> nearest.distance() == 1 ? "but a name directly below it is declared:"
                : "but a name " + nearest.distance() + " levels below is declared:";
            case SIBLING -> "but a name alongside it is declared:";
            case SAME_NAMESPACE -> "what this namespace does hold:";
        };
    }

    private static String describe(Reason reason) {
        return switch (reason) {
            case NOT_DECLARED -> "no bundle in the framework declares that";
            case DECLARATIONS_REJECTED -> "declared, but no declaration satisfies the requirement:";
            case WAITING_ON_PROVIDER -> "a declaration satisfies it, but every bundle providing it is unresolved:";
            case NO_UNSATISFIED_REQUIREMENT -> "every mandatory requirement can be satisfied - look for a uses constraint conflict";
        };
    }

    /** The attributes of a capability without the one carrying its name, which the requirement line above already showed. */
    private static String attributesOf(BundleCapability capability) {
        Map<String, Object> attributes = new LinkedHashMap<>(capability.getAttributes());
        attributes.remove(capability.getNamespace());

        return attributes.toString();
    }

    private static void printLine(StringBuilder reportBuilder, int indent, String text) {
        reportBuilder.append(" ".repeat(indent * INDENT_WIDTH)).append(text).append('\n');
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Model helpers
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A requirement only blocks resolution when it is effective at resolve time and neither optional nor dynamic. Skipping the rest keeps
     * the report free of the osgi.service requirements that Declarative Services adds, which are never resolved against.
     */
    private static boolean isMandatory(BundleRequirement requirement) {
        Map<String, String> directives = requirement.getDirectives();

        if (!EFFECTIVE_RESOLVE.equals(directives.getOrDefault(REQUIREMENT_EFFECTIVE_DIRECTIVE, EFFECTIVE_RESOLVE))) {
            return false;
        }

        String resolution = directives.getOrDefault(REQUIREMENT_RESOLUTION_DIRECTIVE, RESOLUTION_MANDATORY);

        return !RESOLUTION_OPTIONAL.equals(resolution) && !RESOLUTION_DYNAMIC.equals(resolution);
    }

    private static boolean isResolved(Bundle bundle) {
        return (bundle.getState() & (Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING | Bundle.ACTIVE)) != 0;
    }

    private static String toStateName(Bundle bundle) {
        return switch (bundle.getState()) {
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.ACTIVE -> "ACTIVE";
            default -> "UNKNOWN";
        };
    }
}