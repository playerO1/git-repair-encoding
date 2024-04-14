
package repairencoding.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import repairencoding.types.TFile;

/**
 * Load files from path
 */
public class FileLoader extends ALoader{
    Path source;

    public FileLoader(Path source) {
        this.source = source;
    }
    
    protected List<Path> fileSequence() throws IOException {
        if (!Files.isDirectory(source)) {
            throw new IOException("Is not a directory: "+source);
        }
        List<Path> out=Files.list(source)
                .filter((file)->{
                    //todo file name pattern
                    return !Files.isDirectory(file);
                })
                .sorted((f1,f2)->{
                    try {
                        String n1=f1.toFile().getName();
                        String n2=f2.toFile().getName();
                        n1=n1.replace(" ", "");
                        n2=n2.replace(" ", "");
                        // todo order logic
                        int cmp=n1.compareTo(n2);
                        if (cmp!=0)
                            return cmp;
                        FileTime t1=Files.getLastModifiedTime(f1);
                        FileTime t2=Files.getLastModifiedTime(f2);
                        return t1.compareTo(t2);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());
        return out;
    }
    
    @Override
    public List<TFile> load() throws IOException {
        List<Path> files = fileSequence();
        log.debug("Found {} files in folder \"{}\"", files.size(), source);
        log.trace("File sequence: {}", files);
        List<TFile> out=new ArrayList<>();
        for (Path f:files) {
            FileTime t=Files.getLastModifiedTime(f);
            String name = f.toFile().getName();
            out.add(this.readFile(name, t.toMillis(), ()->Files.newInputStream(f)));
        }
        return out;
    }
    
}
