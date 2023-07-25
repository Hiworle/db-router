package site.penghao.dbrouter.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ObjectUtils;
import site.penghao.dbrouter.RouterConfig;
import site.penghao.dbrouter.datasource.DynamicDataSource;
import site.penghao.dbrouter.util.PropertyUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自动配置 Router 的数据源
 */
@Configuration
@AutoConfigureBefore(SpringBootConfiguration.class)
public class DataSourceAutoConfig implements EnvironmentAware {

    /**
     * 组名 -> 数据源参数 <br>
     * db01: {url: something, username: something, ... } <br>
     * 根据 db-router.jdbc.list 的信息创建
     */
    private final Map<String, Map<String, String>> dataSourcePropertiesMap;

    /**
     * 默认数据源，根据 db-router.jdbc.default 的信息创建
     */
    Map<String, String> defaultDataSourceProperty = null;

    /**
     * 数据库总数，读取自 db-router.jdbc.datasource.dbCount
     */
    private String dbCountProperty = null;

    /**
     * 每个数据库的表总数，读取自 db-router.jdbc.datasource.tbCount
     */
    private String tbCountProperty = null;

    public DataSourceAutoConfig() {
        dataSourcePropertiesMap = new HashMap<>();
    }

    @Bean
    public DataSource dataSource() {
        // 创建分库分表数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        Set<String> dbKeys = dataSourcePropertiesMap.keySet();
        for (String dbKey : dbKeys) {
            Map<String, String> property = dataSourcePropertiesMap.get(dbKey);
            DataSource dataSource = new DriverManagerDataSource(
                    property.get("url"),
                    property.get("username"),
                    property.get("password"));
            targetDataSources.put(dbKey, dataSource);
        }

        // 创建默认数据源
        DataSource dufaultTargetDataSource = new DriverManagerDataSource(
                defaultDataSourceProperty.get("url"),
                defaultDataSourceProperty.get("username"),
                defaultDataSourceProperty.get("password"));

        // 设置数据源
        DynamicDataSource dynamicDatasource = new DynamicDataSource();
        dynamicDatasource.setTargetDataSources(targetDataSources);
        dynamicDatasource.setDefaultTargetDataSource(dufaultTargetDataSource);
        return dynamicDatasource;
    }


    @Bean
    public RouterConfig routerConfig() {
        int dbCount = Integer.parseInt(dbCountProperty);
        int tbCount = Integer.parseInt(tbCountProperty);
        return new RouterConfig(dbCount, tbCount);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "db-router.jdbc.datasource.";
        dbCountProperty = environment.getProperty(prefix + "dbCount");
        tbCountProperty = environment.getProperty(prefix + "tbCount");
        String sourceGroupList = environment.getProperty(prefix + "list");
        if (!ObjectUtils.isEmpty(sourceGroupList)) {
            String[] groupNames = sourceGroupList.split(",");
            for (String group : groupNames) {
                Map<String, String> propertyMap = PropertyUtil.handle(environment, prefix + group, Map.class);
                dataSourcePropertiesMap.put(group, propertyMap);
            }
        }

        // 加载默认数据库
        String defaultData = environment.getProperty(prefix + "default");
        assert null != defaultData;
        defaultDataSourceProperty =
                PropertyUtil.handle(environment, prefix + defaultData, Map.class);
    }
}
