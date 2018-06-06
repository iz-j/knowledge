package com.izj.dynamodb;

/**
 * @author ~~~~
 * @see DynamodbClient
 */
public interface MultiTenantSupport {

    String getTenantId();

}
