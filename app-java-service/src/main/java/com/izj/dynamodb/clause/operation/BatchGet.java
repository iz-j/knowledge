package com.izj.dynamodb.clause.operation;

import java.util.Collection;
import java.util.List;

import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.PrimaryKey;

public interface BatchGet<I> {

    List<I> items(Collection<PrimaryKey> keys);

    List<I> itemsByHashOnlyPrimaryKeys(Collection<HashKey> hashKeys);

    BatchGet<I> consistentRead();

}
