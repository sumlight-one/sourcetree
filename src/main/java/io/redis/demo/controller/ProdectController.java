package io.redis.demo.controller;

import io.redis.demo.pojo.Product;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: szz
 * @Date: 2019/5/4 上午11:19
 * @Version 1.0
 */
@RestController
//@CacheConfig(cacheNames = "product")
public class ProdectController {
    /**
     * 添加缓存
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Cacheable(cacheNames = "product", key = "123")
    public List<Product> list() {
       List<Product> products=new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Product product=new Product();
            product.setId(i+1);
            product.setName("第"+(i+1)+"件商品");
            product.setPrice((double) ((i+1)*100));
            products.add(product);
        }
        return products;
    }

    /**
     * 更新缓存
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET)
    @CachePut(cacheNames = "product", key = "123")
    public List<Product> update() {
        System.out.println("更新了缓存");
        List<Product> products=new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Product product=new Product();
            product.setId(i+10);
            product.setName("第"+(i+10)+"件商品");
            product.setPrice((double) ((i+10)*100));
            products.add(product);
        }
        return products;
    }

    /**
     * 删除缓存
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @CacheEvict(cacheNames = "product", key = "123")
    public void delete() {

        System.out.println("缓存被删除了");
    }

    /**
     * key为动态
     */
    @GetMapping("/detail")
    @Cacheable(cacheNames = "product", key = "#id")
    public Product detail(@RequestParam("id") Integer id){
        if (id==1){
            return new Product(1,"电冰箱",20d);
        }else if (id==2){
            return new Product(2,"洗衣机",30d);
        }else {
            return new Product(3,"彩电",40d);
        }
    }

    /**
     * 根据条件缓存
     */
    @GetMapping("/detailOnCondition")
    @Cacheable(cacheNames = "product", key = "#id", condition = "#id > 2")
    public Product detailOnCondition(@RequestParam("id") Integer id){
        return new Product(3,"彩电",40d);
    }


    /**
     *指定 `unless `即条件不成立时缓存。`#result` 代表返回值，意思是当返回码不等于 0 时不缓存，也就是等于 0 时才缓存。
     */
    @GetMapping("/detailOnConditionAndUnless")
    @Cacheable(cacheNames = "product", key = "#id", condition = "#id > 2", unless = "#result!= 0")
    public Integer detailOnConditionAndUnless(@RequestParam("id") Integer id){
        if (id==3){
            return 0;
        }else {
            return 1;
        }
    }
}
