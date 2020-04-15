package com.dromedarydrones.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XmlSerializable {
    Element toXml(Document doc);
}
