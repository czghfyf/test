package cn.bgotech.analytics.bi.service.olap;

import cn.bgotech.analytics.bi.bean.spa.MDQuerySchema;
import cn.bgotech.analytics.bi.exception.BIException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;

import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/19.
 */
public interface OLAPService {

    void importCube(String fileType, String cubeName, byte[] fileBytes) throws BIException;

    void importMeasure(byte[] fileBytes);

    /**
     * Find all the 'cube' under the 'space' corresponding to the current 'user'
     *
     * @return
     */
    List<Cube> findCubes();

    /**
     * @param cubeMgId
     * @return
     */
    List<DimensionRole> cubeDimensionRoles(long cubeMgId);

    /**
     * @param cubeMgId
     * @return
     */
    Cube findCube(Long cubeMgId);

    /**
     * 将多维查询片段拼装后进行查询，查询结果集应为行列两轴结构
     *
     * @param withStr
     * @param rowStr
     * @param colStr
     * @param whereStr
     * @param cubeId
     * @return
     */
    MDXQueryResult query2table(String withStr, String rowStr, String colStr, String whereStr, long cubeId);

    /**
     * 查询MDX
     *
     * @param mdx
     * @return
     */
    MDXQueryResult queryMDX(String mdx);

    MDQuerySchema saveMDQuerySchema(MDQuerySchema qs);

    /**
     * find and return MDQuerySchemas in current user & space scope
     *
     * @return
     */
    List<MDQuerySchema> findMDQuerySchemas();

    /**
     * find all dimensions then return they
     */
    List<Dimension> findDimensions();
}
