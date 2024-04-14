package repairencoding.loader;

import java.io.IOException;
import java.util.List;
import repairencoding.types.TFile;

/**
 * Load file sequence API
 */
public interface ILoader {
    List<TFile> load() throws IOException;
}
