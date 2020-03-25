package xml.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a field contains a list of XML Elements.
 * @author  Christian Burns
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlElementList {
    /**
     * Setting this property to {@code true} will use the output
     * of a Map key's toString method for an Element name and embeds
     * the key's corresponding map value within the Element as a child.
     * <br><pre>{@code
     * @ XmlSerializable
     * public class Foo {
     *      @ XmlAttribute
     *      public int id = 6;
     *
     *      public String toString() {
     *          return "foo" + id;
     *      }
     * }
     *
     * -------------------------------------
     *
     * @ XmlElementList(stringify = false)
     * Map<Foo, String> foos;
     * ...
     *      <foos>
     *          <Foo id="6">Foo Message</Foo>
     *          <Foo id="17">Different Foo Message</Foo>
     *      </foos>
     *
     * -------------------------------------
     *
     * @ XmlElementList(stringify = true)
     * Map<Foo, String> foos;
     * ...
     *      <foos>
     *          <foo6>Foo Message</foo6>
     *          <foo17>Different Foo Message</foo17>
     *      </foos>
     *
     * }</pre>
     */
    boolean stringify() default false;

    /**
     * Setting this property to {@code false} will append the child
     * elements of the list to the parent Element instead of embedding
     * them within a new Element.
     * <br><pre>{@code
     * @ XmlElementList(embed = true)
     * ...
     * <parentclass>
     *     <listelements>
     *         <element>1</element>
     *         <element>2</element>
     *         <element>3</element>
     *     </listelements>
     * </parentclass>
     *
     *
     * @ XmlElementList(embed = false)
     * ...
     * <parentclass>
     *     <element>1</element>
     *     <element>2</element>
     *     <element>3</element>
     * </parentclass>
     *
     * }</pre>
     */
    boolean embed() default true;

    /**
     * Specifies an explicit tag name to use when embedding a list of elements.
     * Defaults to the declared field name.
     * <br><pre>{@code
     * <parentclass>
     *     <outertag>
     *          <innertag>1</innertag>
     *          <innertag>2</innertag>
     *          <innertag>3</innertag>
     *     </outertag>
     * </parentclass>
     * }</pre>
     */
    String outerTag() default "";

    /**
     * Specifies an explicit tag name to use when embedding a list of elements.
     * Defaults to the declared field name.
     * <br><pre>{@code
     * <parentclass>
     *     <outertag>
     *          <innertag>1</innertag>
     *          <innertag>2</innertag>
     *          <innertag>3</innertag>
     *     </outertag>
     * </parentclass>
     * }</pre>
     */
    String innerTag() default "";
}
