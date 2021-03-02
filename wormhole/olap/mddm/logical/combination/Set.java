package cn.bgotech.wormhole.olap.mddm.logical.combination;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class Set implements CombinationEntity {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Tuple> tupleList = new ArrayList<>();

    public Set(Tuple t) {
        tupleList.add(t);
    }

    public Set(List<Member> members, DimensionRole dimensionRole) {
        for (Member m : members) {
            tupleList.add(new Tuple(new MemberRole(dimensionRole, m)));
        }
    }

    public Set(List<? extends BasicEntityModel> entities) {
        if (entities.isEmpty()) {
            return; // this is a null set
        }
        if (entities.get(0) instanceof MemberRole) {
            for (BasicEntityModel e : entities) {
                tupleList.add(new Tuple((MemberRole) e));
            }
        } else if (entities.get(0) instanceof Set) {
            for (int i = 0; i < entities.size(); i++) {
                for (int j = 0; j < ((Set) entities.get(i)).getTupleList().size(); j++) {
                    this.tupleList.add(((Set) entities.get(i)).getTupleList().get(j));
                }
            }
        } else if (entities.get(0) instanceof Tuple) {
            for (int i = 0; i < entities.size(); i++) {
                tupleList.add((Tuple) entities.get(i));
            }
        } else {
            throw new OlapRuntimeException("only processing on MemberRole, Set and Tuple class is supported");
        }
    }

    public List<Tuple> getTupleList() {
        return new ArrayList<>(tupleList);
    }

    public boolean isEmpty() {
        return tupleList.isEmpty();
    }

    public Tuple getTuple(int i) {
        return tupleList.get(i);
    }

    @Override
    public boolean associatedWith(Cube cube) {
        throw new RuntimeException("TODO: at once. new method logic."); // TODO: at once. new method logic.
    }

    /**
     * TODO: Need to modify the multi-dimensional model design.
     *       This method should not be called on the current object.
     *       The method can only be called on a classic multidimensional model entity(C, D, H, L, M, as well as their related roles).
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
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //    ///20151023
    //    package com.dao.wormhole.core.model.entity.compositeObjects;
    //
    //            import java.util.ArrayList;
    //            import java.util.List;
    //
    //            import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.entity.IDimensionRole;
    //    //import com.dao.wormhole.core.model.entity.IMemberRole;
    //    //import com.dao.wormhole.core.exception.WormholeException;
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //    //import com.dao.wormhole.core.model.domain.WormholeBaseObject;
    //    //import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //    //import com.dao.wormhole.core.model.entity.Cube;
    //            import com.dao.wormhole.core.model.entity.Member;
    //            import com.dao.wormhole.core.model.entity.role.IDimensionRole;
    //            import com.dao.wormhole.core.model.entity.role.IMemberRole;
    //    //import com.dao.wormhole.core.model.entity.MemberRole;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //    //import com.dao.wormhole.core.model.entity.WormholeBaseEntity;
    //            import com.dao.wormhole.core.model.entity.role.MemberRole;
    //
    //    /**
    //     *
    //     * @author ChenZhigang
    //     * @version 创建时间:2014-1-23 下午2:58:25
    //     *
    //     */
    //    public /*abstract*/ class Set implements WormholeBaseCompositeEntity /*WormholeEntityBaseObject*/ {
    //
    //        private List<Tuple> coreTuples = new ArrayList<Tuple>();
    //
                                                                                //        public Set(List<WormholeBaseObject> es) {
                                                                                //
                                                                                //            if (es.isEmpty()) {
                                                                                //                // 如果es为空,则当前Set对象表示一个空集合
                                                                                //                return;
                                                                                //            }
                                                                                //
                                                                                //            if (es.get(0) instanceof IMemberRole) {
                                                                                //                for (WormholeBaseObject e : es) {
                                                                                //                    coreTuples.add(new Tuple((IMemberRole)e));
                                                                                //                }
                                                                                //            } else if (es.get(0) instanceof Set) {
                                                                                //                List<Set> tss = new ArrayList<Set>();
                                                                                //                for (WormholeBaseObject e : es) {
                                                                                //                    tss.add((Set)e);
                                                                                //                }
                                                                                //                coreTuples = createSet(tss);
                                                                                //            } else if (es.get(0) instanceof Tuple) {
                                                                                //                for (WormholeBaseObject e : es) {
                                                                                //                    coreTuples.add((Tuple)e);
                                                                                //                }
                                                                                //            } else {
                                                                                //                throw new /*Wromhole*/RuntimeException("不是List<Member>或List<Set>或List<Tuple>");
                                                                                //            }
                                                                                //        }
    //    //	/**
    //    //	 * @deprecated
    //    //	 * @param list
    //    //	 * @param clazz
    //    //	 * @see #Set(List)
    //    //	 */
    //    //	public Set(List/*<WormholeBaseObject>*/ list, Class/*<WormholeBaseObject>*/ clazz) {
    //    //		if (clazz.equals(Member.class)) {
    //    //			/*List<WormholeBaseObject> members = list;*/
    //    //			for (/*Member*//*WormholeBaseObject*/Object m : list) {
    //    //				coreTuples.add(new Tuple((Member)m));
    //    //			}
    //    //		} else if (clazz.equals(Set.class)) {
    //    //			/*List<WormholeBaseObject> sets = list;*/
    //    //			List<Set> tss = new ArrayList<Set>();
    //    //			for (/*WormholeBaseObject*/Object s : list) {
    //    //				tss.add((Set)s);
    //    //			}
    //    //			coreTuples = createSet(tss);
    //    //		} else if (clazz.equals(Tuple.class)) {
    //    //			/*List<Tuple> tts = new ArrayList<Tuple>();*/
    //    //			for (/*WormholeBaseObject*/Object t : list) {
    //    //				coreTuples.add((Tuple)t);
    //    //			}
    //    //			/*coreTuples = tts;*/
    //    //		} else {
    //    //			throw new /*Wromhole*/RuntimeException("不是List<Member>或List<Set>或List<Tuple>");
    //    //		}
    //    //	}
    //
                                                                                //        private List<Tuple> createSet(List<Set> sets) {
                                                                                //    //		List<Tuple> returnThisTuples = sets.get(0).getTupleList();
                                                                                //    //		for (int i = 0; i < sets.size() - 1; i++) {
                                                                                //    //			returnThisTuples = createTupleList(returnThisTuples, sets.get(i + 1).getTupleList());
                                                                                //    //		}
                                                                                //    //		return returnThisTuples;
                                                                                //
                                                                                //            List<Tuple> returnThisTuples = new ArrayList<Tuple>();
                                                                                //            for (Set s : sets) {
                                                                                //                for (Tuple t : s.getTupleList()) {
                                                                                //                    returnThisTuples.add(t);
                                                                                //                }
                                                                                //            }
                                                                                //            return returnThisTuples;
                                                                                //        }
                                                                                //
    //    //	private List<Tuple> createTupleList(List<Tuple> joinLeft,
    //    //			List<Tuple> joinRight) {
    //    //		List<Tuple> tuples = new ArrayList<Tuple>();
    //    //		for (Tuple tf : joinLeft) {
    //    //			for (Tuple tr : joinRight) {
    //    //				tuples.add(new Tuple(tf, tr));
    //    //			}
    //    //		}
    //    //		return tuples;
    //    //	}
    //
    //        public Set(Tuple tuple) {
    //            coreTuples.add(tuple);
    //        }
    //
    //    //	@Override
    //    //	public int getWormholeID() {
    //    //		return Integer.MIN_VALUE;
    //    //	}
    //
    //    //	@Override
    //    //	public boolean byCubeReference(Cube cube) {
    //    //		return false;
    //    //	}
    //
    //    //	@Override
    //    //	public WormholeEntityBaseObject selectHyperspaceModelObjectByPath(
    //    //			List<String> path) {
    //    //		new WormholeException("请不要调用此方法Set.selectHyperspaceModelObjectByPath(List<String>)").printStackTrace();
    //    //		return null;
    //    //	}
    //
    //        public Tuple getTuple(int i) {
    //            return this.coreTuples.get(i);
    //        }
    //
    //        public int tupleSize() {
    //            return coreTuples.size();
    //        }
                                                                                            //
                                                                                            //        public List<Tuple> getTupleList() {
                                                                                            //            return new ArrayList<Tuple>(this.coreTuples);
                                                                                            //        }
                                                                                            //
    //        /**
    //         * 如果Set表示一个空集合,返回true
    //         * @return
    //         */
    //        public boolean isEmpty() {
    //            return coreTuples.isEmpty();
    //        }
    //
    //    //	@Override
    //    //	public String toString() {
    //    //		StringBuilder str = new StringBuilder("{");
    //    //
    //    //		Tuple t = null;
    //    //		for (int i = 0; i < this.coreTuples.size(); i++) {
    //    //			t = this.coreTuples.get(i);
    //    //			str.append(t.toString());
    //    //			if (i < this.coreTuples.size() - 1) {
    //    //				str.append(", ");
    //    //			}
    //    //		}
    //    //		return str.append("}").toString();
    //    //	}
    //
    //    }
    //
    //    /********************************************************************************************/
    //
    //    //package com.dao.wormhole.core.hyperspace;
    //    //
    //    //import java.util.ArrayList;
    //    //import java.util.List;
    //    //
    //    //import com.dao.wormhole.core.exception.WormholeException;
    //    //import com.dao.wormhole.core.exception.WromholeRuntimeException;
    //    ////import com.dao.wormhole.core.model.VectorCoordinateFragment;
    //    //
    //    ///**
    //    // *
    //    // * @author ChenZhigang
    //    // * @version 创建时间:2014-1-23 下午2:58:25
    //    // *
    //    // */
    //    //public class Set implements HyperspaceModelObject {
    //    //
    //    //	public Set(List list, Class cla55) {
    //    //		if (cla55.equals(Member.class)) {
    //    //			List<Member> members = list;
    //    //		} else if (cla55.equals(Set.class)) {
    //    //			List<Set> sets = list;
    //    //		} else if (cla55.equals(Tuple.class)) {
    //    //			List<Tuple> tuples = list;
    //    //		} else {
    //    //			throw new WromholeRuntimeException("不是List<Member>或List<Set>或List<Tuple>");
    //    //		}
    //    //	}
    //    //
    //    //	public Set(Tuple tuple) {
    //    //	}
    //    //
    //    //	@Override
    //    //	public boolean byCubeReference(Cube cube) {
    //    //		return false;
    //    //	}
    //    //
    //    //	@Override
    //    //	public HyperspaceModelObject selectHyperspaceModelObjectByPath(
    //    //			List<String> path) {
    //    //		new WormholeException("请不要调用此方法Set.selectHyperspaceModelObjectByPath(List<String>)").printStackTrace();
    //    //		return null;
    //    //	}
    //    //
    //    //	public Tuple getTuple(int i) {
    //    //		return null;
    //    //	}
    //    //
    //    //	public int tupleSize() {
    //    //		return 0;
    //    //	}
    //    //
    //    //	public List<Tuple> getTupleList() {
    //    //		return new ArrayList<Tuple>();
    //    //	}
    //    //
    //    //}
    //

// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????