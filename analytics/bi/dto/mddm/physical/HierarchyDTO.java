package cn.bgotech.analytics.bi.dto.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/6/29.
 */
public class HierarchyDTO {

    private Hierarchy hierarchy;

    public HierarchyDTO(Hierarchy h) {
        hierarchy = h;
    }

    public long getMgId() {
        return hierarchy.getMgId();
    }

    public String getName() {
        return hierarchy.getName();
    }

    public long getDimensionId() {
        return hierarchy.getDimension().getMgId();
    }

    public static List<HierarchyDTO> transform(List<Hierarchy> hierarchies) {
        List<HierarchyDTO> result = new ArrayList<>();
        for (Hierarchy h : hierarchies) {
            result.add(new HierarchyDTO(h));
        }
        return result;
    }
}
