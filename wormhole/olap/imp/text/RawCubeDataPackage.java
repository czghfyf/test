package cn.bgotech.wormhole.olap.imp.text;

import cn.bgotech.wormhole.olap.exception.OlapException;
import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import cn.bgotech.wormhole.olap.imp.text.parser.ParseException;
import cn.bgotech.wormhole.olap.imp.text.parser.TextFormatCubeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by ChenZhiGang on 2017/5/23.
 */
public class RawCubeDataPackage {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawCubeDataPackage.class);

    private List<Map<String, String>> universalDimensions = new LinkedList<>();

    private List<String> measureMemberNameList = new LinkedList<>();

    private List<VectorLine> lines = new LinkedList<>();

    private Set<String> presetDimensions = new HashSet<>();

    public List<Map<String, String>> getUniversalDimensions() {
        return universalDimensions;
    }

    public List<String> getMeasureMemberNameList() {
        return measureMemberNameList;
    }

    public List<VectorLine> getLines() {
        return lines;
    }

    public void addLine(VectorLine line) {
        lines.add(line);
    }

    public static RawCubeDataPackage buildByText(String text) throws OlapException {
        RawCubeDataPackage result;
        try {
            result = beCreated(new TextFormatCubeParser(text));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            throw new OlapException(e);
        }
        return result;
    }

    public static RawCubeDataPackage buildByByteArray(byte[] bytes) throws OlapException {
        InputStream is = new ByteArrayInputStream(bytes);
        RawCubeDataPackage dataPkg;
        try {
            dataPkg = beCreated(new TextFormatCubeParser(is, "UTF-8"));
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            throw new OlapException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                throw new OlapException(e);
            }
        }
        return dataPkg;
    }

    public static RawCubeDataPackage beCreated(TextFormatCubeParser parser) throws ParseException, OlapException {
        return parser.parserEntry();
    }

    /**
     * @param dimensionName
     * @param dimensionRole
     * @param preDimensionFlag
     * @throws OlapException
     */
    public void addUniversalDimensionName(String dimensionName, String dimensionRole, boolean preDimensionFlag) throws OlapException {
        addUniversalDimensionName(dimensionName, dimensionRole);
        if (preDimensionFlag) {
            presetDimensions.add(dimensionName);
        }
    }

    public void addUniversalDimensionName(String dimensionName, String dimensionRole) throws OlapException {

        for (Map<String, String> m : universalDimensions) {
            if (dimensionRole.equals(m.get("DIMENSION_ROLE"))) {
                throw new OlapException("duplicate general dimension role [" + dimensionRole + "]");
            }
        }
        Map<String, String> m = new HashMap<>();
        m.put("DIMENSION", dimensionName);
        m.put("DIMENSION_ROLE", dimensionRole);
        universalDimensions.add(m);

    }

    public void addMeasureMemberName(String measureMemberName) throws OlapException {
        if (measureMemberNameList.contains(measureMemberName)) {
            throw new OlapException("duplicate measure member[" + measureMemberName + "]");
        }
        measureMemberNameList.add(measureMemberName);
    }

    public int getDimensionRolePosition(String dimensionName) {
        for (int i = 0; i < universalDimensions.size(); i++) {
            if (universalDimensions.get(i).get("DIMENSION_ROLE").equals(dimensionName)) {
                return i;
            }
        }
        throw new OlapRuntimeException("not found dimension role by name [" + dimensionName + "]");
    }

    public boolean hasPresetDimension(String dimensionName) {
        return presetDimensions.contains(dimensionName);
    }
}

