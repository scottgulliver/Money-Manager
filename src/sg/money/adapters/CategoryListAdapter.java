package sg.money.adapters;

import java.util.ArrayList;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import sg.money.R;
import sg.money.domainobjects.Category;

public class CategoryListAdapter extends BaseListAdapter<Category> {

    private ArrayList<String> m_customStrings;

	
	/* Constructors */
 
    public CategoryListAdapter(Activity activity, ArrayList<Category> categories, ArrayList<String> customStrings) {
        super(activity, categories);
        m_customStrings = customStrings;
    }
	
	
	/* Methods */
    
    private String getCategoryName(Category category)
    {
    	return Category.getCategoryName(category, m_activity);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.category_item_layout;
    }

    @Override
    protected void buildView(View view, Category category) {
        TextView nameText = (TextView)view.findViewById(R.id.category_name);
        TextView typeText = (TextView)view.findViewById(R.id.category_type);
        ImageView colorField = (ImageView)view.findViewById(R.id.category_color);

        //set values
        nameText.setText(getCategoryName(category));

        if (m_customStrings == null)
        {
            typeText.setText(category.isIncome() ? "Income" : "Expense");
        }
        else
        {
            typeText.setText(m_customStrings.get(m_items.indexOf(category)));
        }

        colorField.setBackgroundColor(category.getColor());

        if (m_selectedItems.contains(category))
        {
            view.setBackgroundColor(COLOR_SELECTED);
        }
        else
        {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
