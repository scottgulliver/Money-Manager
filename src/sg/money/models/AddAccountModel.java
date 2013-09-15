package sg.money.models;

import sg.money.domainobjects.Account;

public class AddAccountModel extends SimpleObservable {

    private Account account;
    private String accountName;
    private Double startingBalance;
    private boolean newAccount;

    public AddAccountModel() {
        this(new Account(""));
        newAccount = true;
    }

    public AddAccountModel(Account account) {
        this.account = account;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Double getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(Double startingBalance) {
        this.startingBalance = startingBalance;
    }

    public boolean isNewAccount()
    {
        return newAccount;
    }
}
