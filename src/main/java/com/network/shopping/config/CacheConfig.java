package com.network.shopping.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
public class CacheConfig {
    //TODO: handle transaction aware to ensures that any put, evict or clear operations only execute after a successful
    // commit of the current transaction (or immediately if there is no transaction).
}
