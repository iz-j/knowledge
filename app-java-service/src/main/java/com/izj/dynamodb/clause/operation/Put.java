package com.izj.dynamodb.clause.operation;

import java.util.Collection;

import com.izj.dynamodb.exception.ConditionalUpdateFailedException;

/**
 *
 * @author ~~~~
 *
 * @param <I>
 */
public interface Put<I> {

    void item(I item);

    @SuppressWarnings("unchecked")
    void items(I... items);

    void items(Collection<I> items);

    Put<I> ifNotExists();

    /**
     * If put item(s) already exists,<br>
     * throw {@link ConditionalUpdateFailedException}.<br>
     * <br>
     * this method includes ifNotExists().<br>
     * 
     * @return
     */
    Put<I> throwExceptionIfExists();

}
