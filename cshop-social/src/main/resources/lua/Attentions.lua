-- 从 Redis 获取粉丝用户 ID
local follower_id = ARGV[1]

-- 添加列表最左侧
redis.call('LPUSH', KEYS[1], follower_id)

-- 列表长度
local len = redis.call('LLEN', KEYS[1])

-- 列表长度大于 200，删除最右侧
if len > 200 then
    redis.call('RPOP', KEYS[1])
end

return len
