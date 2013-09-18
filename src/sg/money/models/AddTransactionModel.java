package sg.money.models;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import sg.money.domainobjects.Account;
import sg.money.domainobjects.Budget;
import sg.money.domainobjects.Category;
import sg.money.domainobjects.Transaction;

public class AddTransactionModel extends SimpleObservable {

    Transaction transaction;
    boolean newTransaction;
    ArrayList<Category> categories;
    Map<String, Account> accountsMap = new HashMap<String, Account>();
    ArrayList<String> categoryNames;
    Transaction editTransaction;

    public AddTransactionModel(Transaction transaction, Context context) {
        this.transaction = transaction;
        if (this.transaction == null)
        {
            this.transaction = new Transaction();
            newTransaction = true;
        }
    }

    public boolean isNewTransaction() {
        return newTransaction;
    }
}
