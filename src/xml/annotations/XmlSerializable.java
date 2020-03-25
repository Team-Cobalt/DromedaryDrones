package xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class can be parsed into a XML Element.
 * @author  Christian Burns
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlSerializable {
    /**
     * XML Element name
     */
    String name() default "";
}
