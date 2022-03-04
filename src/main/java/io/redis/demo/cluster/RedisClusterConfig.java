package io.redis.demo.cluster;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableCaching
@ConfigurationProperties(prefix = "redis.cache")
public class RedisClusterConfig extends CachingConfigurerSupport {
   // getter,setter自动注入
   private List<String> clusterNodes = new ArrayList<>(); // 接收prop2里面的属性值
   public List<String> getClusterNodes() {
      return clusterNodes;
   }
 
   public void setClusterNodes(List<String> clusterNodes) {
      this.clusterNodes = clusterNodes;
   }
 
 
   /*
   *  1.  jedis 连接redis-cluster
   *
    */
   /**
    * 这里返回的JedisCluster是单例,并且可以直接autowired调用
    * @return
    */
   @Bean
   public JedisCluster getJedisCluster() {
      // 创建set集合
      Set<HostAndPort> nodes = new HashSet<HostAndPort>();
      // 循环数组把集群节点添加到set集合中
      for (String node : clusterNodes) {
         String[] host = node.split(":");
         // 添加集群节点
         nodes.add(new HostAndPort(host[0], Integer.parseInt(host[1])));
      }
      JedisCluster jc = new JedisCluster(nodes);
      return jc;
   }
    /*
     *  2.Spring-data-redis  连接redis集群，并进行操作
     *  优势在于可以配置cacheManager,默认的cacheManager是ConcurrentMapCacheManager
     *  RedisClusterConfiguration -> JedisConnectionFactory -> RedisTemplate -> CacheManager
     *
     */

    @Bean
    public RedisClusterConfiguration getRedisCluster() {

        Set<RedisNode> jedisClusterNodes = new HashSet<RedisNode>();


        clusterNodes.forEach(n ->{
            String[] arr = n.split(":");
            jedisClusterNodes.add(new RedisNode(arr[0], Integer.valueOf(arr[1])));
        });

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.setClusterNodes(jedisClusterNodes);
        return redisClusterConfiguration;
    }


    @Bean
    public JedisConnectionFactory getConnFactory(){
        JedisConnectionFactory     factory = new JedisConnectionFactory (getRedisCluster()) ;
        return factory;
    }





    /**
     * springboot有默认的redisTemplate，一般用RedisTemplate<String,Object>,也常用StringRedisTemplate
     * redisTemplate.opsForValue().set  操作字符串
     * redisTemplate.opsForHash()   操作hash，以此类推
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate redisTemplate(JedisConnectionFactory factory) {

        RedisTemplate template = new RedisTemplate<>();

        RedisSerializer<String> redisSerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        template.setConnectionFactory(factory);
        //key序列化方式
        template.setKeySerializer(redisSerializer);
        //value序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //value hashmap序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate){
        RedisCacheManager manager = new RedisCacheManager(redisTemplate);
        manager.setDefaultExpiration(3000);  // 设定失效时间
        return new RedisCacheManager(redisTemplate);
    }


    /**
     * keyGenerator 自定义@Cacheable key生成策略，必须得配置，否则用redis做缓存时会报错，一般发生于@Cacheable方法参数为空或者为多个时，
     * 默认的key生成策略是SimpleKey[];
     * org.springframework.cache.interceptor.SimpleKey cannot be cast to java.lang.String
     * @return
     */
    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        // lambda表达式
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append("."+method.getName());
            for (Object obj : params) {
                sb.append(obj.toString()+",");
            }
            if(sb.substring(sb.length()-1).toString().equals(","))
                return sb.substring(0,sb.length()-1).toString();
            return sb.toString();
        };
    }
}
