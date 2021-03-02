//core_source_code!_yxIwInyIsIhn_11111


package cn.bgotech.wormhole.olap.mdx.auxi;

import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.List;

/**
 * Created by czg on 2019/7/14.
 * C - insert
 * U - update
 * D - delete
 */
public class /*^!*/AuxCUDMeasure/*?$*//*_yxIwInyIsIhn_11111*/ {

    private List<MultiDimensionalDomainSelector> mbrSelectors;

    private List<MeasureInfo> measures;

    public List<MultiDimensionalDomainSelector> getMbrSelectors() {
        return mbrSelectors;
    }

    public List<MeasureInfo> getMeasures() {
        return measures;
    }

    public AuxCUDMeasure(List<MultiDimensionalDomainSelector> _members, List<MeasureInfo> _measures) {
        mbrSelectors = _members;
        measures = _measures;

    }

    public static class MeasureInfo {

        private String name;
        private Double value;

        public MeasureInfo(String _mn, Double _meaVal) {
            name = _mn;
            value = _meaVal;
        }

        public String getName() {
            return name;
        }

        public Double getValue() {
            return value;
        }
    }

}
