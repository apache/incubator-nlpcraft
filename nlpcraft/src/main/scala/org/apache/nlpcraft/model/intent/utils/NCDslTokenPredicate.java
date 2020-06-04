/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model.intent.utils;

import org.apache.commons.collections.*;
import org.apache.commons.lang3.*;
import org.apache.nlpcraft.common.util.*;
import org.apache.nlpcraft.model.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Internal DSL token predicate.
 */
@SuppressWarnings("unchecked")
public class NCDslTokenPredicate implements Function<NCToken, Boolean> {
    private static final List<String> OPS = Arrays.asList(
        // Order is important!
        "==",
        "!=",
        ">=",
        "<=",
        "@@",
        "!@",
        ">",
        "<"
    );

    private static final List<String> PARAMS = Arrays.asList(
        "id",
        "ancestors",
        "parent",
        "groups",
        "value",
        "aliases",
        "startidx",
        "endidx"
    );

    private List<String> parts;
    private String param;
    private String paramFunc;
    private String op;
    private Object value;
    private String valueStr;
    private Function<NCToken, NCToken> qualFunc;

    private boolean validated = false;

    /**
     * Creates new predicate with given parameters.
     *
     * @param parts List of qualification token IDs or aliases.
     * @param paramFunc Optional parameter function. Can be {@code null}.
     * @param param Rule's left-side parameter.
     * @param op Rule's operation.
     * @param value Rule's right-side value.
     */
    public NCDslTokenPredicate(List<String> parts, String paramFunc, String param, String op, Object value) {
        assert parts != null;

        // Assert?
        if (param == null || (param.charAt(0) != '~' && !PARAMS.contains(param)))
            throw new IllegalArgumentException(String.format(
                "Invalid token predicate DSL parameter ('%s') in: %s %s %s",
                param, param, op, value));

        // Assert?
        if (op == null || !OPS.contains(op))
            throw new IllegalArgumentException(String.format(
                "Invalid token predicate DSL operation ('%s') in: %s %s %s",
                op, param, op, value));

        this.parts = new ArrayList<>(parts);
        this.paramFunc = paramFunc;
        this.param = param;
        this.op = op;

        valueStr = null;

        if (value == null)
            valueStr = "null";
        else if (value instanceof Collection)
            valueStr =
                "(" +
                ((Collection<?>)value).stream().map(Object::toString).collect(Collectors.joining(",")) +
                ")";
        else
            valueStr = value.toString();

        if (value instanceof String)
            this.value = stripQuotes((String)value);
        else if (value instanceof Collection)
            this.value = ((Collection<?>)value).stream().map(obj -> {
                if (obj instanceof String) return stripQuotes((String)obj); else return obj;
            }).collect(Collectors.toList());
        else
            this.value = value;

        qualFunc = new NCDslTokenQualifier(this.parts);
    }

    /**
     *
     * @param s
     * @return
     */
    private String stripQuotes(String s) {
        boolean start = s.charAt(0) == '\'';
        boolean end = s.charAt(s.length() - 1) == '\'';

        return start && end ? s.substring(1, s.length() - 1) : s;
    }

    /**
     *
     */
    private IllegalArgumentException operatorError(Object lv, Object rv) {
        return new IllegalArgumentException(String.format(
            "Unexpected token predicate DSL operator '%s' for values: %s, %s",
            op, lv.toString(), rv.toString()));
    }

    /**
     *
     * @param s
     * @return
     */
    private boolean isRegex(String s) {
        return s.startsWith(NCUtils.REGEX_FIX()) && s.endsWith(NCUtils.REGEX_FIX());
    }

    /**
     *
     * @param s
     * @return
     */
    private String stripRegex(String s) {
        int len = NCUtils.REGEX_FIX().length();

        return s.substring(len, s.length() - len);
    }

    /**
     * 
     * @param lv Left value.
     * @param rv right value.
     * @return
     */
    @SuppressWarnings("rawtypes")
    private boolean doEqual(Object lv, Object rv) {
        if (lv == rv)
            return true;
        // Collection equality.
        else if (lv instanceof Collection && rv instanceof Collection)
            return CollectionUtils.isEqualCollection(
                (Collection)lv,
                (Collection)rv
            );
        // 'in' operator.
        else if (rv instanceof Collection)
            return ((Collection)rv).contains(lv);
        else if (lv instanceof Number && rv instanceof Number)
            return Double.compare(
                ((Number)lv).doubleValue(),
                ((Number)rv).doubleValue()
            ) == 0;
        // Regex.
        else if (lv instanceof String && rv instanceof String && isRegex((String)rv) && !isRegex((String)lv))
            return Pattern.matches(stripRegex((String)rv), (String)lv);
        else if (lv instanceof String && rv instanceof String && isRegex((String)lv) && !isRegex((String)rv))
            return Pattern.matches(stripRegex((String)lv), (String)rv);
        else if (lv == null || rv == null)
            return false;
        else
            return Objects.equals(lv, rv);
    }

    /**
     *
     * @param lv Left value.
     * @param rv right value.
     * @return
     */
    @SuppressWarnings("rawtypes")
    private boolean doContain(Object lv, Object rv) {
        if (lv == null || lv == rv)
            return false;
        else if (lv instanceof Collection && !(rv instanceof Collection))
            return ((Collection)lv).contains(rv);
        else if (lv instanceof Collection) // Two collections.
            return ((Collection)lv).containsAll((Collection)rv);
        else if (lv instanceof String && rv instanceof String) // Substring containment.
            return ((String) lv).contains((String) rv);
        else
            throw operatorError(lv, rv);
    }

    /**
     *
     * @param v1
     * @param v2
     * @return
     */
    private int doCompare(Object v1, Object v2) {
        if (v1 == v2)
            return 0;
        else if (v1 == null)
            return -1;
        else if (v2 == null)
            return 1;
        if (v1 instanceof Number && v2 instanceof Number)
            return Double.compare(
                ((Number)v1).doubleValue(),
                ((Number)v2).doubleValue()
            );
        else
            throw operatorError(v1, v2);
    }

    /**
     *
     * @param lval
     * @return
     */
    private Object processParamFunction(Object lval) {
        switch (paramFunc) {
            case "trim":
                if (lval instanceof String)
                    return ((String) lval).trim();

                break;

            case "uppercase":
                if (lval instanceof String)
                    return ((String) lval).toUpperCase();

                break;

            case "lowercase":
                if (lval instanceof String)
                    return ((String) lval).toLowerCase();

                break;

            case "abs":
                if (lval instanceof Double)
                    return Math.abs((Double) lval);
                else if (lval instanceof Float)
                    return Math.abs((Float) lval);
                else if (lval instanceof Long)
                    return Math.abs((Long) lval);
                else if (lval instanceof Integer)
                    return Math.abs((Integer) lval);

                break;

            case "ceil":
                if (lval instanceof Double)
                    return Math.ceil((Double) lval);

                break;

            case "floor":
                if (lval instanceof Double)
                    return Math.floor((Double) lval);

                break;

            case "rint":
                if (lval instanceof Double)
                    return Math.rint((Double) lval);

                break;

            case "round":
                if (lval instanceof Double)
                    return Math.round((Double) lval);
                else if (lval instanceof Float)
                    return Math.round((Float) lval);

                break;

            case "signum":
                if (lval instanceof Double)
                    return Math.signum((Double) lval);
                else if (lval instanceof Float)
                    return Math.signum((Float) lval);

                break;

            case "keys":
                if (lval instanceof Map)
                    return ((Map<String, Object>)lval).keySet();

                break;

            case "values":
                if (lval instanceof Map)
                    return ((Map<String, Object>)lval).values();

                break;

            case "isalpha":
                if (lval instanceof String)
                    return StringUtils.isAlpha((String) lval);

                break;

            case "isalphanum":
                if (lval instanceof String)
                    return StringUtils.isAlphanumeric((String) lval);

                break;

            case "iswhitespace":
                if (lval instanceof String)
                    return StringUtils.isWhitespace((String) lval);

                break;

            case "isnumeric":
                if (lval instanceof String)
                    return StringUtils.isNumeric((String) lval);

                break;

            case "length":
            case "count":
            case "size":
                if (lval instanceof Collection)
                    return ((java.util.Collection<Object>)lval).size();
                else if (lval instanceof Map)
                    return ((Map<String, Object>)lval).size();
                else if (lval instanceof String)
                    return ((String)lval).length();

                break;
        }

        throw new IllegalArgumentException(String.format("Token predicate function '%s' cannot be applied to value type '%s'",
            paramFunc, lval.getClass().toString()));
    }

    @Override
    public Boolean apply(NCToken tok) {
        // Qualify token, if required.
        tok = qualFunc.apply(tok);

        Object lval = null;

        if (param.charAt(0) == '~') {
            String str = param.substring(1);

            if (str.isEmpty())
                throw new IllegalArgumentException(String.format("Token predicate DSL empty meta parameter name in: %s %s %s",
                    param, op, valueStr));

            String[] parts = str.split("[\\[\\]]");

            String metaName = parts[0];

            if (parts.length == 1)
                lval = tok.meta(metaName);
            else if (parts.length == 2) {
                Object obj = tok.meta(metaName);

                if (obj != null) {
                    String strIdx = parts[1];

                    if (strIdx.isEmpty())
                        throw new IllegalArgumentException(
                            String.format("Token predicate DSL meta parameter empty index in: %s %s %s", param, op,
                                valueStr));
                    else if (obj instanceof java.util.List) {
                        try {
                            lval = ((List<Object>)obj).get(Integer.parseInt(strIdx));
                        }
                        catch (NumberFormatException e) {
                            throw new IllegalArgumentException(String.format(
                                "Invalid token predicate DSL meta parameter index ('%s') for java.util.List value (integer only) in: %s %s %s",
                                strIdx, param, op, valueStr),
                                e);
                        }
                    }
                    else if (obj instanceof Map)
                        lval = ((Map<String, Object>)obj).get(strIdx);
                    else
                        throw new IllegalArgumentException(String.format(
                            "Invalid token predicate DSL meta parameter value " +
                                "for indexed access (java.util.List or java.util.Map only): %s %s %s",
                            param, op, valueStr));
                }
            }
            else
                throw new IllegalArgumentException(String.format(
                    "Invalid token predicate DSL meta parameter in: %s %s %s", param, op, valueStr));
        }
        else
            switch (param) {
                case "id": lval = tok.getId().trim(); break;
                case "groups": lval = tok.getGroups(); break;
                case "aliases": lval = tok.getAliases(); break;
                case "startidx": lval = tok.getStartCharIndex(); break;
                case "endidx": lval = tok.getEndCharIndex(); break;
                case "ancestors": lval = tok.getAncestors(); break;
                case "value": lval = tok.getValue() == null ? null : tok.getValue().trim(); break;
                case "parent": lval = tok.getParentId() == null ? null : tok.getParentId().trim(); break;

                default: throw new IllegalArgumentException(String.format(
                    "Invalid token predicate DSL parameter ('%s') in: %s %s %s",
                    param, param, op, valueStr));
            }

        if (paramFunc != null)
            lval = processParamFunction(lval);

        Object rval = value;

        if (!validated) {
            if ((param.equals("ancestors") ||
                param.equals("id") ||
                param.equals("parent")) &&
                !NCDslTokenChecker.isValidElementId(tok, (String)rval))
                throw new IllegalArgumentException(String.format("Attempt to check unknown element ID '%s' in: %s %s %s",
                    rval, param, op, valueStr));

            if ((param.equals("groups")) &&
                !NCDslTokenChecker.isValidGroup(tok, (String)rval))
                throw new IllegalArgumentException(String.format("Attempt to check unknown group ID '%s' in: %s %s %s",
                    rval, param, op, valueStr));

            validated = true;
        }

        switch (op) {
            case "==": return doEqual(lval, rval);
            case "!=": return !doEqual(lval, rval);
            case ">": return doCompare(lval, rval) == 1;
            case ">=": return doCompare(lval, rval) >= 0;
            case "<": return doCompare(lval, rval) == -1;
            case "<=": return doCompare(lval, rval) <= 0;
            case "@@": return doContain(lval, rval);
            case "!@": return !doContain(lval, rval);

            default:
                throw new AssertionError("Unexpected token predicate DSL operation: " + op);
        }
    }

    @Override
    public String toString() {
        String x = parts.isEmpty() ? param : String.format("%s.%s", String.join(".", parts), param);
        String x1 = paramFunc != null ? String.format("%s(%s)", paramFunc, x) : x;

        return String.format("%s %s %s", x1, op, valueStr);
    }
}
