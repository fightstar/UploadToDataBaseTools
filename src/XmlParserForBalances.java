

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uploadTools.TadsObjectsBalance.tabalance.TaMessage;
import UtilsPack.Common;

public class XmlParserForBalances {
	
	public TaMessage tamessage;
	/**
	 * Parse string to object
	 * @param stringToParse
	 * @return taMessage object
	 */
	public TaMessage xmlParsingInObject(String stringToParse){
		
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
		StringReader reader  = new StringReader(stringToParse);
		
		
		try {
			tamessage = (TaMessage) jaxbUnmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return tamessage;
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
		return new String(encoded, encoding);
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
	
	/**
	 * Get parsed xml TaMessage objects from files on local
	 * 
	 * @param localPath
	 *            - path to temp folder on local
	 * @return list of TaMessage objects
	 */
	protected List<TaMessage> getTaMessages(String localPath) {
		List<String> filesToTest = getXmLFilesFromLocalDir(localPath);
		List<TaMessage> listOfObjects = new ArrayList<>();
		for (String file : filesToTest) {
	
			String xmlString = getXMLStringFromDownloadedFile(file);
			List<String> taMessages = new ArrayList<>();
			Pattern pattern = Pattern.compile("(<ta_message.+?>)(.+?)(<\\/ta_message>)", Pattern.DOTALL); 
		    Matcher matcher = pattern.matcher(xmlString.toString());
		    
			while(matcher.find()) {
				StringBuilder tamessage = new StringBuilder();
				taMessages.add(
						tamessage
						.append(matcher.group(1))
						.append(matcher.group(2))
						.append(matcher.group(3))
						.toString()
						);
			}
			
			for (String taMessage : taMessages){
				listOfObjects.add(xmlParsingInObject(taMessage));
			}
		
		}
	
		return listOfObjects;
	
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
		return stringFromRawXMLFile;
	}

}
