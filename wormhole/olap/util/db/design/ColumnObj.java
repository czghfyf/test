package cn.bgotech.wormhole.olap.util.db.design;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class ColumnObj {

    private String name;
    private String dataType;
    private int dataTypeLength = -1;
    private String defaultValue;
    private boolean noNull;
    private boolean unique;
    private boolean PKFlag;

    private String fkTable;
    private String fkTableCol;

    private String desc;

    @Override
    public String toString() {
        return new StringBuilder(name)
                .append(" ").append(dataType).append(dataTypeLength > 0 ? "(" + dataTypeLength + ")" : "")
                .append(" ").append(defaultValue != null ? ("default " + defaultValue) : "")
                .toString();
    }

    public ColumnObj(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDataTypeLength(int dataTypeLength) {
        this.dataTypeLength = dataTypeLength;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setNoNull(boolean noNull) {
        this.noNull = noNull;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setPKFlag(boolean PKFlag) {
        this.PKFlag = PKFlag;
    }

    public boolean isPKFlag() {
        return PKFlag;
    }

    public void setFK(String fkTable, String fkTableCol) {
        this.fkTable = fkTable;
        this.fkTableCol = fkTableCol;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


}
