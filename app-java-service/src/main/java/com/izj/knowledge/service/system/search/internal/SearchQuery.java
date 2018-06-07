package com.knowledge.hoge.connect.service.system.search.internal;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.amazonaws.services.cloudsearchdomain.model.QueryParser;
import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * QueryBuilder for {@link QueryParser#Lucene} / {@link QueryParser#Structured}
 * 
 * <pre>
 * [Usage]
 * 
 * QueryBuilder query = QueryBuilder.of(QueryParser.Structured, QueryOpe.AND);
 * 
 * query.addValueWithField("A1", "A2");
 * query.addNotValueWithField("B1", "B2");
 * 
 * QueryBuilder subQuery = query.createSubQuery(QueryOpe.OR);
 * subQuery.addValueWithField("C1", "C2");
 * subQuery.addNotValueWithField("D1", "D2");
 * 
 * query.addValueWithField("E1", "E2");
 * 
 * query.build();
 * </pre>
 * 
 * 
 * @author ~~~~
 *
 */
@RequiredArgsConstructor
public class SearchQuery {

    public static interface QueryNode {
        boolean isValue();

        boolean isEmpty();
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryBuilder implements QueryNode {
        public static enum QueryOpe {
            AND,
            OR,
        }

        private final QueryParser parser;
        private final QueryOpe ope;
        private final List<QueryNode> children;

        /**
         * true if not use brackets to value.
         */
        private boolean withoutBrackets = false;

        @Override
        public boolean isValue() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return children.isEmpty();
        }

        public static QueryBuilder of(QueryParser parser, QueryOpe ope) {
            return new QueryBuilder(parser, ope, Lists.newArrayList());
        }

        public QueryBuilder createSubQuery(QueryOpe ope) {
            QueryBuilder subQuery = QueryBuilder.of(parser, ope);
            children.add(subQuery);
            return subQuery;
        }

        public QueryBuilder withoutBrackets() {
            this.withoutBrackets = true;
            return this;
        }

        public QueryBuilder addValue(Object value) {
            Assert.isTrue(parser != QueryParser.Structured,
                    "Structured Query value must have field name, Please use addValueWithField().");

            if (Objects.isNull(value)) {
                return this;
            }

            String str = value.toString();
            if (StringUtils.isNotBlank(str)) {
                children.add(QueryValue.of(str, withoutBrackets));
            }

            return this;
        }

        public QueryBuilder addNotValue(Object value) {
            Assert.isTrue(parser != QueryParser.Structured,
                    "Structured Query value must have field name, Please use addNotValueWithField().");

            if (Objects.isNull(value)) {
                return this;
            }

            String str = value.toString();
            if (StringUtils.isNotBlank(str)) {
                children.add(QueryValue.ofNot(str, withoutBrackets));
            }

            return this;
        }

        public QueryBuilder addValueWithField(String field, Object value) {
            Assert.isTrue(StringUtils.isNotBlank(field), "Field name should not blank.");

            if (Objects.isNull(value)) {
                return this;
            }

            String str = value.toString();
            if (StringUtils.isNotBlank(str)) {
                children.add(QueryValue.of(field, str, withoutBrackets));
            }

            return this;
        }

        public QueryBuilder addNotValueWithField(String field, Object value) {
            Assert.isTrue(StringUtils.isNotBlank(field), "Field name should not blank.");

            if (Objects.isNull(value)) {
                return this;
            }

            String str = value.toString();
            if (StringUtils.isNotBlank(str)) {
                children.add(QueryValue.ofNot(field, str, withoutBrackets));
            }

            return this;
        }

        public String build() {
            switch (parser) {
            case Lucene:
                return toLuceneQuery(this).toString().replaceAll("(^\\(|\\)$)", "");
            case Structured:
                return toStructuredQuery(this).toString();
            default:
                throw new NotImplementedException(parser + " is not implemented.");
            }
        }
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QueryValue implements QueryNode {
        private final boolean not;
        private final String field;
        private final String value;
        private final boolean withoutBrackets;

        @Override
        public boolean isValue() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return StringUtils.isEmpty(value);
        }

        public static QueryValue of(String value, boolean withoutBrackets) {
            return new QueryValue(false, null, value, withoutBrackets);
        }

        public static QueryValue ofNot(String value, boolean withoutBrackets) {
            return new QueryValue(true, null, value, withoutBrackets);
        }

        public static QueryValue of(String field, String value, boolean withoutBrackets) {
            return new QueryValue(false, field, value, withoutBrackets);
        }

        public static QueryValue ofNot(String field, String value, boolean withoutBrackets) {
            return new QueryValue(true, field, value, withoutBrackets);
        }
    }

    private static StringBuilder toLuceneQuery(QueryBuilder node) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");

        boolean first = true;
        for (QueryNode child : node.getChildren()) {
            if (child.isEmpty()) {
                continue;
            }

            if (first) {
                first = false;
            } else {
                sb.append(" ");
                sb.append(node.getOpe().name());
                sb.append(" ");
            }

            if (child.isValue()) {
                sb.append(toLuceneQuery((QueryValue)child));
            } else {
                sb.append(toLuceneQuery((QueryBuilder)child));
            }
        }

        sb.append(")");

        return sb;
    }

    private static StringBuilder toLuceneQuery(QueryValue node) {
        StringBuilder sb = new StringBuilder();

        sb.append(node.isNot() ? "NOT " : "");
        sb.append(StringUtils.isNotBlank(node.getField()) ? node.getField() + ":" : "");
        sb.append(node.getValue());

        return sb;
    }

    private static StringBuilder toStructuredQuery(QueryBuilder node) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        sb.append(node.getOpe().name().toLowerCase());

        for (QueryNode child : node.getChildren()) {
            if (child.isEmpty()) {
                continue;
            }

            sb.append(" ");

            if (child.isValue()) {
                sb.append(toStructuredQuery((QueryValue)child));
            } else {
                sb.append(toStructuredQuery((QueryBuilder)child));
            }
        }

        sb.append(")");

        return sb;
    }

    private static StringBuilder toStructuredQuery(QueryValue node) {
        StringBuilder sb = new StringBuilder();

        QueryValue value = node;
        Assert.isTrue(StringUtils.isNotBlank(value.getField()), "Structured Query node must have field name.");

        sb.append(value.isNot() ? "(not " : "");
        sb.append(value.getField());
        sb.append(":");
        sb.append(value.isWithoutBrackets() ? "" : "'");
        sb.append(value.getValue());
        sb.append(value.isWithoutBrackets() ? "" : "'");
        sb.append(value.isNot() ? ")" : "");

        return sb;
    }
}
