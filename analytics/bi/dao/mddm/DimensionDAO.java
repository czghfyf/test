// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.dao.mddm;

import cn.bgotech.analytics.bi.bean.mddm.physical.dimension.*;
import cn.bgotech.analytics.bi.bean.mddm.physical.role.DimensionRoleBean;
import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.util.BGTechUtil;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ChenZhiGang on 2017/5/17.
 */
public interface DimensionDAO {

    default int save(DimensionBean dimension) {
        if (dimension instanceof DateDimensionBean) {
            _insertDate((DateDimensionBean) dimension);
        } else if (dimension instanceof RegionDimensionBean) {
            _insertRegion((RegionDimensionBean) dimension);
        }
        return _insert(dimension);
    }

    int _insert(DimensionBean dimension);

    int _insertRegion(RegionDimensionBean regionBean);

    int _insertDate(DateDimensionBean dateBean);

    default DimensionBean load(Long dimensionId) {

        DimensionBean dimBean = _load(dimensionId);

        if (dimBean == null) {
            return null;
        }

        if (dimBean.isMeasureDimension()) {
            dimBean = new MeasureDimensionBean(dimBean);
        } else {
            dimBean = new UniversalDimensionBean(dimBean);
        }

        DimensionBean extendedDimBean = _loadIncompleteDateDimension(dimensionId);
        if (extendedDimBean == null) {
            extendedDimBean = _loadIncompleteRegionDimension(dimensionId);
        }
        if (extendedDimBean != null) {
            try {
                BeanUtils.copyProperties(extendedDimBean, dimBean);
                return extendedDimBean;
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new BIRuntimeException(e);
            }
        }
        return dimBean;
    }

    DimensionBean _load(Long dimensionId);

    DateDimensionBean _loadIncompleteDateDimension(Long dimensionId);

    RegionDimensionBean _loadIncompleteRegionDimension(Long dimensionId);

    default List<DimensionBean> loadAll() {
        Map<Long, DimensionBean> dimensionMap = new HashMap<>();
        for (DimensionBean db : _loadAll()) {
            dimensionMap.put(db.getMgId(), (DimensionBean) BGTechUtil.copyProperties(new UniversalDimensionBean(), db));
        }
        for (MeasureDimensionBean db : loadAllMeasureDimensionBeans()) {
            dimensionMap.put(db.getMgId(), db);
        }
        try {
            for (DateDimensionBean db : _loadAllDateDimensionExtending()) {
                BeanUtils.copyProperties(db, dimensionMap.get(db.getMgId()));
                dimensionMap.put(db.getMgId(), db);
            }
            for (RegionDimensionBean db : _loadAllRegionDimensionExtending()) {
                BeanUtils.copyProperties(db, dimensionMap.get(db.getMgId()));
                dimensionMap.put(db.getMgId(), db);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            throw new BIRuntimeException(e);
        }
        return new ArrayList<>(dimensionMap.values());
    }

//    default List<UniversalDimensionBean> loadAllUniversalDimensionBeans() {
//        Map<Long, >
//        // UniversalDimensionBeans
//        //     DateDimensionBeans
//        //     RegionDimensionBeans
//    }

    List<MeasureDimensionBean> loadAllMeasureDimensionBeans();

    List<DimensionBean> _loadAll();

    List<DateDimensionBean> _loadAllDateDimensionExtending();

    List<RegionDimensionBean> _loadAllRegionDimensionExtending();

    int saveRole(DimensionRoleBean drb);

    default List<DimensionBean> loadByName(String name) {
        List<DimensionBean> dimensions = new LinkedList<>();
        for (Long id : _loadByName(name)) {
            dimensions.add(load(id));
        }
        return dimensions;
    }

    List<Long> _loadByName(String name);

    List<DimensionRole> loadRoles(Cube cube);

    /**
     * @deprecated TODO: what's up?
     * @param dimRoleId
     * @return
     */
    @Deprecated
    DimensionRoleBean selectRole(Long dimRoleId);

    DimensionBean loadDimensionByRoleMGIDFromPersistence(long dmRoleMgId);

}
