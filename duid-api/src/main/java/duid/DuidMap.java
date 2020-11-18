package duid;

import java.util.Map;

public interface DuidMap {
    Map<String, Integer> getKeys();

    Integer getValue(String name);

    Integer getOrCreateValue(String name);
}
