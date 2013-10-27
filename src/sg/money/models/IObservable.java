package sg.money.models;

public interface IObservable<T>
{
    void addListener(OnChangeListener<T> listener);
    void removeListener(OnChangeListener<T> listener);
}
