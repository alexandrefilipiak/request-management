package io.github.alexandrefilipiak.requestmanagement.core.domain;

/**
 * A business rule violation.
 * <p>
 * The message is written for the end user and may be shown to them as is:
 * it must describe the rule in domain terms and never contain technical details.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}