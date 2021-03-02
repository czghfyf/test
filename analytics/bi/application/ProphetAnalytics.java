package cn.bgotech.analytics.bi.application;

import cn.bgotech.analytics.bi.cache.CacheByCollectionInSameJVM;
import cn.bgotech.analytics.bi.cache.CacheService;
import cn.bgotech.analytics.bi.cache.MDDMCacheService;
import cn.bgotech.analytics.bi.component.system.SystemStatus;
import cn.bgotech.analytics.bi.service.InitializationActionService;
import cn.bgotech.analytics.bi.system.config.SystemConfiguration;
import cn.bgotech.wormhole.olap.OlapEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ProphetAnalytics {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SystemStatus sysStat;

    @Autowired
    private SystemConfiguration sysCfg;

    @Autowired
    private InitializationActionService initActionService;

    @Autowired
    private CacheService cache;

    @Autowired
    private MDDMCacheService mdmCache;

    @Autowired
    @Qualifier("olap_engine_instance")
    private OlapEngine olapEngine;

    @PostConstruct
    public void systemInitialize() {

        OlapEngine.setSingleInstance(olapEngine);

        if (sysStat.get(SystemStatus.Type.BI_SYSTEM_INIT_STATUS) == null
                || sysStat.match(SystemStatus.Type.BI_SYSTEM_INIT_STATUS, SystemStatus.Value.WAIT_FOR_INIT)) {
            initActionService.init();
        }
        olapEngine.startup();

        if (sysCfg.getCachePreDataOnStart())
            cache.systemPresetDataInit();

        CacheByCollectionInSameJVM.setCacheInstance(cache);

        olapEngine.openListeningService(sysCfg.getMddLisSerPort());

        logger.info("Prophet Analytics startup success ...");

        MDDMCacheService.SI = mdmCache;
    }
}
