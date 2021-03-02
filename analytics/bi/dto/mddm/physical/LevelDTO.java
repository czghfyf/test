package cn.bgotech.analytics.bi.dto.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.physical.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/6/29.
 */
public class LevelDTO {

    private Level level;

    public static List<LevelDTO> transform(List<Level> levels) {
        List<LevelDTO> result = new ArrayList<>();
        for (Level l : levels) {
            result.add(new LevelDTO(l));
        }
        return result;
    }

    public LevelDTO(Level level) {
        this.level = level;
    }

    public long getMgId() {
        return level.getMgId();
    }

    public String getName() {
        return level.getName();
    }

    public long getHierarchyId() {
        return level.getHierarchy().getMgId();
    }
}
