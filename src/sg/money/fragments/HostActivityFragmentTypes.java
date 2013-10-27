package sg.money.fragments;

/**
 * The concrete types of fragments which may be accessed via the navigation drawer
 */
public enum HostActivityFragmentTypes {
    Overview(0),
    Transactions(1),
    Accounts(2),
    Categories(3),
    Budgets(4);

	// access to values() for casting is expensive, so use this instead..
	public static HostActivityFragmentTypes fromInteger(int x) {
		switch(x) {
            case 0:
                return Overview;
			case 1:
				return Transactions;
			case 2:
				return Accounts;
			case 3:
				return Categories;
			case 4:
				return Budgets;
		}
		return null;
	}

	private final int value;
	private HostActivityFragmentTypes(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
