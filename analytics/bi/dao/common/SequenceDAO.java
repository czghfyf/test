// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.common;

import cn.bgotech.analytics.bi.bean.seq.SequenceBean;

/**
 * Created by ChenZhiGang on 2017/5/16.
 */
public interface SequenceDAO {

    SequenceBean loadByName(String name);

    int save(SequenceBean sequenceBean);

    int update(SequenceBean sequenceBean);

}
