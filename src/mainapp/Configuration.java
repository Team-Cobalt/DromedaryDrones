package mainapp;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.ArrayList;

public class Configuration {
	private Simulation currentSim; //the current simulation config.
	private ArrayList<Simulation> simulations; //all saved simulation configs.
	
	public Configuration() {
		currentSim = null;
		simulations = new ArrayList<>();
	}
	
	public boolean initialize(File loadedFile) {
		//TODO: replace with loading in an actual file and parsing it
		Simulation newSim = new Simulation("Grove City College");
		
		//ADD DEFAULT HERE??
		
		currentSim = newSim;
		simulations.add(newSim);
		return true;
	}
	
	public boolean save(File saveFile) {
        try {
            // create document
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            // create root element
            Element root = document.createElement("simulations");
            Attr lastRun = document.createAttribute("current");
            lastRun.setValue(currentSim != null ? currentSim.getName() : "");
            root.setAttributeNode(lastRun);
            document.appendChild(root);

            // create child elements
            for (Simulation sim : simulations) {
                root.appendChild(sim.toXml(document));
            }

            // save xml to file
            // transform the DOM Object into an XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(saveFile);
            transformer.transform(domSource, streamResult);
            return true;
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
            return false;
        }
    }
}
