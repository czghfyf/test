package cn.bgotech.wormhole.olap.util.collection;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ChenZhiGang on 2017/6/6.
 */
public class CollectionUtil {

    /**
     * Traverse Collection-'c'. Place all the items in Collection-'c' into the Set-'s'.
     * If the item in Collection-'c' already exists in Set-'s'. Throws an exception.
     * @param s
     * @param c
     * @param <E>
     * @throws Exception
     */
    public static <E> void mergeCollection(Set<E> s, Collection<E> c) throws Exception {
        for (E e : c) {
            if (s.contains(e)) {
                throw new Exception("there are duplicate objects");
            } else {
                s.add(e);
            }
        }
    }
}
