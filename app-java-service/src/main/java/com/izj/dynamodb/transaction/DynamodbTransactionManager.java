package com.izj.dynamodb.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.izj.dynamodb.DynamodbHolder;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author ~~~~
 *
 */
@Slf4j
public final class DynamodbTransactionManager implements PlatformTransactionManager {
    private final AmazonDynamoDB db;

    public DynamodbTransactionManager(AmazonDynamoDB db) {
        super();
        this.db = db;
        DynamodbHolder.set(db);
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        if (log.isTraceEnabled()) {
            log.trace("getTransaction called. Transactional definition class => {}", definition.getName());
        }
        DynamodbPseidoTransaction transaction = DynamodbPseidoTransactionProvider.get();
        boolean isNewTransaction = transaction == null
                || definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW;
        if (isNewTransaction) {
            transaction = new DynamodbPseidoTransaction(definition.getName(), db);
            DynamodbPseidoTransactionProvider.bind(transaction);
        }
        return new DynamodbPseidoTransactionStatus(transaction, isNewTransaction);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        log.trace("commit called. status => {}", status);
        DynamodbPseidoTransactionStatus dynamoDbTransactionStatus = (DynamodbPseidoTransactionStatus)status;
        dynamoDbTransactionStatus.setCompleted();
        if (status.isNewTransaction()) {
            DynamodbPseidoTransaction transaction = dynamoDbTransactionStatus.getTransaction();
            try {
                transaction.flush();
            } finally {
                DynamodbPseidoTransactionProvider.unbind(transaction);
                log.trace("transaction flushed. status => {}", status);
            }
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        log.trace("rollback called.");
        DynamodbPseidoTransactionStatus dynamoDbTransactionStatus = (DynamodbPseidoTransactionStatus)status;
        dynamoDbTransactionStatus.setCompleted();
        if (status.isNewTransaction()) {
            DynamodbPseidoTransaction transaction = dynamoDbTransactionStatus.getTransaction();
            DynamodbPseidoTransactionProvider.unbind(transaction);
        }
    }

    /**
     *
     * @author ~~~~
     *
     */
    @ToString
    public static class DynamodbPseidoTransactionStatus implements TransactionStatus {

        private final DynamodbPseidoTransaction transaction;
        private final boolean isNewTransaction;
        private boolean completed;

        public DynamodbPseidoTransactionStatus(DynamodbPseidoTransaction transaction,
                boolean isNewTransaction) {
            super();
            this.transaction = transaction;
            this.isNewTransaction = isNewTransaction;
        }

        public DynamodbPseidoTransaction getTransaction() {
            return this.transaction;
        }

        @Override
        public Object createSavepoint() throws TransactionException {
            return null;
        }

        @Override
        public void rollbackToSavepoint(Object savepoint) throws TransactionException {
            // do nothing
        }

        @Override
        public void releaseSavepoint(Object savepoint) throws TransactionException {
            // do nothing
        }

        @Override
        public boolean isNewTransaction() {
            return this.transaction != null && this.isNewTransaction;
        }

        @Override
        public boolean hasSavepoint() {
            return false;
        }

        @Override
        public void setRollbackOnly() {
            // do nothing
        }

        @Override
        public boolean isRollbackOnly() {
            return false;
        }

        @Override
        public void flush() {
            // do nothing
        }

        @Override
        public boolean isCompleted() {
            return this.completed;
        }

        public void setCompleted() {
            this.completed = true;
        }

    }
}
