package com.izj.dynamodb;

import org.springframework.util.Assert;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.izj.dynamodb.clause.key.KeyResolver;
import com.izj.dynamodb.clause.operation.BatchDelete;
import com.izj.dynamodb.clause.operation.BatchGet;
import com.izj.dynamodb.clause.operation.Delete;
import com.izj.dynamodb.clause.operation.Get;
import com.izj.dynamodb.clause.operation.Put;
import com.izj.dynamodb.clause.operation.Query;
import com.izj.dynamodb.clause.operation.Scan;
import com.izj.dynamodb.clause.operation.Update;
import com.izj.dynamodb.clause.operation.impl.BatchDeleteImpl;
import com.izj.dynamodb.clause.operation.impl.BatchGetImpl;
import com.izj.dynamodb.clause.operation.impl.DeleteImpl;
import com.izj.dynamodb.clause.operation.impl.GetImpl;
import com.izj.dynamodb.clause.operation.impl.PutImpl;
import com.izj.dynamodb.clause.operation.impl.QueryImpl;
import com.izj.dynamodb.clause.operation.impl.ScanImpl;
import com.izj.dynamodb.clause.operation.impl.UpdateImpl;
import com.izj.dynamodb.internal.handler.UpdateHandler;
import com.izj.dynamodb.internal.metadata.EntityAnalyzer;
import com.izj.dynamodb.transaction.DynamodbPseidoTransaction;
import com.izj.dynamodb.transaction.DynamodbPseidoTransactionProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Builder(toBuilder = true)
@AllArgsConstructor
public class DynamodbClientImpl implements DynamodbClient {

    private final MultiTenantSupport multiTenantSupport;
    private final AmazonDynamoDB lowLevelDb;
    private final DynamoDB db;

    DynamodbClientImpl(MultiTenantSupport multiTenantSupport, AmazonDynamoDB db) {
        super();
        this.lowLevelDb = db;
        this.db = lowLevelDb != null ? new DynamoDB(lowLevelDb) : null;
        this.multiTenantSupport = multiTenantSupport;
        DynamodbHolder.set(lowLevelDb);
    }

    @Override
    public <E> Put<E> put(Class<E> entityClass, String tableSuffix) {
        checkOpenTransaction();
        return new PutImpl<>(EntityAnalyzer.analyze(entityClass), getUpdateHandler(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> Get<E> get(Class<E> entityClass, String tableSuffix) {
        return new GetImpl<>(EntityAnalyzer.analyze(entityClass),
                getDb(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> Query<E> query(Class<E> entityClass, String tableSuffix) {
        return new QueryImpl<>(EntityAnalyzer.analyze(entityClass),
                getDb(), new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> Update<E> update(Class<E> entityClass, String tableSuffix) {
        checkOpenTransaction();
        return new UpdateImpl<>(EntityAnalyzer.analyze(entityClass), getUpdateHandler(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> BatchGet<E> batchGet(Class<E> entityClass, String tableSuffix) {
        return new BatchGetImpl<>(EntityAnalyzer.analyze(entityClass),
                getDb(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> Delete delete(Class<E> entityClass, String tableSuffix) {
        checkOpenTransaction();
        return new DeleteImpl(EntityAnalyzer.analyze(entityClass), getUpdateHandler(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> BatchDelete batchDelete(Class<E> entityClass, String tableSuffix) {
        checkOpenTransaction();
        return new BatchDeleteImpl(EntityAnalyzer.analyze(entityClass),
                getUpdateHandler(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    @Override
    public <E> Scan<E> scan(Class<E> entityClass, String tableSuffix) {
        return new ScanImpl<E>(EntityAnalyzer.analyze(entityClass),
                getLowLevelDb(),
                new KeyResolver(multiTenantSupport),
                tableSuffix);
    }

    private UpdateHandler getUpdateHandler() {
        if (log.isTraceEnabled()) {
            log.trace(db != null ? "Returns UpdateHandlerPromptly." : "Returns UpdateHandlerTransactionally.");
        }
        return db != null ? UpdateHandler.Factory.promptly(db)
                : UpdateHandler.Factory.transactionally(DynamodbPseidoTransactionProvider.get());
    }

    private DynamoDB getDb() {
        DynamodbPseidoTransaction tx = DynamodbPseidoTransactionProvider.get();
        return db != null ? db : tx != null ? tx.getDb() : DynamodbHolder.get();
    }

    private AmazonDynamoDB getLowLevelDb() {
        DynamodbPseidoTransaction tx = DynamodbPseidoTransactionProvider.get();
        return lowLevelDb != null ? lowLevelDb : tx != null ? tx.getLowLebelDb() : DynamodbHolder.getLowLevel();
    }

    private void checkOpenTransaction() {
        if (db == null && lowLevelDb == null) {
            Assert.notNull(DynamodbPseidoTransactionProvider.get(), "Transaction has not started.");
        }
    }

    @Override
    public <E> Scan<E> scan(Class<E> entityClass) {
        return scan(entityClass, null);
    }

    @Override
    public <E> Put<E> put(Class<E> entityClass) {
        return put(entityClass, null);
    }

    @Override
    public <E> Get<E> get(Class<E> entityClass) {
        return get(entityClass, null);
    }

    @Override
    public <E> Query<E> query(Class<E> entityClass) {
        return query(entityClass, null);
    }

    @Override
    public <E> Update<E> update(Class<E> entityClass) {
        return update(entityClass, null);
    }

    @Override
    public <E> Delete delete(Class<E> entityClass) {
        return delete(entityClass, null);
    }

    @Override
    public <E> BatchGet<E> batchGet(Class<E> entityClass) {
        return batchGet(entityClass, null);
    }

    @Override
    public <E> BatchDelete batchDelete(Class<E> entityClass) {
        return batchDelete(entityClass, null);
    }

    @Override
    public <E> DynamodbAsyncClient async() {
        if (lowLevelDb != null && AmazonDynamoDBAsync.class.isAssignableFrom(lowLevelDb.getClass())) {
            return new DynamodbAsyncClientImpl((AmazonDynamoDBAsync)lowLevelDb,
                    new KeyResolver(multiTenantSupport));
        } else {
            log.warn("No async client assigned on this context, so using synchronous client instead.");
            return new DynamodbAsyncClientTestingImpl(this);
        }
    }

    @Override
    public DynamodbAdminClient forAdmin() {
        return new DynamodbAdminClientImpl();
    }

}
