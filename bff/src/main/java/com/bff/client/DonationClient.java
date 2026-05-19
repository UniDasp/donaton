package com.bff.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "donation-service")
public interface DonationClient {

}
