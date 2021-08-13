package cn.bounter.auto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 货币'元'
 * 加了该注解的货币字段编译后会自动生成以元计算的金额
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
public @interface Unit {

    /**
     * 单位字符串，如'元'
     * @return
     */
    String value() default "";
}
