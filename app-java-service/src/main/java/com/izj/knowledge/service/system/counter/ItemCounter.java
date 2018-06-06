package com.izj.knowledge.service.system.counter;

/**
 * Generic helper service to count records per specific unit.<br>
 * And calculate a partition to store records on KVS.<br>
 * NOTE: <br>
 * Counter value does not have strong consistency.<br>
 * So, use this counter under the condition that some errors can be acceptable.
 *
 * @author iz-j
 *
 */
public interface ItemCounter {

    /**
     * @param unit
     * @return current count
     */
    long getCountOf(CounterUnit unit);

    /**
     * Returns a latest partition number that is calculated by the current counter.
     *
     * @param unit
     * @return partition number
     */
    long getLatestPartitionNumberOf(CounterUnit unit);

    /**
     * Returns a partition number to store new record.
     *
     * @param unit
     * @return partition number
     */
    long getPartitionNumberToStore(CounterUnit unit);

    /**
     * @see #increaseCount(CounterUnit, long)
     */
    void increaseCount(CounterUnit unit);

    /**
     * Increase a count of the specified definition per company.
     *
     * @param unit
     * @param increment
     */
    void increaseCount(CounterUnit unit, long increment);
}
