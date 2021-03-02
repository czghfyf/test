package cn.bgotech.wormhole.olap.mdx.bg_expansion;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhigang on 2019/3/12.
 * @deprecated 将被cn.bgotech.wormhole.olap.mdx.hf__XP__yf包中的类代替
 */
public class ExeDeleteMeasuresUnit extends VCE_ExecutionUnit {

    private List<String> measureMembers = new LinkedList<>();

    public List<String> getMeasureMembers() {
        return measureMembers;
    }

    public void deleteMeasure(String measureMemberName) {
        measureMembers.add(measureMemberName);

    }

}
