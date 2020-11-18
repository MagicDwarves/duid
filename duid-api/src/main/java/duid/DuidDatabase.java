package duid;

public interface DuidDatabase {
    DuidNamespace getRoot();

    boolean isDirty();

    void pull();

    void commit();

    void reset();

    void push();
}
