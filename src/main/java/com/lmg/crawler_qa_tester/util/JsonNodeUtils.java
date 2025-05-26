package com.lmg.crawler_qa_tester.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public final class JsonNodeUtils {
    private static final String NULL_VALUE = "null";
    private static final String UNKNOWN_VALUE = "unknown";

    private JsonNodeUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static JsonNode safeGet(JsonNode node, int index) {
        return (node != null && index < node.size()) ? node.get(index) : null;
    }

    public static JsonNode safeGet(JsonNode node, String field) {
        return node != null ? node.get(field) : null;
    }

    public static JsonNode getContent(JsonNode node) {
        return node != null ? node.path("content") : null;
    }

    public static JsonNode getChildren(JsonNode node) {
        return node != null ? node.path("children") : null;
    }

    public static boolean hasChildren(JsonNode node) {
        return node != null && node.isArray() && node.size() > 0;
    }

    public static String getFieldValue(JsonNode node1, JsonNode node2, String field) {
        return Stream.of(node1, node2)
                .filter(java.util.Objects::nonNull)
                .filter(n -> n.has(field))
                .findFirst()
                .map(n -> n.path(field).asText(NULL_VALUE))
                .orElse(NULL_VALUE);
    }

    public static String getFieldValue(JsonNode node, String field) {
        return (node != null && node.has(field)) ? node.path(field).asText(NULL_VALUE) : NULL_VALUE;
    }

    public static Set<String> collectFieldNames(JsonNode node1, JsonNode node2) {
        Set<String> fieldNames = new HashSet<>();
        if (node1 != null && node1.isObject()) node1.fieldNames().forEachRemaining(fieldNames::add);
        if (node2 != null && node2.isObject()) node2.fieldNames().forEachRemaining(fieldNames::add);
        return fieldNames;
    }

    public static boolean bothNodesNullOrEmpty(JsonNode node1, JsonNode node2) {
        return (node1 == null || node1.isNull() || node1.isEmpty()) &&
                (node2 == null || node2.isNull() || node2.isEmpty());
    }

    public static boolean isContainerNode(JsonNode node) {
        return node != null && node.isContainerNode();
    }

    public static JsonNode getDescriptionValues(JsonNode messageNode) {
        return messageNode != null ? messageNode.path("description").path("values") : null;
    }

    public static String getMetaName(JsonNode messageNode) {
        if (messageNode == null || messageNode.isNull()) return UNKNOWN_VALUE;
        JsonNode meta = messageNode.path("_meta");
        return meta.has("name") ? meta.path("name").asText(UNKNOWN_VALUE) : UNKNOWN_VALUE;
    }
    public static String getTitleName(JsonNode messageNode, String locale) {
        if (messageNode == null || messageNode.isNull()) return UNKNOWN_VALUE;
        JsonNode title = messageNode.path("title");
        for(JsonNode value : title.get("values"))
        {
            if(value.get("locale").asText().equals(locale))
            {
                return value.get("value").asText();
            }
        }
        return  UNKNOWN_VALUE;
    }

    public static String getLocaleValue(JsonNode valuesArray, String locale) {
        if (valuesArray == null || !valuesArray.isArray()) return NULL_VALUE;
        for (JsonNode valueNode : valuesArray) {
            if (locale.equals(valueNode.path("locale").asText())) {
                return valueNode.path("value").asText(NULL_VALUE);
            }
        }
        return NULL_VALUE;
    }
} 