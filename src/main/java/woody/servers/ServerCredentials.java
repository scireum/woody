/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.UserAccount;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.health.Exceptions;
import sirius.mixing.Column;
import sirius.mixing.Mixable;
import sirius.mixing.annotations.*;

import java.util.regex.Pattern;

/**
 * Created by aha on 12.05.15.
 */
@Mixin(UserAccount.class)
@Framework(Servers.FRAMEWORK_SERVERS)
public class ServerCredentials extends Mixable {

    @NullAllowed
    @Lob
    private String publicKey;
    public static final Column PUBLIC_KEY = Column.named("publicKey");

    @Trim
    @NullAllowed
    @Length(length = 100)
    private String otpSecret;
    public static final Column OTP_SECRET = Column.named("otpSecret");

    private static final Pattern VALID_SSH_KEY = Pattern.compile("AAAA[0-9A-Za-z+/]+[=]{0,3}");

    @BeforeSave
    public void verifySSHKey() {
        if (Strings.isFilled(publicKey) && !VALID_SSH_KEY.matcher(publicKey).matches()) {
            throw Exceptions.createHandled().withNLSKey("ServerCredentials.invalidSSHKey").handle();
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getOtpSecret() {
        return otpSecret;
    }

    public void setOtpSecret(String otpSecret) {
        this.otpSecret = otpSecret;
    }

}
