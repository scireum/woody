/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.servers;

import sirius.biz.tenants.UserAccount;
import sirius.biz.web.Autoloaded;
import sirius.db.mixing.Column;
import sirius.db.mixing.Mixable;
import sirius.db.mixing.annotations.BeforeSave;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.Mixin;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.db.mixing.annotations.Trim;
import sirius.kernel.commons.Strings;
import sirius.kernel.di.std.Framework;
import sirius.kernel.health.Exceptions;

import java.util.regex.Pattern;

/**
 * Created by aha on 12.05.15.
 */
@Mixin(UserAccount.class)
@Framework(ServerController.FRAMEWORK_SERVERS)
public class ServerCredentials extends Mixable {

    public static final Column PUBLIC_KEY = Column.named("publicKey");
    @NullAllowed
    @Lob
    @Autoloaded(permissions = ServerController.PERMISSION_MANAGE_SERVERS)
    private String publicKey;

    public static final Column OTP_SECRET = Column.named("otpSecret");
    @Trim
    @NullAllowed
    @Length(100)
    @Autoloaded(permissions = ServerController.PERMISSION_MANAGE_SERVERS)
    private String otpSecret;

    private static final Pattern VALID_SSH_KEY = Pattern.compile("AAAA[0-9A-Za-z+/]+[=]{0,3}");

    @BeforeSave
    protected void verifySSHKey() {
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
