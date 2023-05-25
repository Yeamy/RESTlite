package yeamy.restlite.annotation;

import java.util.HashMap;

class SourceFields {
    public final HashMap<NacosField, NacosField> map = new HashMap<>();

    public NacosField get(String dataId, String group) {
        return map.get(new NacosField(dataId, group, null, null));
    }

    public void put(NacosPullValue pull, String fieldType, String fieldName) {
        NacosField id = new NacosField(pull.dataId(), pull.group(), fieldType, fieldName);
        map.put(id, id);
    }

    public void put(NacosPushValue push, String fieldType, String fieldName) {
        NacosField id = new NacosField(push.dataId(), push.group(), fieldType, fieldName);
        map.put(id, id);
    }

    public int index() {
        return map.size();
    }

    public static class NacosField {
        public final String group;
        public final String dataId;
        public final String type;
        public final String name;
        private final int hashCode;

        public NacosField(String dataId, String group, String type, String name) {
            this.dataId = dataId;
            this.group = group;
            hashCode = (group + dataId).hashCode();
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NacosField id) {
                return group.equals(id.group) && dataId.equals(id.dataId);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
