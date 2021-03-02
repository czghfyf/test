package cn.bgotech.wormhole.olap.mddm.logical.combination;

import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class Tuple implements CombinationEntity {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//	package com.dao.wormhole.core.model.entity.compositeObjects;
//	import com.dao.wormhole.core.model.entity.role.IMemberRole;
//
//	/**
//	 * Tuple是矢量对象
//	 */
//	public class Tuple implements WormholeBaseCompositeEntity/*, Comparable<Tuple>*/ {
//
    private List<MemberRole> memberRoles = new ArrayList<>();
//
//	//	private static enum TupleConstructorType {
//	//		LEFT_RIGHT_TUPLE;
//	//	}
//
//	//	private TupleConstructorType tupleConstructorType = null;
//
//	//	private Tuple joinLeftTuple = null;
//	//	private Tuple joinRightTuple = null;
//
    public Tuple(Tuple joinLeftTuple, Tuple joinRightTuple) {
        memberRoles.addAll(joinLeftTuple.memberRoles);
        memberRoles.addAll(joinRightTuple.memberRoles);
    }
//
//	//	/**
//	//	 * @deprecated
//	//	 * @param m
//	//	 */
//	//	public Tuple(Member m) {
//	//		coreMembers = new ArrayList<Member>();
//	//		coreMembers.add(m);
//	//	}
//
    public Tuple(MemberRole mr) {
        memberRoles.add(mr);
    }
//
//	//	/**
//	//	 * @deprecated
//	//	 * @param members
//	//	 */
//	//	public Tuple(List<Member> members) {
//	//		coreMembers = members;
//	//	}
//
    public Tuple(List<MemberRole> mrs) {
        memberRoles.addAll(mrs);
    }
//
//	//	/**
//	//	 * index 0 的聚集优先级最高，也就是在有自定义计算公式成员的情况下计算顺序从index 0 开始
//	//	 * @deprecated
//	//	 * @return
//	//	 */
//	//	public List<Member> memberListSequence() {
//	////		List<Member> result = null;
//	////		switch (tupleConstructorType) {
//	////		case LEFT_RIGHT_TUPLE:
//	////			List<Member> l = this.joinLeftTuple.memberListSequence();
//	////			List<Member> r = this.joinRightTuple.memberListSequence();
//	////			result = new ArrayList<Member>(l);
//	////			result.addAll(r);
//	////
//	////			break;
//	////
//	////		default:
//	////			throw new WromholeRuntimeException("Tuple构造方法错误");
//	////		}
//	////		return result;
//	//		return new ArrayList<Member>(coreMembers);
//	//	}

    /**
     * index 0 的聚集优先级最高，也就是在有自定义计算公式成员的情况下计算顺序从index 0 开始
     * @return
     */
    public List<MemberRole> getMemberRoles() {
        return new ArrayList<>(memberRoles);
    }

    public int length() {
        return memberRoles.size();
    }

    public String getDisplay() {
        StringBuilder displayStr = new StringBuilder();
        for (int i = 0; i < this.memberRoles.size(); i++) {
            displayStr.append(memberRoles.get(i).getMember().getName());
            if (i < memberRoles.size() - 1) {
                displayStr.append(",");
            }
        }
        if (memberRoles.size() > 1) {
            return "(" + displayStr.toString() + ")";
        }
        return displayStr.toString();
    }

    @Override
    public boolean associatedWith(Cube cube) {
        throw new RuntimeException("TODO: at once. new method logic."); // TODO: at once. new method logic.
    }

    /**
     * TODO: Need to modify the multi-dimensional model design.
     *       This method should not be called on the current object.
     *       The method can only be called on a classic multidimensional model entity.
     *       Need to modify the multi-dimensional model design.
     *
     * @param segmentedSelector Multidimensional selector fragment
     * @return
     */
    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {

        logger.warn("This method should not be called on the current object. " +
                "The method can only be called on a classic multidimensional model entity. " +
                "Need to modify the multi-dimensional model design.");

        return null;
    }

//	//	@Override
//	//	public String toString() {
//	//		StringBuilder str = new StringBuilder("(");
//	//
//	//		Member m = null;
//	//		for (int i = 0; i < this.coreMembers.size(); i++) {
//	//			m = this.coreMembers.get(i);
//	//			str.append(m.getMemberName());
//	//			if (i < this.coreMembers.size() - 1) {
//	//				str.append(", ");
//	//			}
//	//		}
//	//		return str.append(")").toString();
//	//	}
//	//	@Override
//	//	public int compareTo(Tuple t) {
//	//		return this.memberListSequence().c
//	//	}
//
//
//	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tuple tuple = (Tuple) o;
        return memberRoles.equals(tuple.memberRoles);
    }

    @Override
    public int hashCode() {
        return memberRoles.hashCode();
    }
}


// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    package com.dao.wormhole.core.model.entity.compositeObjects;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //
    //    /**
    //     * Tuple是矢量对象
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-23 下午3:58:55
    //     *
    //     */
    //    public class Tuple implements WormholeBaseCompositeEntity/*, Comparable<Tuple>*/ {
    //
    //    //	@Deprecated
    //    //	private List<Member> coreMembers = null;
    //
    //        private List<IMemberRole> memberRoles = new ArrayList<IMemberRole>();
    //
    //    //	private static enum TupleConstructorType {
    //    //		LEFT_RIGHT_TUPLE;
    //    //	}
    //
    //    //	private TupleConstructorType tupleConstructorType = null;
    //
    //    //	private Tuple joinLeftTuple = null;
    //    //	private Tuple joinRightTuple = null;
    //
    //        public Tuple(Tuple joinLeftTuple, Tuple joinRightTuple) {
    //    //		tupleConstructorType = TupleConstructorType.LEFT_RIGHT_TUPLE;
    //    //		this.joinLeftTuple = joinLeftTuple;
    //    //		this.joinRightTuple = joinRightTuple;
    //
    //
    //    //		List<Member> l = joinLeftTuple.memberListSequence();
    //    //		List<Member> r = joinRightTuple.memberListSequence();
    //    //		coreMembers = new ArrayList<Member>(l);
    //    //		coreMembers.addAll(r);
    //
    //
    //            memberRoles.addAll(joinLeftTuple.memberRoles);
    //            memberRoles.addAll(joinRightTuple.memberRoles);
    //        }
    //
    //    //	/**
    //    //	 * @deprecated
    //    //	 * @param m
    //    //	 */
    //    //	public Tuple(Member m) {
    //    //		coreMembers = new ArrayList<Member>();
    //    //		coreMembers.add(m);
    //    //	}
    //
    //        public Tuple(IMemberRole mr) {
    //            memberRoles.add(mr);
    //        }
    //
    //    //	/**
    //    //	 * @deprecated
    //    //	 * @param members
    //    //	 */
    //    //	public Tuple(List<Member> members) {
    //    //		coreMembers = members;
    //    //	}
    //
    //        public Tuple(List<IMemberRole> mrs) {
    //            memberRoles.addAll(mrs);
    //        }
    //
    //    //	/**
    //    //	 * index 0 的聚集优先级最高，也就是在有自定义计算公式成员的情况下计算顺序从index 0 开始
    //    //	 * @deprecated
    //    //	 * @return
    //    //	 */
    //    //	public List<Member> memberListSequence() {
    //    ////		List<Member> result = null;
    //    ////		switch (tupleConstructorType) {
    //    ////		case LEFT_RIGHT_TUPLE:
    //    ////			List<Member> l = this.joinLeftTuple.memberListSequence();
    //    ////			List<Member> r = this.joinRightTuple.memberListSequence();
    //    ////			result = new ArrayList<Member>(l);
    //    ////			result.addAll(r);
    //    ////
    //    ////			break;
    //    ////
    //    ////		default:
    //    ////			throw new WromholeRuntimeException("Tuple构造方法错误");
    //    ////		}
    //    ////		return result;
    //    //		return new ArrayList<Member>(coreMembers);
    //    //	}
    //
    //        /**
    //         * index 0 的聚集优先级最高，也就是在有自定义计算公式成员的情况下计算顺序从index 0 开始
    //         * @return
    //         */
    //        public List<IMemberRole> getMemberRoles() {
    //            return new ArrayList<IMemberRole>(memberRoles);
    //        }
    //
    //        public int length() {
    //            return memberRoles.size();
    //        }
    //
    //        public String getDisplay() {
    //            StringBuilder displayStr = new StringBuilder();
    //            for (int i = 0; i < this.memberRoles.size(); i++) {
    //                displayStr.append(memberRoles.get(i).getMember().getName());
    //                if (i < memberRoles.size() - 1) {
    //                    displayStr.append(",");
    //                }
    //            }
    //            if (memberRoles.size() > 1) {
    //                return "(" + displayStr.toString() + ")";
    //            }
    //            return displayStr.toString();
    //        }
    //
    //    //	@Override
    //    //	public String toString() {
    //    //		StringBuilder str = new StringBuilder("(");
    //    //
    //    //		Member m = null;
    //    //		for (int i = 0; i < this.coreMembers.size(); i++) {
    //    //			m = this.coreMembers.get(i);
    //    //			str.append(m.getMemberName());
    //    //			if (i < this.coreMembers.size() - 1) {
    //    //				str.append(", ");
    //    //			}
    //    //		}
    //    //		return str.append(")").toString();
    //    //	}
    //    //	@Override
    //    //	public int compareTo(Tuple t) {
    //    //		return this.memberListSequence().c
    //    //	}
    //
    //
    //    }

// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????
