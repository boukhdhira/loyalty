package com.network.shopping.service.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@NoArgsConstructor
@Slf4j
public final class RestRequestUtils {

    public static HttpHeaders createAlert(String applicationName, String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-" + applicationName + "-alert", message);
        headers.add("X-" + applicationName + "-params", param);
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(String applicationName, boolean enableTranslation, String entityName, String param) {
        String message = enableTranslation ? applicationName + "." + entityName + ".created" : "A new " + entityName + " is created with identifier " + param;
        return createAlert(applicationName, message, param);
    }

    /**
     * Return a response with the location of the new resource. It's URL is
     * assumed to be a child of the URL just received.
     * <p>
     * Suppose we have just received an incoming URL of, say,
     * <code>http://localhost:8080/accounts</code> and <code>resourceId</code>
     * is "12345". Then the URL of the new resource will be
     * <code>http://localhost:8080/accounts/12345</code>.
     *
     * @param resourceId Is of the new resource.
     * @return
     */
    public static ResponseEntity<Void> entityWithLocation(Object resourceId) {

        // Determines URL of child resource based on the full URL of the given
        // request, appending the path info with the given resource Identifier
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{childId}").buildAndExpand(resourceId)
                .toUri();
        return ResponseEntity.created(location).build();
    }
}