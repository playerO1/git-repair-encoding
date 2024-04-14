package repairencoding.loader;

import java.io.IOException;
import java.io.InputStream;

public interface IFileStreamGetter {
    InputStream get() throws IOException;
}
