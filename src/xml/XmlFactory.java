package xml;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Converts Objects into XML Strings as structured by their XML Annotations.
 * @author  Christian Burns
 */
public class XmlFactory {

    public static <T extends XmlSerializable> String toXmlString(T object) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            doc.appendChild(object.toXml(doc));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(new StringWriter());
            transformer.transform(domSource, streamResult);
            return streamResult.getWriter().toString();
        } catch (Exception e) {
            throw new XmlSerializationException(e);
        }
    }

    public static Document fromXmlString(String xml) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            return docBuilder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new XmlSerializationException(e);
        }
    }

    /**
     * Sanitizes a string to be used as an XML tag name.
     * @param s  string to sanitize
     * @return   the sanitized string
     */
    public static String toXmlTag(String s) {
        s = s.replace(" ", "_");                    // replace spaces with underscores
        s = s.replaceAll("[^a-zA-Z0-9_\\-.]", "");  // replace illegal characters
        // add an underscore prefix if the tag starts with a reserved word or number
        if (s.matches("(([Xx][Mm][Ll])|([^a-zA-Z_])).+")) s = '_' + s;
        return s.isEmpty() ? "_" : s;  // return an underscore if the string becomes empty
    }

}
