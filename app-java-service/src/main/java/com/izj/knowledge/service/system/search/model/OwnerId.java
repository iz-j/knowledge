package com.knowledge.hoge.connect.service.system.search.model;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 *
 * @author iz-j
 *
 */
@Getter
@EqualsAndHashCode
public final class OwnerId {
    private static final String PUBLIC = "*";

    private final String value;

    private OwnerId(String value) {
        this.value = value;
    }

    public static OwnerId of(UUID uid) {
        return new OwnerId(uid.toString());
    }

    public static OwnerId of(String id) {
        return new OwnerId(id);
    }

    public static OwnerId forPublic() {
        return new OwnerId(PUBLIC);
    }

    public UUID toUUID() {
        return UUID.fromString(value);
    }

    @Override
    public String toString() {
        return value;
    }

}
