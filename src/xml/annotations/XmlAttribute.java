package xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a field should be added as an attribute
 * to the parent Element.
 * @author  Christian Burns
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlAttribute {
    /**
     * Specifies an explicit tag name for the attribute.
     * Defaults to the declared field name.
     */
    String name() default "";
}
