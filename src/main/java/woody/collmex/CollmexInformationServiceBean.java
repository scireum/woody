package woody.collmex;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.scireum.common.nls.NLS;
import com.scireum.ocm.annotations.Part;
import com.scireum.ocm.model.LazyPart;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import com.scireum.common.BusinessException;
import com.scireum.common.Tools;
import com.scireum.common.format.Formatter;
import com.scireum.crm.CRM;
import com.scireum.crm.model.Company;
import com.scireum.crm.model.InvoiceMediumType;

import com.scireum.crm.model.Employee;
import com.scireum.ocm.annotations.Register;
import com.scireum.ocm.db.OMA;
import com.scireum.ocm.db.security.Realm;
import com.scireum.ocm.incidents.Incidents;
import com.scireum.ocm.user.User;
import com.scireum.ocm.user.Users;
import com.scireum.ocm.web.components.ParameterizedActionListener;
import com.scireum.ocm.web.jsf.beans.ApplicationController;
import com.scireum.ocm.web.jsf.beans.DialogsBean;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

@Register(name = "CollmexInformationService", classes = {CollmexInformationService.class})
public class CollmexInformationServiceBean implements CollmexInformationService {

	@Part
	private SafeService ss;

	// model of the collmex-customer-data
	private String[] CMXKND = { "2  Kundennummer", "3  Firma Nr", "4  Anrede",
			"5  Titel", "6  Vorname", "7  Name", "8  Firma", "9  Abteilung",
			"10  Straße", "11  PLZ", "12  Ort", "13  Bemerkung", "14  Inaktiv",
			"15  Land", "16  Telefon", "17  Telefax", "18  E-Mail", "19  Kontonr",
			"20  Blz", "21  Iban", "22  Bic", "23  Bankname", "30  Ausgabemedium"};

	private static final int BLOCKLENGTH = 16;

	@Override
	public HashMap<String, String> getCollmexCustomerData(
			String customerNumber, List<String> access) {
		HashMap<String, String> collmexMap = new HashMap<String, String>();
		try {
			if (access == null) {
				return collmexMap;
			}
			// parameterfield like the collmex-API
			String[] para = { "CUSTOMER_GET", customerNumber, "1" };
			// call collmex
			List<String> result = callCollmex(para, access);
			if (result.size() == 1) {
				// store the data in a hashmap
				String[] parameters = result.get(0).split(";");
				for (int i = 0; i < CMXKND.length; i++) {
					String s = CMXKND[i];
					int k = Integer.parseInt(s.substring(0, 2).trim());
					String name = s.substring(2).trim();
					collmexMap.put(name, parameters[k - 1]);
				}
			}
		} catch (Exception e) {
			Incidents.handle(e);
		}
		return collmexMap;

	}

	/**
	 * send a request to collmex and receive the customer-data in a list
	 */
	private List<String> callCollmex(String[] parameter, List<String> access)
			throws Exception {
		// Build the Collmex-URL and add the parameters
		URI collmexUri = new URI("https://www.collmex.de/cgi-bin/cgi.exe?"
				+ access.get(0) + ",0,data_exchange");
		HttpPost postRequest = new HttpPost(collmexUri);
		postRequest.addHeader("Content-Type", "text/csv");
		String entityString = "LOGIN;" + access.get(1) + ";" + access.get(2)
				+ "\n";
		for (int i = 0; i < parameter.length; i++) {
			String s = parameter[i];
			if (!Tools.emptyString(s)) {
				entityString = entityString + s + ";";
			}
		}
		postRequest.setEntity(new StringEntity(entityString));
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = (HttpResponse) httpclient.execute(postRequest);
		// receive the collmex-data and store them in a list
		String result = EntityUtils.toString(response.getEntity());
		if (result.startsWith("MESSAGE")) {
			throw new BusinessException("CollmexApiError: " + result);
		}
		BufferedReader br = new BufferedReader(new StringReader(result));
		List<String> collmexData = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("MESSAGE")) {
				collmexData.add(line);
			}
		}
		return collmexData;
	}

	@Override
	public List<String> checkCollmexAgainstCRM(Company companyCRM,
			HashMap<String, String> collmexMap) {
		List<String> messages = new ArrayList<String>();

		checkItem(messages, collmexMap, "Kundennummer",
				companyCRM.getCustomerNr());
		checkItem(messages, collmexMap, "Firma", companyCRM.getName());
		checkItem(messages, collmexMap, "Abteilung", companyCRM.getName2());
		checkItem(messages, collmexMap, "Straße", companyCRM.getStreet());
		checkItem(messages, collmexMap, "PLZ", companyCRM.getZipCode());
		checkItem(messages, collmexMap, "Ort", companyCRM.getCity());
        InvoiceMediumType crmValueType =  companyCRM.getInvoiceMedium();
        String crmValue = "";
        if(Tools.emptyString(crmValueType))  {
            crmValue =  "PRINT";
        }  else {
            crmValue =  companyCRM.getInvoiceMedium().name();
        }
        checkItem(messages, collmexMap, "Ausgabemedium", adaptToCollmex(crmValue));
		if(companyCRM.getInvoiceMedium().equals(InvoiceMediumType.MAIL)) {
			checkItem(messages, collmexMap, "E-Mail", companyCRM.getInvoiceMailAdr());
		}

		return messages;
	}

    private String adaptToCollmex(String name) {
        return NLS.get(InvoiceMediumType.class.getName() + ".Collmex" + name);
    }

    private void checkItem(List<String> messages,
			HashMap<String, String> collmexMap, String name, String crmValue) {
		if (collmexMap.containsKey(name)) {
			String collmexValue = collmexMap.get(name);
			if (Tools.emptyString(collmexValue))  {        //(collmexValue == null) {
				if (Tools.emptyString(crmValue)) {
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
         if(newCompany) {
             String companyNr = colmexMap.get("Kundennummer");
             if(Tools.emptyString(companyNr)) {
                 throw new BusinessException("Die Company-Nr. fehlt in den Collmex-Daten");
            }
             List<Company> companyList = OMA.select(Realm.BACKEND, Company.class).eq(companyNr, Company.CUSTOMERNR).list();
             if (companyList.size() > 0) {
                 Company companyTest =companyList.get(0);
                 throw new BusinessException("Die Kundennummer " + companyNr + " ist im CRM bereits für "+ companyTest.toString()+" vergeben. Die Funktion 'Neu aus Collmex' ist damit nicht nutzbar");
             }
             companyCRM.setCustomerNr(companyNr);
         }
		companyCRM.setName(colmexMap.get("Firma"));
		companyCRM.setName2(colmexMap.get("Abteilung"));
		companyCRM.setStreet(colmexMap.get("Straße"));
		companyCRM.setZipCode(colmexMap.get("PLZ"));
		companyCRM.setCity(colmexMap.get("Ort"));
        String colmexValue = colmexMap.get("Ausgabemedium") ;
        if(!Tools.emptyString(colmexValue)   ) {
            InvoiceMediumType []   types =   InvoiceMediumType.values()   ;
            for(int i=0;  i <InvoiceMediumType.values().length; i++)  {
               String s = types[i].name() ;
               String adaptName = adaptToCollmex(s)  ;
               if(colmexValue.equals(adaptName)) {
                   companyCRM.setInvoiceMedium(types[i]);
                   break;
               }
            }
            if(InvoiceMediumType.MAIL.equals(companyCRM.getInvoiceMedium()))   {
                String mailAdr = colmexMap.get("E-Mail") ;
                if(!Tools.emptyString(mailAdr))  {
                    if(Tools.emptyString(companyCRM.getInvoiceMailAdr())) {
                        companyCRM.setInvoiceMailAdr(mailAdr);
                    }
                }  else {
                    throw new BusinessException("Bei Colmex ist der Rechnungsversand via Mail vorgegeben, aber es fehlt die Mail-Adresse.");
                }
            }

        }
		if (save) {
			companyCRM = OMA.saveEntity(Realm.BACKEND, companyCRM);
		}
		return companyCRM;
	}

	private List<String> collmexFields = new ArrayList<String>();

	@Override
	public String generateCollmexStrings(Employee employee) {
		// create the message
		String message = "Generierung Collmexstring für "
				+ employee.getShortname();
		// check the current user
		User user = Users.getCurrentUser();
		if (!user.hasGroup(CRM.GL)) {
			return message
					+ " ist nicht erfolgt, da Sie keine GL-Berechtigung haben.";
		}
		// check the permission of the emmloyee
		Set<String> groups = employee.getGroupsSet();
		if (!groups.contains(CRM.COLLMEX)) {

			return message
					+ " ist nicht erfolgt, da keine Collmex-Berechtigung.";
		}
		// is the collmex-string empty?
		if (!Tools.emptyString(employee.getCollmex())) {
			return message
					+ " ist nicht erfolgt, da Collmexstring bereits vorhanden.";
		}
		// generate the collmex-string

		String code = collmexPrepare(employee.getShortname());
		if (Tools.emptyString(code)) {
			return message + " keine Kundennummer angegeben, Abfrage folgt ";
		}
		employee.setCollmex(code);
		employee = OMA.saveEntity(Realm.BACKEND, employee);
		return message + " war erfolgreich.";

	}

//	private static final LazyPart<SafeService> ss = new LazyPart<SafeService>(
//			SafeService.class);

	/**
	 * generates a string containing the collmex-data for the given employee
	 */
	private String collmexPrepare(String name) {
		if (collmexFields.isEmpty()) {
			// ask the login-data
			ApplicationController
					.get(DialogsBean.class)
					.getQueryDialog()
					.displayQuery("Collmex",
							"Kundennummer, Benutzer, Passwort",
							new ParameterizedActionListener<String>() {

								@Override
								public void action(String value1)
										throws Exception {
									String[] values = value1.split(",");
									collmexFields.add(values[0].trim());
									collmexFields.add(values[1].trim());
									collmexFields.add(values[2].trim());
									setAccess(collmexFields);
									accessTime = new Date();
								}

							});
		}

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
		String key = buildKey(name);
		String code = ss.encodeByKey(key, text);
		return code;
	}

	private String checkLength(String text, String message) {
		if (text.length() + 2 > BLOCKLENGTH)
			throw new BusinessException(message + " ist > " + (BLOCKLENGTH - 2)
					+ "Zeichen.");
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

	private List<String> access = null;
	private Date accessTime = null;

	@Override
	public List<String> checkAccess() {
		if (accessTime == null) {
			setAccess(null);
		} else {
			long distance = new Date().getTime() - accessTime.getTime();
			if (distance > 5 * 60 * 1000) {
				setAccess(null);
			} else {
				accessTime = new Date();
			}
		}

		if (access == null || access.isEmpty()
				|| Tools.emptyString(access.get(0))) {
			User user = Users.getCurrentUser();
			String name = user.getLoginName();
			String key = buildKey(name);
			Employee e = OMA.select(Realm.BACKEND, Employee.class)
					.eq(name, Employee.SHORTNAME).first();
			String code = e.getCollmex();
			if (Tools.emptyString(code)) {
				ApplicationController
						.addInfoMessage("Es ist kein Collmex-Schlüssel hinterlegt.");
			}
			// do not touch these statements, see also method collmexPrepare
			String text = ss.decodeByKey(key, code);
			List<String> list = new ArrayList<String>();
			list.add(getBlock(1, text));
			list.add(getBlock(2, text));
			list.add(getBlock(3, text));
			setAccess(list);
			accessTime = new Date();
		}
		return access;
	}

	/**
	 * gets a block at the given position from the given text
	 */
	private String getBlock(int position, String text) {
		if (Tools.emptyString(text)) {
			return null;
		}
		String s = text.substring((position - 1) * BLOCKLENGTH, (position - 1)
				* BLOCKLENGTH + BLOCKLENGTH);
		String ls = s.substring(0, 2);
		int l = Integer.parseInt(ls);
		String block = s.substring(2, 2 + l);
		return block;
	}

	public void setAccess(List<String> access) {
		this.access = access;
	}

}
