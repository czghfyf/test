package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhigang on 2019/3/14.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class VCE_ExecutionUnit implements ExecutionUnit {

    public String spaceName, cubeName;
    public List<MultiDimensionalDomainSelector> tupleInfo = new LinkedList<>();

    @Override
    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public void addMemberSelector(MultiDimensionalDomainSelector memberSelector) {
        tupleInfo.add(memberSelector);
    }
}
