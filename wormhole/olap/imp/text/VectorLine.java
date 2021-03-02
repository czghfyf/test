package cn.bgotech.wormhole.olap.imp.text;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/5/23.
 */
public class VectorLine {

//    private RawCubeDataPackage dataPkg;

    List<MemberTrail> memberTrails = new LinkedList<>();
    List<Double> measureValues = new LinkedList<>();

//    public VectorLine(RawCubeDataPackage dataPkg) {
//        this.dataPkg = dataPkg;
//    }

    public MemberTrail getMemberTrail(int i) {
        return memberTrails.get(i);
    }

    public Double getMeasureValue(int i) {
        return measureValues.get(i);
    }

    // ( ~[ "(", ")", "\n", "\r"] )+
    public void addUniversalMemberTrail(String generalMemberTrail) {
        memberTrails.add(new MemberTrail(generalMemberTrail.split("\\.")));
    }

    // ( [ "0"-"9" ] )+ ( "." ( [ "0"-"9" ] )+ )?
    public void addMeasureValue(String measureValue) {
        measureValues.add(Double.parseDouble(measureValue));
    }

    static public class MemberTrail implements Comparable<MemberTrail> {

        private String[] memberFullNamesPath;

        public MemberTrail(String[] memberFullNamesPath_) {
            this.memberFullNamesPath = new String[memberFullNamesPath_.length];
            for (int i = 0; i < memberFullNamesPath_.length; i++) {
                this.memberFullNamesPath[i] = memberFullNamesPath_[i].trim();
            }
        }

        public MemberTrail(List<String> memberFullNamesPath_) {
            memberFullNamesPath = new String[memberFullNamesPath_.size()];
            for (int i = 0; i < memberFullNamesPath_.size(); i++) {
                memberFullNamesPath[i] = memberFullNamesPath_.get(i);
            }
        }

        public MemberTrail getParentMemberTrail() {
            if (memberFullNamesPath.length < 2) {
                return null;
            }
            return new MemberTrail(Arrays.copyOf(memberFullNamesPath, memberFullNamesPath.length - 1));
        }

        public int getMemberLevel() {
            return memberFullNamesPath.length;
        }

        public String getMemberName() {
            return memberFullNamesPath[memberFullNamesPath.length - 1];
        }

        public Set<MemberTrail> getAncestorTrails() {
            Set<MemberTrail> ancestors = new HashSet<>();
            for (int i = 1; i < memberFullNamesPath.length; i++) {
                ancestors.add(new MemberTrail(Arrays.copyOf(memberFullNamesPath, i)));
            }
            return ancestors;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof MemberTrail)) {
                return false;
            }
            return BGTechUtil.arrayEquals(memberFullNamesPath, ((MemberTrail)obj).memberFullNamesPath);
        }

        @Override
        public int hashCode() {
            return BGTechUtil.arrayHashCode(memberFullNamesPath);
        }

        @Override
        public int compareTo(MemberTrail o) {
            if (o == null || o.memberFullNamesPath == null) {
                throw new OlapRuntimeException("o and o.memberFullNamesPath must be not null");
            }
            int compareLength = o.memberFullNamesPath.length < this.memberFullNamesPath.length ? o.memberFullNamesPath.length : this.memberFullNamesPath.length;
            for (int i = 0; i < compareLength; i++) {
                if (!this.memberFullNamesPath[i].equals(o.memberFullNamesPath[i])) {
                    return this.memberFullNamesPath[i].compareTo(o.memberFullNamesPath[i]);
                }
            }
            return this.memberFullNamesPath.length - o.memberFullNamesPath.length;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(memberFullNamesPath[0]);
            for (int i = 1; i < memberFullNamesPath.length; i++) {
                sb.append(".").append(memberFullNamesPath[i]);
            }
            return sb.toString();
        }

        public List<String> toList() {
            return new LinkedList(Arrays.asList(memberFullNamesPath));
            // return Arrays.asList(memberFullNamesPath);
        }
    }
}
