package sg.money.controllers;
import sg.money.activities.*;
import sg.money.models.*;

public class AddAccountController
{
	private AddAccountActivity view;
	private AddAccountModel model;
	
	
	public AddAccountController(AddAccountActivity view, AddAccountModel model)
	{
		this.model = model;
		this.view = view;
	}

	public void onAccountNameChange(String name)
	{
		model.setAccountName(name);
	}

	public void onStartingBalanceChange(Double balance)
	{
		model.setStartingBalance(balance);
	}
	
}
