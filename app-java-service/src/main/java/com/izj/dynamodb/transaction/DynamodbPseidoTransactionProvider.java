package com.izj.dynamodb.transaction;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.NamedThreadLocal;
import org.springframework.util.Assert;

public final class DynamodbPseidoTransactionProvider {
    private static final ThreadLocal<Queue<DynamodbPseidoTransaction>> TX_STACKS = new NamedThreadLocal<>(
            "Transactional resources");

    static void bind(DynamodbPseidoTransaction transaction) throws IllegalStateException {
        Assert.notNull(transaction, "Transaction must not be null");
        Queue<DynamodbPseidoTransaction> stack = TX_STACKS.get();
        if (stack == null) {
            stack = Collections.asLifoQueue(new LinkedBlockingDeque<>());
            TX_STACKS.set(stack);
        }
        stack.add(transaction);
    }

    static void unbind(DynamodbPseidoTransaction transaction) throws IllegalStateException {
        Assert.notNull(transaction, "Transaction must not be null");
        Queue<DynamodbPseidoTransaction> stack = TX_STACKS.get();
        if (stack == null || !stack.peek().equals(transaction))
            throw new IllegalStateException();
        stack.poll();
    }

    public static DynamodbPseidoTransaction get() {
        Queue<DynamodbPseidoTransaction> stack = TX_STACKS.get();
        return CollectionUtils.isEmpty(stack) ? null : stack.peek();
    }

}
