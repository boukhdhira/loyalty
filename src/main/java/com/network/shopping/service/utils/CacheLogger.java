package com.network.shopping.service.utils;

import com.network.shopping.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

/**
 * logs the occurred cache event on the console
 * The listener reacts to the following events:
 * <p>
 * A cache entry is placed in the cache (CREATED).
 * The validity of a cache entry has expired (EXPIRED).
 * A cache entry is evicted from the cache (EVICTED).
 * A cache entry is evicted from the cache (UPDATED).
 */
@Slf4j
public class CacheLogger implements CacheEventListener<String, Account> {
    @Override
    public void onEvent(final CacheEvent<? extends String, ? extends Account> cacheEvent) {
        log.info("Key: {} | EventType: {} | Old value: {} | New value: {}",
                cacheEvent.getKey(), cacheEvent.getType(), cacheEvent.getOldValue(),
                cacheEvent.getNewValue());
    }
}

