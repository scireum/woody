/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.xrm.tracking.calls;

import sirius.biz.jdbc.BizEntity;
import sirius.db.jdbc.SQLEntityRef;
import sirius.db.mixing.Mapping;
import sirius.db.mixing.annotations.Length;
import sirius.db.mixing.annotations.Lob;
import sirius.db.mixing.annotations.NullAllowed;
import sirius.kernel.commons.Strings;
import sirius.kernel.nls.NLS;
import woody.xrm.Company;
import woody.xrm.Person;

import java.time.LocalDateTime;

public class PhoneCall extends BizEntity {

    @NullAllowed
    private LocalDateTime starttime;
    public static final Mapping STARTTIME = Mapping.named("starttime");

    //    @Filter
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(20)
    private String state;
    public static final Mapping STATE = Mapping.named("state");

    //    @Filter(position = 1)
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean top = false;
    public static final Mapping TOP = Mapping.named("top");

    @Length(255)
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private String reason;
    public static final Mapping REASON = Mapping.named("reason");

    //    @Filter
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean answered;
    public static final Mapping ANSWERED = Mapping.named("answered");

    @Length(255)
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private String answeredElsewhere;
    public static final Mapping ANSWEREDELSEWHERE = Mapping.named("answeredElsewhere");

    //    @Filter
    @NullAllowed
    private boolean incoming;
    public static final Mapping INCOMING = Mapping.named("incoming");

    //    @Filter(position=2)
    @Length(20)
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private String direction = "OPEN";
    public static final Mapping DIRECTION = Mapping.named("direction");

    //    @Filter
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean directionIn = false;
    public static final Mapping DIRECTIONIN = Mapping.named("directionIn");

    //    @Filter(position=2)
    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean save = false;
    public static final Mapping SAVE = Mapping.named("save");

    private long callId;
    public static final Mapping CALLID = Mapping.named("callId");

    private long cdrId;
    public static final Mapping CDRID = Mapping.named("cdrId");

    //    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(255)
    private String callercallerid;
    public static final Mapping CALLERCALLERID = Mapping.named("callercallerid");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(255)
    private String calledcallerid;
    public static final Mapping CALLEDCALLERID = Mapping.named("calledcallerid");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(255)
    private String firstCalled;
    public static final Mapping FIRSTCALLED = Mapping.named("firstCalled");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(255)
    private String lastCalled;
    public static final Mapping LASTCALLED = Mapping.named("lastCalled");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Length(255)
    private String target;
    public static final Mapping TARGET = Mapping.named("target");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean externCall;
    public static final Mapping EXTERNCALL = Mapping.named("externCall");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private boolean transfer;
    public static final Mapping TRANSFER = Mapping.named("transfer");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private long duration = 0;
    public static final Mapping DURATION = Mapping.named("duration");

    @NullAllowed
    @Length(50)
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private String callresult;
    public static final Mapping CALLRESULT = Mapping.named("callresult");

    //    @Filter(position=20)
    @NullAllowed
    @Length(50)
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    private String employeeShortName;
    public static final Mapping EMPLOYEESHORTNAME = Mapping.named("employeeShortName");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Lob
    @Length(5000)
    private String row;
    public static final Mapping ROW = Mapping.named("row");

    @NullAllowed
//    @Params({@Param(name = ParamsFieldConstants.PARAM_READONLY, value = "true")})
    @Lob
    @Length(5000)
    private String difference;
    public static final Mapping DIFFERENCE = Mapping.named("difference");

    @NullAllowed
    private final SQLEntityRef<Person> person = SQLEntityRef.on(Person.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping PERSON = Mapping.named("person");

    @NullAllowed
    private final SQLEntityRef<Company> company = SQLEntityRef.on(Company.class, SQLEntityRef.OnDelete.CASCADE);
    public static final Mapping COMPANY = Mapping.named("company");

    public String toString() {
        StringBuilder sb = new StringBuilder();
        asString(sb);
        return sb.toString();
    }

    protected void asString(StringBuilder sb) {
        if (Strings.isEmpty(getCallercallerid())) {
            sb.append("null");
        } else {
            sb.append(getCallercallerid());
        }
        sb.append("->");
        if (Strings.isEmpty(getCalledcallerid())) {
            sb.append("null");
        } else {
            sb.append(getCalledcallerid());
        }
        sb.append(" @ ");
        sb.append(NLS.toUserString(getStarttime()));
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getAnsweredElsewhere() {
        return answeredElsewhere;
    }

    public void setAnsweredElsewhere(String answeredElsewhere) {
        this.answeredElsewhere = answeredElsewhere;
    }

    public boolean isTransfer() {
        return transfer;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public String getFirstCalled() {
        return firstCalled;
    }

    public void setFirstCalled(String firstCalled) {
        this.firstCalled = firstCalled;
    }

    public String getLastCalled() {
        return lastCalled;
    }

    public void setLastCalled(String lastCalled) {
        this.lastCalled = lastCalled;
    }

    public boolean isExternCall() {
        return externCall;
    }

    public void setExternCall(boolean externCall) {
        this.externCall = externCall;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isDirectionIn() {
        return directionIn;
    }

    public void setDirectionIn(boolean directionIn) {
        this.directionIn = directionIn;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCallresult() {
        return callresult;
    }

    public void setCallresult(String callresult) {
        this.callresult = callresult;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCallercallerid() {
        return callercallerid;
    }

    public void setCallercallerid(String callercallerid) {
        this.callercallerid = callercallerid;
    }

    public String getCalledcallerid() {
        return calledcallerid;
    }

    public void setCalledcallerid(String calledcallerid) {
        this.calledcallerid = calledcallerid;
    }

    public boolean getIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public long getCallId() {
        return callId;
    }

    public void setCallId(long callId) {
        this.callId = callId;
    }

    public String getEmployeeShortName() {
        return employeeShortName;
    }

    public void setEmployeeShortName(String employeeShortName) {
        this.employeeShortName = employeeShortName;
    }

    public long getCdrId() {
        return cdrId;
    }

    public void setCdrId(long cdrId) {
        this.cdrId = cdrId;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public LocalDateTime getStarttime() {
        return starttime;
    }

    public void setStarttime(LocalDateTime starttime) {
        this.starttime = starttime;
    }

    public SQLEntityRef<Person> getPerson() {
        return person;
    }

    public SQLEntityRef<Company> getCompany() {
        return company;
    }
}
