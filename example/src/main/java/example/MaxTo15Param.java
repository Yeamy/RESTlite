package example;

import yeamy.restlite.annotation.ParamFactory;

@ParamFactory(processorClass = MaxTo15.class, processor = "maxTo15", nameMethod = "value", requiredMethod = "required")
public @interface MaxTo15Param {

    String value() default "";

    boolean required() default true;
}
