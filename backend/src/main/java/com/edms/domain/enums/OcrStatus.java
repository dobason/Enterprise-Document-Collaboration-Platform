package com.edms.domain.enums;

public enum OcrStatus {
    PROCESSING("processing"),
    COMPLETED("completed"),
    NOT_FOUND("not_found");

    private final String value;

    OcrStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OcrStatus fromValue(String value) {
        for (OcrStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        return NOT_FOUND;
    }
}
