package cn.bgotech.analytics.bi.dto.mdx.syntax.structures;

import cn.bgotech.analytics.bi.dto.mddm.logical.combination.TupleDTO;
import cn.bgotech.wormhole.olap.mddm.logical.combination.Tuple;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/7/13.
 */
public class AxisDTO {

    private Axis axis;

    public AxisDTO(Axis axis) {
        this.axis = axis;
    }

    public int getThick() {
        return axis.getSet().getTuple(0).length();
    }

    public int getLength() {
        return axis.getSet().getTupleList().size();
    }

    public List<TupleDTO> getTuples() {
        List<TupleDTO> tupleDTOs = new ArrayList<>();
        for (Tuple tuple : axis.getSet().getTupleList()) {
            tupleDTOs.add(new TupleDTO(tuple));
        }
        return tupleDTOs;
    }

}
