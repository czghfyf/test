package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeUpdateMeasuresUnit extends VCE_ExecutionUnit {

    private Map<String, Double> nameValMap = new HashMap<>();

    public Map<String, Double> getNameValMap() {
        return nameValMap;
    }

    public void updateMeasure(String measureMemberName, String value) {
        nameValMap.put(measureMemberName, Double.parseDouble(value));
    }
}
