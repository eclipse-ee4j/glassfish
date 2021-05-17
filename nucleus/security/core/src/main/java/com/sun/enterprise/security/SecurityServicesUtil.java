/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.enterprise.security;

import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.admin.ServerEnvironment;
import java.util.logging.Logger;
import javax.security.auth.callback.CallbackHandler;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;

@Service
@Singleton
public class SecurityServicesUtil {

    private static ServiceLocator habitat = Globals.getDefaultHabitat();

    private static final LocalStringManagerImpl _localStrings =
            new LocalStringManagerImpl(SecurityServicesUtil.class);
    private static final Logger _logger = SecurityLoggerInfo.getLogger();
    // Using SharedSecureRandom from internal-api instead.
    // SecureRandom number used for HTTPS and IIOP/SSL.
    // This number is accessed by iiop/IIOPSSLSocketFactory
    // & web/security/SSLSocketFactory classes.
    //public static final SecureRandom secureRandom = new SecureRandom();

    @Inject
    private ProcessEnvironment processEnv;

    @Inject
    private ServerEnvironment env;

    @Inject
    private AuditManager auditManager;

//    static {
//        secureRandom.setSeed(System.currentTimeMillis());
//    }

    //the appclient CBH
    private CallbackHandler callbackHandler;

    /**
     * replacing all of this with SharedSecureRandom
     * So on restart, all your seed material comes from a new time that varies
     * little from something guessable, plus something you repeat every run?
     * Seems weak to me.  Why do you want to keep the seed around?
     * There are some situations where this might be needed,
     * but if you're just looking for as random numbers as possible,
     * I don't think this is not a good way to achieve it.
     * code moved from J2EEServer.run()
     */
//    public void initSecureSeed() {
//
//        File secureSeedFile = null;
//        if (Util.isEmbeddedServer()) {
//            try {
//                secureSeedFile = Util.writeConfigFileToTempDir("secure.seed");
//
//            } catch (IOException ex) {
//                String errmsg =
//                        "IOException while constructing embedded config file";
//                _logger.log(Level.WARNING, errmsg);
//            }
//        } else {
//            secureSeedFile = new File(env.getConfigDirPath(), "secure.seed");
//        }
//
//        // read the secure random from the file
//        long seed = readSecureSeed(secureSeedFile);
//        secureRandom.setSeed(seed);
//        // generate a new one for the next startup
//        seed = secureRandom.nextLong();
//        writeSecureSeed(secureSeedFile, seed);
//        secureSeedFile = null;
//    }

    /** read the secure random number from the file.
     *  If the seed is not present, the default expensive SecureRandom seed
     *  generation algorithm is invoked to return a new seed number
     *  @param fname the file to be read - here secure.seed file.
     */
//    private long readSecureSeed(File fname) {
//        byte[] seed;
//        try {
//            BufferedReader fis = new BufferedReader(new FileReader(fname));
//            try {
//                String line = fis.readLine();
//                fis.close();
//                // returning a long value.
//                Long lseed = new Long(line);
//                return lseed.longValue();
//            } catch (IOException e) {
//                if (fis != null) {
//                    fis.close();
//                }
//            }
//        } catch (Throwable e) {  // IASRI 4666401 if all fails just create new
//        }
//        // BEGIN IASRI 4703002
//        // In order to work around JVM bug 4709460 avoid internal seeding.
//        // (Call setSeed again (see static block) to attempt to add some
//        // minimal randomness; setSeed calls are cumulative)
//
//        secureRandom.setSeed(System.currentTimeMillis());
//        long newSeed = secureRandom.nextLong();
//        return newSeed;
//    }

    /** write the new secure seed to the secure.seed file to speed up
     * startup the next time the server is started.
     * @param fname secure.seed file
     * @param seed seed the value of the 8 byte seed.
     */
//    private void writeSecureSeed(File fname, long seed) {
//        try {
//            FileOutputStream fos = new FileOutputStream(fname);
//            String sseed = Long.toString(seed);
//            fos.write(sseed.getBytes());
//            fos.close();
//        } catch (IOException e) {
//            String errmsg =
//                    _localStrings.getLocalString("j2ee.startupslow",
//                    "Cannot write the seed file for fast startup. The next startup will be slow.");
//
//            _logger.log(Level.WARNING, errmsg);
//        }
//
//    }

    public ServiceLocator getHabitat() {
        return habitat;
    }

    public AuditManager getAuditManager() {
        return auditManager;
    }

    public static SecurityServicesUtil getInstance() {
        // return my singleton service
        if (habitat == null) {
            return null;
        }
        return habitat.getService(SecurityServicesUtil.class);
    }

    public ProcessEnvironment getProcessEnv() {
        return processEnv;
    }

    public boolean isACC() {
        return processEnv.getProcessType().equals(ProcessType.ACC);
    }
    public boolean isServer() {
        return processEnv.getProcessType().isServer();
    }
    public boolean isNotServerOrACC() {
        return processEnv.getProcessType().equals(ProcessType.Other);
    }

    public CallbackHandler getCallbackHandler() {
        return callbackHandler;
    }

    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

}
