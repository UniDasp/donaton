package com.bff.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "logistics-service")
public interface LogisticsClient {

}
