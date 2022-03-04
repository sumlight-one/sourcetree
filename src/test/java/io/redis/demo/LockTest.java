package io.redis.demo;


import io.redis.demo.lock.RedisLock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: szz
 * @Date: 2019/5/4 下午12:50
 * @Version 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class LockTest {
    /**
     * 模拟秒杀
     */

    @Autowired
    RedisLock redisLock;

    //超时时间10s
    private static final int TIMEOUT = 10 * 1000;

    @Test
    public void secKill(){
        String productId="1";
        long time = System.currentTimeMillis() + TIMEOUT;
        //加锁
        if (!redisLock.lock(productId, String.valueOf(time))){
            throw new RuntimeException("人太多了，等会儿再试吧~");
        }

        //具体的秒杀逻辑
        System.out.println("秒杀的业务逻辑");
        //解锁
        redisLock.unlock(productId, String.valueOf(time));
    }

}
