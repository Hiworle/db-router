package site.penghao.dbrouter.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;
import site.penghao.dbrouter.RouterContext;
import site.penghao.dbrouter.annotation.EnableRouter;
import site.penghao.dbrouter.annotation.TableRouting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 拦截所有的 prepare 语句，通过反射进行 sql 动态修改
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
@Component
public class MybatisPlugin implements Interceptor {

    private static final Pattern pattern = Pattern.compile("(into|update|from)\\s+\\w+", Pattern.CASE_INSENSITIVE);

    /**
     * 拦截器，拦截所有需要分库分表的方法，进行语句的处理后，再执行
     *
     * @param invocation 被拦截的方法
     * @return 执行的返回值
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 通过反射找到声明 SQL 的 Mapper 类
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = MetaObject.forObject(statementHandler, SystemMetaObject.DEFAULT_OBJECT_FACTORY,
                SystemMetaObject.DEFAULT_OBJECT_WRAPPER_FACTORY, new DefaultReflectorFactory());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        String id = mappedStatement.getId();

        // 判断路由是否开启
        Class<?> mapperKlass = Class.forName(id.substring(0, id.lastIndexOf(".")));
        EnableRouter enableRouter = mapperKlass.getAnnotation(EnableRouter.class);
        if (enableRouter == null) {
            // 不开启路由
            return invocation.proceed();
        }
        // 开启路由
        String methodName = id.substring(id.lastIndexOf(".") + 1);
        Method method = null;
        for (Method m : mapperKlass.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        assert method != null;
        TableRouting tableRouting = method.getAnnotation(TableRouting.class);
        if (tableRouting == null) {
            // 不设置 @TableRouting 与不开启路由没有什么区别
            return invocation.proceed();
        }

        // 设置了 @TableRouting，使用上下文更新 SQL
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        Matcher matcher = pattern.matcher(sql);
        String newTbName = "";
        if (matcher.find()) {
            newTbName = matcher.group().trim() + "_" + RouterContext.getTbName();
        }
        String newSql = matcher.replaceAll(newTbName);
        Field field = boundSql.getClass().getDeclaredField("sql");
        field.setAccessible(true);
        field.set(boundSql, newSql);
        field.setAccessible(false);

        return invocation.proceed();
    }

}
