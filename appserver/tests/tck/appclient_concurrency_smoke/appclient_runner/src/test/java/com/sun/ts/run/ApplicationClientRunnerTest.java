/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation. All rights reserved.
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
package com.sun.ts.run;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sun.javatest.Status;
import com.sun.ts.lib.harness.ExecTSTestCmd;

@ExtendWith(ArquillianExtension.class)
public class ApplicationClientRunnerTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
       return Maven.configureResolver()
                   .workOffline()
                   .resolve("org.glassfish.main.tests.tck:concurrency_war:war:1.0")
                   .withoutTransitivity()
                   .as(WebArchive.class)[0];
    }

    @Test
    public void test() {
        ExecTSTestCmd cmd = new ExecTSTestCmd();

        System.getProperties().entrySet().stream().forEach(entry -> System.out.println(entry.getKey() + "=" + entry.getValue()));

        List<String> arguments = new ArrayList<>();
        arguments.addAll(getEnvOptions());
        arguments.addAll(getJavaOptions());
        arguments.addAll(getTestOptions());

        System.out.println("\nProgram arguments:\n");

        arguments.stream().forEach(e -> System.out.println(e));

        PrintWriter log = new PrintWriter(new OutputStreamWriter(System.err));
        PrintWriter ref = new PrintWriter(new OutputStreamWriter(System.out));

        try {
            Status status = cmd.run(arguments.toArray(String[]::new), log, ref);
            if (!status.isPassed()) {
                fail(status.getReason());
            }

        } finally {
            log.flush();
            ref.flush();
        }
    }

    // ### Environment options

    List<String> getEnvOptions() {
        return List.of(
            "DISPLAY=:0.0",
            "HOME=" + System.getProperty("user.home"),
            "LD_LIBRARY_PATH=" + System.getProperty("glassfish.home") + "/glassfish/lib",
            "TMP=" + System.getProperty("java.io.tmpdir"),
            "windir=",
            "SYSTEMROOT=",
            "APPCPATH=" + getAppClassPath());
    }

    String getAppClassPath() {
        String localRepository = System.getProperty("localRepository");

        return String.join(":", List.of(
            localRepository + "/com/sun/tsharness/1.4/tsharness-1.4.jar",
            System.getProperty("glassfish.home") + "/glassfish/modules/weld-osgi-bundle.jar",
            System.getProperty("glassfish.home") + "/glassfish/modules/jakarta.enterprise.cdi-api.jar"
        ));
    }


    // ### Java options

    List<String> getJavaOptions() {
        List<String> javaOptions = new ArrayList<>();
        javaOptions.add(System.getProperty("java.home") + "/bin/java");

        if (System.getProperty("maven.tck.debug") != null && !System.getProperty("maven.tck.debug").isEmpty()) {
            javaOptions.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=9009");
        }

        javaOptions.addAll(getJavaConstantOptions());
        javaOptions.addAll(getJavaDynamicOptions());
        javaOptions.add(getJavaAgentOption());
        javaOptions.addAll(getJavaClassPath());

        return javaOptions;
    }

    List<String> getJavaConstantOptions() {
        return List.of(
            "-Djava.system.class.loader=org.glassfish.appclient.client.acc.agent.ACCAgentClassLoader",
            "-Djava.protocol.handler.pkgs=javax.net.ssl",
            "-Djavax.net.ssl.keyStorePassword=changeit",
            "-Djavax.xml.parsers.SAXParserFactory=com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
            "-Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
            "-Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
            "-Dorg.xml.sax.driver=com.sun.org.apache.xerces.internal.parsers.SAXParser",
            "-Dorg.xml.sax.parser=org.xml.sax.helpers.XMLReaderAdapter",
            "-Doracle.jdbc.J2EE13Compliant=true",
            "-Doracle.jdbc.mapDateToTimestamp",
            "-Djava.security.manager",
            "-Dstartup.login=false",
            "-Dauth.gui=false",
            "-DwebServerHost.2=localhost",
            "-DwebServerPort.2=8002",
            "-Ddeliverable.class=com.sun.ts.lib.deliverable.cts.CTSDeliverable",

            "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
            "--add-opens=java.base/sun.net.www.protocol.jrt=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED",
            "--add-opens=java.naming/javax.naming.spi=ALL-UNNAMED");
    }

    List<String> getJavaDynamicOptions() {
        return List.of(
            "-Djava.security.policy=${javaee.home}/lib/appclient/client.policy",
            "-Dcts.tmp=" + System.getProperty("java.io.tmpdir"),
            "-Djava.security.auth.login.config=${javaee.home}/lib/appclient/appclientlogin.conf",

            "-Dcom.sun.enterprise.home=${javaee.home}",
            "-Djavax.net.ssl.keyStore=${resources.home}/clientcert.jks",

            "-Dcom.sun.aas.installRoot=${javaee.home}",
            "-Dcom.sun.aas.imqLib=${mq.home}/lib",
            "-Djavax.net.ssl.trustStore=${javaee.home}/domains/domain1/config/cacerts.jks",

            "-Dlog.file.location=${javaee.home}/domains/domain1/logs",
            "-Dri.log.file.location=${javaee.home}/domains/domain1/logs",

            "-Dprovider.configuration.file=${javaee.home}/domains/domain1/config/ProviderConfiguration.xml",
            "-Djava.security.properties=${javaee.home}/domains/domain1/config/ts.java.security",
            "-Dcom.sun.aas.configRoot=${javaee.home}/config")
            .stream()
            .map(e -> e.replace("${javaee.home}", System.getProperty("glassfish.home") + "/glassfish"))
            .map(e -> e.replace("${mq.home}", System.getProperty("glassfish.home") + "/mq"))
            .map(e -> e.replace("${resources.home}", System.getProperty("basedir") + "/src/test/resources"))
            .collect(toList());
    }

    String getJavaAgentOption() {
        String argument = "arg=";
        String clientJar = "client=jar=";
        String basedir = System.getProperty("basedir");

        return
          "-javaagent:" + getGFClientJar() + "=" +

          String.join(",", List.of(
            argument + "-configxml",
            argument + basedir + "/src/test/resources/s1as-sun-acc.xml",

            clientJar + getClientJar(),

            argument + "-name",
            argument + "concurrency_spec_ContextService_tx_client"
        ));
    }



    List<String> getJavaClassPath() {
        return List.of(
            "-classpath",
            String.join(":", List.of(
                getGFClientJar(),
                getClientJar()
        )));
    }

    String getGFClientJar() {
        return System.getProperty("glassfish.home") + "/glassfish/lib/gf-client.jar";
    }

    String getClientJar() {
        String glassfishVersion = System.getProperty("glassfish.version");
        String localRepository = System.getProperty("localRepository");

        Path originalEjbJar = Paths.get(localRepository + "/org/glassfish/main/tests/tck/concurrency_appclient/" + glassfishVersion + "/concurrency_appclient-" + glassfishVersion + ".jar");
        Path copiedEjbJar = Paths.get(System.getProperty("tck.home") + "/concurrency_spec_ContextService_txClient.jar");
        try {
            Files.copy(originalEjbJar, copiedEjbJar, REPLACE_EXISTING);
        } catch (IOException e) {
           throw new UncheckedIOException(e);
        }

        return copiedEjbJar.toString();
    }


    // ### Test options

    List<String> getTestOptions() {
        String basedir = System.getProperty("basedir");

        return List.of(
            // The main class we're running that will start the application client code
            "org.glassfish.appclient.client.AppClientGroupFacade",

            "-ap", basedir + "/src/test/resources/tstest.tssql.stmt",
            "-p", basedir + "/src/test/resources/tstest.jte",
            "-t", "testDefaultAndCommit"
        );
    }

}
