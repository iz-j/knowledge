package com.izj.dynamodb.exception;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;

/**
 * Exception thrown when received {@link ProvisionedThroughputExceededException} from DynamoDb.
 *
 * @author ~~~~
 *
 */
public class ThroughputExceedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ThroughputExceedException(AmazonServiceException cause) {
        super(cause);
    }

    public ThroughputExceedException(AmazonServiceException cause, String tableName, boolean isWrite) {
        super(tableName + "is High " + (isWrite ? "writing" : "reading") + " load!");
    }
}
