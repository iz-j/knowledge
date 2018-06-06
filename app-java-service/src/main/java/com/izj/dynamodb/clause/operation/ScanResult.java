package com.izj.dynamodb.clause.operation;

import java.util.List;

import lombok.Getter;

@Getter
public class ScanResult<I> {

    @Getter
    public static class ScannedItem<I> {
        public final String tenantId;
        public final I item;

        public ScannedItem(String tenantId, I item) {
            this.tenantId = tenantId;
            this.item = item;
        }
    }

    private final String lastEvaluatedKey;
    private final List<ScannedItem<I>> items;

    public ScanResult(String lastEvaluatedKey, List<ScannedItem<I>> items) {
        this.lastEvaluatedKey = lastEvaluatedKey;
        this.items = items;
    }
}
