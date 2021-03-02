package cn.bgotech.analytics.bi.component.olap.cache;

import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.bigdata.cache.VectorCache;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Component
public class MeasureValueCachePool implements VectorCache {

    @Autowired
    private SystemConfiguration sysCfg;

    private JedisPool jedisPool;

    public static String generateKey(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole) {
        return new StringBuilder(cubeId + "_")
                .append(new MultiDimensionalVector(universalMemberRoles).toString())
                .append(":").append(measureMemberRole.getMember().getMgId()).toString();
    }

    @Override
    public boolean hasMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole) {
        Jedis jedis = getRedisConnect();
        boolean keyExists = jedis.exists(generateKey(cubeId, universalMemberRoles, measureMemberRole));
        jedis.close();
        return keyExists;
    }

    private Jedis getRedisConnect() {
        if (jedisPool == null) {
            synchronized (this) {
                if (jedisPool == null)
                    jedisPool = new JedisPool(sysCfg.getRedisServer(), sysCfg.getRedisPort());
            }
        }
        return jedisPool.getResource();
    }

    @Override
    public Double getMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole) {
        Jedis jedis = getRedisConnect();
        String measure = jedis.get(generateKey(cubeId, universalMemberRoles, measureMemberRole));
        Double measureValue = null;
        if (!"X".equals(measure)) {
            measureValue = Double.parseDouble(measure);
        }
        jedis.close();
        return measureValue;
    }

    @Override
    public void setMeasureValue(long cubeId, List<MemberRole> universalMemberRoles, MemberRole measureMemberRole, Double measureValue) {
        Jedis jedis = getRedisConnect();
        jedis.set(generateKey(cubeId, universalMemberRoles, measureMemberRole), measureValue == null ? "X" : measureValue.toString());
        jedis.close();
    }
}
