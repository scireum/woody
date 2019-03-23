/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.phoneCalls;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import sirius.biz.tenants.UserAccount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Register;

import sirius.kernel.health.Exceptions;
import sirius.kernel.xml.XMLCall;
import sirius.kernel.xml.XMLStructuredInput;
import sirius.kernel.xml.XMLStructuredOutput;
import woody.core.employees.Employee;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

/**
 * Created by gerhardhaufler on 26.01.17.
 */
@Register(classes = {Starface.class})
public class StarfaceBean implements Starface {

    public static final String STARFACE_IP = "pbx.scireum.local";

    public static final String LOGIN_TEMPLATE = "<methodCall><methodName>ucp.v22.requests.connection.login</methodName></methodCall>";

    public static final String PHONE_TEMPLATE = "<methodCall><methodName>ucp.v22.requests.call.placeCallWithPhone</methodName> <params>" +
                                                "<param><value><string>{0}</string></value></param>" +
                                                "<param><value><string></string></value></param>" +
                                                "<param><value><string></string></value></param>" +
                                                "</params>" + "</methodCall>";

    public static final String LOGOUT_TEMPLATE = "<methodCall><methodName>ucp.v22.requests.connection.logout</methodName></methodCall>";


    @Override
    public void createPhoneCall(Employee employee, String destination) {

        if (Strings.isEmpty(employee.getPhoneExtension())) {
            throw Exceptions.createHandled().withNLSKey("StarfaceBean.starfaceIdMissing").set("shortName", employee.getShortName())
                            .handle();

        }
        if (Strings.isEmpty(employee.getPbxAccessToken())) {
            throw Exceptions.createHandled().withNLSKey("StarfaceBean.pbxAccessTokenMissing").set("shortName", employee.getShortName())
                            .handle();
        }
        // normalize the phone-number 07151 / 741-08 --> +49715174108
        destination = SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(destination, true);

        try {
            //                                                     starfacePasswordHash
            //                                                     <---------------->
            // de.vertico.starface.auth=<LogonId>:SHA512(<LoginId>*SHA512(<PASSWORD>))
            String starfaceId =employee.getPhoneExtension();
            String starfacePasswordHash = employee.getPbxAccessToken() ;
            String auth = starfaceId + ":" + SHA512(starfaceId + "*" + starfacePasswordHash);
            String urlString = MessageFormat.format(
                    "http://{0}/xml-rpc?de.vertico.starface.auth={1}", STARFACE_IP, auth);
            URI uri = new URI(urlString);

            // login into Starface
            sendHttp(uri, LOGIN_TEMPLATE);

            // create the phonecall
            sendHttp(uri, MessageFormat.format(PHONE_TEMPLATE, destination));

            //logout from Starface
            sendHttp(uri, LOGOUT_TEMPLATE);
        } catch (Exception e) {
            Exceptions.handle(e);
        }
    }


    @Override
    public String SHA256(String text) {
        return SHA("SHA-256", text);
    }

    @Override
    public String SHA512(String string) {
        return SHA("SHA-512", string);
    }

    @Override
    public String MD5(String text) {
        return SHA("MD5", text);
    }

    private String SHA(String krypto, String string) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(krypto);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] digest = md.digest(string.getBytes());

        StringBuffer sb = new StringBuffer();
        for (byte b : digest) {
            String hex=Integer.toHexString(0xff & b);
            if(hex.length()==1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();

    }

    @Override
    public String buildStarefacePassword(String starfaceId) {
        return starfaceId + starfaceId + starfaceId + starfaceId;
    }

    /**
     * sends a http.message
     *
     * @param uri: URL, like http://
     * @param body xml-message
     * @throws Exception
     */
    private void sendHttp(URI uri, String body) throws Exception {
//        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost postRequest = new HttpPost(uri);
        if (Strings.isFilled(body)) {
            postRequest.setEntity(new StringEntity(body));
            postRequest.addHeader("Content-Type", "text/xml");
            //  Angabe von Content-Length führt zu Fehler
            //  postRequest.addHeader( "Content-Length", String.valueOf(body.length()) );
        }

        HttpResponse response = (HttpResponse) httpclient.execute(postRequest);

//        if (CRM.LOG.isFiner() || CRM.LOG.isFinest()) {
//            System.out.println("");
//            System.out.println(uri);
//            System.out.println(body);
//
//            String result = EntityUtils.toString(response.getEntity());
//            BufferedReader br = new BufferedReader(new StringReader(result));
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
    }


    /*  05.09.2018 Umstellung auf Starface-Version 6.5.0.x

    Quelle:

    https://knowledge.starface.de/pages/viewpage.action?pageId=7864733&preview=%2F7864733%2F12124474%2FCheatsheet+zum+sicheren+Login+f%C3%BCr+UCI%2C+Chat+und+Adressbuch.pdf

    Cheatsheet zum sicheren Login für UCI, Chat und Adressbuch

    In diesem Cheatsheet werden in einer kurzen Übersicht die Informationen zur UCI-Schnittstelle
    der Version 6.4.2.19 der STARFACE aufgeführt.

    Secure Login via HTTP:Zugriff auf XML-RPC via HTTP:

    de.vertico.starface.auth=<LoginId>:SHA512(<LoginId>*SHA512(<PASSWORD>))

    Hinweis: SHA512(....) ist die Verschlüsselungsfunktion, in den String wird nur der jeweils
    verschlüsselte Wert als Hex-String eingesetzt.

    Alle SHA512-Werte  sind toLower Case

    Zum testen:

    Für die StarfaceId "11" sieht der authLink so aus:
    "http://pbx.scireum.local/xml-rpc?de.vertico.starface.auth=11:af26fc6d1350f001b075bc83327e104df6b400620b90e6d29efd710b4eabc55d13daff7d8cf968252587dc5ae962fd1ef71ae9d1b84e9b6dbcab95696b6d4dd6"

    Hinweis: in Starface ist bei der StarfaceId "11" das Passwort "11111111" (4 x die Id) eingetragen, bitte ggfs. prüfen.
    */

    // T oDo test auf false setzen
    private boolean test = true;
    // ToDo test über CRM.Log.FINE "fernsteuerbar'" machen

//    @Override
    public void createPhoneCall1(Employee employee, String destination) {
            if (employee.getPhoneExtension() == null || Strings.isEmpty(employee.getPhoneExtension())) {
                throw Exceptions.createHandled().withNLSKey("StarfaceBean.starfaceIdMissing").set("shortName", employee.getShortName())
                                .handle();
            }
            if (Strings.isEmpty(employee.getPbxAccessToken())) {
                throw Exceptions.createHandled().withNLSKey("StarfaceBean.pbxAccessTokenMissing").set("shortName", employee.getShortName())
                                .handle();

            }
            // normalize the phone-number 07151 / 741-08 --> +49715174108
            destination = SyncAsterisk.normalizePhonenumberForStarfaceAddressbook(destination, true);

            try {
                // prepare the phonecall
                String urlString = MessageFormat.format(
                        "http://{0}/xml-rpc?de.vertico.starface.user={1}&de.vertico.starface.auth={2}",
                        STARFACE_IP, employee.getPhoneExtension(), employee.getPbxAccessToken());
                URL url = new URL(urlString);
                // login into Starface
                XMLCall call = XMLCall.to(url);
                XMLStructuredOutput out = call.getOutput();
                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.connection.login").endResult();
                XMLStructuredInput in = call.getInput();
                if(test) {
                    System.out.println(in.toString());
                }

                // create the phoneCall
                call = XMLCall.to(url);
                out = call.getOutput();
                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.call.placeCallWithPhone")
                   .beginObject("params")
                   .beginObject("param")
                   .beginObject("value")
                   .property("string", destination)
                   .endObject()
                   .endObject()
                   .beginObject("param")
                   .beginObject("value")
                   .property("string", "")
                   .endObject()
                   .endObject()
                   .beginObject("param")
                   .beginObject("value")
                   .property("string", "")
                   .endObject()
                   .endObject()
                   .endObject()
                   .endResult();

                in = call.getInput();
                if(test) {System.out.println(in.toString());}

                // logout from Starface
                call = XMLCall.to(url);
                out = call.getOutput();
                out.beginOutput("methodCall").property("methodName","ucp.v22.requests.connection.logout").endResult();
                in = call.getInput();
                if(test) {System.out.println(in.toString());}

            } catch (Exception e) {
                Exceptions.handle(e);
            }
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
