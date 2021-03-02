package cn.bgotech.analytics.bi.service;

import cn.bgotech.analytics.bi.dto.DTOUtils;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResultSFP;
import cn.bgotech.wormhole.olap.mdx.bg_expansion.MDDManagementAssistant;
import cn.bgotech.wormhole.olap.mdx.auxi.Auxiliary_MDDL_NQ;
import cn.bgotech.wormhole.olap.mdx.parser.ParseException;
import cn.bgotech.wormhole.olap.mdx.parser.WormholeMDXParser;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by czg on 2019/6/1.
 */
@Service
public class ScriptToolService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public Object executionScript(String script) throws Exception {
        Object obj;
        try {
            obj = new WormholeMDXParser(script, OlapEngine.hold()).execute();
        } catch (ParseException e) {
            throw new Exception("语法错误: " + e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
            throw e;
        }
        if (obj instanceof MDXQueryResultSFP)
            return execution((MDXQueryResultSFP) obj);
        else if (obj instanceof MDDManagementAssistant)
            return execution((MDDManagementAssistant) obj);
        else if (obj instanceof Auxiliary_MDDL_NQ)
            return ((Auxiliary_MDDL_NQ) obj).handle_MDDL_NQ();
        else
            throw new Exception("The program cannot be executed due to an unknown compilation result.");
    }

    public Object execution(MDDManagementAssistant mma) {
        String str = OlapEngine.hold().handleMngAssistant(mma);
        Map<String, Object> result = new HashedMap();
        result.put("type", "META_DATA_MNG");
        result.put("_output_", str);
        return result;

    }

    public Object execution(MDXQueryResultSFP sfp) {
        Map<String, Object> _2D_PIVOT_GRID
                = DTOUtils.convertTo2DPivotGrid((MDXQueryResult) new MDXQueryResult(sfp).transform());
        Map<String, Object> result = new HashedMap();
        result.put("type", "MDX_QUERY");
        result.put("_output_", _2D_PIVOT_GRID);
        return result;
    }

}
