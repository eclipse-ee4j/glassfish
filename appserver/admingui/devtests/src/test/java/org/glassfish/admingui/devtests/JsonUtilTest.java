/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.devtests;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.glassfish.admingui.common.util.JSONUtil;
import static org.junit.Assert.*;

public class JsonUtilTest {
    @Test
    public void readNumbers() {
         assertEquals(3L, JSONUtil.jsonToJava("3"));
    }
    
    @Test 
    public void readConstants() {
        assertTrue((Boolean)JSONUtil.jsonToJava("true"));
        assertFalse((Boolean)JSONUtil.jsonToJava("false"));
        assertNull(JSONUtil.jsonToJava("null"));
    }
    
    @Test
    public void readLists() {
        List list = (List)JSONUtil.jsonToJava("[null]");
        assertNotNull(list);
        assertEquals(1, list.size());
        assertNull(list.get(0));
        
        list = (List)JSONUtil.jsonToJava("[true,false,null,{'a':'b'},['1','2','3']]");
        assertNotNull(list);
        assertEquals(5, list.size());
        assertTrue((Boolean)list.get(0));
        assertFalse((Boolean)list.get(1));
        assertNull(list.get(2));
        assertTrue(list.get(3) instanceof Map);
        assertEquals("b", ((Map)list.get(3)).get("a"));
        assertTrue(list.get(4) instanceof List);
        assertEquals("1", ((List)list.get(4)).get(0));

        list = (List)JSONUtil.jsonToJava("[true,false,null,{'a':'b'},[1,2,3]]");
        assertNotNull(list);
        assertEquals(5, list.size());
        assertEquals(1L, ((List)list.get(4)).get(0));

        list = (List)JSONUtil.jsonToJava("[true,false,null,{'a':'b'},[1.1,2.2,3.3]]");
        assertNotNull(list);
        assertEquals(5, list.size());
        assertEquals(1.1F, ((List)list.get(4)).get(0));

        list = (List)JSONUtil.jsonToJava("[true,false,null,{'a':'b'},['1',2,3.3]]");
        assertNotNull(list);
        assertEquals(5, list.size());
        assertEquals("1", ((List)list.get(4)).get(0));
        assertEquals(2L, ((List)list.get(4)).get(1));
        assertEquals(3.3F, ((List)list.get(4)).get(2));
    }
    
    @Test
    public void readObjects() {
        Map<String, Object> map = (Map<String, Object>)JSONUtil.jsonToJava("{'x':['foo',null ,{'a':true, 'b':false }]}");
        assertEquals(1, map.size());
        assertTrue(map.get("x") instanceof List);
        assertEquals(3, ((List)map.get("x")).size());
        assertTrue(((List)map.get("x")).get(2) instanceof Map);
        
        map = (Map<String, Object>)JSONUtil.jsonToJava("{            'key'   :        \"value\" ,\n  \r \"key2\"   :   {  'innerKey'  : [  3.3E-2 , false  , 800e+8, null , 37  , \"test\" ] , \n \"innerKey2\" : {'a' : 'b', 'c' : 'd'}, 'innerKey3' : true} }");
        assertEquals(2, map.size());
        assertEquals("value", map.get("key"));
        assertTrue(map.get("key2") instanceof Map);
    }
    
    @Test
    public void testEncoding() {
        Map map = new HashMap<String, String>() {{ put("foo", "bar"); }};
        assertEquals("{\"foo\":\"bar\"}", JSONUtil.javaToJSON(map, 2));
    }
    
    @Test
    public void multibyteCharacters() {
        String json = "{\"value\":\"這或是因\"}";
        Map<String, Object> obj = (Map<String, Object>)JSONUtil.jsonToJava(json);
        assertEquals("這或是因", obj.get("value"));
    }
}
