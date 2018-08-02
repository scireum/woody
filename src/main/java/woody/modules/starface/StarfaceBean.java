/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.modules.starface;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import sirius.kernel.di.std.Register;

import woody.core.employees.Employee;

/**
 * Created by gerhardhaufler on 26.01.17.
 */
@Register(classes = {Starface.class})
public class StarfaceBean implements Starface {

    public static final String STARFACE_IP = "pbx.scireum.local";

    // T oDo test auf false setzen
    private boolean test = false;
    // ToDo test über CRM.Log.FINE "fernsteuerbar'" machen

    @Override
    public void createPhoneCall(Employee employee, String destination) {
//            if (employee.getPbxId() == null || Strings.isEmpty(employee.getPbxId())) {
//                throw Exceptions.createHandled().withNLSKey("Starface.pbxIdMissing")
//                                .set("employeeShortName", employee.getShortName()).handle();
//            }
//            if (Strings.isEmpty(employee.getPbxAccessToken())) {
//                throw Exceptions.createHandled().withNLSKey("Starface.pbxAccessTokenMissing")
//                                .set("employeeShortName", employee.getShortName()).handle();
//
//            }
//            // normalize the phone-number 07151 / 741-08 --> +49715174108
//            destination = SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(destination, true);
//
//            try {
//                // prepare the phonecall
//                String urlString = MessageFormat.format(
//                        "http://{0}/xml-rpc?de.vertico.starface.user={1}&de.vertico.starface.auth={2}",
//                        STARFACE_IP, employee.getPbxId(), employee.getPbxAccessToken());
//                URL url = new URL(urlString);
//                // login into Starface
//                XMLCall call = XMLCall.to(url);
//                XMLStructuredOutput out = call.getOutput();
//                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.connection.login").endResult();
//                XMLStructuredInput in = call.getInput();
//                if(test) {
//                    System.out.println(in.toString());
//                }
//
//                // create the phoneCall
//                call = XMLCall.to(url);
//                out = call.getOutput();
//                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.call.placeCallWithPhone")
//                   .beginObject("params")
//                   .beginObject("param")
//                   .beginObject("value")
//                   .property("string", destination)
//                   .endObject()
//                   .endObject()
//                   .beginObject("param")
//                   .beginObject("value")
//                   .property("string", "")
//                   .endObject()
//                   .endObject()
//                   .beginObject("param")
//                   .beginObject("value")
//                   .property("string", "")
//                   .endObject()
//                   .endObject()
//                   .endObject()
//                   .endResult();
//
//                in = call.getInput();
//                if(test) {System.out.println(in.toString());}
//
//                // logout from Starface
//                call = XMLCall.to(url);
//                out = call.getOutput();
//                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.connection.logout").endResult();
//                in = call.getInput();
//                if(test) {System.out.println(in.toString());}
//
//            } catch (Exception e) {
//                Exceptions.handle(e);
//            }
        }

    @Override
    public String buildMd5HexString(String s) {
        //  alte Lösung im CRM: String md5 = BaseEncoding.base64().encode(Hashing.md5().hashString(s, Charsets.UTF_8).asBytes());
        byte[] md5HashBytes = Hashing.md5().hashString(s, Charsets.UTF_8).asBytes();
        StringBuilder sb = new StringBuilder(md5HashBytes.length * 2);
        for (int i = 0; i < md5HashBytes.length; i++) {
            sb.append(Character.forDigit((md5HashBytes[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(md5HashBytes[i] & 0x0f, 16));
        }
        String md5 = sb.toString();
        return md5;
    }
}
