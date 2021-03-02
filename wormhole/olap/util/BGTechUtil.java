package cn.bgotech.wormhole.olap.util;

import cn.bgotech.wormhole.olap.exception.OlapRuntimeException;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by czg on 2019/3/15.
 */
public class BGTechUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(BGTechUtil.class);

    private static final Random rand = new Random();

    public static int readAllBytes(byte[] bytes, DataInputStream dis) {

        int len, offset = 0;
        do {
            try {
                len = dis.read(bytes, offset, 4 - offset);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return -1;
            }
            if (len < 0)
                return len;
            if (len == 0)
                LOGGER.warn("read " + len + " bytes");
            offset += len;
        } while (offset < 4);

        int dataBytesQua = BGTechUtil.bytes2Int(bytes);

        if (dataBytesQua <= 0) {
            LOGGER.error("data quantity of bytes = " + dataBytesQua);
            return dataBytesQua;
        }

        if (dataBytesQua > bytes.length) {
            LOGGER.warn(String.format("data pkg is too large [ %d ], [ bytes.length = %d ].", dataBytesQua, bytes.length));
            return 0 - dataBytesQua;
        }

        offset = 0;
        do {
            try {
                len = dis.read(bytes, offset, bytes.length - offset);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return -1;
            }
            offset += len;
        } while (offset < dataBytesQua);

        if (offset != dataBytesQua) {
            LOGGER.error("The data that has been read is inconsistent with the total amount of data!");
            return -1;
        }

        return offset;
    }

    public static byte[] int2byteArray(int i) {
        byte[] bArray = new byte[4];
        bArray[0] = (byte) i;
        bArray[1] = (byte) (i >> 8);
        bArray[2] = (byte) (i >> 16);
        bArray[3] = (byte) (i >> 24);
        return bArray;
    }

    public static byte[] long2byteArray(long lv) {
        byte[] arr = new byte[8];
        for (int i = 0; i < 8; i++)
            arr[i] = (byte) (lv >> (8 * i));
        return arr;
    }

    public static int bytes2Int(byte[] bs) {
        int j = bs[0] & 255;
        j += (bs[1] & 255) << 8;
        j += (bs[2] & 255) << 16;
        j += (bs[3] & 255) << 24;
        return j;
    }

    public static List<Byte> int2byteList(int i) {
        byte[] bs = int2byteArray(i);
        List<Byte> bls = new LinkedList<>();
        for (byte b : bs) {
            bls.add(b);
        }
        return bls;
    }

    public static List<Byte> double2byteList(double d) {

        long l = Double.doubleToRawLongBits(d);

        Byte[] bArr = new Byte[8];
        for (int i = 0; i < 8; i++) {
            bArr[i] = (byte) (l >> (i * 8));
        }
        return Arrays.asList(bArr);
    }

    public static byte[] convertByteListInto_byteArray(List<Byte> packets) {
        byte[] bs = new byte[packets.size()];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = packets.get(i);
        }
        return bs;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }


    public static String spliceToBeString(List<? extends Object> objects, String separator) {
        StringBuilder rs = new StringBuilder("");
        for (int i = 0; i < objects.size(); i++) {
            rs.append(objects.get(i).toString());
            if (i < objects.size() - 1 && separator != null) {
                rs.append(separator);
            }
        }
        return rs.toString();
    }

    public static String spliceToBeString(Object[] objects, String separator) {
        StringBuilder rs = new StringBuilder("");
        for (int i = 0; i < objects.length; i++) {
            rs.append(objects[i].toString());
            if (i < objects.length - 1 && separator != null) {
                rs.append(separator);
            }
        }
        return rs.toString();
    }

    public static boolean arrayEquals(Object[] os1, Object[] os2) {
        if (os1 == null || os2 == null || os1.length != os2.length) {
            return false;
        }
        for (int i = 0; i < os1.length; i++) {
            if (os1[i] == null) {
                if (os2[i] != null) {
                    return false;
                }
            } else {
                if (!os1[i].equals(os2[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int arrayHashCode(Object[] os) {
        if (os == null) {
            throw new RuntimeException("os not be null");
        }
        int hc = "".hashCode();
        for (int i = 0; i < os.length; i++) {
            if (os[i] != null) {
                hc *= os[i].hashCode();
            }
        }
        return hc;
    }

    public static Object copyProperties(Object dest, Object orig) {
        try {
            BeanUtils.copyProperties(dest, orig);
            return dest;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new OlapRuntimeException(e);
        }
    }

    public static List removeDuplicates(List ls) {
        Set hashSet = new HashSet();
        List result = new ArrayList();
        for (Object o : ls) {
            if (hashSet.add(o)) {
                result.add(o);
            }
        }
        return result;
    }

    public static void memcpy(byte[] bs1, byte[] bs2, int start, int end) {
        for (int i = start; i <= end; i++) {
            bs1[i - start] = bs2[i];
        }
    }

    public static long genRandomLongValue() {
        return rand.nextLong();
    }


}
