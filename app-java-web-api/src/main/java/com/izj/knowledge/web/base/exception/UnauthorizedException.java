package com.izj.knowledge.web.base.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Returns 401 to client.<br>
 * Thrown if request not authenticated or authorized.
 *
 * @author iz-j
 *
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 6477842915863815179L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
