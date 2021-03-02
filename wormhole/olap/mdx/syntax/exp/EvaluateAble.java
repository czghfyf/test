package cn.bgotech.wormhole.olap.mdx.syntax.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicData;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by ChenZhiGang on 2017/6/16.
 */
@FunctionalInterface
public interface EvaluateAble {

    BasicData evaluate(MultiDimensionalVector v);

}
