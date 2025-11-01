/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.jta;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.glassfish.main.jul.handler.LogCollectorHandler;
import org.glassfish.main.jul.record.GlassFishLogRecord;
import org.glassfish.main.test.jdbc.jta.timeout.war.AsynchronousTimeoutingJob;
import org.glassfish.main.test.jdbc.jta.timeout.war.SlowJpaPartitioner;
import org.glassfish.main.test.perf.server.DockerTestEnvironment;
import org.glassfish.tests.utils.junit.TestLoggingExtension;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;

import static jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
import static org.glassfish.main.test.jdbc.jta.timeout.war.SlowJpaPartitioner.TIMEOUT_IN_SECONDS;
import static org.glassfish.tests.utils.junit.matcher.WaitForExecutable.waitFor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * This test verifies log messages in server.log file after JTA exceptions.
 * They have to be in correct order and must contain expected log messages.
 * As it targets race conditions, there is just limited way to verify if everything is correct.
 * Logs are reliable despite the fact that logger threads are not synchronized, in respect
 * to
 *
 * @author David Matejcek
 */
@ExtendWith(TestLoggingExtension.class)
public class JtaTimeoutLoggingITest {

    private static final Logger LOG = LoggerFactory.getLogger(JtaTimeoutLoggingITest.class);

    private static final Class<AsynchronousTimeoutingJob> ASYNCJOB_CLASS = AsynchronousTimeoutingJob.class;

    private static final Set<String> MESSAGES_FOR_FILTER = Arrays
        .stream(new String[] {
            "jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted", //
            "Transaction with id=", //
            "A system exception occurred during an invocation on", //
            "Rolling back timed out transaction" //
        }).collect(Collectors.toSet());

    private static final String LOG_END = "[\\|\\#\\]]*";
    private static final Pattern P_TIMEOUT = Pattern.compile( //
        "\\s+Transaction with id=[0-9]+ timed out after 200[0-9] ms." + LOG_END);
    private static final Pattern P_SYS_EXCEPTION_P = Pattern
        .compile("\\s+A system exception occurred during an invocation on EJB SlowJpaPartitioner," //
            + " method: public void " + SlowJpaPartitioner.class.getName()
            + ".executePreparedPartition\\(\\)" + LOG_END);

    private static final String APPNAME = "jtaTimeout";
    private static boolean dockerAvailable;

    private static DockerTestEnvironment environment;
    private static LogCollectorHandler domainLog;
    private static WebTarget wsEndpoint;


    @BeforeAll
    public static void init() throws Exception {
        dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        assumeTrue(dockerAvailable, "Docker is not available on this environment");
        environment = DockerTestEnvironment.getInstance();
        final java.util.logging.Logger logger = environment.getDomainLogger();
        assertNotNull(logger, "domain logger was not found");
        final Filter filter = event -> {
            final Predicate<? super String> predicate = msgPart -> {
                final String message = event.getMessage();
                return message.contains(msgPart);
            };
            return MESSAGES_FOR_FILTER.stream().anyMatch(predicate);
        };
        domainLog = new LogCollectorHandler(logger, 100_000, 10);
        domainLog.setFilter(filter);

        environment.asadmin("set",
            "configs.config.server-config.transaction-service.timeout-in-seconds=" + TIMEOUT_IN_SECONDS);
        environment.asadmin("restart-domain", "domain1");
        wsEndpoint = environment.deploy(APPNAME, getArchiveToDeploy());
    }


    @AfterEach
    public void resetLogCollector() {
        List<GlassFishLogRecord> logs = domainLog.getAll();
        assertThat("log collector size", logs, hasSize(0));
        domainLog.reset();
    }


    @AfterAll
    public static void cleanup() throws Exception {
        if (!dockerAvailable) {
            return;
        }
        domainLog.close();
        environment.undeploy(APPNAME);
        environment.asadmin("set", "configs.config.server-config.transaction-service.timeout-in-seconds=0");
    }


    @Test
    public void timeoutOnly() throws Throwable {
        callService("1");
        waitFor(() -> assertThat("log entries count", domainLog.getSize(), equalTo(2)), 5000L);
        final List<String> logs = domainLog.getAll(LogRecord::getMessage);
        assertThat(logs, contains( //
            matchesPattern(P_TIMEOUT), //
            allOf(getPatternsForRollbackMessage(STATUS_MARKED_ROLLBACK)) //
        ));
    }


    // FIXME: Is tx marked for rollback or not?
    @Test
    public void timeoutWithError() throws Throwable {
        callService("2");
        waitFor(() -> assertThat("log entries count", domainLog.getSize(), equalTo(5)), 5000L);
        final List<String> logs = domainLog.getAll(LogRecord::getMessage);
        assertThat(logs, contains( //
            matchesPattern(P_TIMEOUT), //
            matchesPattern(P_SYS_EXCEPTION_P), //
            stringContainsInOrder("jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted"), //
            stringContainsInOrder("A system exception occurred during an invocation on EJB "
                    + ASYNCJOB_CLASS.getSimpleName() + ", method: public void " + ASYNCJOB_CLASS.getName()
                    + ".timeoutingAsyncWithFailingNextStep()"), //
            stringContainsInOrder(
                "Caused by: jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted") //
        ));
    }


    @Test
    public void timeoutWithCatchedErrorAndRedo() throws Throwable {
        callService("3");
        waitFor(() -> assertThat("log entries count", domainLog.getSize(), equalTo(7)), 5000L);
        final List<String> logs = domainLog.getAll(LogRecord::getMessage);

        assertThat(logs, contains( //
            matchesPattern(P_TIMEOUT), //
            matchesPattern(P_SYS_EXCEPTION_P), //
            stringContainsInOrder("jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted"), //
            matchesPattern(P_SYS_EXCEPTION_P), //
            stringContainsInOrder(
                "jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted"), //
            stringContainsInOrder("A system exception occurred during an invocation on EJB "
                + ASYNCJOB_CLASS.getSimpleName() + ", method: public void " + ASYNCJOB_CLASS.getName()
                + ".timeoutingAsyncWithFailingNextStepCatchingExceptionAndRedo()"), //
            stringContainsInOrder(
                "Caused by: jakarta.ejb.TransactionRolledbackLocalException: Client's transaction aborted") //
        ));
    }


    private static WebArchive getArchiveToDeploy() throws Exception {
        return ShrinkWrap.create(WebArchive.class)
            .addPackages(true, ASYNCJOB_CLASS.getPackage())
            .addAsWebInfResource("jdbc/jta/timeout/war/persistence.xml", "classes/META-INF/persistence.xml")
        ;
    }


    private void callService(String urlRelativePath) {
        final WebTarget pgstorePath = wsEndpoint.path("/timeout");
        final Builder builder = pgstorePath.path(urlRelativePath).request();
        try (Response response = builder.post(Entity.text(""))) {
            assertEquals(Status.NO_CONTENT, response.getStatusInfo().toEnum(), "response.status");
            assertFalse(response.hasEntity(), "response.hasEntity");
        }
    }


    private Set<Matcher<? super String>> getPatternsForRollbackMessage(final int localTxStatus) {
        return Arrays.stream(new String[] {"EJB5123:Rolling back timed out transaction ", //
            "\\[JavaEETransactionImpl\\: txId=[0-9]+" //
                + " nonXAResource=ResourceHandle\\[id=[0-9]+," //
                + " state=ResourceState\\[enlisted=true\\, busy=true\\, usages=[0-9]+\\]\\]" //
                + " jtsTx=null localTxStatus=" + localTxStatus //
                + " syncs=", //
            "com.sun.ejb.containers.SimpleEjbResourceHandlerImpl\\@[0-9a-f]+", //
            "com.sun.ejb.containers.ContainerSynchronization\\@[0-9a-f]+", //
            "org.eclipse.persistence.internal.jpa.transaction.JTATransactionWrapper\\$1\\@[0-9a-f]+", //
// TODO: Should or should not be here?
//            "org.eclipse.persistence.transaction.JTASynchronizationListener\\@[0-9a-f]+", //
            "com.sun.enterprise.resource.pool.PoolManagerImpl\\$SynchronizationListener\\@[0-9a-f]+", //
            "\\]\\] for \\[AsynchronousTimeoutingJob\\]" + LOG_END}).map(s -> ".*" + s + ".*").map(Pattern::compile)
            .map(Matchers::matchesPattern).collect(Collectors.toSet());
    }
}
