package cn.bgotech.wormhole.olap.util.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ChenZhiGang on 2017/6/6.
 */
@FunctionalInterface
public interface SetConverter<D, O> {

    D convert(O o);

    default Set<D> convert(Collection<O> os) {
        Set<D> ds = new HashSet<>();
        for (O o : os) {
            ds.add(convert(o));
        }
        return ds;
    }

}
