package pl.jakubmiodunka.utilities.xml;

import pl.jakubmiodunka.utilities.xml.exceptions.XmlParsingException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Set of static methods useful during XML files parsing.
 *
 * @author Jakub Miodunka
 * */
public class XmlUtilities {
    /**
     * Extracts the root XML node from given file meanwhile validating its name.
     *
     * @param  xmlFile             File, from which the root element should be extracted.
     * @param  rootNodeName        Expected name of the root element.
     * @return                     Root element node extracted from the given file.
     * @throws XmlParsingException If extraction fail.
     * */
    public static Element getRootNode(Path xmlFile, String rootNodeName) {
        try {
            // Parsing given XML file
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document xmlDocument = documentBuilder.parse(xmlFile.toFile());

            // Normalising content of given XML file
            xmlDocument.normalize();

            // Extracting the root node
            Element rootNode = xmlDocument.getDocumentElement();

            // Checking validity of root node name
            if (!(rootNode.getTagName().equals(rootNodeName)))
                throw new XmlParsingException(String.format("Root node not named as '%s'", rootNodeName));

            // Returning root node after successful validation
            return rootNode;

        } catch (ParserConfigurationException | IOException | SAXException | XmlParsingException exception) {
            // Wrapping caught exception
            String errorMessage = String.format("Failed to extract root node from '%s'", xmlFile.toString());
            throw new XmlParsingException(errorMessage, exception);
        }
    }

    /**
     * Converts the NodeList objects to lists of Node objects.
     * Used as auxiliary method during XML parsing.
     *
     * @param nodeList Object to convert.
     * @return         Corresponding list of Node objects.
     * */
    private static List<Node> toListOfNodes(NodeList nodeList) {
        // Creating a list, where conversion result will be stored
        List<Node> result = new ArrayList<>();

        // Conversion of given NodeList to the list of Node objects
        int length  = nodeList.getLength(); // Extracting the length of NodeList to sepparate variable.

        for (int index = 0; index < length; index++) {  // Iterating through NodeList
            result.add(nodeList.item(index));           // Adding each node from NodeList to created list
        }

        // Returning the result of performed conversion
        return result;
    }

    /**
     * Extracts the node with particular name from node given in argument.
     * Additionally, some simple validation is utilised to prevent data ambiguity.
     *
     * @param  nodeName            Name of searched node.
     * @param  sourceNode          XML node, that serves as a source for searching process.
     * @return                     Extracted node.
     * @throws XmlParsingException If node, with name specified in argument was not found or if many nodes,
     *                             with this name were found.
     *                             Also, if found node is not an element node.
     * */
    public static Element getNode(Element sourceNode, String nodeName) {
        // Extracting all nodes with provided name from root node
        List<Node> matchingNodes = toListOfNodes(sourceNode.getElementsByTagName(nodeName));

        // Filtering only those nodes, that are direct children of provided root node
        matchingNodes = matchingNodes.stream()
                .filter(node -> node.getParentNode().isEqualNode(sourceNode))
                .toList();

        // Checking if any node was found
        if (matchingNodes.isEmpty())
            throw new XmlParsingException(String.format("'%s' node not found", nodeName));

        // Checking if only one node was found to prevent ambiguity
        if (matchingNodes.size() > 1)
            throw new XmlParsingException(String.format("More than one '%s' node found", nodeName));

        // Checking if found node is an element node
        if (matchingNodes.get(0).getNodeType() != Node.ELEMENT_NODE)
            throw new XmlParsingException(String.format("Found '%s' node is not an element node", nodeName));

        // Returning the node after successful validation
        return (Element) matchingNodes.get(0);
    }

    /**
     * Extract the text content from XML node, which name is provided as argument.
     * Text content of found XML node will be returned in "stripped" for (String.strip)
     *
     * @param  nodeName            Name of XML node, from which text should be extracted.
     * @param  sourceNode          XML node, that serves as a source for searching process.
     * @return                     Text content of found XML node in "stripped" form (String.strip() utilised)
     * @throws XmlParsingException If node, with name specified in argument was not found or if many nodes,
     *                             with this name were found.
     *                             Also, if the structure of found node content is invalid.
     * */
    public static String getContentOfNode(Element sourceNode, String nodeName) {
        // Searching for element node in given root node
        Element foundNode = getNode(sourceNode, nodeName);

        // Checking if found node does not contain any other nodes besides of text
        List<Node> childNodes = toListOfNodes(foundNode.getChildNodes());

        if (!(childNodes.stream().allMatch(node -> node.getNodeType() == Node.TEXT_NODE)))
            // Throwing an exception when content of found node have invalid structure
            throw new XmlParsingException(String.format(
                    "Not all child nodes of '%s' node are text nodes", nodeName));

        // Returning node content in form of String
        return  foundNode.getTextContent().strip();
    }
}
