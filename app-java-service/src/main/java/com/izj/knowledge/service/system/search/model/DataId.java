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
public final class DataId {

    private final String value;

    private DataId(String value) {
        this.value = value;
    }

    public static DataId of(UUID uid) {
        return new DataId(uid.toString());
    }

    public static DataId of(String id) {
        return new DataId(id);
    }

    public UUID toUUID() {
        return UUID.fromString(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
