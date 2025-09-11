/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.external.statistics.Statistic;
import org.glassfish.external.statistics.Stats;
import org.glassfish.external.statistics.impl.StatisticImpl;
import org.glassfish.flashlight.MonitoringRuntimeDataRegistry;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.util.StringUtils.ok;
import static com.sun.enterprise.util.SystemPropertyConstants.MONDOT;
import static com.sun.enterprise.util.SystemPropertyConstants.SLASH;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.glassfish.api.ActionReport.ExitCode.FAILURE;
import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;

/**
 *
 * @author Byron Nevins First breathed life on November 6, 2010 The copyright says 1997 because one method in here has
 * code moved verbatim from GetCommand.java which started life in 1997
 *
 * Note: what do you suppose is the worst possible name for a TreeNode class? Correct! TreeNode! Clashing names is why
 * we have to explicitly use this ghastly name: org.glassfish.flashlight.datatree.TreeNode all over the place...
 */
@Service(name = "MonitoringReporter")
@PerLookup
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
public class MonitoringReporter extends V2DottedNameSupport {

    private static final Logger LOG = System.getLogger(MonitoringReporter.class.getName());
    /** used for get */
    private final TreeMap<String, Object> nodeTreeToProcess = new TreeMap<>();
    /** used for list */
    private List<org.glassfish.flashlight.datatree.TreeNode> nodeListToProcess = new ArrayList<>();

    private List<Server> targets = new ArrayList<>();
    private ActionReporter reporter;
    private AdminCommandContext context;
    private String pattern;
    private String userarg;
    @Inject
    @Optional
    private MonitoringRuntimeDataRegistry datareg;
    @Inject
    private Domain domain;
    @Inject
    private Target targetService;
    @Inject
    private ServerEnvironment serverEnv;
    @Inject
    private ServiceLocator habitat;
    private OutputType outputType;
    private final static String DOTTED_NAME = ".dotted-name";
    private boolean targetIsMultiInstanceCluster;
    private String targetName;
    private Boolean aggregateDataOnly = Boolean.FALSE;

    public enum OutputType {

        GET, LIST
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\nPattern=[").append(pattern).append("]").append('\n');

        if (!targets.isEmpty()) {
            for (Server server : targets) {
                if (server != null) {
                    sb.append("Server=[").append(server.getName()).append("]").append('\n');
                }
            }
        } else {
            sb.append("No Targets");
        }

        return sb.toString();
    }
    ///////////////////////////////////////////////////////////////////////
    //////////////////////// The API Methods ///////////////////////////
    ///////////////////////////////////////////////////////////////////////

    public void prepareGet(AdminCommandContext c, String arg, Boolean data) {
        LOG.log(DEBUG, "prepareGet(c={0}, arg={1}, data={2})", c, arg, data);
        aggregateDataOnly = data;
        prepare(c, arg, OutputType.GET);
    }

    public Collection<? extends AccessCheck> getAccessChecksForGet() {
        final Collection<AccessCheck> accessChecks = new ArrayList<>();
        for (String key : nodeTreeToProcess.keySet()) {
            final String name = key.replace('.', '/');
            accessChecks.add(new AccessCheck(sanitizeResourceName(name), "read"));
        }
        return accessChecks;
    }

    public Collection<? extends AccessCheck> getAccessChecksForList() {
        final Collection<AccessCheck> accessChecks = new ArrayList<>();
        for (org.glassfish.flashlight.datatree.TreeNode tn1 : nodeListToProcess) {
            /*
             * doList discards nodes that do not have children, but we include them here in building the access checks because the
             * user needs read access to the node in order to find out that it does or does not have children.
             */
            String name = tn1.getCompletePathName().replace('.', '/');
            accessChecks.add(new AccessCheck(sanitizeResourceName(name), "read"));
        }
        return accessChecks;
    }

    private String sanitizeResourceName(final String resourceName) {
        return StringUtils.replace(resourceName, "[", "_ARRAY_");
    }

    public void prepareList(AdminCommandContext c, String arg) {
        prepare(c, arg, OutputType.LIST);
    }

    public void execute() {
        if (hasError()) {
            LOG.log(DEBUG, "Error detected, returning.");
            return;
        }

        runLocally();
        runRemotely();
        if (targetIsMultiInstanceCluster && isInstanceRunning()) {
            runAggregate();
        }

    }

    private boolean isInstanceRunning() {
        int num = 0;
        List<Server> allServers = targetService.getAllInstances();
        for (Server server : allServers) {
            if (server.isListeningOnAdminPort()) {
                num++;
            }
        }
        return num >= 2;
    }

    private void runAggregate() {
        setClusterInfo(reporter.addSubActionsReport(), getOutputLines());
    }

    private List<String> getOutputLines() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            reporter.writeReport(os);
            String outputMessage = os.toString(UTF_8);
            String[] lines = outputMessage.split("\\n");
            return Arrays.asList(lines);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void setClusterInfo(ActionReport aggregateReporter, List<String> list) {
        List<HashMap<String, String>> data = new ArrayList<>(targets.size());
        int i;
        for (i = 0; i < targets.size(); i++) {
            data.add(new HashMap<>());
        }
        HashMap<String, String> clusterInfo = new HashMap<>();
        int instanceCount = 0;
        for (Server server : targets) {
            String instanceName = server.getName();
            Map<String, String> instanceMap = data.get(instanceCount);
            String key = null;
            for (String str : list) {
                if (str.contains(instanceName) && str.contains("-count =")) {
                    ArrayList<String> kv = getKeyValuePair(str, instanceName);
                    key = kv.get(0);
                    instanceMap.put(kv.get(0), kv.get(1));
                }
                if (key != null) {
                    String desc = key.substring(0, key.indexOf("-count")) + "-description";
                    if (str.contains(desc)) {
                        ArrayList<String> kv = getKeyValuePair(str, instanceName);
                        clusterInfo.put(kv.get(0), kv.get(1));
                    }
                    String lastSampleTime = key.substring(0, key.indexOf("-count")) + "-lastsampletime";
                    if (str.contains(lastSampleTime)) {
                        ArrayList<String> kv = getKeyValuePair(str, instanceName);
                        clusterInfo.put(instanceName + "." + kv.get(0), kv.get(1));
                        key = null;
                    }
                }
            }
            instanceCount++;
        }

        List<Server> allServers = targetService.getAllInstances();
        String instanceListStr = "";
        i = 0;
        for (Server server : allServers) {
            if (server.isListeningOnAdminPort()) {
                if (i == 0) {
                    instanceListStr = server.getName();
                } else {
                    instanceListStr = instanceListStr + ", " + server.getName();
                }
                i++;
            }
        }
        aggregateReporter.appendMessage(
                "\nComputed Aggregate Data for " + i + " instances: " + instanceListStr + " in cluster " + targetName + " :\n");
        boolean noData = true;
        HashMap<String, String> h = data.get(0);
        for (String s : h.keySet()) {
            int total = 0, max = 0, min = 0, index = 0;
            float avg = 0;
            int[] values = new int[data.size()];
            boolean nullValue = false;
            for (Map<String, String> hm : data) {
                String tmp = hm.get(s);
                // if tmp is null then the string is not available in all the instances,
                // so not required to add this in the cluster information
                if (tmp == null) {
                    nullValue = true;
                    break;
                }
                int count = Integer.parseInt(tmp);
                values[index++] = count;
                total = total + count;
            }
            if (!nullValue) {
                noData = false;
                Arrays.sort(values);
                min = values[0];
                max = values[values.length - 1];
                avg = (float) total / (float) data.size();
                String descKey = s.substring(0, s.length() - 5) + "description";
                aggregateReporter.appendMessage(targetName + "." + s + "-total = " + total + "\n");
                aggregateReporter.appendMessage(targetName + "." + s + "-avg = " + avg + "\n");
                aggregateReporter.appendMessage(targetName + "." + s + "-max = " + max + "\n");
                aggregateReporter.appendMessage(targetName + "." + s + "-min = " + min + "\n");
                aggregateReporter.appendMessage(targetName + "." + descKey + " = " + clusterInfo.get(descKey) + "\n");
                String lastSampleTimeKey = s.substring(0, s.length() - 5) + "lastsampletime";
                long sampletime = getLastSampleTime(clusterInfo, lastSampleTimeKey, data.size());
                aggregateReporter.appendMessage(targetName + "." + lastSampleTimeKey + " = " + sampletime + "\n");
            }
        }
        if (noData) {
            aggregateReporter.appendMessage("No aggregated cluster data to report\n");
        }
    }

    private ArrayList<String> getKeyValuePair(String str, String instanceName) {
        ArrayList<String> list = new ArrayList<>(2);
        String key = null;
        String value = null;
        if (str != null) {
            key = str.substring(0, str.lastIndexOf('='));
            key = key.substring(instanceName.length() + 1).trim();
            value = str.substring(str.lastIndexOf('=') + 1, str.length()).trim();
        }
        list.add(0, key);
        list.add(1, value);
        return list;
    }

    private long getLastSampleTime(HashMap<String, String> clusterInfo, String lastSampleTimeKey, int numofInstances) {
        long[] values = new long[numofInstances];
        int index = 0;
        for (Entry<String, String> entry : clusterInfo.entrySet()) {
            if (entry.getKey().contains(lastSampleTimeKey)) {
                values[index++] = Long.parseLong(entry.getValue());
            }
        }
        Arrays.sort(values);
        return values[values.length - 1];
    }

    ///////////////////////////////////////////////////////////////////////
    //////////////////////// ALL PRIVATE BELOW ///////////////////////////
    ///////////////////////////////////////////////////////////////////////
    private void prepare(AdminCommandContext c, String arg, OutputType type) {
        outputType = type;
        context = c;
        prepareReporter();
        // DAS runs the show on this command. If we are running in an
        // instance -- that means we should call runLocally() AND it also
        // means that the pattern is already perfect!

        if (isDas()) {
            prepareDas(arg);
        } else {
            prepareInstance(arg);
        }

        prepareNodesToProcess();
    }

    private void prepareReporter() {
        reporter = (ActionReporter) context.getActionReport();
        LOG.log(DEBUG, "reporter: {0}", reporter);
    }

    private void prepareDas(String arg) {
        LOG.log(DEBUG, "prepareDas(arg={0})", arg);
        try {
            setSuccess();
            userarg = arg;

            if (!validate()) {
                LOG.log(WARNING, "Validation failed, returning.");
                return;
            }
        } catch (Exception e) {
            setError(Strings.get("admin.get.monitoring.unknown", e.getMessage()));
            reporter.setFailureCause(e);
        }
    }

    private void prepareInstance(String arg) {
        // TODO throw an exception if any errors!
        pattern = arg;
    }

    // mostly just copied over from old "get" implementation
    // That's why it is excruciatingly unreadable...
    private void prepareNodesToProcess() {

        // don't run if this is DAS **and** DAS is not in the server list.
        // otherwise we are in an instance and definitely want to run!
        if (isDas() && !dasIsInList()) {
            return;
        }

        // say the pattern is "something" -->
        // we want "server.something" for DAS and "i1.server.something" for i1
        // Yes -- this is difficult to get perfect!!! What if user entered
        // "server.something"?

        String localPattern = prependServerDot(pattern);
        org.glassfish.flashlight.datatree.TreeNode tn = datareg.get(serverEnv.getInstanceName());

        if (tn == null) {
            return;
        }

        List<org.glassfish.flashlight.datatree.TreeNode> ltn = tn.getNodes(localPattern);
        boolean singleStat = false;

        if (ltn == null || ltn.isEmpty()) {
            org.glassfish.flashlight.datatree.TreeNode parent = tn.getPossibleParentNode(localPattern);

            if (parent != null) {
                ltn = new ArrayList<>(1);
                ltn.add(parent);
                singleStat = true;
            }
        }

        if (!singleStat) {
            localPattern = null; // signal to method call below. localPattern was already used above...
        }

        if (outputType == OutputType.GET) {
            prepareNodeTreeToProcess(localPattern, ltn);
        } else if (outputType == OutputType.LIST) {
            nodeListToProcess = ltn;
        }
    }

    private void runLocally() {
        // don't run if this is DAS **and** DAS is not in the server list.
        // otherwise we are in an instance and definitely want to run!
        if (isDas() && !dasIsInList()) {
            LOG.log(DEBUG, "DAS selected, but there is nothing about DAS in the list.");
            return;
        }

        if (outputType == OutputType.GET) {
            doGet();
        } else if (outputType == OutputType.LIST) {
            doList();
        }
    }


    private void prepareNodeTreeToProcess(final String localPattern,
        final List<org.glassfish.flashlight.datatree.TreeNode> ltn) {
        for (org.glassfish.flashlight.datatree.TreeNode tn1 : sortTreeNodesByCompletePathName(ltn)) {
            if (!tn1.hasChildNodes()) {
                insertNameValuePairs(nodeTreeToProcess, tn1, localPattern);
            }
        }
    }

    private void doGet() {
        LOG.log(TRACE, "doGet(); to process: {0}", nodeTreeToProcess);
        ActionReport.MessagePart topPart = reporter.getTopMessagePart();
        for (Entry<String, Object> entry : nodeTreeToProcess.entrySet()) {
            String line = entry.getKey().replace(SLASH, "/") + " = " + entry.getValue();
            LOG.log(TRACE, "line={0}", line);
            topPart.addChild().setMessage(line);
        }
        setSuccess();
    }

    /**
     * List means only print things that have children. Don't print the children.
     */
    private void doList() {
        ActionReport.MessagePart topPart = reporter.getTopMessagePart();
        for (org.glassfish.flashlight.datatree.TreeNode tn1 : nodeListToProcess) {
            if (tn1.hasChildNodes()) {
                topPart.addChild().setMessage(tn1.getCompletePathName());
            }
        }
        setSuccess();
    }

    /**
     * This can be a bit confusing. It is sort of like a recursive call. GetCommand will be called on the instance. BUT --
     * the pattern arg will just have the actual pattern -- the target name will NOT be in there! So "runLocally" will be
     * called on the instance. this method will ONLY run on DAS (guaranteed!)
     */
    private void runRemotely() {
        if (!isDas()) {
            LOG.log(DEBUG, "Not a DAS, returning.");
            return;
        }
        List<Server> remoteServers = getRemoteServers();
        if (remoteServers.isEmpty()) {
            LOG.log(DEBUG, "No remote servers, returning.");
            return;
        }
        try {
            ParameterMap paramMap = new ParameterMap();
            paramMap.set("monitor", "true");
            paramMap.set("DEFAULT", pattern);
            ClusterOperationUtil.replicateCommand("get", FailurePolicy.Error, FailurePolicy.Warn, FailurePolicy.Ignore,
                remoteServers, context, paramMap, habitat);
        } catch (Exception ex) {
            setError(Strings.get("admin.get.monitoring.remote.error", getNames(remoteServers)));
        }
    }

    private String prependServerDot(String s) {
        // note -- we are now running in either DAS or an instance and we are going to gather up
        // data ONLY for this server. I.e. the DAS dispatching has already happened.
        // we really need this pattern to start with the instance-name (DAS's instance-name is "server"

        // Issue#15054
        // this is pretty intricate but this is what we want to happen for these samples:
        // asadmin get -m network.thread-pool.totalexecutedtasks-count ==> ERROR no target
        // asadmin get -m server.network.thread-pool.totalexecutedtasks-count ==> OK, return DAS's data
        // asadmin get -m *.network.thread-pool.totalexecutedtasks-count ==> OK return DAS and instances' data
        // asadmin get -m i1.network.thread-pool.totalexecutedtasks-count ==> OK return data for i1

        final String namedot = serverEnv.getInstanceName() + ".";

        if (s.startsWith(namedot)) {
            return s;
        }

        return namedot + s;
    }

    private boolean validate() {
        if (datareg == null) {
            setError(Strings.get("admin.get.no.monitoring"));
            return false;
        }

        if (!initPatternAndTargets()) {
            return false;
        }

        return true;
    }

    /*
     * VERY VERY complicated to get this right!
     */
    private boolean initPatternAndTargets() {
        Server das = domain.getServerNamed("server");

        // no DAS in here!
        List<Server> allServers = targetService.getAllInstances();

        allServers.add(das);

        // 0 decode special things
        // \\ == literal backslash and \ is escaping next char
        userarg = handleEscapes(userarg); // too complicated to do in-line

        // MONDOT, SLASH should be replaced with literals
        userarg = userarg.replace(MONDOT, ".").replace(SLASH, "/");

        // double star makes no sense. The loop gets rid of "***", "****", etc.
        while (userarg.indexOf("**") >= 0) {
            userarg = userarg.replace("**", "*");
        }

        // 1. nothing
        // 2. *
        // 3. *. --> which is a weird input but let's accept it anyway!
        // 4 . --> very weird but we'll take it
        if (!ok(userarg) || userarg.equals("*") || userarg.equals(".") || userarg.equals("*.")) {
            // By definition this means ALL servers and ALL data
            targets = allServers;
            pattern = "*";
            return true;
        }

        // 5. *..
        // 6. *.<something>
        if (userarg.startsWith("*.")) {
            targets = allServers;

            // note: it can NOT be just "*." -- there is something at posn #2 !!
            pattern = userarg.substring(2);

            // "*.." is an error
            if (pattern.startsWith(".")) {
                String specificError = Strings.get("admin.get.monitoring.nodoubledot");
                setError(Strings.get("admin.get.monitoring.invalidpattern", specificError));
                return false;
            }
            return true;
        }

        // 7. See 14685 for an example --> "*jsp*"
        // 16313 for another example
        if (userarg.startsWith("*")) {
            targets = allServers;
            pattern = userarg;
            return true;
        }

        // Another example:
        // servername*something*
        // IT 14778
        // note we will NOT support serv*something getting resolved to server*something
        // that's too crazy. They have to enter a reasonable name

        // we are looking for, e.g. instance1*foo.goo*
        // target is instance1 pattern is *foo.goo*
        // instance1.something is handled below
        String re = "[^\\.]+\\*.*";

        if (userarg.matches(re)) {
            int index = userarg.indexOf("*");

            if (index < 0) { // can't happen!!
                setError(Strings.get("admin.get.monitoring.invalidtarget", userarg));
                return false;
            }
            targetName = userarg.substring(0, index);
            pattern = userarg.substring(index);
        }

        if (targetName == null) {
            int index = userarg.indexOf(".");

            if (index >= 0) {
                targetName = userarg.substring(0, index);

                if (userarg.length() == index + 1) {
                    // 8. <servername>.
                    pattern = "*";
                } else {
                    // 9. <servername>.<pattern>
                    pattern = userarg.substring(index + 1);
                }
            } else {
                // no dots in userarg
                // 10. <servername>
                targetName = userarg;
                pattern = "*";
            }
        }

        // note that "server" is hard-coded everywhere in GF code. We're stuck with it!!

        if (targetName.equals("server") || targetName.equals("server-config")) {
            targets.add(das);
            return true;
        }

        // targetName is either 1 instance or a cluster or garbage!
        targets = targetService.getInstances(targetName);

        if (targets.isEmpty()) {
            setError(Strings.get("admin.get.monitoring.invalidtarget", userarg));
            return false;
        }

        if (targetService.isCluster(targetName) && targets.size() > 1) {
            targetIsMultiInstanceCluster = true;
        }

        return true;
    }

    private void insertNameValuePairs(TreeMap<String, Object> map, org.glassfish.flashlight.datatree.TreeNode tn1, String exactMatch) {
        String name = tn1.getCompletePathName();
        Object value = tn1.getValue();
        if (tn1.getParent() != null) {
            map.put(tn1.getParent().getCompletePathName() + DOTTED_NAME, tn1.getParent().getCompletePathName());
        }
        if (value instanceof Stats) {
            for (Statistic s : ((Stats) value).getStatistics()) {
                String statisticName = s.getName();
                if (statisticName != null) {
                    statisticName = s.getName().toLowerCase(Locale.getDefault());
                }
                addStatisticInfo(s, name + "." + statisticName, map);
            }
        } else if (value instanceof Statistic) {
            addStatisticInfo(value, name, map);
        } else {
            map.put(name, value);
        }

        // IT 8985 bnevins
        // Hack to get single stats. The code above above would take a lot of
        // time to unwind. For development speed we just remove unwanted items
        // after the fact...
        if (exactMatch != null) {
            NameValue nv = getIgnoreBackslash(map, exactMatch);
            map.clear();

            if (nv != null) {
                map.put(nv.name, nv.value);
            }
        }
    }

    /*
     * bnevins, 1-11-11 Note that we can not GUESS where to put the backslash into 'pattern'. If so -- we could simply add
     * it into pattern and do a get on the HashMap. Instead we have to get each and every key in the map, remove backslashes
     * and compare.
     */
    private NameValue getIgnoreBackslash(TreeMap<String, Object> map, String localPattern) {

        if (localPattern == null) {
            return null;
        }

        Object match = map.get(localPattern);
        if (match != null) {
            return new NameValue(localPattern, match);
        }

        localPattern = localPattern.replace("\\", "");
        match = map.get(localPattern);
        if (match != null) {
            return new NameValue(localPattern, match);
        }

        // No easy match...
        for (Entry<String, Object> elem : map.entrySet()) {
            String key = elem.getKey().toString();
            if (!ok(key)) {
                continue;
            }
            String name = key.replace("\\", "");
            if (localPattern.equals(name)) {
                return new NameValue(key, elem.getValue());
            }
        }
        return null;
    }

    private void addStatisticInfo(Object value, String name, TreeMap<String, Object> map) {
        // Most likely we will get the proxy of the StatisticImpl,
        // reconvert that so you can access getStatisticAsMap method
        final Map<String, Object> statsMap;
        if (Proxy.isProxyClass(value.getClass())) {
            statsMap = ((StatisticImpl) Proxy.getInvocationHandler(value)).getStaticAsMap();
        } else {
            statsMap = ((StatisticImpl) value).getStaticAsMap();
        }
        for (Map.Entry<String, Object> entry : statsMap.entrySet()) {
            map.put(name + "-" + entry.getKey(), entry.getValue());
        }
    }

    private void setError(String msg) {
        reporter.setActionExitCode(FAILURE);
        appendStatusMessage(msg);
        clear();
    }

    private void setSuccess() {
        reporter.setActionExitCode(SUCCESS);
    }

    private void appendStatusMessage(String newMessage) {
        reporter.appendMessage("\n");
        reporter.appendMessage(newMessage);
    }

    private boolean hasError() {
        // return reporter.hasFailures();
        return reporter.getActionExitCode() == FAILURE;
    }

    private void clear() {
        targets = Collections.emptyList();
        pattern = "";
    }

    private List<Server> getRemoteServers() {
        // only call on DAS !!!
        if (!isDas()) {
            throw new RuntimeException("Internal Error"); // todo?
        }

        List<Server> notdas = new ArrayList<>(targets.size());
        String dasName = serverEnv.getInstanceName();

        for (Server server : targets) {
            if (!dasName.equals(server.getName())) {
                notdas.add(server);
            }
        }

        return notdas;
    }

    private boolean dasIsInList() {
        List<Server> remoteServers = getRemoteServers();
        LOG.log(TRACE, "dasIsInLiist: remote servers list: {0}, targets: {1}", remoteServers, targets);
        return remoteServers.size() != targets.size();
    }

    private String getNames(List<Server> list) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Server server : list) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }

            sb.append(server.getName());
        }
        return sb.toString();
    }

    private static String handleEscapes(String s) {
        // replace double backslash with backslash
        // simply remove single backslash
        // there is probably a much better, and very very complicated way to do
        // this with regexp. I don't care - it is only done once for each time
        // a user runs a get -m comand.
        final String UNLIKELY_STRING = "___~~~~$$$$___";
        return s.replace("\\\\", UNLIKELY_STRING).replace("\\", "").replace(UNLIKELY_STRING, "\\");
    }

    private boolean isDas() {
        return serverEnv.isDas();
    }

    private static class NameValue {

        String name;
        Object value;

        private NameValue(String s, Object o) {
            name = s;
            value = o;
        }
    }
}
