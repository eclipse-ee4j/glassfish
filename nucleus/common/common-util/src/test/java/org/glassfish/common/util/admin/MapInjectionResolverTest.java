/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.common.util.admin;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.glassfish.api.ExecutionContext;
import org.glassfish.api.Param;
import org.glassfish.api.ParamDefaultCalculator;
import org.glassfish.api.admin.CommandModel;
import org.glassfish.api.admin.ParameterMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * junit test to test MapInjectionResolver class
 */
public class MapInjectionResolverTest {

    @Test
    public void getParameterValueTest() {

        ParameterMap params = new ParameterMap();
        params.set("foo", "bar");
        params.set("hellO", "world");
        params.set("one", "two");
        params.set("thrEE", "Four");
        params.set("FivE", "six");
        params.set("sIx", "seVen");
        params.set("eiGHT", "niNe");
        String value = MapInjectionResolver.getParameterValue(params, "foo", false);
        assertEquals("bar", value, "value is bar");
        value = MapInjectionResolver.getParameterValue(params, "hello", true);
        assertEquals("world", value, "value is world");
        value = MapInjectionResolver.getParameterValue(params, "onE", true);
        assertEquals("two", value, "value is two");
        value = MapInjectionResolver.getParameterValue(params, "three", true);
        assertEquals("Four", value, "value is four");
        value = MapInjectionResolver.getParameterValue(params, "five", false);
        assertEquals(null, value, "value is null");
        value = MapInjectionResolver.getParameterValue(params, "six", true);
        assertEquals("seVen", value, "value is SeVen");
        value = MapInjectionResolver.getParameterValue(params, "eight", true);
        assertEquals("niNe", value, "value is niNe");
        value = MapInjectionResolver.getParameterValue(params, "none", true);
        assertEquals(null, value, "value is null");
    }

    @Test
    public void convertStringToPropertiesTest() {
        String propsStr = "prop1=valA:prop2=valB:prop3=valC";
        Properties propsExpected = new Properties();
        propsExpected.put("prop1", "valA");
        propsExpected.put("prop2", "valB");
        propsExpected.put("prop3", "valC");
        Properties propsActual = MapInjectionResolver.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }

    @Test
    public void parsePropertiesEscapeCharTest() {
        String propsStr = "connectionAttributes=\\;create\\\\\\=true";
        Properties propsExpected = new Properties();
        propsExpected.put("connectionAttributes", ";create\\=true");
        Properties propsActual = null;
        propsActual = MapInjectionResolver.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }

    @Test
    public void parsePropertiesEscapeCharTest2() {
        String propsStr = "connectionAttributes=;create\\=true";
        Properties propsExpected = new Properties();
        propsExpected.put("connectionAttributes", ";create=true");
        Properties propsActual = null;
        propsActual = MapInjectionResolver.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }

    @Test
    public void parsePropertiesQuoteTest() {
        String propsStr =
            "java.naming.provider.url=\"ldap://ldapserver.sun.com:389\":" +
            "java.naming.security.authentication=simple:" +
            "java.naming.security.credentials=changeit:" +
            "java.naming.security.principal=\"uid=admin,ou=People," +
                "o=foo,o=something\"";
        Properties propsExpected = new Properties();
        propsExpected.put("java.naming.provider.url", "ldap://ldapserver.sun.com:389");
        propsExpected.put("java.naming.security.authentication", "simple");
        propsExpected.put("java.naming.security.credentials", "changeit");
        propsExpected.put("java.naming.security.principal", "uid=admin,ou=People,o=foo,o=something");
        Properties propsActual = MapInjectionResolver.convertStringToProperties(propsStr, ':');
        assertEquals(propsExpected, propsActual);
    }

    @Test
    public void convertStringToObjectTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        AnnotatedElement target = cl.getDeclaredField("foo");
        String paramValueStr = "prop1=valA:prop2=valB:prop3=valC";
        Object paramValActual = MapInjectionResolver.convertStringToObject(target, String.class, paramValueStr);
        Object paramValExpected =  "prop1=valA:prop2=valB:prop3=valC";
        assertEquals(paramValExpected, paramValActual, "String type");

        target = cl.getDeclaredField("prop");
        paramValActual = MapInjectionResolver.convertStringToObject(target, Properties.class, paramValueStr);
        paramValExpected = new Properties();
        ((Properties) paramValExpected).put("prop1", "valA");
        ((Properties) paramValExpected).put("prop2", "valB");
        ((Properties) paramValExpected).put("prop3", "valC");
        assertEquals(paramValExpected, paramValActual, "Properties type");

        target = cl.getDeclaredField("portnum");
        paramValueStr = "8080";
        paramValActual = MapInjectionResolver.convertStringToObject(target, Integer.class, paramValueStr);
        paramValExpected = Integer.valueOf(8080);
        assertEquals(paramValExpected, paramValActual, "Integer type");

        paramValueStr = "server1:server2:server3";
        target = cl.getDeclaredField("lstr");
        paramValActual = MapInjectionResolver.convertStringToObject(
                                    target, List.class, paramValueStr);
        List<String> paramValueList = new java.util.ArrayList();
        paramValueList.add("server1");
        paramValueList.add("server2");
        paramValueList.add("server3");
        assertEquals(paramValueList, paramValActual, "List type");

        paramValueStr = "server1,server2,server3";
        target = cl.getDeclaredField("astr");
        paramValActual = MapInjectionResolver.convertStringToObject(target, (new String[] {}).getClass(), paramValueStr);
        String[] strArray = new String[3];
        strArray[0] = "server1";
        strArray[1] = "server2";
        strArray[2] = "server3";
        assertArrayEquals(strArray, (String[]) paramValActual, "String Array type");
    }

    @Test
    public void convertListToObjectTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        AnnotatedElement target = cl.getDeclaredField("propm");
        List<String> paramValueList = new ArrayList<>();
        paramValueList.add("prop1=valA");
        paramValueList.add("prop2=valB");
        paramValueList.add("prop3=valC");
        Object paramValActual = MapInjectionResolver.convertListToObject(target, Properties.class, paramValueList);
        Object paramValExpected = new Properties();
        ((Properties) paramValExpected).put("prop1", "valA");
        ((Properties) paramValExpected).put("prop2", "valB");
        ((Properties) paramValExpected).put("prop3", "valC");
        assertEquals(paramValExpected, paramValActual, "Properties type");

        paramValueList.clear();
        paramValueList.add("server1");
        paramValueList.add("server2");
        paramValueList.add("server3");
        target = cl.getDeclaredField("lstrm");
        paramValActual = MapInjectionResolver.convertListToObject(target, List.class, paramValueList);
        assertEquals(paramValueList, paramValActual, "List type");

        target = cl.getDeclaredField("astrm");
        paramValActual = MapInjectionResolver.convertListToObject(target, (new String[] {}).getClass(), paramValueList);
        String[] strArray = new String[3];
        strArray[0] = "server1";
        strArray[1] = "server2";
        strArray[2] = "server3";
        assertArrayEquals(strArray, (String[]) paramValActual, "String Array type");
    }


    @Test
    public void getParamValueStringTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        AnnotatedElement ae = cl.getDeclaredField("foo");
        Param param = ae.getAnnotation(Param.class);
        ParameterMap params = new ParameterMap();
        params.set("foo", "true");
        String val = MapInjectionResolver.getParamValueString(params, param, ae, null);
        assertEquals("true", val, "val should be true");

        ae = cl.getDeclaredField("bar");
        param = ae.getAnnotation(Param.class);
        val = MapInjectionResolver.getParamValueString(params, param, ae, null);
        assertEquals("false", val, "val should be false");

        ae = cl.getDeclaredField("hello");
        param = ae.getAnnotation(Param.class);
        val = MapInjectionResolver.getParamValueString(params, param, ae, null);
        assertEquals(null, val, "val should be null");

        ae = cl.getDeclaredField("dyn");
        param = ae.getAnnotation(Param.class);
        val = MapInjectionResolver.getParamValueString(params, param, ae, null);
        assertEquals("dynamic-default-value", val, "val should be dynamic-default-value");
    }

    @Test
    public void getParamFieldTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        AnnotatedElement ae = cl.getDeclaredField("hello");
        Object obj = MapInjectionResolver.getParamField(dc, ae);
        assertEquals("world", obj, "obj should be world");
        ae = cl.getDeclaredField("prop");
        obj = MapInjectionResolver.getParamField(dc, ae);
        assertEquals(null, obj, "obj should be null");

        ae = cl.getDeclaredField("dyn3");
        obj = MapInjectionResolver.getParamField(dc, ae);
        assertEquals("dynamic-default-value", obj, "obj should be dynamic-default-value");
    }


    @Test
    public void getParamValueTest() throws Exception {
        DummyCommand dc = new DummyCommand();
        Class<?> cl = dc.getClass();
        ParameterMap params = new ParameterMap();
        params.add("hello", "world");

        CommandModel dccm = new CommandModelImpl(dc.getClass());
        MapInjectionResolver mir = new MapInjectionResolver(dccm, params);

        AnnotatedElement ae = cl.getDeclaredField("hello");
        String hello = mir.getValue(dc, ae, null, String.class);
        assertEquals("world", hello, "hello should be world");
    }

    @Test
    public void convertStringToListTest() {
        String listStr = "server1\\:server2:\\\\server3:server4";
        List<String> listExpected = new java.util.ArrayList();
        listExpected.add("server1:server2");
        listExpected.add("\\server3");
        listExpected.add("server4");
        List<String> listActual = MapInjectionResolver.convertStringToList(listStr, ':');
        assertEquals(listExpected, listActual);
    }

    @Test
    public void convertStringToStringArrayTest() {
        String strArray = "server1\\,server2,\\\\server3,server4";
        String[] strArrayExpected = new String[3];
        strArrayExpected[0] = "server1,server2";
        strArrayExpected[1] = "\\server3";
        strArrayExpected[2] = "server4";
        String[] strArrayActual = MapInjectionResolver.convertStringToStringArray(strArray, ',');
        assertArrayEquals(strArrayExpected, strArrayActual);
    }

    public static class DynTest extends ParamDefaultCalculator {

        public DynTest() {
        }


        @Override
        public String defaultValue(ExecutionContext ec) {
            return "dynamic-default-value";
        }
    }

    public static class DynCalculator {
        public static String getDefault() {
            return "dynamic-default-value";
        }
    }


    // mock-up DummyCommand object
    public class DummyCommand {
        @Param(name="foo")
        String foo;
        @Param(name="bar", defaultValue="false")
        String bar;
        @Param
        String hello="world";
        @Param
        int portnum;
        @Param(name="prop", separator=':')
        Properties prop;
        @Param(name="lstr", separator=':')
        List<String> lstr;
        @Param(name="astr")
        String[] astr;
        @Param(name="propm", multiple=true)
        Properties propm;
        @Param(name="lstrm", multiple=true)
        List<String> lstrm;
        @Param(name="astrm", multiple=true)
        String[] astrm;

        @Param(name="dyn", optional=true, defaultCalculator=DynTest.class)
        String dyn;

        @Param(optional=true)
        String dyn3 = DynCalculator.getDefault();
    }
}
