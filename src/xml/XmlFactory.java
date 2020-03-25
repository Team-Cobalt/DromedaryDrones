package xml;

import org.w3c.dom.*;
import xml.exceptions.XmlSerializationException;
import xml.annotations.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Converts Objects into XML Strings as structured by their XML Annotations.
 * @author  Christian Burns
 */
public class XmlFactory {

    /**
     * Generates an XML String of the passed Object as structured by the
     * Object's XML Annotations.
     * @param object  object to serialize into xml
     * @return        tab formatted XML String
     * @throws XmlSerializationException  an Annotation was misused
     */
    public static String toXmlString(Object object) throws XmlSerializationException {
        try {
            // construct the document
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // convert the object into xml and add it to the document
            objectToXml(document, null, null, object);

            // convert the xml document into the string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new StringWriter());
            transformer.transform(domSource, streamResult);

            return streamResult.getWriter().toString();
        } catch (Exception e) {
            throw new XmlSerializationException(e);
        }
    }

    /**
     * Checks if the Object has the XmlSerializable Annotation.
     * @param object  object to check
     * @return  {@code true} if the Annotation was found
     */
    private static boolean checkIfXmlSerializable(Object object) {
        if (Objects.isNull(object)) return false;
        return object.getClass().isAnnotationPresent(XmlSerializable.class);
    }

    /**
     * Converts an Object to an XML Element and adds it to the parent or to
     * the document if the parent is null.
     * @param document  root document element
     * @param parent    parent element the object belongs to, or null
     * @param name      an element name override to use, or null
     * @param object    object to convert into XML
     * @throws IllegalAccessException  if permissions are lacking to access the object's data
     */
    @SuppressWarnings("unchecked")
    private static void objectToXml(Document document, Element parent, String name, Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        String tagName = name;

        // construct the new element tag name
        if (name == null || name.isBlank()) {
            if (checkIfXmlSerializable(object)) {
                tagName = clazz.getAnnotation(XmlSerializable.class).name();
                if (tagName.isEmpty()) tagName = clazz.getSimpleName().toLowerCase();
            }
        }

        if (checkIfXmlSerializable(object)) {

            // create the new element for the object
            Element xmlElement = document.createElement(toXmlTag(tagName));
            Objects.requireNonNullElse(parent, document).appendChild(xmlElement);

            // for every class variable within
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(XmlAttribute.class)) {           // variable is flagged as an attribute

                    String attrName = field.getAnnotation(XmlAttribute.class).name();
                    if (attrName.isEmpty()) attrName = field.getName().toLowerCase();
                    Attr xmlAttribute = document.createAttribute(toXmlTag(attrName));
                    xmlAttribute.setValue(String.valueOf(field.get(object)));
                    xmlElement.setAttributeNode(xmlAttribute);

                } else if (field.isAnnotationPresent(XmlElement.class)) {      // field is flagged as an element

                    String elemName = field.getAnnotation(XmlElement.class).name();
                    objectToXml(document, xmlElement, elemName, field.get(object));

                } else if (field.isAnnotationPresent(XmlElementList.class)) {  // field is flagged as a list of elements
                    if (Collection.class.isAssignableFrom(field.getType())) {  // field is a collection

                        XmlElementList anno = field.getAnnotation(XmlElementList.class);
                        String childName = anno.innerTag();
                        Element xmlChildElement;

                        if (anno.embed()) {
                            String elemName = anno.outerTag();
                            if (elemName.isEmpty()) elemName = field.getName().toLowerCase();
                            xmlChildElement = document.createElement(toXmlTag(elemName));
                            xmlElement.appendChild(xmlChildElement);
                        } else {
                            xmlChildElement = xmlElement;
                        }
                        for (Object o : (Collection<Object>) field.get(object))
                            objectToXml(document, xmlChildElement, childName, o);

                    } else if (field.getType().isArray()) {                        // field is an array

                        XmlElementList anno = field.getAnnotation(XmlElementList.class);
                        String childName = anno.innerTag();
                        Element xmlChildElement;

                        if (anno.embed()) {
                            String elemName = anno.outerTag();
                            if (elemName.isEmpty()) elemName = field.getName().toLowerCase();
                            xmlChildElement = document.createElement(toXmlTag(elemName));
                            xmlElement.appendChild(xmlChildElement);
                        } else {
                            xmlChildElement = xmlElement;
                        }
                        Object array = field.get(object);
                        int length = Array.getLength(field.get(object));
                        for (int i = 0; i < length; i++) {
                            Object o = Array.get(array, i);
                            objectToXml(document, xmlChildElement, childName, o);
                        }

                    } else if (Map.class.isAssignableFrom(field.getType())) {  // field is a map

                        XmlElementList anno = field.getAnnotation(XmlElementList.class);
                        String childName = anno.innerTag();
                        Element xmlChildElement;

                        if (anno.embed()) {
                            String elemName = anno.outerTag();
                            if (elemName.isEmpty()) elemName = field.getName().toLowerCase();
                            xmlChildElement = document.createElement(toXmlTag(elemName));
                            xmlElement.appendChild(xmlChildElement);
                        } else {
                            xmlChildElement = xmlElement;
                        }

                        // for each entry in the map
                        for (Map.Entry<Object, Object> o : ((Map<Object, Object>) field.get(object)).entrySet()) {
                            if (anno.stringify()) {

                                Element mapElem = document.createElement(toXmlTag(o.getKey().toString()));
                                xmlChildElement.appendChild(mapElem);
                                objectToXml(document, mapElem, null, o.getValue());

                            } else {

                                objectToXml(document, xmlChildElement, childName, o.getKey());
                                Node child = xmlChildElement.getLastChild();
                                if (child.getNodeType() == Node.ELEMENT_NODE)
                                    objectToXml(document, (Element) child, null, o.getValue());

                            }
                        }
                    }
                }
            }
        } else {
            Text text = document.createTextNode(object.toString());
            if (tagName == null || tagName.isBlank()) {

                if (parent == null) {
                    Element xmlElement = document.createElement(toXmlTag(clazz.getSimpleName().toLowerCase()));
                    document.appendChild(xmlElement);
                    xmlElement.appendChild(text);
                } else parent.appendChild(text);

            } else {

                Element xmlElement = document.createElement(toXmlTag(tagName));
                Objects.requireNonNullElse(parent, document).appendChild(xmlElement);
                xmlElement.appendChild(text);

            }
        }
    }

    /**
     * Sanitizes a string to be used as an XML tag name.
     * @param s  string to sanitize
     * @return   the sanitized string
     */
    private static String toXmlTag(String s) {
        s = s.replace(" ", "_");                    // replace spaces with underscores
        s = s.replaceAll("[^a-zA-Z0-9_\\-.]", "");  // replace illegal characters
        // add an underscore prefix if the tag starts with a reserved word or number
        if (s.matches("(([Xx][Mm][Ll])|([^a-zA-Z_])).+")) s = '_' + s;
        return s.isEmpty() ? "_" : s;  // return an underscore if the string becomes empty
    }

}
