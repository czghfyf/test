package cn.bgotech.analytics.bi.system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by ChenZhiGang on 2017/5/31.
 */
@Component
public class SystemConfiguration {

    @Value("#{cfgproperties['bi.sys.init.admin']}")
    private String bsia;

    @Value("#{cfgproperties['bi.sys.init.password']}")
    private String bsip;

    @Value("#{cfgproperties['redis.enable']}")
    private boolean redisEnable;

    @Value("#{cfgproperties['redis.server']}")
    private String redisServer;

    @Value("#{cfgproperties['redis.port']}")
    private int redisPort;

    @Value("#{cfgproperties['mdvs.folder']}")
    private String mdvsFolder;

    @Value("#{cfgproperties['vce.root.master.host']}")
    private String vceRootMasterHost;

    @Value("#{cfgproperties['vce.root.master.port']}")
    private int vceRootMasterPort;

    @Value("#{cfgproperties['cvce.data.buf.size']}")
    private int vceDataBufSize;

    @Value("#{cfgproperties['mdd.listening.service.port']}")
    private int mddLisSerPort;

    @Value("#{cfgproperties['sys.start.cache.preset.data']}")
    private boolean cachePreDataOnStart;

    public boolean getCachePreDataOnStart() {
        return cachePreDataOnStart;
    }

    public String getBsia() {
        return bsia;
    }

    public String getBsip() {
        return bsip;
    }

    public String getMdvsFolder() {
        File directory = new File(mdvsFolder);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return mdvsFolder;
    }

    public String getRedisServer() {
        return redisServer;
    }

    public boolean isRedisEnable() {
        return redisEnable;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public int getMddLisSerPort() {
        return mddLisSerPort;
    }

    public int getVceDataBufSize() {
        return vceDataBufSize;
    }

    public String getVceRootMasterHost() {
        return vceRootMasterHost;
    }

    public int getVceRootMasterPort() {
        return vceRootMasterPort;
    }
}
