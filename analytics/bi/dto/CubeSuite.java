package cn.bgotech.analytics.bi.dto;

import cn.bgotech.analytics.bi.dto.mddm.physical.CubeDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.HierarchyDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.LevelDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.dimension.DimensionDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.member.MemberDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.role.DimensionRoleDTO;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/6/29.
 */
public class CubeSuite {

    private Cube cube;

    public CubeSuite(Cube cube) {

        this.cube = cube;

    }

    public CubeDTO getCube() {
        return new CubeDTO(cube);
    }

    public List<DimensionRoleDTO> getDimensionRoles() {
        return DimensionRoleDTO.transform(cube.getDimensionRoles(null));
    }

    public List<DimensionDTO> getDimensions() {
        return DimensionDTO.transform(cube.getDimensions(null));
    }

    public List<HierarchyDTO> getHierarchies() {
        return HierarchyDTO.transform(cube.getHierarchies(null));
    }

    public List<LevelDTO> getLevels() {
        return LevelDTO.transform(cube.getLevels(null));
    }

    public List<MemberDTO> getMembers() {
        return MemberDTO.transform(cube.getMembers(null));
    }
}
