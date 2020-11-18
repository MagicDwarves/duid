package test;

import duid.DuidMap;
import duid.DuidNamespace;
import duid.DuidProvider;

public class TestDuidProvider2 implements DuidProvider {
    @Override
    public void apply(DuidNamespace root) {
        DuidMap testMap = root.getOrCreateMap("test");
        testMap.getOrCreateValue("value3");
        testMap.getOrCreateValue("value4");
        DuidNamespace testNamespace = root.getOrCreateNamespace("name1");
        DuidMap classesMap = testNamespace.getOrCreateMap("classes");
        classesMap.getOrCreateValue("class3");
        classesMap.getOrCreateValue("class4");
        DuidNamespace name2Namespace = root.getOrCreateNamespace("name2");
        DuidMap idsMap = name2Namespace.getOrCreateMap("ids");
        idsMap.getOrCreateValue("id1");
    }
}
