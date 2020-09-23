package com.baidu.shop.feign;

import com.baidu.shop.service.BrandService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId = "BrandService")
public interface BrandFeign extends BrandService {
}
