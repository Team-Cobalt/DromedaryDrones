package xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import xml.exceptions.XmlSerializationException;
import xml.annotations.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

//
//    public static Object fromXmlString(String xml, Class<?> clazz) {
//        try {
//
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            ByteArrayInputStream input = new ByteArrayInputStream(
//                    xml.getBytes(StandardCharsets.UTF_8));
//            Document document = builder.parse(input);
//            Element root = document.getDocumentElement();
//
//            return xmlToObject(document, root, clazz);
//        } catch (Exception e) {
//            throw new XmlSerializationException(e);
//        }
//    }
//
//    private static Object xmlToObject(Document document, Element root, Class<?> clazz) throws Exception {
//        if (checkIfXmlSerializable(clazz)) {
//
//            Object object = clazz.getConstructor().newInstance();
//            HashMap<String, ArrayList<Element>> children = new HashMap<>();
//            NodeList nodes = root.getChildNodes();
//
//            // get all child elements
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Node n = nodes.item(i);
//                if (n.getNodeType() == Node.ELEMENT_NODE) {
//                    String name = ((Element)n).getTagName();
//                    children.putIfAbsent(name, new ArrayList<>());
//                    children.get(name).add(((Element) n));
//                }
//            }
//
//            for (Field field : clazz.getDeclaredFields()) {
//                field.setAccessible(true);
//                if (field.isAnnotationPresent(XmlAttribute.class)) {
//                    XmlAttribute xmlA = field.getAnnotation(XmlAttribute.class);
//                    String tag = xmlA.name().isEmpty() ? field.getName().toLowerCase() : xmlA.name();
//                    String attrValue = root.getAttribute(toXmlTag(tag));
//                    //field.set(object, attrValue);
//                } else if (field.isAnnotationPresent(XmlElement.class)) {
//
//                } else if (field.isAnnotationPresent(XmlElementList.class)) {
//
//                }
//            }
//        } else {
//            if (clazz.isPrimitive()) {
//                if (clazz.isAssignableFrom(boolean.class)) {
//                    return Boolean.parseBoolean(root.getTextContent());
//                } else if (clazz.isAssignableFrom(char.class)) {
//                    return root.getTextContent().charAt(0);
//                } else if (clazz.isAssignableFrom(byte.class)) {
//                    return Byte.parseByte(root.getTextContent());
//                } else if (clazz.isAssignableFrom(short.class)) {
//                    return Short.parseShort(root.getTextContent());
//                } else if (clazz.isAssignableFrom(int.class)) {
//                    return Integer.parseInt(root.getTextContent());
//                } else if (clazz.isAssignableFrom(long.class)) {
//                    return Long.parseLong(root.getTextContent());
//                } else if (clazz.isAssignableFrom(float.class)) {
//                    return Float.parseFloat(root.getTextContent());
//                } else if (clazz.isAssignableFrom(double.class)) {
//                    return Double.parseDouble(root.getTextContent());
//                }
//            } else if (clazz.isAssignableFrom(Boolean.class)) {
//                return Boolean.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Character.class)) {
//                return root.getTextContent().charAt(0);
//            } else if (clazz.isAssignableFrom(Byte.class)) {
//                return Byte.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Short.class)) {
//                return Short.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Integer.class)) {
//                return Integer.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Long.class)) {
//                return Long.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Float.class)) {
//                return Float.valueOf(root.getTextContent());
//            } else if (clazz.isAssignableFrom(Double.class)) {
//                return Double.valueOf(root.getTextContent());
//            }
//        }
//    }
//

    /**
     * Checks if the Class has the XmlSerializable Annotation.
     * @param clazz  class to check
     * @return  {@code true} if the Annotation was found
     */
    private static boolean checkIfXmlSerializable(Class<?> clazz) {
        return clazz.isAnnotationPresent(XmlSerializable.class);
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
            if (checkIfXmlSerializable(clazz)) {
                tagName = clazz.getAnnotation(XmlSerializable.class).name();
                if (tagName.isEmpty()) tagName = clazz.getSimpleName().toLowerCase();
            }
        }

        if (checkIfXmlSerializable(clazz)) {

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
                        for (Object o : (Collection<Object>) field.get(object)) {
                            if (anno.stringify()) {
                                objectToXml(document, xmlChildElement, toXmlTag(o.toString()), o);
                            } else {
                                objectToXml(document, xmlChildElement, childName, o);
                            }
                        }

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
                            if (anno.stringify()) {
                                objectToXml(document, xmlChildElement, toXmlTag(o.toString()), o);
                            } else {
                                objectToXml(document, xmlChildElement, childName, o);
                            }
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
