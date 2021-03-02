package cn.bgotech.analytics.bi.cache;

import cn.bgotech.wormhole.olap.mddm.physical.schema.Space;

import java.util.*;

/**
 * Created by ChenZhiGang on 2017/6/20.
 * @deprecated 过于复杂，即将被新缓存服务{@link MDDMCacheService}代替，目前采取两套缓存并行的临时方案。2019/3/27
 */
@Deprecated
public interface CacheService {

//    /**
//     * create a link from clazz to the cache created by obj.getClass()
//     * @param clazz super class or interface
//     * @param obj this obj must extends on clazz or implements of clazz
//     * @param key
//     */
//    <C> void set(Class<? super C> clazz, C obj, Object key);

//    default void set(Object obj, Object key) {
//        set(null, obj, key);
//    }

//    /**
//     * @deprecated Not easy to use. May be deleted in the future.
//     * @param obj
//     * @param key
//     */
//    @Deprecated
//    void set(Object obj, Object key);

//    /**
//     * @deprecated Not easy to use. May be deleted in the future.
//     * @param clazz
//     * @param key
//     * @param <E>
//     * @return
//     */
//    @Deprecated
//    <E> E get(Class<E> clazz, Object key);

//    CacheNode createDataSuite(Object dataSuiteKey);

    /**
     * a data suite is a tree building by cache nodes.
     * the method create a data suite for multi-dimensional domain model.
     * @param space
     */
    void createMDDMDataSuite(Space space);

    void rebuildMDDMDataSuite(Space space);

    /**
     * @param dataSuiteKey
     * @return a root cache node of a data suite tree building by cache nodes
     */
    CacheNode getDataSuite(Object dataSuiteKey);

    /**
     * @return - Returns the cached data suite for the space corresponding to the current thread user
     */
    CacheNode getCurrentUserSpaceMDDMDataSuite();

    /**
     * preload ths system preset data, as: Dimensions, Hierarchies, Levels, Members
     */
    void systemPresetDataInit();

    void buildFinalDescendantMembersMapping(CacheNode dataSuite);

    /**
     * @param space 当对应多维模型缓存没有被加载时，构建对应的缓存套件。
     */
    void buildSpaceCacheSuiteWhenIsNo(Space space);

    class CacheNode {

//        public enum Relation { // public static enum Relation
//            CUBE,
//            CUBE__DIMENSION_ROLE,
//            DIMENSION,
//            DIMENSION__ROOT_MEMBER,
//            DIMENSION__HIERARCHY,
//            HIERARCHY__LEVEL,
//            LEVEL__MEMBER
//        }

        public enum SpecialLinkType { // public static enum SpecialLinkType
            ROOT_MEMBER,
            SYSTEM_PRESET_DATA,
            FINAL_DESCENDANT_MEMBERS_MAPPING
        }

        private Object object;

        private Map<Object, Map<Object, Object>> cacheRelationTopologyNode = new HashMap<>();

        public CacheNode(Object o) {
            object = o;
        }

        public void setMapping(Object linkType, GetKeyFun getKeyFun, List<?> objects) {
            set(true, linkType, getKeyFun, objects);
        }

        public void setMapping(Object linkType, GetKeyFun getKeyFun, Object object) {
            setMapping(linkType, getKeyFun, Arrays.asList(object));
        }

        public void setFinalMapping(Object linkType, GetKeyFun getKeyFun, List<?> objects) {
            set(false, linkType, getKeyFun, objects);
        }

        public void setFinalMapping(Object linkType, GetKeyFun getKeyFun, Object object) {
            setFinalMapping(linkType, getKeyFun, Arrays.asList(object));
        }

        /**
         *
         * @param isStoreCacheNodes - if true, store MDDMCacheNode.
         * @param linkType
         * @param getKeyFun
         * @param objects
         */
        private void set(boolean isStoreCacheNodes, Object linkType, GetKeyFun getKeyFun, List<?> objects) {
            if (!cacheRelationTopologyNode.containsKey(linkType)) {
                cacheRelationTopologyNode.put(linkType, new HashMap<>());
            }
            for (Object o : objects) {
                cacheRelationTopologyNode.get(linkType).put(getKeyFun.getKey(o), isStoreCacheNodes ? new CacheNode(o) : o);
            }
        }

        public CacheNode getCacheNode(Object linkType, Object key) {
            if (cacheRelationTopologyNode.containsKey(linkType)) {
                Object v = cacheRelationTopologyNode.get(linkType).get(key);
                if (v instanceof CacheNode) {
                    return (CacheNode) v;
                }
            }
            return null;
        }

        /**
         *
         * @param linkType
         * @param key
         * @return
         */
        public Object getObject(Object linkType, Object key) {
            return getObject(linkType, key, true);

        }

        /**
         *
         * @param linkType
         * @param key
         * @param unpacking
         * @return
         */
        public Object getObject(Object linkType, Object key, boolean unpacking) {
            if (cacheRelationTopologyNode.containsKey(linkType)) {
                Object v = cacheRelationTopologyNode.get(linkType).get(key);
                if (v instanceof CacheNode && unpacking) {
                    return ((CacheNode) v).object;
                }
                return v;
            }
            return null;
        }

        /**
         *
         * @param linkType
         * @param matcher
         * @return
         */
        public List<Object> findObjects(Object linkType, ObjectMatcher matcher) {
            return findObjects(linkType, matcher, true);
        }

        /**
         *
         * @param linkType
         * @return
         */
        public List<Object> findObjects(Object linkType) {
            return findObjects(linkType, o -> true);
        }

        /**
         *
         * @param linkType
         * @param matcher
         * @param unpacking
         * @return
         */
        public List<Object> findObjects(Object linkType, ObjectMatcher matcher, boolean unpacking) {
            List<Object> objects = new ArrayList();
            if (cacheRelationTopologyNode.containsKey(linkType)) {
                Object entity;
                for (Object o : cacheRelationTopologyNode.get(linkType).values()) {
                    entity = unpacking && o instanceof CacheNode ? ((CacheNode) o).object : o;
                    if (matcher.match(entity)) {
                        objects.add(entity);
                    }
                }
            }
            return objects;
        }

//        public void set(Relation relation, GetKeyFun getKeyFun, List<?> objects) {
//
//            if (!cacheRelationTopologyNode.containsKey(relation)) {
//                cacheRelationTopologyNode.put(relation, new HashMap<>());
//            }
//            for (Object o : objects) {
//                cacheRelationTopologyNode.get(relation).put(getKeyFun.getKey(o), new MDDMCacheNode(o));
//            }
//
//        }

//        public void set(Relation relation, GetKeyFun getKeyFun, Object object) {
//            set(relation, getKeyFun, Arrays.asList(object));
//        }

//        public MDDMCacheNode get(Relation relation, Object key) {
//            if (cacheRelationTopologyNode.containsKey(relation)) {
//                return cacheRelationTopologyNode.get(relation).get(key);
//            }
//            return null;
//        }

    }

    /**
     * return cache key be used store this object-v
     */
    @FunctionalInterface
    interface GetKeyFun { // public static interface GetKeyFun
        Object getKey(Object v);
    }

    @FunctionalInterface
    interface ObjectMatcher {
        boolean match(Object o);
    }

}
