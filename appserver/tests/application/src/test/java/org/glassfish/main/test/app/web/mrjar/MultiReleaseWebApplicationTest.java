/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.web.mrjar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.util.jar.Manifest;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.AsadminResult;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.util.jar.Attributes.Name.MANIFEST_VERSION;
import static java.util.jar.Attributes.Name.MULTI_RELEASE;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.condition.JRE.JAVA_16;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V11;
import static org.objectweb.asm.Opcodes.V17;
import static org.objectweb.asm.Opcodes.V1_8;

public class MultiReleaseWebApplicationTest {

    private static final System.Logger LOG = System.getLogger(MultiReleaseWebApplicationTest.class.getName());

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    private static final String APP_NAME = "mrwebapp";

    private static final String CONTEXT_ROOT = "/" + APP_NAME;

    private static final String APP_FILE_NAME = APP_NAME + ".war";

    private static final String LIB_FILE_NAME = "mrlib.jar";

    @BeforeAll
    public static void deploy() throws IOException {
        File warFile = createDeployment();
        try {
            AsadminResult result = ASADMIN.exec("deploy", warFile.getAbsolutePath());
            assertThat(result, asadminOK());
        } finally {
            try {
                Files.deleteIfExists(warFile.toPath());
            } catch (IOException e) {
                LOG.log(WARNING, "An error occurred while delete file {0}", warFile.getAbsolutePath());
            }

        }
    }

    @AfterAll
    public static void undeploy() {
        assertThat(ASADMIN.exec("undeploy", APP_NAME), asadminOK());
    }

    @Test
    @EnabledForJreRange(max = JAVA_16)
    public void testMultiReleaseJarProcessingJdk11(TestInfo testInfo) throws IOException {
        LOG.log(INFO, "Run test method {0}", testInfo.getTestMethod().orElseThrow().getName());
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        assertAll(
            () -> assertThat(connection.getResponseCode(), equalTo(200)),
            // Check version of loaded class file
            () -> assertThat(Integer.parseInt(readResponse(connection)), equalTo(V11))
        );
    }

    @Test
    @EnabledForJreRange(min = JAVA_17)
    public void testMultiReleaseJarProcessingJdk17(TestInfo testInfo) throws IOException {
        LOG.log(INFO, "Run test method {0}", testInfo.getTestMethod().orElseThrow().getName());
        HttpURLConnection connection = GlassFishTestEnvironment.openConnection(8080, CONTEXT_ROOT);
        connection.setRequestMethod("GET");
        assertAll(
            () -> assertThat(connection.getResponseCode(), equalTo(200)),
            // Check version of loaded class file
            () -> assertThat(Integer.parseInt(readResponse(connection)), equalTo(V17))
        );
    }

    private String readResponse(HttpURLConnection connection) throws Exception {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private static String classFileNameFor(Class<?> c) {
        return c.getSimpleName() + ".class";
    }

    private static String packageJarEntryFor(Class<?> c, int version) {
        StringBuilder jarTarget = new StringBuilder();
        switch (version) {
            case V11:
                jarTarget.append("META-INF/versions/11/");
                break;
            case V17:
                jarTarget.append("META-INF/versions/17/");
                break;
            default:
                break;
        }
        jarTarget.append(c.getPackageName().replace('.', '/'));
        return  jarTarget.toString();
    }

    private static File createDeployment() throws IOException {
        // Create Multi-Release JAR library
        JavaArchive jarArchive = ShrinkWrap.create(JavaArchive.class, LIB_FILE_NAME)
            .setManifest(generateManifest())
            .addClass(Version.class)
            .add(generateVersionClass(V1_8), packageJarEntryFor(VersionImpl.class, V1_8), classFileNameFor(VersionImpl.class))
            .add(generateVersionClass(V11), packageJarEntryFor(VersionImpl.class, V11), classFileNameFor(VersionImpl.class))
            .add(generateVersionClass(V17), packageJarEntryFor(VersionImpl.class, V17), classFileNameFor(VersionImpl.class));

        // Create test web application archive
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class)
            .addAsLibrary(jarArchive)
            .addClass(MultiReleaseResource.class)
            .addClass(MultiReleaseApplication.class);

        File tmpDir = Files.createTempDirectory(APP_NAME).toFile();
        File warFile = new File(tmpDir, APP_FILE_NAME);
        webArchive.as(ZipExporter.class).exportTo(warFile, true);
        tmpDir.deleteOnExit();
        return warFile;
    }

    private static Asset generateManifest() throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(MULTI_RELEASE, String.valueOf(Boolean.TRUE));
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            manifest.write(out);
            return new ByteArrayAsset(out.toByteArray());
        }
    }

    private static Asset generateVersionClass(int version) {
        ClassWriter cw = new ClassWriter(0);

        cw.visit(version,
            ACC_PUBLIC + ACC_SUPER,
            Type.getInternalName(VersionImpl.class),
            null,
            "java/lang/Object",
            new String[] {Type.getInternalName(Version.class)});

        // Generate default constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Generate actual getVersion() method
        mv = cw.visitMethod(ACC_PUBLIC, "getVersion", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(String.valueOf(version));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();

        return new ByteArrayAsset(cw.toByteArray());
    }
}
