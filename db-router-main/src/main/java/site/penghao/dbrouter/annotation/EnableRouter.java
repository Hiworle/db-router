package site.penghao.dbrouter.annotation;

import site.penghao.dbrouter.strategy.IRoutingStrategy;
import site.penghao.dbrouter.strategy.impl.HashRoutingStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnableRouter {
}
