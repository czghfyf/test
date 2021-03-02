//core_source_code!_yxIwIhhIyyIhv_111l


package cn.bgotech.wormhole.olap.mdx.syntax.structures;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ChenZhiGang on 2017/5/15.
 */
public class /*^!*/MultiDimensionalDomainSelector/*?$*//*_yxIwIhhIyyIhv_111l*/ implements Cloneable {

    private List<Part> parts = new LinkedList<>();

    public static class Part {

        private Long mgId;
        private String image;

        public Part(String mddmGlobalId, String image) {
            if (mddmGlobalId != null) {
                this.mgId = Long.parseLong(mddmGlobalId);
            }
            this.image = image;
        }

        public Long getMgId() {
            return mgId;
        }

        public String getImage() {
            return image;
        }

        @Override
        public String toString() {

//            StringBuilder toStr = new StringBuilder(mgId != null ? "&" + mgId : "").append(image != null ? "[" + image + "]" : "");
//            toStr.append(image != null ? "[" + image + "]" : "");

            return new StringBuilder(mgId != null ? "&" + mgId : "")
                    .append(image != null ? "[" + image + "]" : "").toString();
        }

        /**
         *   this.mgId == null                    this.mgId != null
         * ┌────────────────────────────────────┬──────────────────────────────────┐
         * │ return this.image.equals(op.image) │ return false                     │ op.mgId == null
         * ├────────────────────────────────────┼──────────────────────────────────┤
         * │ return false                       │ return this.mgId.equals(op.mgId) │ op.mgId != null
         * └────────────────────────────────────┴──────────────────────────────────┘
         *
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Part)) {
                return false;
            }
            Part op = (Part) obj;
            if (mgId != null && mgId.equals(op.getMgId())) {
                return true;
            } else if (mgId == null && op.getMgId() == null) {
                return image.equals(op.getImage());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return mgId != null ? mgId.hashCode() : image.hashCode();
        }

                                                                                                                    //    ///20151022
                                                                                                                    //    package com.dao.wormhole.core.mdx.IR;
                                                                                                                    //
                                                                                                                    //    public class EntityPathBlock {
                                                                                                                    //
                                                                                                                    //        private String idSelector;
                                                                                                                    //        private String image;
                                                                                                                    //        private String text;
                                                                                                                    //
                                                                                                                    //        public int getId() {
                                                                                                                    //            return idSelector == null ? -1 : Integer.parseInt(idSelector.replaceFirst("&", ""));
                                                                                                                    //        }
                                                                                                                    //    }

    }

    /**
     * WormholeMDXParser.jjt file need this method !!!
     */
    public MultiDimensionalDomainSelector() {

    }

    public MultiDimensionalDomainSelector(List<Part> parts) {
        if (parts instanceof ArrayList)
            this.parts = parts;
        else
            this.parts = new LinkedList<>(parts);
    }

    public int length() {
        return parts.size();
    }

    public void append(Part part) {
        parts.add(part);
    }

    public Part getPart(int idx) {
        return parts.get(idx);
    }

    public MultiDimensionalDomainSelector removePart(int idx) {
        parts.remove(idx);
        return this;
    }

    @Override
    public MultiDimensionalDomainSelector clone() throws CloneNotSupportedException {
//        return (MultiDimensionalDomainSelector) super.clone();
        return new MultiDimensionalDomainSelector(new ArrayList(parts));
    }

    public MultiDimensionalDomainSelector subSelector(int fromIdx, int toIdx) {
        return new MultiDimensionalDomainSelector(parts.subList(fromIdx, toIdx));
    }

    public MultiDimensionalDomainSelector subSelector(int startPosition) {
        return subSelector(startPosition, parts.size());
    }

    @Override
    public String toString() {
        StringBuilder toStr = new StringBuilder();
        for (Part p : parts) {
            toStr.append(p.toString()).append('.');
        }
        return toStr.substring(0, toStr.length() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof MultiDimensionalDomainSelector)) {
            return false;
        }
        return parts.equals(((MultiDimensionalDomainSelector) o).parts);
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }
}
