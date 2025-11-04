package com.gpr.domain;

public enum ReviewSource {
    GOOGLE_PLAY,
    STEAM;

    public String value() {
        return name().toLowerCase();
    }
}
