//core_source_code!_yxIwIhhIyyIhv_11l1


package cn.bgotech.wormhole.olap.mdx.syntax.structures;

import cn.bgotech.wormhole.olap.mddm.logical.combination.Set;
import cn.bgotech.wormhole.olap.mdx.profile.SetPE;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class /*^!*/Axis/*?$*//*_yxIwIhhIyyIhv_11l1*/ {

    private static final List<String> AXIS_NAMES = Arrays.asList(new String[]{"COLUMNS", "ROWS", "PAGES", "CHAPTERS", "SECTIONS"});

    private int axisIdx;
    private SetPE setPe;
    private Set set;

    public Axis(int axisIdx, SetPE setPe) {
        this.axisIdx = axisIdx;
        this.setPe = setPe;
    }

    public Axis(String axisAlias, SetPE setPe) {
        this.axisIdx = AXIS_NAMES.indexOf(axisAlias.toUpperCase());
        this.setPe = setPe;
    }

    public int getAxisIdx() {
        return axisIdx;
    }

    public Set evolving(MultiDimensionalVector v) {
        return set = setPe.evolving(v);
    }

    public Set getSet() {
        return set;
    }

    public static int getIndex(String axisName) {
        return AXIS_NAMES.indexOf(axisName.toUpperCase());
    }
}
