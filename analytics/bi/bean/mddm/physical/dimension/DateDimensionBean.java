// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.dimension;

import cn.bgotech.analytics.bi.bean.mddm.physical.member.DateMemberBean;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public class DateDimensionBean extends UniversalDimensionBean implements DateDimension {

//    @Override
//    public boolean isWithinRange(Space space) {
//        return false;
//    }


    @Override
    public DateMemberBean findMemberByFullNamesPath(List<String> fullNamesPath) {
        String yearName = fullNamesPath.remove(0);
        DateMemberBean targetDateBean = DateMemberBean.getYearByName(yearName);
        for (String s : fullNamesPath) {
            targetDateBean = targetDateBean.findNearestDescendantMember(s);
        }
        return targetDateBean;
    }

    @Override
    public Member getSuperRootMember() {
        DateMemberBean root = new DateMemberBean();
        root.setMgId((long) DateMember.ROOT_DATE_MEMBER_MG_ID);
        return root;
    }

    @Override
    public Member getDefaultMember() {
        return DateMemberBean.getYearByName(LocalDate.now().getYear() + "年");
    }
}
