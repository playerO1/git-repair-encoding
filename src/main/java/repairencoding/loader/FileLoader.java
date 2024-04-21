
package repairencoding.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import repairencoding.types.TFile;
import repairencoding.types.TUtil;

/**
 * Load files from path
 */
public class FileLoader extends ALoader{
    Path source;

    public FileLoader(Path source) {
        super(true, TUtil.MT_THREAD_COUNT>1);
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
        if (TUtil.MT_THREAD_COUNT<=1) {
            for (Path f:files) {
                FileTime t=Files.getLastModifiedTime(f);
                String name = f.toFile().getName();
                out.add(this.readFile(name, t.toMillis(), ()->Files.newInputStream(f)));
            }
        } else {
            log.debug("Loading with {} thread", TUtil.MT_THREAD_COUNT);
            ExecutorService executor = TUtil.newExecutor("File_loader", files.size());
            try {
                List<Future<TFile>> wait=new LinkedList<>();
                for (Path f:files) {
                    wait.add(executor.submit(()-> {
                        FileTime t=Files.getLastModifiedTime(f);
                        String name = f.toFile().getName();
                        return readFile(name, t.toMillis(), ()->Files.newInputStream(f));
                    }));
                }
                try {
                for (Future<TFile> tf:wait)
                        out.add(tf.get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    log.error("Error at multithread loading: {}", ex.toString());
                    throw new IOException(ex.getCause());
                }
            } finally {
                executor.shutdownNow();
            }
        }
        return out;
    }
    
}
