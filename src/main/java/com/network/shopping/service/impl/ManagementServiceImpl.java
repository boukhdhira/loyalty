package com.network.shopping.service.impl;

import com.network.shopping.repository.StoreRepository;
import com.network.shopping.service.ManagementService;
import com.network.shopping.service.dto.StoreDTO;
import com.network.shopping.service.mapper.StoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ManagementServiceImpl implements ManagementService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Autowired
    public ManagementServiceImpl(final StoreRepository storeRepository, final StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
    }

    @Override
    public void registerStore(final StoreDTO storeDTO) {
        if (this.storeRepository.findByMerchantNumber(storeDTO.getMerchantNumber()).isPresent()) {
            throw new DataIntegrityViolationException("Merchant number already exist!");
        }
        this.storeRepository.save(this.storeMapper.toEntity(storeDTO));
        log.debug("A new product store has been saved {}", storeDTO);
    }
}
