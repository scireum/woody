/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package woody.sales;

/**
 * Created by gerhardhaufler on 19.09.16.
 */
public class ContractToDos<Contract, Contract1, Contract2, Integer> {


    public static enum Command {
        // only one contract
        NEW_CONTRACT, // F1
        ACC_IS_PRESSENT_TO_IS_NULL, // F2
        TO_IS_PRESENT_ACC_IS_NULL, // F3
        FINISHED_CONTRACT, // F4
        ACC_IS_BEFORE_TO, // F5
        ACC_IS_AFTER_TO, // F6
        // two contracts
        ACC_IS_NULL_TO_IS_NULL, // F7
        ACC_IS_NULL_TO_IS_PRESENT, // F8
        ACC_IS_PRESENT_CONTRACT2_AFTER_ACC, // F9
        ACC_IS_PRESENT_CONTRACT2_BEFORE_ACC, // F10
        NOTHING,
        C1_START_BEFORE_C0_ACC;  // keine Anwendung
        // volumeLicences   19.2.2016 keine Erweiterung erforderlich
        // VOLUME_NEW_LICENCE, // neue Volumenlizenz, noch nicht abgerechnet
        // VOLUME_RUNNING_LICENCE, // lfd. Volumenlizenz, für Vergangenheit
        // abgerechnet
        ;
    }



    public ContractToDos(Contract before, Contract first, Contract second,
                         Command command) {
        super();
        this.before = before;
        this.first = first;
        this.second = second;
        this.command = command;
    }

    public ContractToDos(Contract first, Contract second, Command command) {
        super();
        this.first = first;
        this.second = second;
        this.command = command;
    }

    private Contract before;

    public Contract getBefore() {
        return before;
    }

    public void setBefore(Contract before) {
        this.before = before;
    }

    private Contract first;
    private Contract second;
    private Command command;

    public Contract getFirst() {
        return first;
    }

    public void setFirst(Contract first) {
        this.first = first;
    }

    public Contract getSecond() {
        return second;
    }

    public void setSecond(Contract second) {
        this.second = second;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

}
