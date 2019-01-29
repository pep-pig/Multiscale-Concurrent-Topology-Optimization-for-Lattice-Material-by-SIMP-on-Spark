package algorithms;


import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class Parameter {
    public  Parameter(){}
    public static void init(FiniteElementAnalysis fem) {
        //Create a DocumentBuilderFactory instance
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //Create a DocumentBuilder instance
        try {
            //create DocumentBuilder instance
            DocumentBuilder db = dbf.newDocumentBuilder();
            //load parameter.xml file to current project directory by parser method of  DocumentBuilder
            Document document = db.parse("src/algorithms/parameter.xml");
            //obtain all nodes of which tag name is parameter
            NodeList parameterList = document.getElementsByTagName("parameter");
            for (int i = 0; i < parameterList.getLength(); i++) {
                Node parameter = parameterList.item(i);
                NamedNodeMap attrs = parameter.getAttributes();
                String type = attrs.getNamedItem("name").getTextContent();
                if (type.equals("macroMeshParameter")) {
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2) {
                        //get one attribute of parameter node  by "item(index)" method
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if (key.equals("length")) {
                            fem.length = Double.parseDouble(value);
                        } else if (key.equals("height")) {
                            fem.height = Double.parseDouble(value);
                        } else if (key.equals("nelx")) {
                            fem.nelx = Integer.parseInt(value);
                        } else if (key.equals("nely")) {
                            fem.nely = Integer.parseInt(value);
                        }  else if (key.equals("boundaryCondition")) {
                            fem.boundaryConditions = Integer.parseInt(value);
                        } else if(key.equals("force")){
                            fem.force = Double.parseDouble(value);
                        }
                    }
                }
                if (type.equals("microMeshParameter")) {
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2) {
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if (key.equals("length")) {
                            fem.cellModel.length = Double.parseDouble(value);
                        } else if (key.equals("height")) {
                            fem.cellModel.height = Double.parseDouble(value);
                        } else if (key.equals("nelx")) {
                            fem.cellModel.nelx = Integer.parseInt(value);
                        } else if (key.equals("nely")) {
                            fem.cellModel.nely = Integer.parseInt(value);
                        }  else if (key.equals("mu")){
                            fem.cellModel.mu = Double.parseDouble(value);
                        } else if (key.equals("lambda")){
                            fem.cellModel.lambda = Double.parseDouble(value);
                        }
                    }

                }
                if (type.equals("macroSimpParameter")){
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2){
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if(key.equals("stopChangeValue")){
                            fem.macroStopChangeValue = Double.parseDouble(value);
                        }else if(key.equals("stopIteration")){
                            fem.macroStopIteration = Integer.parseInt(value);
                        }else if (key.equals("volf")) {
                            fem.volf = Double.parseDouble(value);
                        }else if (key.equals("penal")) {
                            fem.penal = Double.parseDouble(value);
                        }else if(key.equals("filterRadius")){
                            fem.filterRadius = Double.parseDouble(value)*fem.length/fem.nelx;
                        }
                    }
                }
                if (type.equals("microSimpParameter")){
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2){
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if(key.equals("stopChangeValue")){
                            fem.microStopChangeValue = Double.parseDouble(value);
                        }else if(key.equals("stopIteration")){
                            fem.microStopIteration = Integer.parseInt(value);
                        }else if (key.equals("penal")) {
                            fem.cellModel.penal = Double.parseDouble(value);
                        }else if(key.equals("filterRadius")){
                            fem.cellModel.filterRadius = Double.parseDouble(value)*fem.cellModel.length/fem.cellModel.nelx;
                        }
                    }
                }
                if (type.equals("macroOcParameter")){
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2){
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if(key.equals("move")){
                            fem.macroOcMove = Double.parseDouble(value);
                        }else if(key.equals("densityUpperLimit")){
                            fem.macroOcDensityUpperLimit = Double.parseDouble(value);
                        }
                        else if(key.equals("densityLowerLimit")){
                            fem.macroOcDensityLowerLimit = Double.parseDouble(value);
                        }
                    }
                }
                if (type.equals("microOcParameter")){
                    NodeList childList = parameter.getChildNodes();
                    for (int j = 1; j < childList.getLength(); j+=2){
                        Node node = childList.item(j);
                        String key = node.getNodeName();
                        String value = node.getFirstChild().getTextContent();
                        if(key.equals("move")){
                            fem.microOcMove = Double.parseDouble(value);
                        }else if(key.equals("densityUpperLimit")){
                            fem.microOcDensityUpperLimit = Double.parseDouble(value);
                        }
                        else if(key.equals("densityLowerLimit")){
                            fem.microOcDensityLowerLimit = Double.parseDouble(value);
                        }
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void show(){}
}
