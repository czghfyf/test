// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.LevelBean;
import cn.bgotech.analytics.bi.bean.mddm.physical.member.*;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public interface MemberDAO {

    default int save(MemberBean member) {
        if (member instanceof RegionMemberBean) {
            _insertRegion((RegionMemberBean) member);
        }
        return _insert(member);
    }

    int _insertRegion(RegionMemberBean member);

    int _insert(MemberBean member);

    default MemberBean load(long id) {
        MemberBean member = _select(id);
        if (member == null) {
            return null;
        }
        RegionMemberBean region = _selectRegion(id);
        if (region != null) {
            BGTechUtil.copyProperties(region, member);
            return region;
        }
        MemberBean memberBean = member.isMeasure() ? new MeasureMemberBean() : new UniversalMemberBean();
        BGTechUtil.copyProperties(memberBean, member);
        return memberBean;
    }

    MemberBean _select(long id);

    RegionMemberBean _selectRegion(long id);

    default List<Member> loadChildren(Member member) {
        List<Member> children = new ArrayList<>();
        for (Long childId : _loadChildren(member)) {
            children.add(load(childId));
        }
        return children;
    }

    List<Long> _loadChildren(Member member);

    default List<Member> loadMembers(LevelBean levelBean) {
        List<Member> members = new ArrayList<>();
        for (Long memberId : _loadLevelMembers(levelBean)) {
            members.add(load(memberId));
        }
        return members;
    }

    List<Long> _loadLevelMembers(LevelBean levelBean);

    default List<MemberBean> loadByName(String name) {
        List<MemberBean> members = new LinkedList<>();
        for (Long id : _loadByName(name)) {
            members.add(load(id));
        }
        return members;
    }

    List<Long> _loadByName(String name);

    default List<MemberBean> loadByParams(Object... params) throws OlapException {
        Map<String, Object> paramMap = new HashMap<>();
        if (params.length % 2 != 0) {
            throw new OlapException("The number of param(K, V) must be a double number");
        }
        for (int i = 0; i < params.length; i += 2) {
            paramMap.put(params[i].toString(), params[i + 1]);
        }

//        List<Long> idList = _loadByParams(paramMap);

        List<MemberBean> members = _batchLoad(_loadByParams(paramMap));

//        for (Long id : idList) {
//            members.add(load(id));
//        }

        Collections.sort(members, Comparator.comparingLong(v -> v.getMgId()));
        return members;
    }

    default List<MemberBean> _batchLoad(List<Long> idList) {
        List<MemberBean> members = new ArrayList<>();

        if (idList.isEmpty())
            return members;

        // measure members
        members.addAll(loadMeasureMembersInIds(idList));
        // universal members
        members.addAll(loadUniversalMembersInIds(idList));
        return members;
    }

    List<MeasureMemberBean> loadMeasureMembersInIds(List<Long> idList);

    default List<UniversalMemberBean> loadUniversalMembersInIds(List<Long> idList) {
//        List<UniversalMemberBean> universalMembers = _loadUniversalMemberBaseInfoInIds(idList);
        Set<UniversalMemberBean> universalMembers = new HashSet<>(_loadUniversalMemberBaseInfoInIds(idList));
        universalMembers.addAll(loadRegionMembersInIds(idList));
        return new ArrayList<>(universalMembers);
    }

    List<RegionMemberBean> loadRegionMembersInIds(List<Long> idList);

    List<UniversalMemberBean> _loadUniversalMemberBaseInfoInIds(List<Long> idList);

    List<Long> _loadByParams(Map<String, Object> params);

//    List<MemberBean> queryByAttributes(MemberBean member);

    int update(MemberBean memberBean);

    List<MemberBean> loadByNameAndParentMemberId(MemberBean param);

}
