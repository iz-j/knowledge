package com.izj.dynamodb.entity;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 *
 * @author ~~~~
 *
 */
public interface TypeReferenceProvider {

    TypeReference<?> get(String fieldName);

}
