package cn.bgotech.analytics.bi.dto;

import cn.bgotech.analytics.bi.dto.mdx.syntax.structures.AxisDTO;
import cn.bgotech.wormhole.olap.mdx.MDXQueryResult;
import cn.bgotech.wormhole.olap.mdx.syntax.structures.Axis;
import org.apache.commons.collections.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2018/1/31.
 */
public class DTOUtils {

    public static Map<String, Object> convertTo2DPivotGrid(MDXQueryResult queryResult) {

        Map<String, Object> pivotGrid = new HashedMap();

        List<Axis> axes = queryResult.getAxes();
        for (Axis axis : axes) {
            pivotGrid.put(axis.getAxisIdx() == 0 ? "columns" : "rows", new AxisDTO(axis));
        }

//        Map<String, Object> cells = new HashMap<>();
        List<Map<String, Object>> cells = new ArrayList<>();
        Map<String, Object> cellDTO;

//        Map<String, Object> mCell;
//        String cellPointerStr;

//        BasicData cellValue;
        for (MDXQueryResult.DataCellPointer cell : queryResult.dataCells()) {
            // tr position
            // td position
            //mCell = new HashMap<>();
            //cellPointerStr = cell.getTuplePosition("rows") + '_' + cell.getTuplePosition("columns");
            //// cell value
            //cell.getValue();

//            cellValue = cell.getValue();
            cellDTO = new HashMap<>();
            cellDTO.put("td", cell.getTuplePosition("columns"));
            cellDTO.put("tr", cell.getTuplePosition("rows"));
            cellDTO.put("value", cell.getValue().value());

            // cells.put(cell.getTuplePosition("rows") + "_" + cell.getTuplePosition("columns"), cell.getValue().value());
            cells.add(cellDTO);
        }

        pivotGrid.put("cells", cells);

        return pivotGrid;
    }

}
