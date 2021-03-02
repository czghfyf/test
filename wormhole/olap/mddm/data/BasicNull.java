package cn.bgotech.wormhole.olap.mddm.data;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public abstract class BasicNull implements BasicData {

    public static final BasicNull INSTANCE = new BasicNull() {
        @Override
        public String image() {
            return "null";
        }

        @Override
        public Object value() {
            return null;
        }
    };

}
