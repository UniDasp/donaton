package com.bff.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "needs-service")
public interface NeedsClient {

}
