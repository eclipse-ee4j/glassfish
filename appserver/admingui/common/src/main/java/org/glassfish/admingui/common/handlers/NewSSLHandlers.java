/*
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

/*
 * SSLHandlers.java
 *
 * Created on June 25, 2009, 11:30 PM
 *
 */

package org.glassfish.admingui.common.handlers;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.util.Arrays;
import java.util.Vector;

import org.glassfish.admingui.common.util.GuiUtil;

/**
 *
 * @author anilam
 *
 */
public class NewSSLHandlers {

    static String[] COMMON_CIPHERS = {"SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};

    static String[] BIT_CIPHERS = {"SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
        "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
        "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"};

    public NewSSLHandlers() {
    }

    @Handler(id="convertToDifferentCiphersGroup",
    input={
        @HandlerInput(name="ciphers",   type=Object.class)},
    output={
        @HandlerOutput(name="CommonCiphersList",    type=String[].class),
        @HandlerOutput(name="EphemeralCiphersList", type=String[].class),
        @HandlerOutput(name="OtherCiphersList",     type=String[].class),
        @HandlerOutput(name="EccCiphersList",       type=String[].class)}
    )
    public static void convertToDifferentCiphersGroup(HandlerContext handlerCtx) {

        Vector ciphersVector = null;
        Object ciphers = (Object)handlerCtx.getInputValue("ciphers");
        if (ciphers != null) {
            if (ciphers instanceof String) {
                String[] ary = getSelectedCiphersList((String) ciphers);
                ciphersVector = getCiphersVector(ary);
            } else {
                ciphersVector = getCiphersVector((String[]) ciphers);
            }
        }
        handlerCtx.setOutputValue("CommonCiphersList", getCommonCiphers(ciphersVector));
        handlerCtx.setOutputValue("EphemeralCiphersList", getEphemeralCiphers(ciphersVector));
        handlerCtx.setOutputValue("OtherCiphersList", getOtherCiphers(ciphersVector));
        handlerCtx.setOutputValue("EccCiphersList", getEccCiphers(ciphersVector));
    }


    @Handler(id="convertCiphersItemsToStr",
    input={
        @HandlerInput(name="common",    type=String[].class),
        @HandlerInput(name="ephemeral", type=String[].class),
        @HandlerInput(name="other",     type=String[].class),
        @HandlerInput(name="ecc",       type=String[].class)},
    output={
        @HandlerOutput(name="ciphers",   type=Object.class)}
    )
    public static void convertCiphersItemsToStr(HandlerContext handlerCtx) {

        String[] common = (String[])handlerCtx.getInputValue("common");
        String[] ephemeral = (String[])handlerCtx.getInputValue("ephemeral");
        String[] other = (String[])handlerCtx.getInputValue("other");
        String[] ecc = (String[])handlerCtx.getInputValue("ecc");

        String ciphers = processSelectedCiphers(common, "");
        ciphers = processSelectedCiphers(ephemeral, ciphers);
        ciphers = processSelectedCiphers(other, ciphers);
        ciphers = processSelectedCiphers(ecc, ciphers);

        handlerCtx.setOutputValue("ciphers", ciphers);
    }



        private static String[] getSelectedCiphersList(String selectedCiphers){
            Vector selItems = new Vector();
            if(selectedCiphers != null){
                String[] sel = selectedCiphers.split(","); //NOI18N
                for(int i=0; i<sel.length; i++){
                    String cName = sel[i];
                    if(cName.startsWith("+")){ //NOI18N
                        cName = cName.substring(1, cName.length());
                        selItems.add(cName);
                    }
                }
            }
            return (String[])selItems.toArray(new String[selItems.size()]);
        }

        private static String processSelectedCiphers(String[] selectedCiphers, String ciphers){
            StringBuilder sb = new StringBuilder();
            String sep = "";
            if ( ! GuiUtil.isEmpty(ciphers)){
                sb.append(ciphers);
                sep = ",";
            }
            if(selectedCiphers != null){
                for (int i = 0; i < selectedCiphers.length; i++) {
                    sb.append(sep).append("+").append(selectedCiphers[i]);
                    sep = ",";
                }
            }
            return sb.toString();
        }

        private static Vector getCiphersVector(String[] allCiphers){
            Vector ciphers = new Vector();
            if (allCiphers != null){
                for(int i=0; i<allCiphers.length; i++){
                    ciphers.add(allCiphers[i]);
                }
            }
            return ciphers;
        }

        private static String[] getCommonCiphers(Vector ciphers){
            Vector commonCiphers = filterCiphers(ciphers, COMMON_CIPHERS);
            String[] ciphersList = (String[])commonCiphers.toArray(new String[commonCiphers.size()]);
            return ciphersList;
        }

        private static String[] getEccCiphers(Vector ciphers){
            Vector eccCiphers = breakUpCiphers(new Vector(), ciphers, "_ECDH_"); //NOI18N
            eccCiphers = breakUpCiphers(eccCiphers, ciphers, "_ECDHE_"); //NOI18N
            String[] ciphersList = (String[])eccCiphers.toArray(new String[eccCiphers.size()]);
            return ciphersList;
        }

        private static String[] getEphemeralCiphers(Vector ciphers){
            Vector ephmCiphers = breakUpCiphers(new Vector(), ciphers, "_DHE_RSA_"); //NOI18N
            ephmCiphers = breakUpCiphers(ephmCiphers, ciphers, "_DHE_DSS_"); //NOI18N
            String[] ciphersList = (String[])ephmCiphers.toArray(new String[ephmCiphers.size()]);
            return ciphersList;
        }

        private static String[] getOtherCiphers(Vector ciphers){
            Vector bitCiphers = filterCiphers(ciphers, BIT_CIPHERS);
            String[] ciphersList = (String[])bitCiphers.toArray(new String[bitCiphers.size()]);
            return ciphersList;
        }

        private static Vector filterCiphers(Vector ciphers, String[] filterList){
            Vector listCiphers = new Vector();
            if (ciphers != null){
                for(int i=0; i<ciphers.size(); i++){
                    String cipherName = ciphers.get(i).toString();
                    if (Arrays.asList(filterList).contains(cipherName)){
                        listCiphers.add(ciphers.get(i));
                    }
                }
            }
            return listCiphers;
        }


    private static Vector breakUpCiphers(Vector cipherSubset, Vector allCiphers, String type){
            if (allCiphers != null){
                for(int i=0; i<allCiphers.size(); i++){
                    String cipherName = allCiphers.get(i).toString();
                    if(cipherName.indexOf(type) != -1) {
                        if(! Arrays.asList(BIT_CIPHERS).contains(cipherName)){
                            cipherSubset.add(cipherName);
                        }
                    }
                }
            }
            return cipherSubset;
        }

}
