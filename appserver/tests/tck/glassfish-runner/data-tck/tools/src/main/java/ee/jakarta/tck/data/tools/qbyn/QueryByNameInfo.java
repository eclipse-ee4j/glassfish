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
package ee.jakarta.tck.data.tools.qbyn;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of the information parsed from a Query by name method name
 * using the BNF grammar defined in QBN.g4
 */
public class QueryByNameInfo {
    /**
     *  The support &lt;action&gt; types
     */
    public enum Action {
        // find | delete | update | count | exists
        FIND, DELETE, UPDATE, COUNT, EXISTS, NONE
    }

    /**
     * The support &lt;operator&gt; types
     */
    public enum Operator {
        CONTAINS("%||...||%"), ENDSWITH("right(...)"), STARTSWITH("left(...)"), LESSTHAN(" <"), LESSTHANEQUAL(" <="),
        GREATERTHAN(" >"), GREATERTHANEQUAL(" >="), BETWEEN(" between", 2) , EMPTY(" empty") ,
        LIKE(" like") , IN(" in") , NULL(" null", 0),  TRUE("=true", 0) ,
        FALSE("=false", 0), EQUAL(" =")
        ;
        private Operator(String jdql) {
            this(jdql, 1);
        }
        private Operator(String jdql, int parameters) {
            this.jdql = jdql;
            this.parameters = parameters;
        }
        private String jdql;
        private int parameters = 0;
        public String getJDQL() {
            return jdql;
        }
        public int parameters() {
            return parameters;
        }
    }
    public enum OrderBySortDirection {
        ASC, DESC, NONE
    }

    /**
     * A &lt;condition&gt; in the &lt;predicate&gt; statement
     */
    public static class Condition {
        // an entity property name
        String property;
        // the operator to apply to the property
        Operator operator = Operator.EQUAL;
        // is the condition case-insensitive
        boolean ignoreCase;
        // is the condition negated
        boolean not;
        // for multiple conditions, is this condition joined by AND(true) or OR(false)
        boolean and;
    }

    /**
     * A &lt;order-item&gt; or &lt;property&gt; in the &lt;order-clause&gt;
     */
    public static class OrderBy {
        // an entity property name
        public String property;
        // the direction to sort the property
        public OrderBySortDirection direction = OrderBySortDirection.NONE;

        public OrderBy() {
        }
        public OrderBy(String property, OrderBySortDirection direction) {
            this.property = property;
            this.direction = direction;
        }
        public boolean isDescending() {
            return direction == OrderBySortDirection.DESC;
        }
    }
    private Action action = Action.NONE;
    private List<Condition> predicates = new ArrayList<>();
    private List<OrderBy> orderBy = new ArrayList<>();
    // > 0 means find expression exists
    int findExpressionCount = -1;
    String ignoredText;
    // The entity FQN name
    String entity;

    /**
     * The entity FQN
     * @return entity FQN
     */
    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getSimpleName() {
        String simpleName = entity;
        int lastDot = entity.lastIndexOf('.');
        if(lastDot >= 0) {
            simpleName = entity.substring(lastDot + 1);
        }
        return simpleName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<Condition> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<Condition> predicates) {
        this.predicates = predicates;
    }
    public List<Condition> addCondition(Condition condition) {
        this.predicates.add(condition);
        return this.predicates;
    }
    public List<Condition> addCondition(String property, Operator operator, boolean ignoreCase, boolean not, boolean and) {
        Condition c = new Condition();
        c.property = property;
        c.operator = operator;
        c.ignoreCase = ignoreCase;
        c.not = not;
        c.and = and;
        this.predicates.add(c);
        return this.predicates;
    }

    public int getFindExpressionCount() {
        return findExpressionCount;
    }

    public void setFindExpressionCount(int findExpressionCount) {
        this.findExpressionCount = findExpressionCount;
    }

    public String getIgnoredText() {
        return ignoredText;
    }
    public void setIgnoredText(String ignoredText) {
        this.ignoredText = ignoredText;
    }

    public List<OrderBy> getOrderBy() {
        return orderBy;
    }
    public void setOrderBy(List<OrderBy> orderBy) {
        this.orderBy = orderBy;
    }
    public List<OrderBy> addOrderBy(OrderBy orderBy) {
        this.orderBy.add(orderBy);
        return this.orderBy;
    }
    public List<OrderBy> addOrderBy(String property, OrderBySortDirection direction) {
        OrderBy ob = new OrderBy();
        ob.property = property;
        ob.direction = direction;
        this.orderBy.add(ob);
        return this.orderBy;
    }

    /**
     * Returns a string representation of the parsed query by name method
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        // Subject
        if(action != Action.NONE) {
            sb.append(action.name().toLowerCase());
        } else {
            sb.append("findFirst");
            if(findExpressionCount > 0) {
                sb.append(findExpressionCount);
            }
        }
        if(ignoredText != null && !ignoredText.isEmpty()) {
            sb.append(ignoredText);
        }
        // Predicates
        boolean first = true;
        if(!predicates.isEmpty()) {
            sb.append("By");
            for(Condition c : predicates) {
                // Add the join condition
                if(!first) {
                    sb.append(c.and ? "AND" : "OR");
                }
                sb.append('(');
                sb.append(c.property);
                sb.append(' ');
                if(c.ignoreCase) {
                    sb.append("IgnoreCase");
                }
                if(c.not) {
                    sb.append("NOT");
                }
                if(c.operator != Operator.EQUAL) {
                    sb.append(c.operator.name().toUpperCase());
                }
                sb.append(')');
                first = false;
            }
            sb.append(')');
        }
        // OrderBy
        if(!orderBy.isEmpty()) {
            sb.append("(OrderBy ");
            for(OrderBy ob : orderBy) {
                sb.append('(');
                sb.append(ob.property);
                sb.append(' ');
                if(ob.direction != OrderBySortDirection.NONE) {
                    sb.append(ob.direction.name().toUpperCase());
                }
                sb.append(')');
            }
            sb.append(')');
        }
        sb.append(')');
        return sb.toString();
    }

}
