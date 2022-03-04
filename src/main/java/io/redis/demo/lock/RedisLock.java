package io.redis.demo.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class RedisLock {

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value 当前时间 + 超时时间
     * @return
     */
    public boolean lock(String key, String value){
        if (redisTemplate.opsForValue().setIfAbsent(key, value)){
            return true;
        }

        //解决死锁，且当多个线程同时来时，只会让一个线程拿到锁
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果过期
        if (!StringUtils.isEmpty(currentValue) &&
                Long.parseLong(currentValue) < System.currentTimeMillis()){
            //获取上一个锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                return true;
            }
        }

        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value){

        try {
            String currentValue = redisTemplate.opsForValue().get(key);

            redisTemplate.opsForValue().getOperations().delete(key);
            System.out.println("锁被释放了");

        }catch (Exception e){
            log.error("【redis锁】解锁失败, {}", e);
        }
    }
}