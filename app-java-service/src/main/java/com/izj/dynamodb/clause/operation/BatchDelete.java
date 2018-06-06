package com.izj.dynamodb.clause.operation;

import java.util.Collection;

import com.izj.dynamodb.clause.key.HashKey;
import com.izj.dynamodb.clause.key.PrimaryKey;

public interface BatchDelete {

    void items(Collection<PrimaryKey> collection);

    void itemsByHashOnlyPrimaryKeys(Collection<HashKey> hashKeys);

}
