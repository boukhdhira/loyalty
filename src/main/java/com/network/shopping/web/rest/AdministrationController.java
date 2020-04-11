package com.network.shopping.web.rest;

import com.network.shopping.service.ManagementService;
import com.network.shopping.service.dto.StoreDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.network.shopping.service.utils.RestRequestUtils.entityWithLocation;

@RestController
@RequestMapping("/api/v1/management")
@Slf4j
public class AdministrationController {

    private final ManagementService managementService;

    @Autowired
    public AdministrationController(final ManagementService managementService) {
        this.managementService = managementService;
    }

    @PostMapping("/store")
    public ResponseEntity<Void> addProductStore(@RequestBody @Valid final StoreDTO store) {
        log.debug("Attempt to register a new store product.. {}", store);
        this.managementService.registerStore(store);
        return entityWithLocation(store.getMerchantNumber());
    }
}
