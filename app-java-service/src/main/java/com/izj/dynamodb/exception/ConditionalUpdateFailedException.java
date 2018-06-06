package com.izj.dynamodb.exception;

import com.amazonaws.AmazonServiceException;

/**
 *
 * @author ~~~~
 *
 */
public class ConditionalUpdateFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConditionalUpdateFailedException(AmazonServiceException cause) {
        super(cause);
    }

    public ConditionalUpdateFailedException(String tableName, AmazonServiceException cause) {
        super(tableName, cause);
    }
}
