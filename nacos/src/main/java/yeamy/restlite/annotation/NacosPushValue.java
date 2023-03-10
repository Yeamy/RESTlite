package yeamy.restlite.annotation;

import com.alibaba.nacos.api.config.ConfigType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * interface public method
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface NacosPushValue {

    String group();

    String dataId();

    ConfigType type() default ConfigType.TEXT;
}
