package site.penghao.dbrouter;

import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.penghao.dbrouter.annotation.EnableRouter;
import site.penghao.dbrouter.annotation.TableRouting;
import site.penghao.dbrouter.strategy.IRoutingStrategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 切面控制，调用 Mapper 上注解配置的路由策略，设置上下文
 */
@Component
@Aspect
public class RouterJoinPoint {

    @Autowired
    private RouterConfig routerConfig;

    /**
     * 拦截带有 @EnableRouter 注解的类下的 @tableRouting 修饰的方法，
     * 调用设定的策略，计算分到的库名、表名，保存到上下文
     *
     * @param proceedingJoinPoint
     * @param tableRouting
     * @return
     */
    @Around(value = "@within(site.penghao.dbrouter.annotation.EnableRouter) && @annotation(tableRouting)")
    public Object aroundMethod(ProceedingJoinPoint proceedingJoinPoint, TableRouting tableRouting) {
        try {

            // 获取路由 key 对应的值，用于路由策略的参数
            String key = tableRouting.key();
            Object[] args = proceedingJoinPoint.getArgs();
            String value = null;
            if (args.length == 1 && args[0] instanceof Integer) {
                value = String.valueOf(args[0]);
            } else {
                for (Object arg : args) {
                    value = BeanUtils.getProperty(arg, key);
                    if (value != null) {
                        break;
                    }
                }
            }

            // 执行策略，route()
            Class<? extends IRoutingStrategy> strategyKlass = tableRouting.strategy();
            IRoutingStrategy strategy = strategyKlass.getDeclaredConstructor().newInstance();
            Method route = strategyKlass.getMethod("route", Object.class, int.class, int.class);
            route.invoke(strategy, value, routerConfig.getDbCount(), routerConfig.getTbCount());

            // 执行数据库操作
            Object result = proceedingJoinPoint.proceed();

            // 后处理，clear()
            Method clear = strategyKlass.getMethod("clear");
            clear.invoke(strategy);

            return result;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

}
