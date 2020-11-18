package duid;

import java.util.Map;

public interface DuidNamespace {
    boolean isDirty();

    Map<String, DuidNamespace> getNamespaces();

    DuidNamespace getNamespace(String name);

    DuidNamespace getOrCreateNamespace(String name);

    Map<String, DuidMap> getMaps();

    DuidMap getMap(String name);

    DuidMap getOrCreateMap(String name);
}
