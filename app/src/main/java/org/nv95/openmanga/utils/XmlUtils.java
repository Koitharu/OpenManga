package org.nv95.openmanga.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by unravel22 on 26.02.17.
 */

public class XmlUtils {
    
    public static Document convertNodesFromXml(String xml) throws Exception {
        
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(is);
    }
        
    public static String getElementValue(Element el, String key) {
        return el.getElementsByTagName(key).item(0).getFirstChild().getNodeValue();
    }
}
