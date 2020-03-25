package xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a field can be embedded as a child of an XML Element.
 * @author  Christian Burns
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlElement {
    /**
     * Specifies an explicit tag name to use when embedding the Element.
     * Defaults to the declared field name.
     */
    String name() default "";
}
