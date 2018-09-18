package orchi.HHCloud.Api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Repeatable(value = Operations.class)
@Documented
@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD})
public @interface Operation {

    String name();

    boolean isRequired() default false;

}
