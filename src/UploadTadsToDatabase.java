

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import uploadTools.TadsObjectsMovement.TaMessage;
import UtilsPack.Common;
import UtilsPack.UtilsSQL;
import UtilsPack.UtilsXLS;
import UtilsPack.WorkWithProperties;
/**
 * This loader can be used only for TAMovement entities.
 * @author shevvla
 *
 */
public class UploadTadsToDatabase extends XmlParser {
	public static Properties props = WorkWithProperties
			.LoadProps(".\\load.properties");
	static String pathToTads;
	public List<TaMessage> listOfTads;
	private static  List<String> listOfInputStrings = new ArrayList<String>();
	
	public static void main(String [] args) {
		UploadTadsToDatabase instance = new UploadTadsToDatabase();
		XmlParser parser = new XmlParser();
	
	String[] fileNametoSplit = parser.getXmLFilesFromLocalDir(props.getProperty("PATH_TO_XML")).get(0).split("\\\\");
	int i =  fileNametoSplit.length;
	String fileName = fileNametoSplit[i-1];
	System.out.println("start programm");
	System.out.println(props.getProperty("DB_CONNECTION"));
	//read xml, parse xml and create objects
	instance.listOfTads = parser.getTaMessages(props.getProperty("PATH_TO_XML"));
	//read xls file
    listOfInputStrings = instance.getListOfXLSdata(props.getProperty("PATH_TO_XLSX"));
    System.out.println("Excel input file has "+listOfInputStrings.size()+ " records");
	//create inserts
    //get db connection
    UtilsSQL.getDBConnection(props);
    //verify if created table exists
    if(UtilsSQL.isTableExists(props.getProperty("TABLE_NAME"))==true){
    	System.out.println("Table with name " + props.getProperty("TABLE_NAME") + " is exist" );
    } else {
    	createTable(props.getProperty("TABLE_NAME"));
    	System.out.println("Table " + props.getProperty("TABLE_NAME")+ " is created!");
    }
    int count = 1;
    for(TaMessage tamessage : instance.listOfTads){
    	StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT INTO ");
		insertQuery.append(props.getProperty("TABLE_NAME"));
		
		insertQuery.append(" ");
		insertQuery.append("( ");
		
		StringBuilder columnsToInsert = new StringBuilder();
		columnsToInsert.append("ID");
		columnsToInsert.append(",");
		
		for(String columnName : listOfInputStrings){
			columnsToInsert.append(columnName);
			columnsToInsert.append(",");
		}
		
		int columnLength =  columnsToInsert.length();
		String finalColumnsString = columnsToInsert.substring(0, columnLength-1).toString();
		insertQuery.append(finalColumnsString);
		insertQuery.append(")");
		insertQuery.append(" VALUES ('" + fileName+"_"+count);
		insertQuery.append("','");
		
    	for(String attributeFromXLS : listOfInputStrings){
    		switch(attributeFromXLS) {
    		case "ACCOUNTABLE_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getAccountableId());break;
    		case "ACCOUNTABLE_VERSION" : supplementInsertStatement(insertQuery, String.valueOf(tamessage.getMovement().getVersion()));break;
    		case "ACCOUNTABLE_INT_VERSION" : supplementInsertStatement(insertQuery, tamessage.getMovement().getInternalVersion());break;
    		case "ACCOUNTABLE_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getAccountableType());break;
    		case "ACCOUNTABLE_SOURCE_SYSTEM" : supplementInsertStatement(insertQuery, tamessage.getMovement().getSourceSystem());break;
    		case "ACCOUNT_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getAccount().getAccountId());break;
    		case "VALUE_DATE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getValueDate().toString());break;
    		case "AMOUNT" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getAmount().toString());break;
    		case "CURRENCY" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getCurrency());break;
    		case "BOOKING_PARTY_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getLegalEntity().getId());break;
    		case "BOOKING_PARTY_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getLegalEntity().getType());break;
    		case "BOOKING_PARTY_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getLegalEntity().getSourceSystem());break;
    		case "BOOKING_PARTY_ORG_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getOrganizationUnitId());break;
    		case "BOOKING_PARTY_ORG_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getOrganizationUnitType());break;
    		case "BOOKING_PARTY_ORG_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getOrganizationUnitSourceSystem());break;
    		case "INSTRUMENT_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getInstrument().getInstrumentId());break;
    		case "INSTRUMENT_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getInstrument().getInstrumentType());break;
    		case "INSTRUMENT_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getInstrument().getInstrumentSourceName());break;
    		case "CPTY_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getId());break;
    		case "CPTY_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getType());break;
    		case "CPTY_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getSourceSystem());break;
    		case "CPTY_ORG_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getOrganizationUnitId());break;
    		case "CPTY_ORG_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getOrganizationUnitType());break;
    		case "CPTY_ORG_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getCounterParty().getOrganizationUnitSourceSystem());break;
    		case "FO_PRODUCT_TYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getFrontOfficeProductType());break;
    		case "FO_PRODUCT_SUBTYPE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getFrontOfficeProductSubtype());break;
    		case "PRODUCT_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getProductId());break;
    		case "PRODUCT_NAME" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getProductName());break;
    		case "PRODUCT_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getProductSource());break;
    		case "ACCOUNTING_SCHEMA_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getProduct().getAccountingSchemaId());break;
    		case "GAAP" : supplementInsertStatement(insertQuery, tamessage.getMovement().getBook().getGaap());break;
    		case "CR_DR" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getCrDr());break;
    		case "BUSINESS_EVENT_TIMESTAMP" : supplementInsertStatement(insertQuery, tamessage.getHeader().getBusinessEventTimestamp().toString());break;
    		case "BUSINESS_OBJECT_OWNER" : supplementInsertStatement(insertQuery, tamessage.getHeader().getBusinessObjectOwner());break;
    		case "TRADE_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getTradeIdentity().getId());break;
    		case "TRADE_ID_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getTradeIdentity().getSourceSystem());break;
    		case "LEG_ID" : supplementInsertStatement(insertQuery, tamessage.getMovement().getFeature().get(0).getCashflow().getLegId());break;
    		case "ORIGN_SOURCE_SYSTEM" : supplementInsertStatement(insertQuery, tamessage.getMovement().getOriginationSourceSystem());break;
    		case "DBP_TYPE_CODE" : supplementInsertStatement(insertQuery, tamessage.getMovement().getDbpTypCd());break;
    		}
    	}
    	count++;
    	int lengthOfString = insertQuery.length();
    	String finalInsertQuery =	insertQuery.substring(0, lengthOfString-2).toString();
    	finalInsertQuery = finalInsertQuery + ")";
    	// execute inserts in db
    	finalInsertQuery = finalInsertQuery.replaceAll("'null'", "''"); //substitution "null" to NULL object
    	instance.executeInsert(finalInsertQuery);
    	System.out.println(finalInsertQuery);
    	
    }
    UtilsSQL.closeConnection();	
    System.out.println("end programm");
}

	private static String supplementInsertStatement(StringBuilder insert, String value) {
		
	    insert.append(value);
	    insert.append("','");
	    
	    return insert.toString();
	}
	
	private static void createTable(String tableName) {
		StringBuffer query  = new StringBuffer();
		query.append("CREATE TABLE ");
		query.append(tableName + " ");
		query.append("(ID VARCHAR2(500 BYTE),");
		for(String s :listOfInputStrings ){
			query.append(s + " VARCHAR2(150 BYTE),");
		}
		int lengthOfString = query.length();
		String finalCreateQuery =	query.substring(0, lengthOfString-1).toString();
		finalCreateQuery = finalCreateQuery + ")";
		System.out.println(UtilsSQL.executeSQLStatement(finalCreateQuery));
	}



	private String executeInsert(String insertQuery) {
		String result = "";
		result += UtilsSQL.executeSQLStatement(insertQuery);
		return result;
	}

	private List<List<String>> getXLSdata(String pathToFile) {
		List<List<String>> lists = null;
		try {
			lists = UtilsXLS.readXSLX(pathToFile);
		} catch (IOException e) {
			Common.print("Can't read xls file");
			e.printStackTrace();
		}
		return lists;
	}

	private List<String> getListOfXLSdata(String pathToFile) {
		List<String> list  = new ArrayList<String>();
		for(List<String> l : getXLSdata(pathToFile)){
			list.add(l.get(0));
		}
		return list;
	}
}
