package cn.bgotech.analytics.bi.controller.olap;

import cn.bgotech.analytics.bi.bean.spa.MDQuerySchema;
import cn.bgotech.analytics.bi.controller.response.ResponseData;
import cn.bgotech.analytics.bi.dto.CubeSuite;
import cn.bgotech.analytics.bi.dto.DTOUtils;
import cn.bgotech.analytics.bi.dto.mddm.physical.CubeDTO;
import cn.bgotech.analytics.bi.dto.mddm.physical.role.DimensionRoleDTO;
import cn.bgotech.analytics.bi.service.olap.OLAPService;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/6/1.
 */
@RestController
@RequestMapping("/rest/olap")
public class OLAPRestController {

    @Autowired
    private OLAPService olapService;

    @GetMapping("/cubes")
    public ResponseData cubes() {
        List<Cube> cubes = olapService.findCubes();
        List<CubeDTO> cubeDTOS = new LinkedList<>();
        for (Cube c : cubes) {
            cubeDTOS.add(new CubeDTO(c));
        }
        return new ResponseData().setData(cubeDTOS);
    }


    @GetMapping("/dimensionRole/cube/{cubeMgId}")
    public ResponseData cubeDimensionRoles(@PathVariable("cubeMgId") Long cubeMgId) {
        List<DimensionRole> dimensionRoles = olapService.cubeDimensionRoles(cubeMgId);
        List<DimensionRoleDTO> dtos = new LinkedList<>();
        for (DimensionRole dr : dimensionRoles) {
            dtos.add(new DimensionRoleDTO(dr));
        }
        return new ResponseData().setData(dtos);
    }

    @GetMapping("/cubeSuite/{cubeMgId}")
    public ResponseData cubeSuite(@PathVariable("cubeMgId") Long cubeMgId) {
        return new ResponseData().setData(new CubeSuite(olapService.findCube(cubeMgId)));
    }

    @GetMapping("/cubeSuites")
    public ResponseData cubeSuites() {
        List<CubeSuite> cubeSuites = new ArrayList<>();
        for (Cube cube : olapService.findCubes()) {
            cubeSuites.add(new CubeSuite(cube));
        }
        return new ResponseData().setData(cubeSuites);
    }

    /**
     *
     * @param paramsMap
     * {
     *     withStr: <String>
     *     rowStr: <String>
     *     colStr: <String>
     *     whereStr: <String>
     *     cubeId: <String>
     * }
     * @return
     */
    @PostMapping("/multiDimQuery")
    public ResponseData multiDimQuery(@RequestBody Map<String, String> paramsMap) {

        MDXQueryResult queryResult = olapService.query2table(paramsMap.get("withStr"), paramsMap.get("rowStr"), paramsMap.get("colStr"),
                                            paramsMap.get("whereStr"), Long.parseLong(paramsMap.get("cubeId")));

        Map<String, Object> pivotGrid = DTOUtils.convertTo2DPivotGrid(queryResult);

        return new ResponseData().setData(pivotGrid);
    }

    /**
     *
     * @param qs
     * @return
     */
    @PostMapping("/querySchema/save")
    public ResponseData saveMDQuerySchema(@RequestBody MDQuerySchema qs) {
        return new ResponseData().setData(olapService.saveMDQuerySchema(qs));
    }

    @GetMapping("/MDQuerySchemas")
    public List<MDQuerySchema> findMDQuerySchemas() {
        return olapService.findMDQuerySchemas();
    }

}
