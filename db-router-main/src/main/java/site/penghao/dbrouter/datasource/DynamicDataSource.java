package site.penghao.dbrouter.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import site.penghao.dbrouter.RouterContext;

/**
 * 动态配置数据库，JDBC 每次连接数据库时，都会使用该方法选择数据库配置
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + RouterContext.getDbName();
    }
}
