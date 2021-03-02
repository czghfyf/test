// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.member;

import cn.bgotech.wormhole.olap.mddm.physical.member.MeasureMember;

import java.util.Collections;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class MeasureMemberBean extends MemberBean implements MeasureMember {

    private int sorted_position = -1;

    @Override
    public int sortedPosition() {

        if (sorted_position > -1)
            return sorted_position;

        List<MeasureMember> mms = (List<MeasureMember>) getDimension().getMembers(null);
        for (int i = 0; i < mms.size(); i++) {
            if (mms.get(i).isRoot()) {
                mms.remove(i);
                break;
            }
        }
        Collections.sort(mms);
        return sorted_position = mms.indexOf(this);
    }
}
