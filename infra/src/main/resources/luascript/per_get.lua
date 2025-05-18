local valueAndDelta = redis.call('mget', KEYS[1], KEYS[2])
local ttl = redis.call('pttl', KEYS[1])
-- 평탄화된 리스트로 반환
return { valueAndDelta[1], valueAndDelta[2], ttl }