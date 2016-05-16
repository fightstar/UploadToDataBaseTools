
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.XMLGregorianCalendar;

import UtilsPack.Common;
import UtilsPack.UtilsSQL;
import UtilsPack.UtilsXLS;
import UtilsPack.WorkWithProperties;
import uploadTools.TadsObjectsBalance.tabalance.TaMessage;

public class UploadTadsToDatabaseForBalances extends XmlParserForBalances {
	public static Properties props = WorkWithProperties.LoadProps(".\\loadbalances.properties");
	static String pathToTads;
	//public List<Accountable> listOfTads;
	public List<TaMessage> listOfTads;
	private static  List<String> listOfInputStrings = new ArrayList<String>();
	
	public static void main(String [] args) {
		
		UploadTadsToDatabaseForBalances instance = new UploadTadsToDatabaseForBalances();
		
		XmlParserForBalances parser = new XmlParserForBalances();
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
	    		case "ACCOUNTABLE_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getAccountableId());break;
	    		case "ACCOUNTABLE_VERSION" : supplementInsertStatement(insertQuery, String.valueOf(tamessage.getBalance().getVersion()));break;
	    		case "ACCOUNTABLE_INT_VERSION" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInternalVersion());break;
	    		case "ACCOUNTABLE_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getAccountableType());break;
	    		case "ACCOUNTABLE_SOURCE_SYSTEM" : supplementInsertStatement(insertQuery, tamessage.getBalance().getSourceSystem());break;
	    		case "ACCOUNT_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getAccount().getAccountId());break;
	    		case "VALUE_DATE" : supplementInsertStatement(insertQuery, convertXMLDateToString(tamessage.getBalance().getFeature().get(0).getCashflow().getValueDate()));break;
	    		case "AMOUNT" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getAmount().toString());break;
	    		case "CURRENCY" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getCurrency());break;
	    		case "BOOKING_PARTY_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getLegalEntity().getId());break;
	    		case "BOOKING_PARTY_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getLegalEntity().getType());break;
	    		case "BOOKING_PARTY_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getLegalEntity().getSourceSystem());break;
	    		case "BOOKING_PARTY_ORG_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getOrganizationUnitId());break;
	    		case "BOOKING_PARTY_ORG_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getOrganizationUnitType());break;
	    		case "BOOKING_PARTY_ORG_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getOrganizationUnitSourceSystem());break;
	    		case "OWNER_PROFIT_CENTER" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getProfitCenter());break;
	    		case "INSTRUMENT_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInstrument().getInstrumentId());break;
	    		case "INSTRUMENT_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInstrument().getInstrumentType());break;
	    		case "INSTRUMENT_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInstrument().getInstrumentSourceName());break;
	    		case "CPTY_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getId());break;
	    		case "CPTY_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getType());break;
	    		case "CPTY_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getSourceSystem());break;
	    		case "CPTY_ORG_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getOrganizationUnitId());break;
	    		case "CPTY_ORG_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getOrganizationUnitType());break;
	    		case "CPTY_ORG_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getOrganizationUnitSourceSystem());break;
	    		case "CPTY_ROLE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getCounterParty().getRole());break;
	    		case "FO_PRODUCT_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getFrontOfficeProductType());break;
	    		case "FO_PRODUCT_SUBTYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getFrontOfficeProductSubtype());break;
	    		case "PRODUCT_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getProductId());break;
	    		case "PRODUCT_NAME" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getProductName());break;
	    		case "PRODUCT_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getProductSource());break;
	    		case "ACCOUNTING_SCHEMA_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getProduct().getAccountingSchemaId());break;
	    		case "GAAP" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getGaap());break;
	    		case "FEATURE_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getType());break;
	    		case "FEATURE_DIRECTION" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getDirection());break;
	    		case "CASHFLOW_NAME" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getCashFlowName());break;
	    		case "CASHFLOW_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getCashFlowType());break;
	    		case "BUSINESS_EVENT_TIMESTAMP" : supplementInsertStatement(insertQuery, convertXMLDateToString(tamessage.getHeader().getBusinessEventTimestamp()));break;
	    		case "BUSINESS_OBJECT_OWNER" : supplementInsertStatement(insertQuery, tamessage.getHeader().getBusinessObjectOwner());break;
	    		case "TRADE_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getTradeIdentity().getId());break;
	    		case "TRADE_ID_SOURCE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getTradeIdentity().getSourceSystem());break;
	    		case "LEG_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getFeature().get(0).getCashflow().getLegId()); break;
	    		case "ORIGN_SOURCE_SYSTEM" : supplementInsertStatement(insertQuery, tamessage.getBalance().getBook().getLegalEntity().getSubsystem());break;
	    		case "DBP_TYPE_CODE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getDbpTypCd());break;
	    		case "SRC_ACC_CODE_TYPE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getSrcAccCodeType());break;
	    		case "SEG_CODE" : supplementInsertStatement(insertQuery, tamessage.getBalance().getSegCode());break;
	    		case "INVST_DB_ETY_ID" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInvstDbEtyId());break;
	    		case "INVST_DB_ETY_ID_TYP" : supplementInsertStatement(insertQuery, tamessage.getBalance().getInvstDbEtyIdTyp());break;
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
	
	private static String convertXMLDateToString(XMLGregorianCalendar xmldate) {
		String convertedString;
		Date javaDate  = xmldate.toGregorianCalendar().getTime();
		SimpleDateFormat sdft  = new SimpleDateFormat("dd.MM.yy");
		convertedString = sdft.format(javaDate);
		return convertedString;
	}
	    
	    private static String supplementInsertStatement(StringBuilder insert, String value) {
			
		    insert.append(value);
		    insert.append("','");
		    
		    return insert.toString();
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

}
