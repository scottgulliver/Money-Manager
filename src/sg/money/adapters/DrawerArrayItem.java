package sg.money.adapters;

public class DrawerArrayItem {
    private String text;
    private int imageId;

    public DrawerArrayItem(String text, int imageId) {
        this.text = text;
        this.imageId = imageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
