// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical;

import cn.bgotech.analytics.bi.bean.mddm.MddmBean;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.BasicEntityModel;
import cn.bgotech.wormhole.olap.mddm.physical.ClassicMDDM;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.Hierarchy;
import cn.bgotech.wormhole.olap.mddm.physical.Level;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.MeasureDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.EntityRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.MultiDimensionalDomainSelector;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Set;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class CubeBean extends MddmBean implements Cube {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Deprecated
    private String factTableName;

    private Long spaceId;

    @Deprecated
    public String getFactTableName() {
        return factTableName;
    }

    @Deprecated
    public void setFactTableName(String factTableName) {
        this.factTableName = factTableName;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    @Override
    public boolean isWithinRange(Space space) {
        return spaceId.equals(space.getMgId());
    }

    @Override
    public MultiDimensionalVector getDefaultVector() {
        List<DimensionRole> dimRoles = getDimensionRoles(null);
        Collections.sort(dimRoles);

        for (int i = 0; i < dimRoles.size(); i++) {
            if (dimRoles.get(i).getDimension() instanceof MeasureDimension) {
                dimRoles.add(dimRoles.get(i));
                dimRoles.remove(i);
                break;
            }
        }

        List<MemberRole> mrs = new ArrayList<>();
        DimensionRole dimensionRole;
        Member defMbr;
        for (int i = 0; i < dimRoles.size(); i++) {
            // TODO: 以后维度角色与默认Hierarchy关联(&xx[xx].&xx[xx]查询逻辑会很复杂)? 此处返回Hierarchy的默认Member
            dimensionRole = dimRoles.get(i);
            defMbr = dimRoles.get(i).getDimension().getDefaultMember();
            mrs.add(new MemberRole(dimensionRole, defMbr));
        }
        return new MultiDimensionalVector(mrs);
    }

    @Override
    public EntityRole getEntityRole(long mgId) {

        // OlapEngine.getSingleInstance().findEntity(mgId);
        // BasicEntityModel entity = OlapEngine.getSingleInstance().find(BasicEntityModel.class, mgId);
        ClassicMDDM entity = OlapEngine.hold().find(ClassicMDDM.class, mgId);

        if (entity instanceof DimensionRole) {
            return associatedWith(this) ? (DimensionRole) entity : null;
        }

        if (entity instanceof Dimension || entity instanceof Hierarchy
                || entity instanceof Level || entity instanceof Member) {
            List<DimensionRole> dimensionRoles = findRelationalDimensionRoles((ClassicMDDM) entity);
            if (dimensionRoles.isEmpty()) {
                return null;
            } else if (dimensionRoles.size() > 1) {
                // TODO: throw new RuntimeException("无法确定此对象在哪个维度角色上");
            }
            try {
                return EntityRole.createOne((ClassicMDDM) entity, dimensionRoles.get(0)); // entity can not be Cube
            } catch (OlapException e) {
                e.printStackTrace();
                throw new OlapRuntimeException(e);
            }

        }
        return null;

    }

    @Override
    public List<DimensionRole> findRelationalDimensionRoles(ClassicMDDM ce) {

        if (ce instanceof Cube) {
            logger.warn("ce is Cube");
            return null;
        }

        Dimension d = null;
        if (ce instanceof Dimension) {
            d = (Dimension) ce;
        } else if (ce instanceof Hierarchy) {
            d = ((Hierarchy) ce).getDimension();
        } else if (ce instanceof Level) {
            d = ((Level) ce).getDimension();
        } else if (ce instanceof Member) {
            d = ((Member) ce).getDimension();
        }

        if (OlapEngine.hold().findMeasureDimensionRole(this).getDimension().equals(d)) {
            return Arrays.asList(OlapEngine.hold().findMeasureDimensionRole(this));
        } else {
            List<DimensionRole> dimensionRoles = new ArrayList<>();
            for (DimensionRole dr : OlapEngine.hold().findUniversalDimensionRoles(this)) {
                if (dr.getDimension().equals(d)) {
                    dimensionRoles.add(dr);
                }
            }
            return dimensionRoles;
        }

    }

    @Override
    public boolean associatedWith(Cube cube) {
        return this.equals(cube);
    }

    @Override
    public BasicEntityModel selectSingleEntity(MultiDimensionalDomainSelector segmentedSelector) {
        DimensionRole dimensionRole = null;
//		boolean hasDimRole = false;


        for (DimensionRole dr : OlapEngine.hold().findUniversalDimensionRoles(this)) {
            if (dr.getMgId().equals(segmentedSelector.getPart(0).getMgId())
                    || (segmentedSelector.getPart(0).getMgId() == null
                    && dr.getName().equals(segmentedSelector.getPart(0).getImage()))) {
                dimensionRole = dr;
                break;
            }
        }

        return dimensionRole != null ? dimensionRole.selectSingleEntity(segmentedSelector.subSelector(1)) : null;
    }

    @Override
    public List<DimensionRole> getDimensionRoles(Dimension dimension) {

        List<DimensionRole> rs = new ArrayList<>();

        if (dimension == null || dimension.isMeasureDimension()) {
            rs.add(OlapEngine.hold().findMeasureDimensionRole(this));
        }

        for (DimensionRole dr : OlapEngine.hold().findUniversalDimensionRoles(this)) {
            if (!(dimension != null && !dimension.equals(dr.getDimension()))) {
                rs.add(dr);
            }
        }

        return rs;

    }


    @Override
    public boolean isReferenceDim(Dimension dim) {
        boolean rs = false;
        if (dim.equals(/*this.measureDimension*/OlapEngine.hold().findMeasureDimensionRole(this).getDimension())) {
            rs = true;
        }
        for (DimensionRole dr : OlapEngine.hold().findUniversalDimensionRoles(this)) {
            if (dr.getDimension().equals(dim)) {
                rs = true;
            }
        }
        return rs;
    }

    @Override
    public List<Dimension> getDimensions(String dimensionName) {
        Set<Dimension> dimensions = new HashSet<>();
        for (DimensionRole dr : getDimensionRoles(null)) {
            if (!(dimensionName != null && !dimensionName.equals(dr.getDimension().getName()))) {
                dimensions.add(dr.getDimension());
            }
        }
        return new LinkedList<>(dimensions);
    }

    @Override
    public List<Hierarchy> getHierarchies(String name) {
        Set<Hierarchy> result = new HashSet<>();
        List<Dimension> dimensions = getDimensions(null);
        for (Dimension d : dimensions) {
            List<Hierarchy> hierarchies = d.getHierarchies(name);
            for (Hierarchy h : hierarchies) {
                if (!(name != null && !name.equals(h.getName()))) {
                    result.add(h);
                }
            }
        }
        return new LinkedList<>(result);
    }

    @Override
    public List<Level> getLevels(String name) {
        List<Level> levels = new LinkedList<>();
        for (Hierarchy h : getHierarchies(null)) {
            for (Level level : h.getLevels()) {
                if (!(name != null && !level.getName().equals(name))) {
                    levels.add(level);
                }
            }
        }
        return levels;
    }

    @Override
    public List<Member> getMembers(String name) {
        List<Member> members = new ArrayList<>();
        for (Dimension d : getDimensions(null)) {
            members.addAll(d.getMembers(name));
        }
        return members;
    }

}

