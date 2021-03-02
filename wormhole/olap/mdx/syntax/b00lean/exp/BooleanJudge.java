package cn.bgotech.wormhole.olap.mdx.syntax.b00lean.exp;

import cn.bgotech.wormhole.olap.mddm.data.BasicBoolean;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;

/**
 * Created by chenzhigang on 2018/2/28.
 */
public interface BooleanJudge {

    BasicBoolean determine(MultiDimensionalVector vector);

}
