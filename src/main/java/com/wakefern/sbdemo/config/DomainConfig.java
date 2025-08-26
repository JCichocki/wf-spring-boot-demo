package com.wakefern.sbdemo.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("com.wakefern.sbdemo")
@EnableJpaRepositories("com.wakefern.sbdemo")
@EnableTransactionManagement
public class DomainConfig {
}
