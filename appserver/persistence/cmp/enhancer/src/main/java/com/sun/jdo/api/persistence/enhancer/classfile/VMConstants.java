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

package com.sun.jdo.api.persistence.enhancer.classfile;

/**
 * VMConstants is a collection of the constants defined in the virtual machine spec.
 * Also included are several assorted constants of our own which seem logical to define here.
 */
public interface VMConstants {
  /* Access types */
  int ACCPublic    = 0x0001;
  int ACCPrivate   = 0x0002;
  int ACCProtected = 0x0004;
  int ACCStatic    = 0x0008;
  int ACCFinal     = 0x0010;
  int ACCSuper         = 0x0020;   /* For Class file */
  int ACCSynchronized  = 0x0020;   /* For methods    */
  int ACCVolatile  = 0x0040;
  int ACCTransient = 0x0080;
  int ACCNative    = 0x0100;
  int ACCInterface = 0x0200;
  int ACCAbstract  = 0x0400;


  /* Primitive Types */
  /* These correspond to the values used by newarray */
  int T_BOOLEAN = 4;
  int T_CHAR = 5;
  int T_FLOAT = 6;
  int T_DOUBLE = 7;
  int T_BYTE = 8;
  int T_SHORT = 9;
  int T_INT = 10;
  int T_LONG = 11;

  /* Class types - Not really part of the VM spec */
  int TC_OBJECT = 12;
  int TC_INTERFACE = 13;
  int TC_STRING = 14;

  /* Special pseudo types - Not really part of the VM spec */
  int T_UNKNOWN = 15;
  int T_WORD = 16;
  int T_TWOWORD = 17;

  /* Constant pool types */
  int CONSTANTUtf8 = 1;
  int CONSTANTUnicode = 2; /* Def.in Beta doc - not in 1.0.2 */
  int CONSTANTInteger = 3;
  int CONSTANTFloat = 4;
  int CONSTANTLong = 5;
  int CONSTANTDouble = 6;
  int CONSTANTClass = 7;
  int CONSTANTString = 8;
  int CONSTANTFieldRef = 9;
  int CONSTANTMethodRef = 10;
  int CONSTANTInterfaceMethodRef = 11;
  int CONSTANTNameAndType = 12;



  /* Java VM opcodes */
  int opc_nop = 0;
  int opc_aconst_null = 1;
  int opc_iconst_m1 = 2;
  int opc_iconst_0 = 3;
  int opc_iconst_1 = 4;
  int opc_iconst_2 = 5;
  int opc_iconst_3 = 6;
  int opc_iconst_4 = 7;
  int opc_iconst_5 = 8;
  int opc_lconst_0 = 9;
  int opc_lconst_1 = 10;
  int opc_fconst_0 = 11;
  int opc_fconst_1 = 12;
  int opc_fconst_2 = 13;
  int opc_dconst_0 = 14;
  int opc_dconst_1 = 15;
  int opc_bipush = 16;
  int opc_sipush = 17;
  int opc_ldc = 18;
  int opc_ldc_w = 19;
  int opc_ldc2_w = 20;
  int opc_iload = 21;
  int opc_lload = 22;
  int opc_fload = 23;
  int opc_dload = 24;
  int opc_aload = 25;
  int opc_iload_0 = 26;
  int opc_iload_1 = 27;
  int opc_iload_2 = 28;
  int opc_iload_3 = 29;
  int opc_lload_0 = 30;
  int opc_lload_1 = 31;
  int opc_lload_2 = 32;
  int opc_lload_3 = 33;
  int opc_fload_0 = 34;
  int opc_fload_1 = 35;
  int opc_fload_2 = 36;
  int opc_fload_3 = 37;
  int opc_dload_0 = 38;
  int opc_dload_1 = 39;
  int opc_dload_2 = 40;
  int opc_dload_3 = 41;
  int opc_aload_0 = 42;
  int opc_aload_1 = 43;
  int opc_aload_2 = 44;
  int opc_aload_3 = 45;
  int opc_iaload = 46;
  int opc_laload = 47;
  int opc_faload = 48;
  int opc_daload = 49;
  int opc_aaload = 50;
  int opc_baload = 51;
  int opc_caload = 52;
  int opc_saload = 53;
  int opc_istore = 54;
  int opc_lstore = 55;
  int opc_fstore = 56;
  int opc_dstore = 57;
  int opc_astore = 58;
  int opc_istore_0 = 59;
  int opc_istore_1 = 60;
  int opc_istore_2 = 61;
  int opc_istore_3 = 62;
  int opc_lstore_0 = 63;
  int opc_lstore_1 = 64;
  int opc_lstore_2 = 65;
  int opc_lstore_3 = 66;
  int opc_fstore_0 = 67;
  int opc_fstore_1 = 68;
  int opc_fstore_2 = 69;
  int opc_fstore_3 = 70;
  int opc_dstore_0 = 71;
  int opc_dstore_1 = 72;
  int opc_dstore_2 = 73;
  int opc_dstore_3 = 74;
  int opc_astore_0 = 75;
  int opc_astore_1 = 76;
  int opc_astore_2 = 77;
  int opc_astore_3 = 78;
  int opc_iastore = 79;
  int opc_lastore = 80;
  int opc_fastore = 81;
  int opc_dastore = 82;
  int opc_aastore = 83;
  int opc_bastore = 84;
  int opc_castore = 85;
  int opc_sastore = 86;
  int opc_pop = 87;
  int opc_pop2 = 88;
  int opc_dup = 89;
  int opc_dup_x1 = 90;
  int opc_dup_x2 = 91;
  int opc_dup2 = 92;
  int opc_dup2_x1 = 93;
  int opc_dup2_x2 = 94;
  int opc_swap = 95;
  int opc_iadd = 96;
  int opc_ladd = 97;
  int opc_fadd = 98;
  int opc_dadd = 99;
  int opc_isub = 100;
  int opc_lsub = 101;
  int opc_fsub = 102;
  int opc_dsub = 103;
  int opc_imul = 104;
  int opc_lmul = 105;
  int opc_fmul = 106;
  int opc_dmul = 107;
  int opc_idiv = 108;
  int opc_ldiv = 109;
  int opc_fdiv = 110;
  int opc_ddiv = 111;
  int opc_irem = 112;
  int opc_lrem = 113;
  int opc_frem = 114;
  int opc_drem = 115;
  int opc_ineg = 116;
  int opc_lneg = 117;
  int opc_fneg = 118;
  int opc_dneg = 119;
  int opc_ishl = 120;
  int opc_lshl = 121;
  int opc_ishr = 122;
  int opc_lshr = 123;
  int opc_iushr = 124;
  int opc_lushr = 125;
  int opc_iand = 126;
  int opc_land = 127;
  int opc_ior = 128;
  int opc_lor = 129;
  int opc_ixor = 130;
  int opc_lxor = 131;
  int opc_iinc = 132;
  int opc_i2l = 133;
  int opc_i2f = 134;
  int opc_i2d = 135;
  int opc_l2i = 136;
  int opc_l2f = 137;
  int opc_l2d = 138;
  int opc_f2i = 139;
  int opc_f2l = 140;
  int opc_f2d = 141;
  int opc_d2i = 142;
  int opc_d2l = 143;
  int opc_d2f = 144;
  int opc_i2b = 145;
  int opc_i2c = 146;
  int opc_i2s = 147;
  int opc_lcmp = 148;
  int opc_fcmpl = 149;
  int opc_fcmpg = 150;
  int opc_dcmpl = 151;
  int opc_dcmpg = 152;
  int opc_ifeq = 153;
  int opc_ifne = 154;
  int opc_iflt = 155;
  int opc_ifge = 156;
  int opc_ifgt = 157;
  int opc_ifle = 158;
  int opc_if_icmpeq = 159;
  int opc_if_icmpne = 160;
  int opc_if_icmplt = 161;
  int opc_if_icmpge = 162;
  int opc_if_icmpgt = 163;
  int opc_if_icmple = 164;
  int opc_if_acmpeq = 165;
  int opc_if_acmpne = 166;
  int opc_goto = 167;
  int opc_jsr = 168;
  int opc_ret = 169;
  int opc_tableswitch = 170;
  int opc_lookupswitch = 171;
  int opc_ireturn = 172;
  int opc_lreturn = 173;
  int opc_freturn = 174;
  int opc_dreturn = 175;
  int opc_areturn = 176;
  int opc_return = 177;
  int opc_getstatic = 178;
  int opc_putstatic = 179;
  int opc_getfield = 180;
  int opc_putfield = 181;
  int opc_invokevirtual = 182;
  int opc_invokespecial = 183;
  int opc_invokestatic = 184;
  int opc_invokeinterface = 185;
  int opc_xxxunusedxxx = 186;
  int opc_new = 187;
  int opc_newarray = 188;
  int opc_anewarray = 189;
  int opc_arraylength = 190;
  int opc_athrow = 191;
  int opc_checkcast = 192;
  int opc_instanceof = 193;
  int opc_monitorenter = 194;
  int opc_monitorexit = 195;
  int opc_wide = 196;
  int opc_multianewarray = 197;
  int opc_ifnull = 198;
  int opc_ifnonnull = 199;
  int opc_goto_w = 200;
  int opc_jsr_w = 201;
}
