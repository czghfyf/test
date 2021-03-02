package cn.bgotech.analytics.bi.component.olap.vce;

import cn.bgotech.wormhole.olap.util.BGTechUtil;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by czg on 2019/3/20.
 */
public class DataBuf {

    private byte[] bytes = new byte[1024 * 1024 * 4]; // 4M

    private int idx = 0;

    public void add(int i) {
        byte[] byteArr = BGTechUtil.int2byteArray(i);
        for (int j = 0; j < byteArr.length; j++) {
            bytes[idx++] = byteArr[j];
        }
    }

    public void add(long l) {
        byte[] bs = BGTechUtil.long2byteArray(l);
        for (int j = 0; j < bs.length; j++) {
            bytes[idx++] = bs[j];
        }
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void clean() {
        idx = 0;
    }

    public void add(List<Byte> bs) {
        byte[] bytes_ = new byte[bs.size()];
        ListIterator<Byte> it = bs.listIterator();
        int i = 0;
        while (it.hasNext()) {
            bytes_[i++] = it.next();
        }
        add(bytes_);
    }

    public void add(byte[] bytes_) {
        if ((bytes.length - idx + 1) < bytes_.length)
            throw new RuntimeException(getClass().getSimpleName() + ": The amount of data is too large.");
        for (int i = 0; i < bytes_.length; i++) {
            bytes[idx + i] = bytes_[i];
        }
        idx += bytes_.length;
    }

    public int effectiveDataLength() {
        return idx;
    }

    /**
     * copy src data to tar
     *
     * @param tar
     * @param src
     */
    public static void copy(DataBuf tar, DataBuf src) {
        tar.idx = src.idx;
        for (int i = 0; i < tar.idx; i++)
            tar.bytes[i] = src.bytes[i];
    }

    public void setIndex(int idx_) {
        idx = idx_;
    }
}
