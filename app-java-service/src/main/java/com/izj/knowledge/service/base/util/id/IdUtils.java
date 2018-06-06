package com.izj.knowledge.service.base.util.id;

import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ~~~~
 *
 */
public final class IdUtils {

    private IdUtils() {
    }

    private static final String JOINER = "+";
    private static final UUID ZERO_UUID = new UUID(0, 0);

    public static UUID next() {
        return UUID.randomUUID();
    }

    public static String join(UUID... ids) {
        return StringUtils.join(ids, JOINER);
    }

    public static UUID[] split(String id) {
        return Arrays.stream(StringUtils.split(id, JOINER)).map(s -> UUID.fromString(s)).toArray(UUID[]::new);
    }

    public static UUID zeroUUID() {
        return ZERO_UUID;
    }

    public static boolean isZeroUUID(UUID id) {
        return ZERO_UUID.equals(id);
    }

}
