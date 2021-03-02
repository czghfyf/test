package cn.bgotech.wormhole.olap.util.db.design;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class TableObj {

    private static ThreadLocal<TableInfoContext> TABLE_CONTEXT_THREAD_LOCAL
            = new ThreadLocal<>();

    private boolean abstract_;
    private String name;

    private List<String> extendTablesBeforeCurrentTable = new LinkedList<>();
    private List<String> extendTablesAfterCurrentTable = new LinkedList<>();

    private List<ColumnObj> columns = new LinkedList<>();

    public void setAbstract(boolean abstract_) {
        this.abstract_ = abstract_;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void appendExtendTableBeforeCurrentTable(String extendTable) {
        extendTablesBeforeCurrentTable.add(extendTable);
    }

    public void appendExtendTableAfterCurrentTable(String extendTable) {
        extendTablesAfterCurrentTable.add(extendTable);
    }

    public void addColumn(ColumnObj col) {
        this.columns.add(col);
    }

    public void generateDDL(SQLGenerator sg) {

        if (this.abstract_) {
            return;
        }

        TABLE_CONTEXT_THREAD_LOCAL.set(new TableInfoContext());

        System.out.println("DROP TABLE IF EXISTS " + this.name + ";");
        System.out.println("CREATE TABLE " + name + "(");

//        printColumns(sg);
        collectColumns(sg);

        List<String> columnInfos = TABLE_CONTEXT_THREAD_LOCAL.get().getColumnInfos();
        for (int i = 0; i < columnInfos.size(); i++) {
            System.out.println(columnInfos.get(i) + ((i < columnInfos.size() - 1) ? "," : ""));
        }

        System.out.println(");");

        if (TABLE_CONTEXT_THREAD_LOCAL.get().hasPK()) {
            // ALTER TABLE SS_ACCOUNTS ADD CONSTRAINT PK_SS_ACCOUNTS PRIMARY KEY( ID );
            System.out.println("ALTER TABLE " + name + " ADD CONSTRAINT PK_" + name + " PRIMARY KEY(" + TABLE_CONTEXT_THREAD_LOCAL.get().getPKCols() + ");");
        }

        TABLE_CONTEXT_THREAD_LOCAL.remove();

    }

    private void collectColumns(SQLGenerator sg) {
        for (int i = 0; i < extendTablesBeforeCurrentTable.size(); i++) {
            sg.findTableObj(extendTablesBeforeCurrentTable.get(i)).collectColumns(sg);
        }

        for (int i = 0; i < columns.size(); i++) {
//            System.out.println(columns.get(i));
            TABLE_CONTEXT_THREAD_LOCAL.get().collectColumnInfo(columns.get(i));
            if (columns.get(i).isPKFlag()) {
                TABLE_CONTEXT_THREAD_LOCAL.get().addPK(columns.get(i).getName());
            }
        }

        for (int i = 0; i < extendTablesAfterCurrentTable.size(); i++) {
            sg.findTableObj(extendTablesAfterCurrentTable.get(i)).collectColumns(sg);
        }
    }

    private static class TableInfoContext {
        private List<String> pks = new LinkedList<>();
        private List<String> columnInfos = new LinkedList<>();

        List<String> getColumnInfos() {
            return columnInfos;
        }

        boolean hasPK() {
            return !pks.isEmpty();
        }

        String getPKCols() {
            String pkCols = "";
            for (String pk : pks ) {
                pkCols += pk + ",";
            }
            return pkCols.substring(0, pkCols.length() - 1);
        }

        void collectColumnInfo(ColumnObj columnObj) {
            columnInfos.add(columnObj.toString());
        }

        public void addPK(String pkCol) {
            pks.add(pkCol);
        }
    }
}
