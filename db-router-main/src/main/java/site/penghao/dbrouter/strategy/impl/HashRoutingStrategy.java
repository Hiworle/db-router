package site.penghao.dbrouter.strategy.impl;

import site.penghao.dbrouter.RouterContext;
import site.penghao.dbrouter.strategy.IRoutingStrategy;

import java.util.HashMap;

public class HashRoutingStrategy implements IRoutingStrategy {

    public HashRoutingStrategy() {
    }

    @Override
    public void route(Object value, int dbCount, int tbCount) {
        assert value != null;

        int hash = hash(value);
        int size = dbCount * tbCount;
        int index = hash % size;

        // 数据库命名从 1 开始，需要 +1
        int dbNum = index / tbCount + 1;
        int tbIdx = index - (dbNum - 1) * tbCount;

        // 命名规则：
        // 数据库：01、02、03
        // 表：000、001、002、003
        RouterContext.setDbName(String.format("%02d", dbNum));
        RouterContext.setTbName(String.format("%03d", tbIdx));
    }

    @Override
    public void clear() {
        RouterContext.clearDbName();
        RouterContext.clearTbName();
    }

    final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
}
