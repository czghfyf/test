// PROHIBITION CONFUSION !

package cn.bgotech.analytics.bi.bean.mddm.physical.member;

import cn.bgotech.analytics.bi.exception.BIRuntimeException;
import cn.bgotech.wormhole.olap.OlapEngine;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.DateDimension;
import cn.bgotech.wormhole.olap.mddm.physical.dimension.Dimension;
import cn.bgotech.wormhole.olap.mddm.physical.member.DateMember;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by ChenZhiGang on 2017/5/16.
 * <p>
 * not stored in the database
 */
public class DateMemberBean extends UniversalMemberBean implements DateMember {

    private static final Map<Integer, DateMemberBean> DATE_MEMBER_BEAN_MAP = new HashMap<>();

    private DateLevelType levelType;

    private List<Long> finalDescendantMembersMgIdList;

    public static DateMemberBean getYearByName(String yearMemberName) {
        if (parseToDateLevelType(yearMemberName) != DateLevelType.YEAR) {
            throw new BIRuntimeException(String.format("year member name format error: '%s'", yearMemberName));
        }
        DateMemberBean year = new DateMemberBean();
        year.levelType = DateLevelType.YEAR;
        year.setName(yearMemberName);
        year.setMgId(10000 * Long.valueOf(yearMemberName.substring(0, 4)));
        return year;
    }

    public static DateMemberBean get(long dateMemberMgId) {
        int dateMgId = (int) dateMemberMgId;
        DateMemberBean result = DATE_MEMBER_BEAN_MAP.get(dateMgId);
        if (result != null) {
            return result;
        }
        result = createInstance(dateMgId);
        if (result != null) {
            DATE_MEMBER_BEAN_MAP.put(dateMgId, result);
        }
        return result;
    }

    private static DateMemberBean createInstance(int dateMgId) {
        if (dateMgId < DateMember.MD_GLOBAL_ID_START || dateMgId > DateMember.MD_GLOBAL_ID_END) {
            return null;
        }
        int mm = dateMgId % 10000 / 100;
        int dd = dateMgId % 100;
        DateMemberBean resultDate;
        if (mm == 0 && dd == 0) { // year
            resultDate = new DateMemberBean(dateMgId, DateLevelType.YEAR);
        } else if (mm > 12 && mm < 15 && dd == 0) { // half year
            resultDate = new DateMemberBean(dateMgId, DateLevelType.HALF_OF_YEAR);
        } else if ((mm == 13 && dd > 0 && dd < 3) || (mm == 14 && dd > 2 && dd < 5)) { // quarter
            resultDate = new DateMemberBean(dateMgId, DateLevelType.QUARTER);
        } else if (mm > 0 && mm < 13 && dd == 00) { // month
            resultDate = new DateMemberBean(dateMgId, DateLevelType.MONTH);
        } else { // day
            resultDate = new DateMemberBean(dateMgId, DateLevelType.DAY);
        }
        return resultDate;
    }

    public static int getDaysCount(int year, int month) {
        if (month == 2) {
            return LocalDate.of(year, 1, 1).isLeapYear() ? 29 : 28;
        }
        if (Arrays.asList(4, 6, 9, 11).contains(month)) {
            return 30;
        }
        if (month > 0 && month < 13) {
            return 31;
        }
        throw new BIRuntimeException("error month: " + month);
    }

    public DateMemberBean() {

    }

    public DateMemberBean(int mgId, DateLevelType levelType) {
        setMgId((long) mgId);
        this.levelType = levelType;
    }

    @Override
    public DateMemberBean findNearestDescendantMember(String descendantName) {
        DateLevelType descendantLvType = parseToDateLevelType(descendantName);
        if (descendantLvType == null) {
            throw new BIRuntimeException(String.format("descendant date member name error: '%s'", descendantName));
        }
        if (levelType.equals(DateLevelType.YEAR)) {
            return findYearDescendantMember(descendantLvType, descendantName);
        } else if (levelType.equals(DateLevelType.HALF_OF_YEAR)) {
            return findHalfYearDescendantMember(descendantLvType, descendantName);
        } else if (levelType.equals(DateLevelType.QUARTER)) {
            return findQuarterDescendantMember(descendantLvType, descendantName);
        } else if (levelType.equals(DateLevelType.MONTH)) {
            return findMonthDescendantMember(descendantLvType, descendantName);
        } else if (levelType.equals(DateLevelType.DAY)) {
            throw new BIRuntimeException(String.format("current date member[mgId = %d, levelType = %s] is leaf.", getMgId(), levelType));
        } else {
            throw new BIRuntimeException("level type is " + levelType);
        }
    }

    private DateMemberBean findMonthDescendantMember(DateLevelType lt, String descendantName) {
        if (DateLevelType.DAY.equals(lt)) {
            DateMemberBean day = new DateMemberBean();
            day.levelType = DateLevelType.DAY;
            day.setMgId(getMgId() + Integer.valueOf(descendantName.substring(0, descendantName.length() - 1)));
            day.setName(descendantName);
            return day;
        }
        throw new BIRuntimeException("improper date level type: " + lt);
    }

    private DateMemberBean findQuarterDescendantMember(DateLevelType lt, String descendantName) {
        if (DateLevelType.MONTH.equals(lt)) {
            DateMemberBean month = new DateMemberBean();
            month.levelType = DateLevelType.MONTH;
            month.setMgId(getMgId() / 10000 * 10000 + Long.parseLong(descendantName.substring(0, descendantName.length() - 1)) * 100);
            month.setName(descendantName);
            return month;
        }
        throw new BIRuntimeException("improper date level type: " + lt);
    }

    private DateMemberBean findHalfYearDescendantMember(DateLevelType lt, String descendantName) {
        DateMemberBean result = new DateMemberBean();
        result.levelType = lt;
        if (DateLevelType.QUARTER.equals(lt)) {
            String q = descendantName.substring(0, 1);
            int quarter = ("1".equals(q) || "一".equals(q)) ? 1 : (("2".equals(q) || "二".equals(q)) ? 2 : (("3".equals(q) || "三".equals(q)) ? 3 : 4));
            result.setMgId(getMgId() + quarter);
        } else if (DateLevelType.MONTH.equals(lt)) {
            int month = Integer.parseInt(descendantName.substring(0, descendantName.length() - 1));
            if (((getMgId() / 100 % 100 == 13) && month >= 1 && month <= 6) || ((getMgId() / 100 % 100 == 14) && month >= 7 && month <= 12)) {
                result.setMgId(getMgId() / 10000 * 10000 + month * 100);
            } else {
                throw new BIRuntimeException(String.format("error: halfOfYear = %d, descendantLevelType = %s, descendantName = S", getMgId(), lt.toString(), descendantName));
            }
        } else {
            throw new BIRuntimeException("improper date level type: " + lt);
        }
        result.setName(descendantName);
        return result;
    }

    private DateMemberBean findYearDescendantMember(DateLevelType lt, String descendantName) {
        DateMemberBean result = new DateMemberBean();
        result.levelType = lt;
        if (DateLevelType.HALF_OF_YEAR.equals(lt)) {
            result.setMgId(getMgId() + ("上半年".equals(descendantName) ? 1300 : 1400));
        } else if (DateLevelType.QUARTER.equals(lt)) {
            String q = descendantName.substring(0, 1);
            int quarter = ("1".equals(q) || "一".equals(q)) ? 1 : (("2".equals(q) || "二".equals(q)) ? 2 : (("3".equals(q) || "三".equals(q)) ? 3 : 4));
            result.setMgId(getMgId() + (quarter < 3 ? 1300 : 1400) + quarter);
        } else if (DateLevelType.MONTH.equals(lt)) {
            result.setMgId(getMgId() + Long.parseLong(descendantName.substring(0, descendantName.length() - 1)) * 100);
        } else {
            throw new BIRuntimeException(String.format("error: year = %d, descendantLevelType = %s, descendantName = S", getMgId(), lt.toString(), descendantName));
        }
        result.setName(descendantName);
        return result;
    }

    public static DateLevelType parseToDateLevelType(String dateMemberName) {
        return Pattern.matches("^\\d{4}年$", dateMemberName) ? DateLevelType.YEAR :
                (Pattern.matches("^(上|下)半年$", dateMemberName) ? DateLevelType.HALF_OF_YEAR :
                        (Pattern.matches("^[1234]季度$", dateMemberName) ? DateLevelType.QUARTER :
                                (Pattern.matches("^[123456789(10)(11)(12)]月$", dateMemberName) ? DateLevelType.MONTH :
                                        (Pattern.matches("^\\d{1,2}日$", dateMemberName) ? DateLevelType.DAY : null))));
    }

    @Override
    public String getName() {
        String name = super.getName();
        if (name == null) {
            int yyyy = (int) (getMgId() / 10000);
            int mm = (int) (getMgId() % 10000 / 100);
            int dd = (int) (getMgId() % 100);
            switch (levelType) {
                case YEAR:
                    name = yyyy + "年";
                    break;
                case HALF_OF_YEAR:
                    name = mm == 13 ? "上半年" : "下半年";
                    break;
                case QUARTER:
                    name = dd + "季度";
                    break;
                case MONTH:
                    name = mm + "月";
                    break;
                case DAY:
                    name = dd + "日";
                    break;
                default:
                    throw new BIRuntimeException(String.format("error DateLevelType enum value [%s]", levelType));
            }
            setName(name);
        }
        return name;
    }

    @Override
    public boolean isLeaf() {
        return DateLevelType.DAY.equals(levelType);
    }

    @Override
    public Integer getMemberLevel() {
        if (isRoot()) {
            return 0;
        }
        return levelType.ordinal() + 1;
    }

    @Override
    public Dimension getDimension() {
        return OlapEngine.hold().getSysPredefinedDimensions().stream().filter(d -> d instanceof DateDimension)
                .collect(Collectors.toList()).get(0);
    }

    @Override
    public boolean isRoot() {
        return getMgId() == DateMember.ROOT_DATE_MEMBER_MG_ID;
    }

    @Override
    public List<Long> getFinalDescendantMembersMgIdList() {
        if (finalDescendantMembersMgIdList != null && !finalDescendantMembersMgIdList.isEmpty()) {
            return finalDescendantMembersMgIdList;
        }
        finalDescendantMembersMgIdList = new ArrayList<>();
        int mgId = Math.toIntExact(getMgId());
        int yyyy = mgId / 10000;
        int mm = mgId % 10000 / 100;
        int dd = mgId % 100;
        int[] monthScope;
        if (isRoot()) {
            throw new BIRuntimeException("prohibit call the method on predefined date dimension root member");
        }
        if (DateLevelType.YEAR.equals(levelType)) {
            monthScope = new int[]{1, 12};
        } else if (DateLevelType.HALF_OF_YEAR.equals(levelType)) {
            monthScope = mm == 13 ? new int[]{1, 6} : new int[]{7, 12};
        } else if (DateLevelType.QUARTER.equals(levelType)) {
            monthScope = dd == 1 ? new int[]{1, 3} : (dd == 2 ? new int[]{4, 6} : (dd == 3 ? new int[]{7, 9} : new int[]{10, 12}));
        } else if (DateLevelType.MONTH.equals(levelType)) {
            monthScope = new int[]{mm, mm};
        } else if (DateLevelType.DAY.equals(levelType)) {
            return Arrays.asList((long) mgId);
        } else {
            finalDescendantMembersMgIdList = null;
            throw new BIRuntimeException("error levelType: " + levelType);
        }
        for (int m = monthScope[0]; m <= monthScope[1]; m++) {
            int monthDaysCount = getDaysCount(yyyy, m);
            for (int d = 1; d <= monthDaysCount; d++) {
                finalDescendantMembersMgIdList.add((long) (yyyy * 10000 + m * 100 + d));
            }
        }
        return finalDescendantMembersMgIdList;
    }
}
