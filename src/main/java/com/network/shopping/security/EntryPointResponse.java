package com.network.shopping.security;

import lombok.Builder;
import lombok.Data;

/**
 * The type Entry point response.
 */
@Data
@Builder
public class EntryPointResponse {
    String timestamp;
    String status;
    String error;
    String code;

    @Override
    public String toString() {
        return "{" +
                "\"timestamp\":\"" + this.timestamp + "\"" +
                ", \"status\":\"" + this.status + "\"" +
                ", \"error\":\"" + this.error + "\"" +
                ", \"code\":\"" + this.code + "\"}"
                ;
    }
}
