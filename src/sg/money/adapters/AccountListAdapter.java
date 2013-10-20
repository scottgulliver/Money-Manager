package sg.money.adapters;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import sg.money.R;
import sg.money.domainobjects.Account;
import sg.money.utils.Misc;

public class AccountListAdapter extends BaseListAdapter<Account> {

	/* Constructor */
 
    public AccountListAdapter(Activity activity, ArrayList<Account> accounts) {
        super(activity, accounts);
    }


    /* Methods */

    @Override
    protected int getLayoutResourceId() {
        return R.layout.account_item_layout;
    }

    @Override
    protected void buildView(View view, Account account) {

        TextView nameText = (TextView)view.findViewById(R.id.account_name);
        TextView balanceText = (TextView)view.findViewById(R.id.account_balance);

        //set values

        nameText.setText(account.getName());
        balanceText.setText(Misc.formatValue(m_activity, account.getValue()));
        if (account.getValue() >= 0)
        {
            balanceText.setTextColor(Color.argb(255, 102, 153, 0));
        }
        else
        {
            balanceText.setTextColor(Color.argb(255, 204, 0, 0));
        }

        if (m_selectedItems.contains(account))
        {
            view.setBackgroundColor(COLOR_SELECTED);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
