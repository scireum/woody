/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.collmex;





import sirius.biz.codelists.CodeListEntry;
import sirius.biz.codelists.CodeLists;
import sirius.biz.tenants.UserAccount;
import sirius.db.mixing.OMA;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Part;
import sirius.kernel.di.std.Register;
import sirius.kernel.health.Exceptions;

import sirius.kernel.xml.Outcall;

import sirius.web.security.UserContext;
import sirius.web.security.UserInfo;
import woody.core.employees.Employee;
import woody.xrm.Company;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Register(name = "CollmexInformationService", classes = {CollmexInformationService.class})
public class CollmexInformationServiceBean implements CollmexInformationService {

	@Part
	private SafeService ss;

	@Part
	private static OMA oma;

	@Part
	private static CodeLists cls;

	// model of the collmex-customer-data
	private String[] CMXKND = { "1  Satzart", "2  Kundennummer", "3  Firma Nr", "4  Anrede",
			"5  Titel", "6  Vorname", "7  Name", "8  Firma", "9  Abteilung",
			"10  Straße", "11  PLZ", "12  Ort", "13  Bemerkung", "14  Inaktiv",
			"15  Land", "16  Telefon", "17  Telefax", "18  E-Mail", "19  Kontonr",
			"20  Blz", "21  Iban", "22  Bic", "23  Bankname", "30  Ausgabemedium", "42  Ausgabesprache"};

	private static final int BLOCKLENGTH = 16;

	@Override
	public HashMap<String, String> getCollmexCustomerData(
			String customerNumber, List<String> accessList) {
		HashMap<String, String> collmexMap = new HashMap<String, String>();
		try {
			if (accessList == null) {
				return collmexMap;
			}
			// parameterfield like the collmex-API
			String para =  "CUSTOMER_GET;" + customerNumber + ";1" ;
			List<String> parameterList = new ArrayList<String>();
			parameterList.add(para);
			// call collmex
			List<String> result = callCollmex(parameterList , accessList);
			if (result.size() == 1) {
				// store the data in a hashmap
				String[] parameters = result.get(0).split(";");
				for (int i = 0; i < CMXKND.length; i++) {
					String s = CMXKND[i];
					int k = Integer.parseInt(s.substring(0, 2).trim());
					String key = s.substring(2).trim();
					String value = parameters[k - 1];
					collmexMap.put(key, value);
				}
			}
		} catch (Exception e) {
			Exceptions.handle(e);
		}
		return collmexMap;

	}

	/**
	 * send a request to collmex and receive the customer-data in a list
	 */

	private List<String> callCollmex(List<String> parameterList, List<String> access)
			throws Exception {

		// Build the Collmex-URL and add the parameters
		URL collmexUrl = new URL("https://www.collmex.de/cgi-bin/cgi.exe?"
								 + access.get(0) + ",0,data_exchange");
		Outcall outcall = new Outcall(collmexUrl);

		// add the header-data
		outcall.setRequestProperty("Content-Type", "text/csv");

		// prepare the request-data
		String request = "LOGIN;" + access.get(1) + ";" + access.get(2)
				+ "\n";
		for (String p:parameterList) {
			request = request + p + "\n";
		}
		// send the request-data to Collmex
		byte[] outByte = request.getBytes();
		OutputStream outputStream = outcall.getOutput();
		outputStream.write(outByte);
		outputStream.flush();

		// receive the response from Collmex
		InputStream inputStream = outcall.getInput();

		// read the Collmex-Data in the list 'collmexData'
		List<String> collmexData = new ArrayList<String>();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "ISO-8859-1"));
		boolean success = false;
		List<String> messages = new ArrayList<String>();
		String line = bufferedReader.readLine();
		while(line != null){
			if (line.startsWith("MESSAGE")) {
				messages.add(line);
			} else {
				collmexData.add(line);
				System.out.println("callCollmex:"+line);
				success = true;
			}
			line = bufferedReader.readLine();
		}

		if(!success) {
			String string = "";
			for (String s : messages) {
				string = string +  s + "  ";
			}
			throw Exceptions.createHandled().withNLSKey("CollmexInformationServiceBean.CollmexApiError")
							.set("message", string).handle();
		}
//		System.out.println("Version 4:");
//		for (String l: collmexData) {
//			System.out.println(l);
//		}

		return collmexData;
	}


	@Override
	public List<String> checkCollmexAgainstCRM(Company companyCRM,
			HashMap<String, String> collmexMap) {
//		List<String> messages = new ArrayList<String>();
//
//		checkItem(messages, collmexMap, "Kundennummer",
//				companyCRM.getCustomerNr());
//		checkItem(messages, collmexMap, "Firma", companyCRM.getName());
//		checkItem(messages, collmexMap, "Abteilung", companyCRM.getName2());
//		checkItem(messages, collmexMap, "Straße", companyCRM.getAddress().getStreet());
//		checkItem(messages, collmexMap, "PLZ", companyCRM.getAddress().getZip());
//		checkItem(messages, collmexMap, "Ort", companyCRM.getAddress().getCity());
//        String invoiceMedium = companyCRM.getCompanyAccountingData().getInvoiceMedium();
//        String crmValue = "";
//        if(Strings.isEmpty(invoiceMedium))  {
//            crmValue =  "PRINT";
//        }  else {
//            crmValue =  invoiceMedium;
//        }
//        checkItem(messages, collmexMap, "Ausgabemedium", adaptToCollmex(crmValue));
//		if(invoiceMedium.equals("MAIL")) {
//			checkItem(messages, collmexMap, "E-Mail", companyCRM.getCompanyAccountingData().getInvoiceMailAdr());
//		}
//		return messages;
        return null;
	}

    private String adaptToCollmex(String name) {
		return name;
		// ToDo: alte Lösung wierder einbauen
//        return NLS.get(InvoiceMediumType.class.getName() + ".Collmex" + name);
    }

    private void checkItem(List<String> messages,
			HashMap<String, String> collmexMap, String name, String crmValue) {
		if (collmexMap.containsKey(name)) {
			String collmexValue = collmexMap.get(name);
			if (Strings.isEmpty(collmexValue))  {        //(collmexValue == null) {
				if (Strings.isEmpty(crmValue)) {
					// do nothing
				} else {
					messages.add("Parameter: " + name
							+ ", die Werte sind verschieden, Collmex: "
							+ collmexValue + ", CRM: " + crmValue);
				}
			} else {
				if (!collmexValue.equals(crmValue)) {
					messages.add("Parameter: " + name
							+ ", die Werte sind verschieden, Collmex: "
							+ collmexValue + ", CRM: " + crmValue + ".");
				}
			}

		} else {
			messages.add("Der Parameter " + name
					+ " fehlt in den Collmexdaten.");
		}
	}

	@Override
	public Company updateCompanyWithCollmex(Company companyCRM,
			HashMap<String, String> colmexMap, boolean save, boolean newCompany) {
//         if(newCompany) {
//             String companyNr = colmexMap.get("Kundennummer");
//             if(Strings.isEmpty(companyNr)) {
////                 throw new BusinessException("Die Company-Nr. fehlt in den Collmex-Daten");
//				 throw Exceptions.createHandled().handle() ;
//            }
//             List<Company> companyList = oma.select(Company.class).eq(Company.CUSTOMERNR, companyNr).queryList();
//             if (companyList.size() > 0) {
//                 Company companyTest =companyList.get(0);
////                 throw new BusinessException("Die Kundennummer " + companyNr + " ist im CRM bereits für "+ companyTest.toString()+" vergeben. Die Funktion 'Neu aus Collmex' ist damit nicht nutzbar");
//				 throw Exceptions.createHandled().handle() ;
//             }
//             companyCRM.setCustomerNr(companyNr);
//         }
//		companyCRM.setName(colmexMap.get("Firma"));
//		companyCRM.setName2(colmexMap.get("Abteilung"));
//		companyCRM.getAddress().setStreet(colmexMap.get("Straße"));
//		companyCRM.getAddress().setZip(colmexMap.get("PLZ"));
//		companyCRM.getAddress().setCity(colmexMap.get("Ort"));
//        String colmexValue = colmexMap.get("Ausgabemedium") ;
//        if(Strings.isFilled(colmexValue)   ) {
//            List<CodeListEntry>   codeList =   cls.getEntries("invoicemedium")   ;
//            for(CodeListEntry cle: codeList)  {
//				String medium = cle.getCode();
////               String s = types[i].name() ;
////               String adaptName = adaptToCollmex(s)  ;
//               if(colmexValue.equals(medium)) {
//                   companyCRM.getCompanyAccountingData().setInvoiceMedium(medium);
//                   break;
//               }
//            }
//            if("MAIL".equals(companyCRM.getCompanyAccountingData().getInvoiceMedium()))   {
//                String mailAdr = colmexMap.get("E-Mail") ;
//                if(Strings.isFilled(mailAdr))  {
//                    if(Strings.isEmpty(companyCRM.getCompanyAccountingData().getInvoiceMailAdr())) {
//                        companyCRM.getCompanyAccountingData().setInvoiceMailAdr(mailAdr);
//                    }
//                }  else {
// //                   throw new BusinessException("Bei Colmex ist der Rechnungsversand via Mail vorgegeben, aber es fehlt die Mail-Adresse.");
//					throw Exceptions.createHandled().handle() ;
//                }
//            }
//
//        }
//		if (save) {
//			oma.update(companyCRM);
//		}
//		return companyCRM;
		return null;
	}



	@Override
	public String generateCollmexAccessStrings(UserAccount uac) {
		UserInfo userInfo = UserContext.getCurrentUser();
		userInfo.assertPermission("offers");


		// create the message
		Employee employee = uac.as(Employee.class);
		String message = "Generierung Collmexstring für "
				+ employee.getShortName();


//		User user = Users.getCurrentUser();
//		if (!user.hasGroup(CRM.GL)) {
//			return message
//					+ " ist nicht erfolgt, da Sie keine GL-Berechtigung haben.";
//		}
//		// check the permission of the emmloyee
//		Set<String> groups = employee.getGroupsSet();
//		if (!groups.contains(CRM.COLLMEX)) {
//
//			return message
//					+ " ist nicht erfolgt, da keine Collmex-Berechtigung.";
//		}
		// is the collmex-string empty?
//		if (Strings.isFilled(employee.getCollmex())) {
//			return message
//					+ " ist nicht erfolgt, da Collmexstring bereits vorhanden.";
//		}
//		// generate the collmex access code
//		String code = prepareCollmexCode(employee.getShortName());
//		if (Strings.isEmpty(code)) {
//			return message + " keine Kundennummer angegeben, Abfrage folgt ";
//		}
//		employee.setCollmex(code);
//
//		oma.update(uac);

		return message + " war erfolgreich.";

	}


	/**
	 * generates a string containing the collmex-data for the given employee
	 */
	private String prepareCollmexCode(String employeeShortName) {


//			// ToDo ask the login-data
//							"Kundennummer, Benutzer, Passwort",
		String collmexLoginData = "143476,5561455,8262070";
		String[] values = collmexLoginData.split(",");
		List<String> collmexFields = new ArrayList<String>();
		collmexFields.add(values[0].trim());
		collmexFields.add(values[1].trim());
		collmexFields.add(values[2].trim());

		if (collmexFields.isEmpty()) {
			return "";
		}
		// do not touch these statements, see also method checkAccess
		String nrBlock = makeBlock(checkLength(collmexFields.get(0),
				"Collmex-Kundennummer"));
		String userBlock = makeBlock(checkLength(collmexFields.get(1),
				"Collmex-User"));
		String passwordBlock = makeBlock(checkLength(collmexFields.get(2),
				"Collmex-Password"));
		String text = nrBlock + userBlock + passwordBlock;
		String key = buildKey(employeeShortName);
		String code = ss.encodeByKey(key, text);
		return code;
	}

	private String checkLength(String text, String message) {
		if (text.length() + 2 > BLOCKLENGTH)
//			throw new BusinessException(message + " ist > " + (BLOCKLENGTH - 2)
//					+ "Zeichen.");
		throw Exceptions.createHandled().handle();
		return text;
	}

	/**
	 * returns a key with the given name
	 */
	private String buildKey(String name) {
		return name + "bewsucldaqrs" + name;
	}

	/**
	 * put the given text in a block
	 */
	private static String makeBlock(String text) {

		Integer l = text.length();
		text = l.toString() + text;
		if (l < 10) {
			text = "0" + text;
		}
		for (int i = 0; i < BLOCKLENGTH - l - 2; i++) {
			text = text + "a";
		}
		return text;
	}


	@Override
	public List<String> getCollmexAccessData(UserAccount uac) {
//			if(uac == null) {
//				UserInfo ui = UserContext.getCurrentUser();
//				uac = ui.as(UserAccount.class);
//			}
//			Employee e = uac.as(Employee.class);
//			String code = e.getCollmex();
//			if (Strings.isEmpty(code)) {
//			   throw Exceptions.createHandled().withNLSKey("CollmexInformationServiceBean.noCollmexAccessData")
//					   .set("employee", e.getShortName()).handle();
//			}
//			// do not touch these statements, see also method collmexPrepare
//			String name = e.getShortName();
//			String key = buildKey(name);
//			String text = ss.decodeByKey(key, code);
//			List<String> list = new ArrayList<String>();
//			list.add(getBlock(1, text));
//			list.add(getBlock(2, text));
//			list.add(getBlock(3, text));
//		return list;
        return null;
	}

	@Override
	public String generateCollmexCmxKnd(Company company) {
//		// generate a csv-line for the export in Collmex-Notation
//		final int csvLae = 52; // field #1 - #52
//		String[] csv = new String[csvLae + 1]; // csv[0] - csv[82],
//		csv[1]="CMXKND";
//		csv[2]=company.getCustomerNr(); //Kundennummer
//		csv[3]="1";   // Firma-Nr Gerhard Haufler
//		csv[8]=company.getName();    // Firma
//		csv[14]="0"; // Inaktiv 0 = aktiv, 1 = inaktiv
//		if(company.getCompanyAccountingData().getInvoiceAddress().getCity() != null) {
//			csv[10]=company.getCompanyAccountingData().getInvoiceAddress().getStreet();  // Strasse
//			csv[11]=company.getCompanyAccountingData().getInvoiceAddress().getZip();     // PLZ
//			csv[12]=company.getCompanyAccountingData().getInvoiceAddress().getCity();    // Stadt
//			csv[15]=company.getCompanyAccountingData().getInvoiceAddress().getCountry().toUpperCase(); // Land, ISO-Codes Gross
//		} else {
//			csv[10] = company.getAddress().getStreet();  // Strasse
//			csv[11] = company.getAddress().getZip();     // PLZ
//			csv[12] = company.getAddress().getCity();    // Stadt
//			csv[15] = company.getAddress().getCountry().toUpperCase(); // Land, ISO-Codes Gross
//		}
//		String mainPhone = company.getMainPhoneNr();
//		if(mainPhone != null) {
//			csv[16] = mainPhone; // 16 Telefon
//		}
//		// 17 Telefax
//		String mailAdr = company.getCompanyAccountingData().getInvoiceMailAdr();
//		if(mailAdr != null) {
//			csv[18] = mailAdr; // 18 E-Mail
//		}
//		//csv[26]="0";  // Zahlungsbedingungen, 0 = 30 Tage ohne Abzug
//		//csv[27]="0";  // Rabattgrupope, 0 = kein Rabatt
//		String invoiceMedium = company.getCompanyAccountingData().getInvoiceMedium();
//		if("PRINT".equals(invoiceMedium)) {
//			csv[30] = "0"; // PRINT
//		}
//		if("MAIL".equals(invoiceMedium)) {
//			csv[30] = "1"; // MAIL
//		}
//
////		csv[34] = "0";  // Preisgruppe
////		csv[35] = "EUR"; // Währung
////		csv[36] = "0";  // Vermittler
////		csv[39] = "0";  // Liefersperre
////		csv[40] = "0";
//
//		String outputLanguage = company.getCompanyAccountingData().getOutputLanguage();
//		csv[42] = outputLanguage;
//
//		StringBuilder sb = new StringBuilder();
//		for (int i = 1; i <= csvLae; i++) {
//			if(csv[i] != null) {
//				sb.append(csv[i]);
//			}
//			sb.append(";");
//
//		}
//		return sb.toString();
        return null;
	}

	@Override
	public void sendCmxKnd(List<String> collmexAccessList, String csvString) {

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(csvString);

		String para =  "CUSTOMER_GET;" + "15555" + ";1" ;
		parameterList.add(para);

		try {
			List<String> result = callCollmex(parameterList, collmexAccessList);
			System.out.println("sendCmxKnd:"+result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets a block at the given position from the given text
	 */
	private String getBlock(int position, String text) {
		if (Strings.isEmpty(text)) {
			return null;
		}
		String s = text.substring((position - 1) * BLOCKLENGTH, (position - 1)
				* BLOCKLENGTH + BLOCKLENGTH);
		String ls = s.substring(0, 2);
		int l = Integer.parseInt(ls);
		String block = s.substring(2, 2 + l);
		return block;
	}

}
