package duid;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Map;

public class DuidServiceTest {
    @Test
    public void testLoadProviders() {
        DuidDatabaseContext context = new DuidDatabaseContext();
        context.setBasedir(new File("target/database"));
        DuidService service = new DuidService(new AbstractDuidDatabaseImpl(context) {
            @NotNull
            @Override
            public Map<String, Integer> readKeys$duid_lib(@NotNull File file) {
                return Collections.EMPTY_MAP;
            }

            @Override
            public void writeKeys$duid_lib(@NotNull Map<String, Integer> map, @NotNull File file) {

            }
        }, Thread.currentThread().getContextClassLoader());
    }
}
