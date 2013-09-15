package sg.money.models;

public interface EasyObservable<T> {

    void addListener(OnChangeListener<T> listener);
    void removeListener(OnChangeListener<T> listener);

}