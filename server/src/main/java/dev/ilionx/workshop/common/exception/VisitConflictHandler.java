package dev.ilionx.workshop.common.exception;

import io.github.jframe.exception.factory.ErrorResponseEntityBuilder;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import lombok.RequiredArgsConstructor;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class VisitConflictHandler {

    private final ErrorResponseEntityBuilder errorResponseEntityBuilder;

    @ExceptionHandler(VetReferencedException.class)
    public ResponseEntity<ApiErrorResponseResource> handle(final VetReferencedException exception, final WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(errorResponseEntityBuilder.buildErrorResponseBody(exception, HttpStatus.CONFLICT, request));
    }
}
