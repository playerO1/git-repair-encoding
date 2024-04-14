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
    protected boolean compressString=true;
    private Map<String,String> stringCompressor;
    private int compressStatNew, compressStatRepeat;
    
    protected TikaEncodingDetector tEncodingDetector=new TikaEncodingDetector();
    
    protected String primaryEncoding=TConst.DEFAULT_ENODING;
    
    
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
            if (stringCompressor==null) {
                log.trace("Loader using String memory minimalization (like intern)");
                stringCompressor=new HashMap<>();
                compressStatNew = compressStatRepeat = 0;
            }
            // return s.intern() - bad way - it is temp text. Do not use JVM string cache
            String exist=stringCompressor.get(s);
            if (exist==null) {
                stringCompressor.put(s, s);
                compressStatNew++;
                return s;
            } else {
                compressStatRepeat++;
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
        stringCompressor = null;
        if (compressString) {
            log.trace("Loader String minimalization stat: {} new, {} repeat", 
                    compressStatNew, compressStatRepeat);
        }
        log.debug("Loader ");
    }
    
    
}
