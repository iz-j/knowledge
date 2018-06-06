package com.izj.knowledge.web.base.interceptor;

/**
 *
 * @author iz-j
 *
 */
public enum InterceptorOrder {
    TENANT(0),
    USER_AUTHENTICATION(1),
    USER_AUTHORIZATION(2),
    LOGGING(3);

    private final int value;

    private InterceptorOrder(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
