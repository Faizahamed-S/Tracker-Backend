package com.ApplyZap.Tracker.exception;

/**
 * Invalid profileData on save. {@link #getField()} is the JSON path of the
 * offending value, e.g. {@code experiences[1].startDate}.
 */
public class ProfileValidationException extends IllegalArgumentException {

    private final String field;

    public ProfileValidationException(String field, String message) {
        super(field + ": " + message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
