package com.izj.knowledge.service.base.util.json;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author shirakawa_d
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonUtils {
    public static void removeEmptyNodeRecursive(JsonNode node) {
        for (Iterator<JsonNode> it = node.iterator(); it.hasNext();) {
            JsonNode child = it.next();

            if (child.isNull()) {
                it.remove();
            } else {
                removeEmptyNodeRecursive(child);
            }
        }
    }
}
