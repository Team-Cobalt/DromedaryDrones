package xml.exceptions;

/**
 * {@code XmlSerializationException} is the superclass of those
 * exceptions that can be thrown during serialization of a class into XML.
 *
 * <p>{@code XmlSerializationException} and its subclasses are <em>unchecked
 * exceptions</em>.  Unchecked exceptions do <em>not</em> need to be
 * declared in a method or constructor's {@code throws} clause if they
 * can be thrown by the execution of the method or constructor and
 * propagate outside the method or constructor boundary.
 *
 * @author  Christian Burns
 */
public class XmlSerializationException extends RuntimeException {

    /** Constructs a new xml serialization exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public XmlSerializationException() {
        super();
    }

    /** Constructs a new xml serialization exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public XmlSerializationException(String message) {
        super(message);
    }

    /** Constructs a new xml serialization exception with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of
     * {@code cause}).  This constructor is useful for xml serialization exceptions
     * that are little more than wrappers for other throwables.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A {@code null} value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public XmlSerializationException(Throwable cause) {
        super(cause);
    }
}
