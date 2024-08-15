/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.libpam.impl;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;

import java.util.Arrays;
import java.util.List;

import static org.jvnet.libpam.impl.CLibrary.libc;

/**
 * libpam.so binding.
 *
 * See http://www.opengroup.org/onlinepubs/008329799/apdxa.htm for the online reference of pam_appl.h
 *
 * @author Kohsuke Kawaguchi
 */
public interface PAMLibrary extends Library {

    final int PAM_USER = 2;

    // error code
    final int PAM_SUCCESS = 0;
    final int PAM_CONV_ERR = 6;

    final int PAM_PROMPT_ECHO_OFF = 1; /* Echo off when getting response */
    final int PAM_PROMPT_ECHO_ON = 2; /* Echo on when getting response */
    final int PAM_ERROR_MSG = 3; /* Error message */
    final int PAM_TEXT_INFO = 4; /* Textual information */

    public static final PAMLibrary libpam = Native.loadLibrary("pam", PAMLibrary.class);


    class pam_handle_t extends PointerType {
        public pam_handle_t() {
        }

        public pam_handle_t(Pointer pointer) {
            super(pointer);
        }
    }

    class pam_message extends Structure {
        public int msg_style;
        public String msg;

        /**
         * Attach to the memory region pointed by the given pointer.
         */
        public pam_message(Pointer src) {
            useMemory(src);
            read();
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("msg_style", "msg");
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void setMsgStyle(int msg_style) {
            this.msg_style = msg_style;
        }
    }

    class pam_response extends Structure {

        public static final int SIZE = new pam_response().size();

        /**
         * This is really a string, but this field needs to be malloc-ed by the conversation method, and to be freed by the
         * caler, so I bind it to {@link Pointer} here.
         *
         * The man page doesn't say that, but see http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv This
         * behavior is confirmed with a test, too; if I don't do strdup, libpam crashes.
         */
        public Pointer resp;
        public int resp_retcode;

        /**
         * Attach to the memory region pointed by the given memory.
         */
        public pam_response(Pointer src) {
            useMemory(src);
            read();
        }

        public pam_response() {
        }

        /**
         * Sets the response code.
         */
        public void setResp(String msg) {
            this.resp = libc.strdup(msg);
        }

        protected Pointer getResp() {
            return resp;
        }

        public void setRespCode(int resp_retcode) {
            this.resp_retcode = resp_retcode;
        }

        public int getRespCode() {
            return resp_retcode;
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("resp", "resp_retcode");
        }
    }

    class pam_conv extends Structure {
        public interface PamCallback extends Callback {
            /**
             * According to http://www.netbsd.org/docs/guide/en/chap-pam.html#pam-sample-conv, resp and its member string both needs
             * to be allocated by malloc, to be freed by the caller.
             */
            int callback(int num_msg, Pointer msg, Pointer resp, Pointer pointer);
        }

        public PamCallback conv;
        public Pointer pointer;

        public pam_conv(PamCallback conv) {
            this.conv = conv;
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("conv", "pointer");
        }

        protected PamCallback getConv() {
            return conv;
        }
    }

    int pam_start(String service, String user, pam_conv conv, PointerByReference/* pam_handle_t** */ pamh_p);

    int pam_end(pam_handle_t handle, int pam_status);

    int pam_set_item(pam_handle_t handle, int item_type, String item);

    int pam_get_item(pam_handle_t handle, int item_type, PointerByReference item);

    int pam_authenticate(pam_handle_t handle, int flags);

    int pam_setcred(pam_handle_t handle, int flags);

    int pam_acct_mgmt(pam_handle_t handle, int flags);

    String pam_strerror(pam_handle_t handle, int pam_error);


}
