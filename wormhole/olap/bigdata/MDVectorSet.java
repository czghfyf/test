package cn.bgotech.wormhole.olap.bigdata;

import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.bigdata.cache.VectorCache;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.mddm.physical.Cube;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;
import cn.bgotech.wormhole.olap.mddm.physical.member.Member;
import cn.bgotech.wormhole.olap.mddm.physical.role.DimensionRole;
import cn.bgotech.wormhole.olap.mddm.physical.role.MemberRole;
import cn.bgotech.wormhole.olap.mdx.vector.MultiDimensionalVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * Multi-dimensional Vector Set
 * Created by ChenZhiGang on 2017/5/27.
 *
 * @deprecated
 */
@Deprecated
public class MDVectorSet implements Serializable {

    private static final long serialVersionUID = 2017_06_20_20_58L;

    private static final Map<Integer, Integer> NULL_VALUE_MASK_POOL = new HashMap<>();

    private static final Map<String, Integer> DATE_DIMENSION_ROLE_DATE_SCOPE_MAP = new HashMap<>();

    static {
        for (int i = 1; i <= 20; i++) {
            NULL_VALUE_MASK_POOL.put(i, calculateMaskCode(i));
        }
    }

    private transient Logger logger;

    private transient VectorCache vectorCache;

    private transient Cube cube;

    private long cubeId;

    /* multi-dimensional vector set file structure
     *
     * +--------+
     * |        | <- header:cubeId(8bytes)
     * +--------+
     *
     * +--------+--------+--------+--------+            +--------+--------+--------+            +--------+
     * |        |        |        |        | <- pointer |        |        |        | <- pointer |        | <- pointer
     * +--------+--------+--------+--------+            +--------+--------+--------+            +--------+
     * |        |        |        |        |            |        |        |        |            |        |
     * +--------+--------+--------+--------+            +--------+--------+--------+            +--------+
     * |        |        |        |        |            |        |        |        |            |        |
     * +--------+--------+--------+--------+            +--------+--------+--------+            +--------+
     * |        |        |        |        |            |        |        |        |            |        |
     * +--------+--------+--------+--------+            +--------+--------+--------+            +--------+
     * universalDimensionMember2DArray                  measureValue2DArray                     nullMeasureFlagArray
     * (long[][])                                       (double[][])                            (int[])
     *
     */
    private long[][] universalDimensionMember2DArray;
    private double[][] measureValue2DArray;
    private int[] nullMeasureFlagArray;

    private transient Map<List<Long>, Integer> vectorMeasureValueMap;

    private transient int pointer;

    public static int calculateMaskCode(int measureCount) {
        if (measureCount < 1) {
            throw new OlapRuntimeException("measureCount is at least 1, but measureCount = " + measureCount);
        }
        return (1 << measureCount) - 1;
    }

    /**
     * TODO: Will the method be called when de-serializing?
     */
    public MDVectorSet() {
        if (logger != null) {
            logger.debug("be called when de-serializing");
        }
    }

    /**
     * @param cube
     * @param size multi-dimensional vector size
     *             universalDimensionMember2DArray.length = size
     *             measureValue2DArray.length = size
     *             nullMeasureFlagArray.length = size
     */
    public MDVectorSet(Cube cube, int size) {
        this.cube = cube;
        this.cubeId = cube.getMgId();
        universalDimensionMember2DArray = new long[size][];
        measureValue2DArray = new double[size][];
        nullMeasureFlagArray = new int[size];
    }

    private Logger getLogger() {
        return logger != null ? logger : (logger = LoggerFactory.getLogger(this.getClass()));
    }

    public void addVector(long[] universalDimensionMemberArray, double[] measureValueArray, int nullMeasureFlag) {
        universalDimensionMember2DArray[pointer] = universalDimensionMemberArray;
        measureValue2DArray[pointer] = measureValueArray;
        nullMeasureFlagArray[pointer++] = nullMeasureFlag;
    }

    /**
     * @param vectors
     * @param finalDescendantMembersMap - a mapping of not-leaf members and their leaf-descendant members
     * @return
     */
    public Double[] evaluate(List<MultiDimensionalVector> vectors, Map<Long, List<Long>> finalDescendantMembersMap) {

        Double[] doubles = new Double[vectors.size()];

        MemberRole measureMemberRole;
        //List<Long> leafDescendantIdArr;

        //long[] measureValueKey = new long[vectors.get(0).getMemberRoles().size() - 1];
        List<Long> measureValueKey = new ArrayList<>(vectors.get(0).getMemberRoles().size() - 1);
        for (int i = 0; i < vectors.get(0).getMemberRoles().size() - 1; i++) {
            measureValueKey.add(Long.MIN_VALUE);
        }

        NullMeasureFlag nullMeasureFlag = new NullMeasureFlag().setTrue();

        double vectorMeasureValue;

        //int coordinatePosition;

        MultiDimensionalVector v;
        for (int i = 0; i < vectors.size(); i++) {
            v = vectors.get(i);
//        for (MultiDimensionalVector v : vectors) {

            // MultiDimensionalVector.print(v); // TODO: delete this line

            measureMemberRole = v.sort(Comparator.comparingLong(mr -> mr.getDimensionRole().getMgId())).popupMeasure();

            if (vectorCache.hasMeasureValue(cubeId, v.getMemberRoles(), measureMemberRole)) {
                Double cacheVectorMeasureValue = vectorCache.getMeasureValue(cubeId, v.getMemberRoles(), measureMemberRole);
                if (cacheVectorMeasureValue != null) {
                    doubles[i] = cacheVectorMeasureValue;
                }
            } else {


                // MultiDimensionalVector.print(v); // TODO: delete this line

//            int iiiii = measureMemberRole.getMember().isRoot() ? -1 : measureMemberRole.getDimensionRole().getDimension().getAllMembers().indexOf(measureMemberRole.getMember()) - 1;
//
//            measureIndex[i] = -1;
//
//            if (!measures.get(i).getMember().isRoot()) {
//                measureIndex[i] = measures.get(i).getDimensionRole().getDimension().getAllMembers().indexOf(measures.get(i).getMember()) - 1;
//            }


                vectorMeasureValue = calculateAggregateMeasureValue(
                        finalDescendantMembersMap,
                        nullMeasureFlag.setTrue(),
                        v.getMemberRoles(),
                        0,
                        measureValueKey,
                        (measureMemberRole.getMember().isRoot() ? -1
                                : measureMemberRole.getDimensionRole().getDimension().getAllMembers().indexOf(measureMemberRole.getMember()) - 1)
                );

                if (!nullMeasureFlag.isNull()) {
                    doubles[i] = vectorMeasureValue;
                    vectorCache.setMeasureValue(cubeId, v.getMemberRoles(), measureMemberRole, vectorMeasureValue);
                } else {
                    vectorCache.setMeasureValue(cubeId, v.getMemberRoles(), measureMemberRole, null);
                }

//            xxxxxxxxxxxxxxxxxxxxxxxxxxxxx(v.getMemberRoles(), 0, measureValueKey, finalDescendantMembersMap,
//                    measureMemberRole.getMember().isRoot() ? -1 : measureMemberRole.getDimensionRole().getDimension().getAllMembers().indexOf(measureMemberRole.getMember()) - 1);

//            for (MemberRole mr : v.getMemberRoles()) {
//                leafDescendantIdArr = finalDescendantMembersMap.get(mr.getMember().getMgId());
//                for (long leafId : leafDescendantIdArr) {
//                    measureValuekey[++coordinatePosition] = leafId;
//                    if (coordinatePosition == measureValuekey.length - 1) {
//                        // TODO: 求值，并累加
//                    }
//                }
//            }

            }
        }

        return doubles;

    }


    /**
     * @param finalDescendantMembersMap - not-leaf member and it's leaf-descendant members mapping
     * @param nullFlag                  - null measure value flag
     * @param memberRoles               - current vector's not-measure member role list
     * @param focusMemberRolePosition   - the location of the member role that is currently of interest in the memberRoles
     * @param measuresAddress           - a location in measureValue2DArray
     * @param measureIndex              - measure member location flag, if -1 mean that is a root member
     * @return
     */
    private double calculateAggregateMeasureValue(Map<Long, List<Long>> finalDescendantMembersMap, NullMeasureFlag nullFlag,
                                                  List<MemberRole> memberRoles, int focusMemberRolePosition,
                                                  List<Long> measuresAddress, int measureIndex) {

        double result = 0.0;

        Member member = memberRoles.get(focusMemberRolePosition).getMember();
        List<Long> leafDescendantIdArr = member instanceof DateMember
                ? ((DateMember) member).getFinalDescendantMembersMgIdList()
                : finalDescendantMembersMap.get(member.getMgId());

        for (long leafDescendantId : leafDescendantIdArr != null ? leafDescendantIdArr : Arrays.asList(member.getMgId())) {
            measuresAddress.set(focusMemberRolePosition, leafDescendantId);
            if (focusMemberRolePosition == measuresAddress.size() - 1) {

                if (vectorMeasureValueMap.containsKey(measuresAddress)
                        && measureValueExist(vectorMeasureValueMap.get(measuresAddress), measureIndex)) {

                    nullFlag.setFalse();
                    result += getMeasureValue(vectorMeasureValueMap.get(measuresAddress), measureIndex);

                }


                // TODO: 求值
                // int pointer = vectorMeasureValueMap.get(vvKey);
                // double[] measures = vectorMeasureValueMap.get(vvKey);

//                if (vectorMeasureValueMap.containsKey(vvKey) && measureValueExist(vectorMeasureValueMap.get(vvKey), measureIndex)) {
////                    for (int i = 0; i < universalDimensionMember2DArray[pointer].length; i++) {
////                        // member = olap().find(Member.class, universalDimensionMember2DArray[pointer][i]);
////
////                        member = olap().findMemberById(universalDimensionMember2DArray[pointer][i]);
////                        if (!(universalDimensionMember2DArray[pointer][i] == vectors.get(vi).get(i).getMember().getMgId()
////                                || member.hasAncestor(vectors.get(vi).get(i).getMember()))) {
////                            continue scan;
////                        }
////
////                    }
//                    // TODO: WTF!
//                    结果值撒 += getMeasureValue(vectorMeasureValueMap.get(vvKey), measureIndex); // TODO: add value !!!
////                    returnNull = false;
////                    result += getMeasureValue(pointer, measureIndex);
//                }


            } else {
                // TODO: WTF!
                //结果值撒 = xxxxxxxxxxxxxxxxxxxxxxxxxxxxx(memberRoles, wei_zhi_ + 1, vvKey, finalDescendantMembersMap, measureIndex);
                result += calculateAggregateMeasureValue(finalDescendantMembersMap, nullFlag, memberRoles,
                        focusMemberRolePosition + 1, measuresAddress, measureIndex);
            }
        }

        return result;

    }

    /**
     * TODO:
     *
     * @param vectors
     * @return
     * @deprecated
     */
    @Deprecated
    public Double[] evaluate(List<MultiDimensionalVector> vectors) {

        double[] results = new double[vectors.size()];
        List<MemberRole> measures = new ArrayList<>(vectors.size());
        boolean[] notNullFlags = new boolean[vectors.size()];

        // the horizontal position of MeasureMember ID in measureValue2DArray, -1 meaning the MemberRole is root.
        int[] measureIndex = new int[vectors.size()];

        for (int i = 0; i < vectors.size(); i++) {
            measures.add(vectors.get(i).sort(Comparator.comparingLong(v -> v.getDimensionRole().getMgId())).popupMeasure());
            measureIndex[i] = -1;

            if (!measures.get(i).getMember().isRoot()) {
                measureIndex[i] = measures.get(i).getDimensionRole().getDimension().getAllMembers().indexOf(measures.get(i).getMember()) - 1;
            }
        }

        // getLogger().debug("#####################################\n#####################################\n#####################################\n#####################################");
        getLogger().debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // TODO: 优化程序！！！提高性能！！！

        Member member;
        for (int pointer = 0; pointer < universalDimensionMember2DArray.length; pointer++) {
            scan:
            for (int vi = 0; vi < vectors.size(); vi++) {

                if (measureValueExist(pointer, measureIndex[vi])) {
                    for (int i = 0; i < universalDimensionMember2DArray[pointer].length; i++) {
                        // member = olap().find(Member.class, universalDimensionMember2DArray[pointer][i]);

                        member = OlapEngine.hold().findMemberById(universalDimensionMember2DArray[pointer][i]);
                        if (!(universalDimensionMember2DArray[pointer][i] == vectors.get(vi).get(i).getMember().getMgId()
                                || member.hasAncestor(vectors.get(vi).get(i).getMember()))) {
                            continue scan;
                        }
                    }
                    notNullFlags[vi] = true;
                    results[vi] += getMeasureValue(pointer, measureIndex[vi]);
//                    returnNull = false;
//                    result += getMeasureValue(pointer, measureIndex);
                }

            }

//            if (measureValueExist(pointer, measureIndex)) {
//
//                for (int i = 0; i < universalDimensionMember2DArray[pointer].length; i++) {
//                    member = olap().find(Member.class, universalDimensionMember2DArray[pointer][i]);
//                    if (!(universalDimensionMember2DArray[pointer][i] == vector.get(i).getMember().getMgId()
//                            || member.hasAncestor(vector.get(i).getMember()))) {
//                        continue scan;
//                    }
//                }
//
//                returnNull = false;
//                result += getMeasureValue(pointer, measureIndex);
//
//            }
//            if (measureValueExist(pointer, measureIndex)) {
//                returnNull = false;
//                result += getMeasureValue(pointer, measureIndex);
//            }
        }

        getLogger().debug("###########################################################################");

        // getLogger().debug("#####################################\n#####################################\n#####################################\n#####################################");


//        return returnNull ? null : result;

        Double[] results_ = new Double[vectors.size()];
        for (int i = 0; i < vectors.size(); i++) {
            if (notNullFlags[i]) {
                // results_.add(results[i]);
                results_[i] = results[i];
            }
        }
        return results_;


//        MemberRole measure
//                = vector.sort(Comparator.comparingLong(v -> v.getDimensionRole().getMgId())).popupMeasure();
//
//        boolean returnNull = true;
//        double result = 0;
//
//        // the horizontal position of MeasureMember ID in measureValue2DArray, -1 meaning the MemberRole is root.
//        int measureIndex = -1;
//
//        if (!measure.getMember().isRoot()) {
//            measureIndex = measure.getDimensionRole().getDimension().getAllMembers().indexOf(measure.getMember()) - 1;
//        }
//
//        Member member;
//
//        scan:
//        for (int pointer = 0; pointer < universalDimensionMember2DArray.length; pointer++) {
//
//            if (measureValueExist(pointer, measureIndex)) {
//
//                for (int i = 0; i < universalDimensionMember2DArray[pointer].length; i++) {
//                    member = olap().find(Member.class, universalDimensionMember2DArray[pointer][i]);
//                    if (!(universalDimensionMember2DArray[pointer][i] == vector.get(i).getMember().getMgId()
//                            || member.hasAncestor(vector.get(i).getMember()))) {
//                        continue scan;
//                    }
//                }
//
//                returnNull = false;
//                result += getMeasureValue(pointer, measureIndex);
//
//            }
//
//
////            if (measureValueExist(pointer, measureIndex)) {
////                returnNull = false;
////                result += getMeasureValue(pointer, measureIndex);
////            }
//        }
//        return returnNull ? null : result;


    }

    private double getMeasureValue(int pointer, int measureIndex) {
        double value = 0;
        if (measureIndex == -1) {
            for (double d : measureValue2DArray[pointer]) {
                value += d;
            }
            return value;
        } else {
            return measureValue2DArray[pointer][measureIndex];
        }
    }

    private boolean measureValueExist(int pointer, int measureIndex) {
        if (measureIndex == -1) {
            return getMaskCode(measureValue2DArray[pointer].length) != nullMeasureFlagArray[pointer];
        } else {
//            return measureValueExist_(pointer, measureIndex);
            return ((1 << (measureValue2DArray[pointer].length - measureIndex - 1)) & nullMeasureFlagArray[pointer]) == 0;
        }
    }

//    private boolean measureValueExist_(int pointer, int measureIndex) {
//        return ((1 << measureValue2DArray[pointer].length - measureIndex - 1) & nullMeasureFlagArray[pointer]) == 0;
//    }

    public int getMaskCode(int measureCount) {
        return NULL_VALUE_MASK_POOL.containsKey(measureCount)
                ? NULL_VALUE_MASK_POOL.get(measureCount) : calculateMaskCode(measureCount);
    }

    public static String fileName(Cube cube) {
        return "cube-" + cube.getMgId() + ".mdvs";
    }

    public synchronized void buildVectorValueMap() {

        extractDateDimensionRoleScope();

        if (vectorMeasureValueMap == null) {
            vectorMeasureValueMap = new HashMap<>();
            List<Long> vk;
            for (int i = 0; i < universalDimensionMember2DArray.length; i++) {
//                vectorMeasureValueMap.put(universalDimensionMember2DArray[i], i);
                vk = new ArrayList<>(universalDimensionMember2DArray[i].length);
                for (long id : universalDimensionMember2DArray[i]) {
                    vk.add(id);
                }
                vectorMeasureValueMap.put(vk, i);
            }
            universalDimensionMember2DArray = null; // release resources
        }
    }

    private void extractDateDimensionRoleScope() {
        cube = cube != null ? cube : OlapEngine.hold().find(Cube.class, cubeId);
        List<DimensionRole> dimensionRoles = cube.getDimensionRoles(null);
        Collections.sort(dimensionRoles);
        for (int i = 0; i < dimensionRoles.size(); i++) {
            if (dimensionRoles.get(i).getDimension() instanceof DateDimension) {
                Set<Integer> yearSet = new HashSet<>();

                for (long[] ls : universalDimensionMember2DArray) {

                    int year = (int) (ls[i] / 10000);
                    int month = (int) (ls[i] % 10000 / 100);
                    int day = (int) (ls[i] % 100);
                    if (!(month > 0 && month < 13 && day > 0)) {
//                        System.out.println(String.format("%d / %d / %d", year, month, day));
                        getLogger().error("data error: date dimension not-leaf member " + ls[i]);
                    }
                    yearSet.add(year);
                }

                DATE_DIMENSION_ROLE_DATE_SCOPE_MAP.put(dimensionRoles.get(i).getMgId() + "_MIN", Collections.min(yearSet));
                DATE_DIMENSION_ROLE_DATE_SCOPE_MAP.put(dimensionRoles.get(i).getMgId() + "_MAX", Collections.max(yearSet));
            }
        }
    }

    public Integer getDateDimensionRoleMinYear(DimensionRole dimensionRole) {
        return DATE_DIMENSION_ROLE_DATE_SCOPE_MAP.get(dimensionRole.getMgId() + "_MIN");
    }

    public Integer getDateDimensionRoleMaxYear(DimensionRole dimensionRole) {
        return DATE_DIMENSION_ROLE_DATE_SCOPE_MAP.get(dimensionRole.getMgId() + "_MAX");
    }

    private static class NullMeasureFlag {
        private boolean isNull = true;

        public NullMeasureFlag setTrue() {
            isNull = true;
            return this;
        }

        public NullMeasureFlag setFalse() {
            isNull = false;
            return this;
        }

        public boolean isNull() {
            return isNull;
        }
    }

    public MDVectorSet setVectorCache(VectorCache vc) {
        vectorCache = vc;
        return this;
    }
}
