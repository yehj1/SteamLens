package com.gpr.dto;

import java.util.UUID;

public class SteamFetchResponse {

    private final UUID appUuid;
    private final String source;
    private final int saved;

    public SteamFetchResponse(UUID appUuid, String source, int saved) {
        this.appUuid = appUuid;
        this.source = source;
        this.saved = saved;
    }

    public UUID getAppUuid() {
        return appUuid;
    }

    public String getSource() {
        return source;
    }

    public int getSaved() {
        return saved;
    }
}
