#Redis 支持
    1. 自动注册redis实例
    2. 提供自定义扩展渠道
    3. Redis 操作基本封装
  
  
#标准类型Redis注册
   前缀： redis.std
       配置项：
       注： Redis ID, 唯一标识这个 Redis 实例
       required: commonspring.redis.standards.{redisId}.server=localhost:6379   HOST:POST
       optional: commonspring.redis.standards..{redisId}.timeout=3000            连接超时，默认是3000
       optional: commonspring.redis.standards..{redisId}.password=123456         安全链接密码，默认是null
       optional: commonspring.redis.standards..{redisId}.database                连接的数据库，从0开始，默认是 0
       optional: commonspring.redis.standards..{redisId}.pool.xxx                连接池配置，参考标准的： redis.clients.jedis.JedisPoolConfig
   
       以上配置，最终会注册以下 Bean（参见 StdRedisDefRegister）：
           ${redisId}Redis                         DefaultRedisImpl
           ${redisId}JedisProvider                 DefaultJedisProvider
           ${redisId}JedisPool                     redis.clients.jedis.JedisPool
           ${redisId}JedisPoolConfig               redis.clients.jedis.JedisPoolConfig
   
       示例：
       yyspring.redis.standards.common.server=127.0.0.1:6379
       
       然后代码中可以通过以下方式进行注入：
       @Resource(name = "commonRedis")
       Redis commonRedis;
   
       会注册Bean：
           commonRedis
           commonJedisProvider
           commonJedisPool
           commonJedisPoolConfig
           

# 哨兵模式 Redis
    哨兵模式 Redis 定义解析器， 解析格式：
    前缀： sentinel.redis
    配置项：
    注： RedisID, 唯一标识这个 Redis 实例
    yyspring.redis.sentinels.{redisId}.sentinels=host1:port1,host2:port2... 哨兵主机、端口列表英文逗号分割
    yyspring.redis.sentinels.{redisId}.masterName=xxx 要连接redis的名称
    yyspring.redis.sentinels.{redisId}.timeout=3000   连接超时，默认是3000
    yyspring.redis.sentinels.{redisId}.password=      安全链接密码，默认是null
    yyspring.redis.sentinels.{redisId}.database=0     连接的数据库，从0开始，默认是 0
    yyspring.redis.sentinels.{redisId}.pool.xxx=      连接池配置，参考标准的：
    
    
# 升龙类型Redis注册
    前缀： redis.rise
    配置项：
    注： Redis ID, 唯一标识这个 Redis 实例(这个目前适合所有配置项在环境变量里面,只需要知道数据源名称就可以自动组装配置)
    required: yyspring.redis.rises.{redisId}.name=cloudapp_cloudredis1000 升龙Redis数据源
    optional: yyspring.redis.rises.{redisId}.timeout=3000                 连接超时，默认是3000
    optional: yyspring.redis.rises.{redisId}.password=123456              安全链接密码，默认是null
    optional: yyspring.redis.rises.{redisId}.database                     连接的数据库，从0开始，默认是 0
    optional: yyspring.redis.rises.{redisId}.pool.xxx                     连接池配置，参考标准的：
                                                                redis.clients.jedis.JedisPoolConfig

    以上配置，最终会注册以下 Bean（参见 StdRedisDefRegister）：
        ${redisId}Redis                         DefaultRedisImpl
        ${redisId}JedisProvider                 DefaultJedisProvider
        ${redisId}JedisPool                     redis.clients.jedis.JedisPool
        ${redisId}JedisPoolConfig               redis.clients.jedis.JedisPoolConfig

    示例：
    redis.rise.common.name=cloudapp_cloudredis1000
    
    然后代码中可以通过以下方式进行注入：
    @Resource(name = "commonRedis")
    Redis commonRedis;

    会注册Bean：
        commonRedis
        commonJedisProvider
        commonJedisPool
        commonJedisPoolConfig redis.clients.jedis.JedisPoolConfig
        
    注意，如果在开发环境下，是没有写入到环境变量的，因此需要在开发环境下设置属性：
        cloudapp_cloudredis1000_host=xxx
        cloudapp_cloudredis1000_port=xxx


# 启用 Redis 的规则
    # 配置启用的 Redis IDS， 中间用英文逗号分隔，如果为空或为配置都会启用所有的 Redis
    yyspring.redis.enabled-ids=xxx
    
# 禁用 Redis 的规则
    # 配置禁用的 Redis IDS， 中间用英文逗号分隔, 支持通配符 *
    yyspring.redis.exclude-ids=xxx

# 设置主Redis
    # 主Redis
    yyspring.redis.primary-id=user
    