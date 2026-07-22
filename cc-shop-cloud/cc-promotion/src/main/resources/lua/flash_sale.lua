-- 秒杀 Lua 脚本：原子扣库存 + 限购校验
-- KEYS[1]: flash:stock:{itemId} (秒杀库存)
-- KEYS[2]: flash:limit:{itemId}:{userId} (用户购买次数)
-- ARGV[1]: userId
-- ARGV[2]: limitPerUser (限购数量)

-- 1. 检查库存
local stock = tonumber(redis.call('get', KEYS[1]))
if stock == nil then
    return -1  -- 库存未初始化
end
if stock <= 0 then
    return 0   -- 库存不足
end

-- 2. 检查限购
local limit = tonumber(ARGV[2])
local bought = tonumber(redis.call('get', KEYS[2]))
if bought == nil then
    bought = 0
end
if bought >= limit then
    return -2  -- 超过限购
end

-- 3. 扣减库存
redis.call('decr', KEYS[1])

-- 4. 增加购买次数
redis.call('incr', KEYS[2])

return 1  -- 成功
