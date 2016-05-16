

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import UtilsPack.Common;
import UtilsPack.Report;
import uploadTools.TadsObjectsMovement.TaMessage;

public class XmlParser {
 
	public TaMessage taMessage;
	
	/**
	 * Parse file located on "pathToFile" in TaMessage object
	 * 
	 * @param pathToFile
	 * @param reportFile
	 * @return TaMessage object
	 */
	public TaMessage xmlParsingInObject(String pathToFile) {
		File xmlFile = new File(pathToFile);
		JAXBContext jaxbContext = null;
		try {
			jaxbContext = JAXBContext.newInstance(TaMessage.class);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		Unmarshaller jaxbUnmarshaller = null;
		try {
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		try {
			taMessage = (TaMessage) jaxbUnmarshaller.unmarshal(xmlFile);
			Common.print("Xml is parsed successfully from file " + pathToFile
							+ ", object is created");
		} catch (JAXBException e) {
			e.printStackTrace();
			Common.print(pathToFile + " xml isn't parsed");
		} finally {
			xmlFile.delete();
		}
		return taMessage;
	}
	
	/**
	 * Read file to string
	 * 
	 * @param path
	 * @param encoding
	 * @return string
	 * @throws IOException
	 */
	public String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
//		File file = new File(path);
//		InputStream in =  new BufferedInputStream(new FileInputStream(file));
//		byte[] encoded = IOUtils.toByteArray(in);
//		in.close();
		return new String(encoded, encoding);
	}
	
	/**
	 * Create .xml file from string
	 * 
	 * @param string
	 * @param fileName
	 * @return file
	 */
	public File createXmlFileFromString(String string, String fileName) {
		File file = new File(fileName);
		try {
			FileUtils.writeStringToFile(file, string);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	/**
	 * Clean incoming string and return valid string for parsing
	 * 
	 * @param tadsPathForTest
	 * @return
	 */
	public String getXMLStringFromDownloadedFile(String tadsPathForTest) {
	
		String stringFromRawXMLFile = null;
		try {
			stringFromRawXMLFile = readFile(tadsPathForTest,
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringFromRawXMLFile.substring(stringFromRawXMLFile
				.indexOf("<?xml"));
	}

	/**
	 * Get parsed xml TaMessage objects from files on local
	 * 
	 * @param localPath
	 *            - path to temp folder on local
	 * @return list of TaMessage objects
	 */
	protected List<TaMessage> getTaMessages(String localPath) {
		List<String> filesToTest = getXmLFilesFromLocalDir(localPath);
		String findString = "<ta_message ";
		int count = 0;
	
		List<TaMessage> listOfObjects = new ArrayList<>();
		for (String file : filesToTest) {
	
			String xmlString = getXMLStringFromDownloadedFile(file);
			count = StringUtils.countMatches(xmlString, findString);
			List<String> objectsToParse = new ArrayList<String>();
			if (count == 1) {
				Common.print("count of tads = 1");
				createXmlFileFromString(xmlString, file);
				listOfObjects.add(xmlParsingInObject(file));
			} else {
				Common.print("count of tads = " + count);
				int startIndex = xmlString.indexOf(findString);
				xmlString = xmlString.substring(startIndex);
				String[] splittedXml = xmlString.split(findString);
				Common.print("Size of splittedXml: " + splittedXml.length); //-1
				for (String oneStringObject : splittedXml) {
					if (oneStringObject.length() > 0) {
						String closingTab = "</ta_message>";
						oneStringObject = oneStringObject.split(closingTab)[0];
						String objectToParse = findString + oneStringObject
								+ closingTab;
						objectsToParse.add(objectToParse);
					}
				}
				Common.print("Number of strings to parse: "
						+ objectsToParse.size());
				for (String stringObject : objectsToParse) {
					String tempFileName =  file+ "_"+ "temp";
					createXmlFileFromString(stringObject, tempFileName);
					listOfObjects.add(xmlParsingInObject(tempFileName));
				}
			}
		}
	
		return listOfObjects;
	
	}
	/**
	 * Get .xml files from folder used regex mask
	 * 
	 * @param folder
	 * @return list of files
	 */
	protected List<String> getXmLFilesFromLocalDir(String folder) {
		String regex_mask = ".*.xml";
		return Common.listFilesForFolder(new File(folder), regex_mask);
	}
}
