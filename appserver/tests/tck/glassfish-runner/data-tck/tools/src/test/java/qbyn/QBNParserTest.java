/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package qbyn;

import ee.jakarta.tck.data.tools.qbyn.ParseUtils;
import ee.jakarta.tck.data.tools.qbyn.QueryByNameInfo;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ee.jakarta.tck.data.tools.antlr.QBNLexer;
import ee.jakarta.tck.data.tools.antlr.QBNParser;
import ee.jakarta.tck.data.tools.antlr.QBNBaseListener;

import java.io.IOException;

public class QBNParserTest {
    // Some of these are not actual query by name examples even though they follow the pattern
    String actionExamples = """
        findByHexadecimalContainsAndIsControlNot
        findByDepartmentCountAndPriceBelow
        countByHexadecimalNotNull
        existsByThisCharacter
        findByDepartmentsContains
        findByDepartmentsEmpty
        findByFloorOfSquareRootNotAndIdLessThanOrderByBitsRequiredDesc
        findByFloorOfSquareRootOrderByIdAsc
        findByHexadecimalIgnoreCase
        findByHexadecimalIgnoreCaseBetweenAndHexadecimalNotIn
        findById
        findByIdBetween
        findByIdBetweenOrderByNumTypeAsc
        findByIdGreaterThanEqual
        findByIdIn
        findByIdLessThan
        findByIdLessThanEqual
        findByIdLessThanOrderByFloorOfSquareRootDesc
        findByIsControlTrueAndNumericValueBetween
        findByIsOddFalseAndIdBetween
        findByIsOddTrueAndIdLessThanEqualOrderByIdDesc
        findByNameLike
        findByNumTypeAndFloorOfSquareRootLessThanEqual
        findByNumTypeAndNumBitsRequiredLessThan
        findByNumTypeInOrderByIdAsc
        findByNumTypeNot
        findByNumTypeOrFloorOfSquareRoot
        findByNumericValue
        findByNumericValueBetween
        findByNumericValueLessThanEqualAndNumericValueGreaterThanEqual
        findFirst3ByNumericValueGreaterThanEqualAndHexadecimalEndsWith
        findFirstByHexadecimalStartsWithAndIsControlOrderByIdAsc
        findByPriceNotNullAndPriceLessThanEqual
        findByPriceNull
        findByProductNumLike
        """;

    /**
     * Test the parser using a local QBNBaseListener implementation
     * @throws IOException
     */
    @Test
    public void testQueryByNameExamples() throws IOException {
        String[] examples = actionExamples.split("\n");
        for (String example : examples) {
            System.out.println(example);
            CodePointCharStream input = CharStreams.fromString(example); // create a lexer that feeds off of input CharStream
            QBNLexer lexer = new QBNLexer(input); // create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer); // create a parser that feeds off the tokens buffer
            QBNParser parser = new QBNParser(tokens);
            QueryByNameInfo info = new QueryByNameInfo();
            parser.addParseListener(new QBNBaseListener() {
                @Override
                public void exitPredicate(QBNParser.PredicateContext ctx) {
                    int count = ctx.condition().size();
                    for (int i = 0; i < count; i++) {
                        QBNParser.ConditionContext cctx = ctx.condition(i);
                        String property = cctx.property().getText();
                        QueryByNameInfo.Operator operator = QueryByNameInfo.Operator.EQUAL;
                        if(cctx.operator() != null) {
                            operator = QueryByNameInfo.Operator.valueOf(cctx.operator().getText().toUpperCase());
                        }
                        boolean ignoreCase = cctx.ignore_case() != null;
                        boolean not = cctx.not() != null;
                        boolean and = false;
                        if(i > 0) {
                            // The AND/OR is only present if there is more than one condition
                            and = ctx.AND(i-1) != null;
                        }
                        // String property, Operator operator, boolean ignoreCase, boolean not, boolean and
                        info.addCondition(property, operator, ignoreCase, not, and);
                    }
                }

                @Override
                public void exitFind_query(QBNParser.Find_queryContext ctx) {
                    System.out.println("find: " + ctx.find().getText());
                    if(ctx.limit() != null) {
                        System.out.println("find_expression.INTEGER: " + ctx.limit().INTEGER());
                        int findCount = 0;
                        if(ctx.limit().INTEGER() != null) {
                            findCount = Integer.parseInt(ctx.limit().INTEGER().getText());
                        }
                        info.setFindExpressionCount(findCount);
                        if(ctx.ignored_text() != null) {
                            info.setIgnoredText(ctx.ignored_text().getText());
                        }
                    }
                }

                @Override
                public void exitAction_query(QBNParser.Action_queryContext ctx) {
                    QueryByNameInfo.Action action = QueryByNameInfo.Action.valueOf(ctx.action().getText().toUpperCase());
                    info.setAction(action);
                    if(ctx.ignored_text() != null) {
                        info.setIgnoredText(ctx.ignored_text().getText());
                    }
                }

                @Override
                public void exitOrder(QBNParser.OrderContext ctx) {
                    int count = ctx.order_item().size();
                    if(ctx.property() != null) {
                        String property = ctx.property().getText();
                        info.addOrderBy(property, QueryByNameInfo.OrderBySortDirection.NONE);
                    }
                    for (int i = 0; i < count; i++) {
                        QBNParser.Order_itemContext octx = ctx.order_item(i);
                        String property = octx.property().getText();
                        QueryByNameInfo.OrderBySortDirection direction = octx.ASC() != null ? QueryByNameInfo.OrderBySortDirection.ASC : QueryByNameInfo.OrderBySortDirection.DESC;
                        info.addOrderBy(property, direction);
                    }
                }
            });
            ParseTree tree = parser.query_method();
            // print LISP-style tree for the
            System.out.println(tree.toStringTree(parser));
            // Print out the parsed QueryByNameInfo
            System.out.println(info);

        }
    }

    /**
     * Test the parser using the ParseUtils class
     */
    @Test
    public void testParseUtils() {
        String[] examples = actionExamples.split("\n");
        for (String example : examples) {
            System.out.println(example);
            QueryByNameInfo info = ParseUtils.parseQueryByName(example);
            System.out.println(info);
        }
    }

    @Test
    /** Should produce:
    @Query("where floorOfSquareRoot <> ?1 and id < ?2")
    @OrderBy("numBitsRequired", descending = true)
     */
    public void test_findByFloorOfSquareRootNotAndIdLessThanOrderByNumBitsRequiredDesc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByFloorOfSquareRootNotAndIdLessThanOrderByNumBitsRequiredDesc");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where floorOfSquareRoot <> ?1 and id < ?2", query);
        Assertions.assertEquals(1, info.getOrderBy().size());
        Assertions.assertEquals("numBitsRequired", info.getOrderBy().get(0).property);
        Assertions.assertEquals(QueryByNameInfo.OrderBySortDirection.DESC, info.getOrderBy().get(0).direction);
    }

    /** Should produce
     @Query("where isOdd=true and id <= ?1")
     @OrderBy(value = "id", descending = true)
     */
    @Test
    public void test_findByIsOddTrueAndIdLessThanEqualOrderByIdDesc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByIsOddTrueAndIdLessThanEqualOrderByIdDesc");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where isOdd=true and id <= ?1", query);
        Assertions.assertTrue(info.getOrderBy().size() == 1);
        Assertions.assertEquals("id", info.getOrderBy().get(0).property);
        Assertions.assertEquals(QueryByNameInfo.OrderBySortDirection.DESC, info.getOrderBy().get(0).direction);
    }
    /** Should produce
     @Query("where isOdd=false and id between ?1 and ?2")
     */
    @Test
    public void test_findByIsOddFalseAndIdBetween() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByIsOddFalseAndIdBetween");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where isOdd=false and id between ?1 and ?2", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }
    /** Should produce
     @Query("where numType in ?1 order by id asc")
     */
    @Test
    public void test_findByNumTypeInOrderByIdAsc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNumTypeInOrderByIdAsc");
        String query = ParseUtils.toQuery(info, ParseUtils.ToQueryOptions.INCLUDE_ORDER_BY);
        System.out.println(query);
        Assertions.assertEquals("where numType in ?1 order by id asc", query);
        Assertions.assertEquals(1, info.getOrderBy().size());
        Assertions.assertEquals(QueryByNameInfo.OrderBySortDirection.ASC, info.getOrderBy().get(0).direction);
    }

    /** Should produce
     @Query("where numType = ?1 or floorOfSquareRoot = ?2")
     */
    @Test
    public void test_findByNumTypeOrFloorOfSquareRoot() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNumTypeOrFloorOfSquareRoot");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where numType = ?1 or floorOfSquareRoot = ?2", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    /** Should produce
     @Query("where numType <> ?1")
     */
    @Test
    public void test_findByNumTypeNot() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNumTypeNot");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where numType <> ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("where numType = ?1 and numBitsRequired < ?2")
     */
    @Test
    public void test_findByNumTypeAndNumBitsRequiredLessThan() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNumTypeAndNumBitsRequiredLessThan");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where numType = ?1 and numBitsRequired < ?2", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("where id between ?1 and ?2")
     */
    @Test
    public void test_findByIdBetweenOrderByNumTypeAsc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByIdBetweenOrderByNumTypeAsc");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where id between ?1 and ?2", query);
        Assertions.assertEquals(1, info.getOrderBy().size());
        Assertions.assertEquals(QueryByNameInfo.OrderBySortDirection.ASC, info.getOrderBy().get(0).direction);
    }


    /** Should produce
     @Query("where lower(hexadecimal) between lower(?1) and lower(?2) and hexadecimal not in ?3")
     */
    @Test
    public void test_findByHexadecimalIgnoreCaseBetweenAndHexadecimalNotIn() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByHexadecimalIgnoreCaseBetweenAndHexadecimalNotIn");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where lower(hexadecimal) between lower(?1) and lower(?2) and hexadecimal not in ?3", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    /** Should produce
     @Query("where numericValue >= ?1 and right(hexadecimal, length(?2)) = ?2")
     */
    @Test
    public void test_findByNumericValueGreaterThanEqualAndHexadecimalEndsWith() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNumericValueGreaterThanEqualAndHexadecimalEndsWith");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where numericValue >= ?1 and right(hexadecimal, length(?2)) = ?2", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }
    /** Should produce
     @Query("where left(hexadecimal, length(?1)) = ?1 and isControl = ?2 order by id asc")
     */
    @Test
    public void test_findByHexadecimalStartsWithAndIsControlOrderByIdAsc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByHexadecimalStartsWithAndIsControlOrderByIdAsc");
        String query = ParseUtils.toQuery(info, ParseUtils.ToQueryOptions.INCLUDE_ORDER_BY);
        System.out.println(query);
        Assertions.assertEquals("where left(hexadecimal, length(?1)) = ?1 and isControl = ?2 order by id asc", query);
        Assertions.assertEquals(1, info.getOrderBy().size());
    }

    /** Should produce
     @Query("where name like ?1")
     */
    @Test
    public void test_findByNameLike() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByNameLike");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where name like ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("where hexadecimal like '%'||?1||'%' and isControl <> ?2")
     */
    @Test
    public void test_findByHexadecimalContainsAndIsControlNot() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByHexadecimalContainsAndIsControlNot");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where hexadecimal like '%'||?1||'%' and isControl <> ?2", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("where price is not null and price <= ?1")
     */
    @Test
    public void test_findByPriceNotNullAndPriceLessThanEqual() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByPriceNotNullAndPriceLessThanEqual");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where price is not null and price <= ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    /** Should produce
     @Query("where price is null")
     */
    @Test
    public void test_findByPriceNull() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByPriceNull");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where price is null", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("where departments is empty")
     */
    @Test
    public void test_findByDepartmentsEmpty() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findByDepartmentsEmpty");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where departments is empty", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    @Test
    public void test_countBy() {
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            QueryByNameInfo info = ParseUtils.parseQueryByName("countBy");
        });
        Assertions.assertNotNull(ex, "parse of countBy should fail");
    }

    /** Should produce
     @Query("delete Product where productNum like ?1")
     */
    @Test
    public void test_deleteByProductNumLike() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("deleteByProductNumLike");
        info.setEntity("Product");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("delete Product where productNum like ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());

    }
    /** Should produce
     @Query("delete Product where productNum like ?1")
     */
    @Test
    public void test_deleteByProductNumLikeNoFQN() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("deleteByProductNumLike");
        info.setEntity("com.example.Product");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("delete Product where productNum like ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());

    }

    /** Should produce
     @Query("select count(this)>0 where thisCharacter = ?1")
     */
    @Test
    public void test_existsByThisCharacter() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("existsByThisCharacter");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this)>0 where thisCharacter = ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    /** Should produce
     @Query("select count(this) where hexadecimal is not null")
     */
    @Test
    public void test_countByHexadecimalNotNull() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("countByHexadecimalNotNull");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this) where hexadecimal is not null", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("select count(this) where id(this) < ?1")
     */
    @Test
    @Disabled("Disabled until id refs are fixed")
    public void test_countByIdLessThan() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("countByIdLessThan");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this) where id(this) < ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    /** Should produce
     @Query("select count(this)>0 where id in ?1")
     */
    @Test
    public void test_existsByIdIn() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("existsByIdIn");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this)>0 where id in ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }


    /** Should produce
     @Query("select count(this)>0 where id > ?1")
     */
    @Test
    public void test_existsByIdGreaterThan() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("existsByIdGreaterThan");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this)>0 where id > ?1", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    @Test
    public void test_findFirstNameByIdInOrderByAgeDesc() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findFirstXxxxxByIdInOrderByAgeDesc");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where id in ?1 order by '' limit 1", query);
        Assertions.assertEquals(1, info.getOrderBy().size());
    }

    @Test
    public void test_findFirst3ByNumericValueGreaterThanEqualAndHexadecimalEndsWith() {
        QueryByNameInfo info = ParseUtils.parseQueryByName("findFirst3ByNumericValueGreaterThanEqualAndHexadecimalEndsWith");
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("where numericValue >= ?1 and right(hexadecimal, length(?2)) = ?2 order by '' limit 3", query);
        Assertions.assertEquals(0, info.getOrderBy().size());
    }

    @Test
    public void test_countByByHand() {
        QueryByNameInfo info = new QueryByNameInfo();
        info.setAction(QueryByNameInfo.Action.COUNT);
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this)", query);
    }

    /**
     * Test the countBy method with an int return type is cast to an integer
     */
    @Test
    public void test_countByByHandIntReturn() {
        QueryByNameInfo info = new QueryByNameInfo();
        info.setAction(QueryByNameInfo.Action.COUNT);
        String query = ParseUtils.toQuery(info, ParseUtils.ToQueryOptions.CAST_COUNT_TO_INTEGER);
        System.out.println(query);
        Assertions.assertEquals("select cast(count(this) as Integer)", query);
    }

    /**
     * Test the countBy method with a long return type is cast to an integer
     */
    @Test
    public void test_countByByHandLongReturn() {
        QueryByNameInfo info = new QueryByNameInfo();
        info.setAction(QueryByNameInfo.Action.COUNT);
        String query = ParseUtils.toQuery(info, ParseUtils.ToQueryOptions.CAST_LONG_TO_INTEGER);
        System.out.println(query);
        Assertions.assertEquals("select count(this) as Integer", query);
    }

    @Test
    public void testExistsBy() {
        QueryByNameInfo info = new QueryByNameInfo();
        info.setAction(QueryByNameInfo.Action.EXISTS);
        String query = ParseUtils.toQuery(info);
        System.out.println(query);
        Assertions.assertEquals("select count(this)>0", query);
    }
}
