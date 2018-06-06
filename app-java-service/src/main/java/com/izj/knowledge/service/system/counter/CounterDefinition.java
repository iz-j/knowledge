package com.izj.knowledge.service.system.counter;

/**
 * Definitions of counter.<br>
 * NOTE:<br>
 * Once partition size determined and it has been used, it is hard to change it.<br>
 * If you want to change size after use it,<br>
 * you have to migrate the hash key of existing records...
 *
 * @author iz-j
 *
 */
public enum CounterDefinition {
    TEST(25),
    ;

    private final long partitionSize;

    private CounterDefinition(int partitionSize) {
        this.partitionSize = partitionSize;
    }

    public long getPartitionSize() {
        return partitionSize;
    }
}
