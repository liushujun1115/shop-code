package com.baidu.shop.feign;

import com.baidu.shop.document.GoodsDoc;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface Goods extends ElasticsearchRepository<GoodsDoc,Long> {
}
