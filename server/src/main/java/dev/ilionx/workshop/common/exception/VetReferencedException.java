package dev.ilionx.workshop.common.exception;

import io.github.jframe.exception.ApiException;

public class VetReferencedException extends ApiException {

    public VetReferencedException() {
        super(ApiErrorCode.VET_REFERENCED_BY_VISITS);
    }
}
