// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.spa;

import cn.bgotech.analytics.bi.bean.CommonBean;

public class MDQuerySchema extends CommonBean {

    private Long cubeMgId; // CUBE_MG_ID
    private String rowJson; // ROW_JSON
    private String colJson; // COL_JSON
    private String whrJson; // WHR_JSON
    private String rowMdx; // ROW_MDX
    private String colMdx; // COL_MDX
    private String whrMdx; // WHR_MDX

    public Long getCubeMgId() {
        return cubeMgId;
    }

    public void setCubeMgId(Long cubeMgId) {
        this.cubeMgId = cubeMgId;
    }

    public String getRowJson() {
        return rowJson;
    }

    public void setRowJson(String rowJson) {
        this.rowJson = rowJson;
    }

    public String getColJson() {
        return colJson;
    }

    public void setColJson(String colJson) {
        this.colJson = colJson;
    }

    public String getWhrJson() {
        return whrJson;
    }

    public void setWhrJson(String whrJson) {
        this.whrJson = whrJson;
    }

    public String getRowMdx() {
        return rowMdx;
    }

    public void setRowMdx(String rowMdx) {
        this.rowMdx = rowMdx;
    }

    public String getColMdx() {
        return colMdx;
    }

    public void setColMdx(String colMdx) {
        this.colMdx = colMdx;
    }

    public String getWhrMdx() {
        return whrMdx;
    }

    public void setWhrMdx(String whrMdx) {
        this.whrMdx = whrMdx;
    }
}
