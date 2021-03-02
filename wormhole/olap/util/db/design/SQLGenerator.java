package cn.bgotech.wormhole.olap.util.db.design;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class SQLGenerator {

    private String dbType;

    private List<TableObj> tableObjs = new LinkedList<>();

    private Map<String, TableObj> tablesPool = new HashMap<>();

    public void setDatabaseType(String dbType) {
        this.dbType = dbType;
    }

    public void addTable(TableObj table) {
        tableObjs.add(table);
        tablesPool.put(table.getName(), table);
    }

    public void generateDDL() {

        System.out.println("<< !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! >>\n\n");

        System.out.println("drop database prophet;");
        System.out.println("create database prophet;");
        System.out.println("use prophet;");

        for (TableObj tbl : tableObjs ) {
            tbl.generateDDL(this);
        }

        System.out.println("\n\n<< ?????????????????????????????????????????????????????????????????????? >>");

    }

    public TableObj findTableObj(String tableName) {
        return tablesPool.get(tableName);
    }
}
