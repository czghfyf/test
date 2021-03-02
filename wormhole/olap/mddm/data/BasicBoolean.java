package cn.bgotech.wormhole.olap.mddm.data;

/**
 * Created by ChenZhiGang on 2017/5/12.
 */
public abstract class BasicBoolean implements BasicData {

    public static final BasicBoolean TRUE = new BasicBoolean() {

        private final Boolean booleanObj = new Boolean(true);

        @Override
        public boolean isTrue() {
            return true;
        }

        @Override
        public BasicBoolean inversely() {
            return BasicBoolean.FALSE;
        }

        @Override
        public String image() {
            return "true";
        }

        @Override
        public Object value() {
            return booleanObj;
        }
    };

    public static final BasicBoolean FALSE = new BasicBoolean() {

        private final Boolean booleanObj = new Boolean(false);

        @Override
        public boolean isTrue() {
            return false;
        }

        @Override
        public BasicBoolean inversely() {
            return BasicBoolean.TRUE;
        }

        @Override
        public String image() {
            return "false";
        }

        @Override
        public Object value() {
            return booleanObj;
        }
    };

    public abstract boolean isTrue();

    public abstract BasicBoolean inversely();

}
