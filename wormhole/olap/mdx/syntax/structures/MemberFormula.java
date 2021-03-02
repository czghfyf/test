//core_source_code!_yxIwIhhIyyIhv_1lll


package cn.bgotech.wormhole.olap.mdx.syntax.structures;

import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class /*^!*/MemberFormula/*?$*//*_yxIwIhhIyyIhv_1lll*/ implements WithFormula, SelectedByMDDAble {

    private MultiDimensionalDomainSelector mbrMatch;
    private Expression exp;

    public MemberFormula(MultiDimensionalDomainSelector mbrMatch, Expression exp) {
        this.mbrMatch = mbrMatch;
        this.exp = exp;
    }

    @Override
    public MultiDimensionalDomainSelector getMDDSelector() {
        return mbrMatch;
    }

    public Expression getExp() {
        return exp;
    }
}
