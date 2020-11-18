package test;

import duid.DuidMap;
import duid.DuidNamespace;
import duid.DuidProvider;

public class TestDuidProvider1 implements DuidProvider {
    @Override
    public void apply(DuidNamespace root) {
        DuidMap testMap = root.getOrCreateMap("test");
        testMap.getOrCreateValue("value1");
        testMap.getOrCreateValue("value2");
        DuidNamespace testNamespace = root.getOrCreateNamespace("name1");
        DuidMap classesMap = testNamespace.getOrCreateMap("classes");
        classesMap.getOrCreateValue("class1");
        classesMap.getOrCreateValue("class2");
        root.getOrCreateNamespace("name2");
    }
}
