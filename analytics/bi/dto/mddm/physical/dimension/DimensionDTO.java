package cn.bgotech.analytics.bi.dto.mddm.physical.dimension;

import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.RegionDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/6/29.
 */
public class DimensionDTO {

    private Dimension dimension;

    public static List<DimensionDTO> transform(List<Dimension> dimensions) {
        List<DimensionDTO> result = new ArrayList<>();
        for (Dimension d : dimensions) {
            result.add(new DimensionDTO(d));
        }
        return result;
    }

    public DimensionDTO(Dimension dimension) {
        this.dimension = dimension;
    }

    public long getMgId() {
        return dimension.getMgId();
    }

    public String getName() {
        return dimension.getName();
    }

    public long getDefaultMemberMgId() {
        return dimension.getDefaultMember().getMgId();
    }

    public boolean isMeasureDimension() {
        return dimension.isMeasureDimension();
    }

    public String getPredefinedDimensionType() {
        if (dimension instanceof DateDimension) {
            return "DATE";
        } else if (dimension instanceof RegionDimension) {
            return "REGION";
        }
        return null;
    }

    public Object getExtraInfo() {
        Map<String, Integer> extraInfo = new HashMap<>();
        if (dimension instanceof DateDimension) {
            extraInfo.put("memberMgIdStart", DateMember.MD_GLOBAL_ID_START);
            extraInfo.put("memberMgIdEnd", DateMember.MD_GLOBAL_ID_END);
            extraInfo.put("rootMemberMgId", DateMember.ROOT_DATE_MEMBER_MG_ID);
        }
        return extraInfo;
    }
}
