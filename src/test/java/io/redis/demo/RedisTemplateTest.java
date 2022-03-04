package io.redis.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author: szz
 * @Date: 2019/5/4 下午12:23
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTemplateTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void test1(){
        redisTemplate.opsForValue().set("name","zhangsan");
        String name = (String)redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

    @Test
    public void test2(){
        stringRedisTemplate.opsForValue().set("name","zhangsan");
        String name = stringRedisTemplate.opsForValue().get("name");
        System.out.println(name);
    }

    @Test
    public void test3(){
        redisTemplate.opsForHash().put("produce","1","电视机");
        redisTemplate.opsForHash().put("produce","2","冰箱");
        redisTemplate.opsForHash().put("produce","3","彩电");
        redisTemplate.opsForHash().put("produce","4","自行车");

        String name = (String) redisTemplate.opsForHash().get("produce", "4");
        System.out.println(name);
    }
    @Test
    public void test4(){
        redisTemplate.opsForList().leftPush("name","zhangfei");
        redisTemplate.opsForList().leftPush("name","liubei");
        redisTemplate.opsForList().leftPush("name","guanyu");
        List names = redisTemplate.opsForList().range("name", 0, -1);
        for (Object name : names) {
            System.out.println(name);
        }
    }
}
