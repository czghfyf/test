package cn.bgotech.analytics.bi.dto.mddm.physical.role;

import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.component.olap.CVCEProxy;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.bigdata.MDVectorSet;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/6/28.
 */
public class DimensionRoleDTO {

    private DimensionRole dimensionRole;

    private MDVectorSet mdVectorSet;

    public static List<DimensionRoleDTO> transform(List<DimensionRole> dimensionRoles) {
        List<DimensionRoleDTO> result = new LinkedList<>();
        // Cube cube = dimensionRoles.get(0).getCube();
        // MDVectorSet vectorSet = ((CVCEProxy) OlapEngine.hold().getVectorComputingEngine()).getMDVectorSet(cube);
        for (DimensionRole dr : dimensionRoles) {
            result.add(new DimensionRoleDTO(dr));
        }
        return result;
    }

    public DimensionRoleDTO(DimensionRole dimensionRole) {
        this.dimensionRole = dimensionRole;
    }

    public DimensionRoleDTO(DimensionRole dimensionRole, MDVectorSet mdVectorSet) {
        this.dimensionRole = dimensionRole;
        this.mdVectorSet = mdVectorSet;
    }

    public Long getMgId() {
        return dimensionRole.getMgId();
    }

    public String getName() {
        return dimensionRole.getName();
    }

    public Long getDimensionId() {
        return ((DimensionRoleBean) dimensionRole).getDimensionId();
    }

    public Long getRootMemberId() {
        return dimensionRole.getDimension().getSuperRootMember().getMgId();
    }

    public Long getCubeId() {
        return ((DimensionRoleBean) dimensionRole).getCubeId();
    }

    public Object getExtraInfo() {
        Map<String, Integer> extraInfo = new HashMap<>();
        if (mdVectorSet != null) {
            Integer minYear = mdVectorSet.getDateDimensionRoleMinYear(dimensionRole);
            Integer maxYear = mdVectorSet.getDateDimensionRoleMaxYear(dimensionRole);
            if (minYear != null) {
                extraInfo.put("minYear", minYear);
            }
            if (maxYear != null) {
                extraInfo.put("maxYear", maxYear);
            }
        }
        return extraInfo;
    }
}
