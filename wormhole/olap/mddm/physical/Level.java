package cn.bgotech.wormhole.olap.mddm.physical;

import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public interface Level extends ClassicMDDM {

    int HIGHEST_LEVEL = Integer.MAX_VALUE;

    static String generateSimpleName(int i) {
        return "level-" + i;
    }

    default Dimension getDimension() {
        return getHierarchy().getDimension();
    }

    @Override
    default boolean associatedWith(Cube cube) {
        return getHierarchy().associatedWith(cube);
    }

    Hierarchy getHierarchy();

    /**
     * 根据memberName返回当前Level对象下的一个维度成员
     * 如果有且只有一个同名维度成员，将其返回
     * 如果没有同名维度成员，返回null
     * 如果有不止一个同名维度成员，报错“level下具有多个名为memberName的维度成员，无法判定应该返回哪个”
     *
     * @param memberName
     * @return
     * @createdDate 2015/10/30
     */
    Member findUniqueNamedMember(String memberName);

    List<Member> getMembers();

    int getLevelValue();

    Level getAboveLevel();

}

