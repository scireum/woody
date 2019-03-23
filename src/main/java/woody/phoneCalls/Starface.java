/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.phoneCalls;

import woody.core.employees.Employee;

/**
 * Created by gerhardhaufler on 26.01.17.
 */
public interface Starface {/**

    /**
     * creates a phone-connection from a given scireum-employee to a phone-destination.
     * @param employee: a scireum-employee
     * @param destination: phone-number, the number must not be normalized
     */

    public void createPhoneCall(Employee employee, String destination);

    /**
     * Builds a MD% Hash-String of the given String 's'
     */
    public String buildMd5HexString(String s);


    /**
     * encreypts the given text in a SHA512-hash und transform it to a hex-string
     */
    public String SHA512(String text);

    /**
     * encreypts the given text in a SHA256-hash und transform it to a hex-string
     */
    public String SHA256(String text);

    /**
     * encreypts the given text in a MD5-hash und transform it to a hex-string
     */
    public String MD5(String text);

    /**
     * builds a starfacePassword from the starfaceId
     * the starfacePassword is starfaceId + starfaceId + starfaceId + starfaceId
     */
    public String buildStarefacePassword(String starfaceId);


}