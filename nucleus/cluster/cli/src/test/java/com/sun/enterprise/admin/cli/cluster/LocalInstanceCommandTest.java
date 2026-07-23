/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.universal.io.SmartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;

import javax.crypto.spec.SecretKeySpec;

import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_ALIAS;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME_LEGACY;
import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_PASSWORD;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author bnevins
 */
public class LocalInstanceCommandTest extends LocalInstanceCommand {

    private static File installDir;
    private static File nodeAgentsDir;

    @BeforeEach
    public void setUp() {
        String installDirPath = LocalInstanceCommandTest.class.getClassLoader().getResource("fake_gf_install_dir").getPath();
        installDir = SmartFile.sanitize(new File(installDirPath));
        System.out.println("install dir: " + installDir);
        nodeAgentsDir = new File(installDir, "nodes");
    }

    /**
     * Test of validate method, of class LocalInstanceCommand.
     */
    @Test
    public void testValidate() throws Exception {
        nodeDir = nodeAgentsDir.getAbsolutePath();
        instanceName = "i1";
        isCreateInstanceFilesystem = true;
        assertDoesNotThrow(() -> validate());
    }

    /**
     * A node created by 7.0.x keeps its saved master password in a JCEKS store named
     * {@code master-password} (no extension) that upgrade never touches. The first instance command
     * on such a node must convert it to {@code master-password.p12} in place, preserving the password.
     */
    @Test
    public void testLegacyMasterPasswordMigratedToPkcs12(@TempDir File nodeDir) throws Exception {
        String savedPassword = "s3cr3t-master";
        File agentDir = new File(nodeDir, "agent");
        assertTrue(agentDir.mkdirs());

        // Simulate a 7.0.x node: the saved master password stored in a JCEKS keystore with no extension.
        File legacyFile = new File(agentDir, MASTER_PASSWORD_FILENAME_LEGACY);
        char[] fixedPassword = MASTER_PASSWORD_PASSWORD.toCharArray();
        KeyStore legacyStore = KeyStore.getInstance("JCEKS");
        legacyStore.load(null, fixedPassword);
        legacyStore.setKeyEntry(MASTER_PASSWORD_ALIAS, new SecretKeySpec(savedPassword.getBytes(UTF_8), "AES"),
            fixedPassword, null);
        try (FileOutputStream out = new FileOutputStream(legacyFile)) {
            legacyStore.store(out, fixedPassword);
        }

        nodeDirChild = nodeDir;
        File pkcs12 = new File(agentDir, MASTER_PASSWORD_FILENAME);

        assertEquals(pkcs12, getMasterPasswordFile());
        assertTrue(pkcs12.canRead(), "PKCS12 master password store should have been created");
        assertFalse(legacyFile.exists(), "Legacy store should have been renamed away");
        assertTrue(new File(agentDir, MASTER_PASSWORD_FILENAME_LEGACY + ".bak").exists(),
            "Legacy store should be kept as a .bak backup");

        PasswordAdapter reader = new PasswordAdapter(pkcs12.getAbsolutePath(), fixedPassword);
        assertEquals(savedPassword, reader.getPasswordForAlias(MASTER_PASSWORD_ALIAS),
            "Migrated store must yield the original master password");

        // A second call finds the PKCS12 store directly and does not need the legacy file again.
        assertEquals(pkcs12, getMasterPasswordFile());
    }

    @Test
    public void testNoMasterPasswordFileReturnsNull(@TempDir File nodeDir) {
        assertTrue(new File(nodeDir, "agent").mkdirs());
        nodeDirChild = nodeDir;
        assertNull(getMasterPasswordFile(), "No PKCS12 and no legacy store means no saved master password");
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        System.out.println("Do nothing!");
        return 0;
    }
}
