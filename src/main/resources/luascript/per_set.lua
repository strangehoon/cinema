-- KEYS[1]: value key
-- KEYS[2]: delta key
-- ARGV[1]: value (JSON)
-- ARGV[2]: delta (ms)
-- ARGV[3]: TTL (ms)

local ttl = tonumber(ARGV[3])

redis.call('mset', KEYS[1], ARGV[1], KEYS[2], ARGV[2])
redis.call('pexpire', KEYS[1], ttl)
redis.call('pexpire', KEYS[2], ttl)

return 1