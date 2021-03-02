package cn.bgotech.wormhole.olap.mddm.physical.member;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.ContextAtExecutingMDX;
import cn.bgotech.wormhole.olap.mdx.syntax.exp.Expression;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public class CalculatedMember implements Member { // TODO: at once. complete this class.

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ContextAtExecutingMDX ctx;
    private MultiDimensionalDomainSelector selector;
    private Expression exp;

    private DimensionRole dimensionRole;

    public CalculatedMember(ContextAtExecutingMDX ctx, MultiDimensionalDomainSelector selector, Expression exp) {

        // verification: does the entity with the same name exist ?
        if (ctx.getOLAPEngine().findUniqueEntity(ctx.getCube(), BasicEntityModel.class, selector) != null) {
            throw new OlapRuntimeException("already exist entity named " + selector);
        }

        // verification: whether the name length is legal ?
        if (selector.length() < 2) {
            // throw new OlapRuntimeException(selector + " is too short");
            logger.warn(selector + " is too short");
        } else if (selector.length() > 2) {
            logger.warn(selector + " is too long");
        }

        dimensionRole = ctx.getOLAPEngine().getMddmStorageService().findDimensionRole(ctx.getCube(), selector.getPart(0));
        if (dimensionRole == null) { // verification: is dimensionRole empty ?
            throw new OlapRuntimeException(
                    "have no DimensionRole entity named " + selector.getPart(0) +
                            " in cube[" + ctx.getCube() + "]");
        }

        this.ctx = ctx;
        this.selector = selector;
        this.exp = exp;

    }

    @Override
    public Long getMgId() {
        return null; // have no MDDM Global ID
    }

    @Override
    public String getName() {
        return selector.getPart(selector.length() - 1).getImage();
    }

    @Override
    public boolean isWithinRange(Space space) {
        return false; // TODO: do it later.
    }


    /**
     * 根据做在维度角色返回自身的成员角色对象
     * @return
     */
    public MemberRole getSelfRole() {
        return new MemberRole(dimensionRole, this);
    }

    /**
     * TODO: 计算公式成员是否与某个Cube匹配应该由其属性expression决定
     * @param cube
     * @return 暂时返回: ctx.getCube().equals(cube)
     */
    @Override
    public boolean associatedWith(Cube cube) {
        return ctx.getCube().equals(cube);
    }

    @Override
    public Member selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        return null;
    }

    @Override
    public Member getParent() {
        return null; // TODO: CalculatedMember has no parent.
    }

    @Override
    public Dimension getDimension() {
        return dimensionRole.getDimension();
    }

    /**
     * have no descendant member
     * @param descendantId
     * @return
     */
    @Override
    public Member findDescendantMember(long descendantId) {
        return null;
    }

    /**
     * have no descendant member
     * @param descendantName
     * @return
     */
    @Override
    public Member findNearestDescendantMember(String descendantName) {
        return null;
    }

    @Override
    public boolean hasAncestor(Member m) {
        return false; // CalculatedMember has no ancestor
    }

    @Override
    public boolean isMeasure() {
        return dimensionRole.getDimension().isMeasureDimension();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Member getBrotherMember(int offset) {
        return Member.NONEXISTENT_MEMBER; // return a fixed object that represents an empty member
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    /**
     * does not belong a hierarchy
     * @return
     */
    @Override
    public Hierarchy getHierarchy() {
        return null;
    }

    /**
     * does not belong a level
     * @return
     */
    @Override
    public Level getLevel() {
        return null;
    }


    @Override
    public int compareTo(Member m) {
        return -1; // TODO: Why return a fixed value ?
    }


    public Expression getExpression() {
        return exp;
    }

    @Override
    public List<Integer> relativePosition(Member ancestorMember) {
        // TODO Auto-generated method stub
        return new ArrayList();
    }

    @Override
    public Member moveRelativePosition(List<Integer> relativePosition) {
        // TODO Auto-generated method stub
        return null;
    }

}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //	package com.dao.wormhole.core.model.entity;
    //
    //	public class CustomFormulaMember /*extends*/implements Member, ContextProduction { // 自定义计算公式成员可以属于度量维或普通维
                                                                                                                //
                                                                                                                //		public CustomFormulaMember(AnalyticalContext context, /*boolean measureMember,*/CharacterBlocksToken memberNameToken, Expression expression/*, Cube cubeAsExecuted*/) {
                                                                                                                //          // do something ...
                                                                                                                //			checkMemberNameToken(memberNameToken); // TODO: what's this code ?
                                                                                                                //		}
                                                                                                                //
                                                                                                                //		/**
                                                                                                                //		 * 校验
                                                                                                                //		 * @param memberNameToken
                                                                                                                //		 */
                                                                                                                //		private void checkMemberNameToken(CharacterBlocksToken memberNameToken) {
                                                                                                                //			ModelService ms = // WormholeApplication.getWormholeAppInstance().getModelService();
                                                                                                                //					context.getWormholeApp().getModelService();
                                                                                                                //
                                                                                                                //			// memberNameToken长度必须为2
                                                                                                                //			if (memberNameToken.size() != 2) {
                                                                                                                //	//			throw new WromholeRuntimeException(memberNameToken + " 长度不符合要求(目前长度要求为2)");
                                                                                                                //				WormholeLog.WARN(memberNameToken + " size = " + memberNameToken.size() + ", 建议长度为2");
                                                                                                                //			}
                                                                                                                //
                                                                                                                //	//		// memberNameToken.getCharacterBlock(0)是一个维度
                                                                                                                //	//		Dimension d = ms.getDimensionByName(memberNameToken.getCharacterBlock(0), this.cubeAsExecuted);
                                                                                                                //			// memberNameToken.getCharacterBlock(0)是一个维度角色
                                                                                                                //			IDimensionRole dr = // ms.findDimensionRole(memberNameToken.getCharacterBlock(0), this.context.getExecuteCube());
                                                                                                                //					ms.findDimensionRole(memberNameToken.getBlock(0), this.context.getExecuteCube());
                                                                                                                //					// create modelservice method
                                                                                                                //
                                                                                                                //			if (dr == null) {
                                                                                                                //				throw new /*Wromhole*/RuntimeException("找不到维度角色：cube[" + context.getExecuteCube().getName() + "]与dimension[" + memberNameToken.getBlock(0).getText() + "]");
                                                                                                                //			}
                                                                                                                //
                                                                                                                //			// memberNameToken.getBlock(0)所表示的维度角色下不存在名为memberNameToken.getCharacterBlock(1)的对象
                                                                                                                //			if (ms.findUniqueWormholeEntity(memberNameToken, context.getExecuteCube()) != null) {
                                                                                                                //				throw new RuntimeException("已经存在一个名为[" + memberNameToken.toString() + "]的对象，不能再将其定义成CustomFormulaMember对象");
                                                                                                                //			}
                                                                                                                //		}
                                                                                                                //
    //		@Override
    //		public int getWormholeID() {
    //			return -1;
    //		}
    //
                                                                                                    //		@Override
                                                                                                    //		public boolean isMeasureMember() {
                                                                                                    //	//		ModelService modelService = WormholeApplication.getWormholeAppInstance().getModelService();
                                                                                                    //	//		Dimension dimension = modelService.loadDimensionsByName(memberNameToken.getCharacterBlock(0)).get(0);
                                                                                                    //	//		return dimension.isMeasureDimension();
                                                                                                    //
                                                                                                    //			return this.dimRole.getDimension().isMeasureDimension();
                                                                                                    //		}
                                                                                                    //
                                                                                                    //		@Override
                                                                                                    //		public boolean byCubeReference(Cube cube) {
                                                                                                    //			// TODO 计算公式成员是否与某个Cube匹配应该由其属性expression决定, 此方法暂时返回false
                                                                                                    //			/*return false;*/
                                                                                                    //
                                                                                                    //			return this.context.getExecuteCube().equals(cube);
                                                                                                    //		}
                                                                                                    //
    //		@Override
    //		public Member selectWormholeEntityByPath(List<String> path) {
    //			// TODO 直接返回 null
    //			return null;
    //		}
                                                                                                //
                                                                                                //		@Override
                                                                                                //		public WormholeBaseEntity selectSingleEntity(List<EntityPathBlock> path) {
                                                                                                //			// TODO Auto-generated method stub
                                                                                                //			return null;
                                                                                                //		}
                                                                                                //
                                                                                                //		@Override
                                                                                                //		public int compareTo(Member m) {
                                                                                                //			return -1;
                                                                                                //		}
                                                                                                //
    //		@Override
    //		public Level getLevel() {
    //			// TODO 计算公式成员没有 Level
    //			return null;
    //		}
    //
    //		@Override
    //		public boolean isLeaf() {
    //			// TODO 计算公式成员都是最明细级
    //			return true;
    //		}
    //
    //		@Override
    //		public Member getParentMember() {
    //			// TODO 计算公式成员没有父节点
    //			return null;
    //		}
    //
    //		public Expression getExpression() {
    //			return expression;
    //		}
    //
    //		public String getFormulaImage() {
    //			return this.memberNameToken.getImage();
    //		}
                                                                                                    //
                                                                                                    //		/**
                                                                                                    //		 * 计算公式成员不是Super Root成员
                                                                                                    //		 */
                                                                                                    //		@Override
                                                                                                    //		public boolean isSuperRootMember() {
                                                                                                    //			return false;
                                                                                                    //		}
                                                                                                    //
    //		/**
    //		 * 计算公式成员不属于任一Hierarchy
    //		 */
    //		@Override
    //		public Hierarchy getHierarchy() {
    //			return null;
    //		}
    //
    //	//	@Override
    //	//	public String getMemberName() {
    //	//		return getName();
    //	//	}
                                                                                            //
                                                                                            //		@Override
                                                                                            //		public Dimension getDimension() {
                                                                                            //	//		ModelService modelService = WormholeApplication.getWormholeAppInstance().getModelService();
                                                                                            //	//		return modelService.getDimensionByName(this.memberNameToken.getCharacterBlock(0), cubeAsExecuted);
                                                                                            //	//		return this.dimension;
                                                                                            //
                                                                                            //			return this.dimRole.getDimension();
                                                                                            //		}
                                                                                            //
    //	//	@Override
    //	//	public String toString() {
    //	////		return getMemberName();
    //	//		return getName();
    //	//	}
    //
    //		@Override
    //		public String fullPathName() {
    //	//		StringBuilder returnThis = new StringBuilder("");
    //	//		for (String s : this.memberNameToken.getCharacterBlockList()) {
    //	//			returnThis.append("[").append(s).append("].");
    //	//		}
    //	//		return returnThis.substring(0, returnThis.length() - 1);
    //
    //			return null;
    //		}
    //
    //		@Override
    //		public List<Member> getChildren() {
    //			// 自定义计算公式成员没有子级成员
    //			return new ArrayList<Member>();
    //		}
    //
    //		/**
    //		 * 返回此对象的全部属性，若没有属性返回一空列表
    //		 * @createDate 2015-08-05
    //		 */
    //		@Override
    //		public List<Property> getProperties() {
    //			// 自定义维度成员没有属性，返回空列表
    //			return new ArrayList<Property>();
    //		}
    //
    //	//	@Override
    //	//	public String getProperty(String propertyName) {
    //	//		// TODO 计算公式成员没有属性
    //	//		return null;
    //	//	}
    //
    //		/**
    //		 * @createDate 2015-08-05
    //		 */
    //		@Override
    //		public Property getProperty(String key) {
    //			return null; // 自定义维度成员没有属性，返回null
    //		}
    //
    //		@Override
    //		public String getName() {
    //			return this.memberNameToken.getBlock(this.memberNameToken.size() - 1).getText();
    //		}
    //
    //		@Override
    //		public void setContext(AnalyticalContext context) {
    //			this.context = context;
    //		}
    //
    //		@Override
    //		public Member findDescendantMember(int desdntMbrId) {
    //			return null; // 自定义计算公式成员没有后代成员
    //		}
    //
    //		@Override
    //		public Member findNearestDescendantMember(String desdntMbrName) {
    //			return null; // 自定义计算公式成员没有后代成员
    //		}
                                                                                            //
                                                                                            //		/**
                                                                                            //		 * 根据做在维度角色返回自身的成员角色对象
                                                                                            //		 * @return
                                                                                            //		 */
                                                                                            //		public IMemberRole getSelfRole() {
                                                                                            //			return new MemberRole(this.dimRole, this);
                                                                                            //		}
                                                                                            //
    //		@Override
    //		public int hashCode() {
    //			return this.memberNameToken.hashCode() * this.context.hashCode() + 80706050;
    //		}
    //
    //		@Override
    //		public boolean equals(Object obj) {
    //			if (obj == null || !(obj instanceof CustomFormulaMember)) {
    //				return false;
    //			}
    //			CustomFormulaMember cfm = (CustomFormulaMember) obj;
    //			return this.memberNameToken.equals(cfm.memberNameToken)
    //					&& this.context.equals(cfm.context);
    //		}
    //
    //		@Override
    //		public boolean isCustomFormulaMember() {
    //			return true;
    //		}
    //
    //
    //
    //	//	@Override
    //	//	public List<String> getAllPropertyNames() {
    //	//		// 自定义计算公式成员没有属性
    //	//		return new ArrayList<String>();
    //	//	}
    //
    //	//	public CustomFormulaMember(Member member, Expression expression) {
    //	//		this.expression = expression;
    //	//
    //	//	}
    //
    //	//	public CustomFormulaMember(int wormholeID, String memberName,
    //	//			int hierarchyID, int parentID, int memberLevel, Expression expression) {
    //	//		super(wormholeID, memberName, hierarchyID, parentID, memberLevel);
    //	//		this.expression = expression;
    //	//	}
    //
    //	//	public CustomFormulaMember(Expression expression) {
    //	//		this(-999999, null, -999999, -999999, -999999);
    //	//		this.expression = expression;
    //	//	}
    //
    //	}

// ????????????????????????????????????????????????????????????????????????????????????????????????????????
