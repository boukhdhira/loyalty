package com.network.shopping.service.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mail request data
 */
@Data
public class MailRequest {
    private String recipient;
    private List<String> cc;
    private Map<String, Object> props = new HashMap<>();
}
