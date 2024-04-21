package repairencoding.loader;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.apache.any23.encoding.TikaEncodingDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

/**
 * Abstract text file loader
 * @author Alexey K. (github.com/PlayerO1)
 */
public abstract class ALoader implements ILoader, Closeable{
    protected final Logger log=LoggerFactory.getLogger(getClass());
    /**
     * Memory optimization for same string
     */
    protected final boolean compressString;
    private final Map<String,String> stringCompressor;
    // stats:
    private int compressStatNew, compressStatRepeat;
    /**
     * true multithread statistic. But can be slow.
     * Are you sure that need slow perfomance for true log trace?
     */
    private final AtomicInteger compressStatANew, compressStatARepeat;
    
    protected TikaEncodingDetector tEncodingDetector=new TikaEncodingDetector();
    
    protected String primaryEncoding=TConst.DEFAULT_ENODING;

    public ALoader(boolean compressString0, boolean concurrent) {
        this.compressString=compressString0;
        if (compressString) {
            log.trace("Loader using String memory minimalization (like intern). Support MT: {}",concurrent);
            if (concurrent) {
                stringCompressor=new ConcurrentHashMap<>();
                compressStatANew=new AtomicInteger(0);
                compressStatARepeat=new AtomicInteger(0);
            } else {
                compressStatNew = compressStatRepeat = 0;
                stringCompressor=new HashMap<>();
                compressStatARepeat=null;
                compressStatANew=null;
            }
        } else {
            stringCompressor=null;
            compressStatARepeat=null;
            compressStatANew=null;
        }
    }
    
    protected TFile readFile(String name, long timestamp, IFileStreamGetter isCreator) throws IOException {
        log.debug("Read file \"{}\", timestamp={}", name, timestamp);
        TFile file=new TFile(name, timestamp);
        try (InputStream is=isCreator.get()) {
            file.encoding = detectEncoding(is);
        }
        try (InputStream is=isCreator.get();
        InputStreamReader sr=new InputStreamReader(is, file.encoding);
                BufferedReader br=new BufferedReader(sr)
        ) {
            List<TLine> tLines = convertToLines(br.lines());
            log.trace("From \"{}\" read {} lines", name, tLines.size());
            file.lines=tLines;
        }
        return file;
    }
    
    protected List<TLine> convertToLines(Stream<String> textLines) {
        ArrayList<TLine> out=new ArrayList<>(); // (textLines.size()) or use trimToSize
        int i[]=new int[1];
        //for (String s:textLines) {
        textLines.forEach((s)->{
            TLine t=new TLine();
            t.pos=i[0]++;
            t.line=strIntern(s);
            t.hasBroken=false;// uncknown, later
            t.prePos=t.nextPos=TConst.UNDEFINED;
            out.add(t);
        });
        out.trimToSize();
        return out;
    }
    
    protected String strIntern(String s) {
        if (compressString) {
            if (s==null) return null;// never on this programm
            // return s.intern() - bad way - it is temp text. Do not use JVM string cache
            String exist=stringCompressor.putIfAbsent(s, s);
            if (exist==null) {
                if (compressStatANew==null)
                     compressStatNew++;
                else compressStatANew.incrementAndGet();
                return s;
            } else {
                if (compressStatARepeat==null)
                     compressStatRepeat++;
                else compressStatARepeat.incrementAndGet();
                return exist;
            }
        } else
            return s;
    }

    protected String detectEncoding(InputStream is) throws IOException {
        String encoding = tEncodingDetector.guessEncoding(is);
        if (encoding==null) {
            encoding=primaryEncoding;
            log.warn("Encoding not detected, use default: {}",encoding);
        } else {
            if ("ISO-8859-1".equals(encoding))
                encoding="windows-1251";
            if ("ISO-8859-15".equals(encoding))
                encoding="utf-8";
            log.debug("Encoding \"{}\" detected",encoding);
        }
        return encoding;
    }

    @Override
    public void close() throws IOException {
        if (compressString) {
            if (compressStatANew!=null) {
                compressStatNew = compressStatANew.get();
                compressStatRepeat = compressStatARepeat.get();
            }
            log.trace("Loader String minimalization stat: {} new, {} repeat", 
                    compressStatNew, compressStatRepeat);
            stringCompressor.clear();
            //compressStatNew = compressStatRepeat = 0;
        }
        log.trace("Loader closed");
    }
    
    
}
