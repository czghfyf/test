package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ChenZhigang on 2019/3/15.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeInsertMeasuresUnit extends VCE_ExecutionUnit {
    private Map<String, Double> measureValMap = new HashMap<>();

    public Map<String, Double> getMeasureValMap() {
        return measureValMap;
    }

    public void insertMeasure(String measureMemberName, String value) {
        measureValMap.put(measureMemberName, Double.parseDouble(value));
    }
}
