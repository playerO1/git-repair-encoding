package repairencoding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repairencoding.engine.CompareEngine;
import repairencoding.engine.DiffVisualization;
import repairencoding.engine.RepairEngine;
import repairencoding.loader.ALoader;
import repairencoding.loader.FileLoader;
import repairencoding.types.TFile;

/**
 * Main class. Programm launcher.
 * @author Alexey K. (github.com/PlayerO1)
 */
public class Main {
    static final Logger log=LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws IOException {
        if (args.length<1) {
            System.out.println("Required path with text files in CMD argument");
            System.exit(-1);
        }
        String folder=args[0];
        List<TFile> historySeq;
        log.info("Load history from \"{}\"", folder);
        try (ALoader loader=new FileLoader(Paths.get(folder))) {
            historySeq=loader.load();
        }
        log.info("Analyzing {} item content...", historySeq.size());
        CompareEngine cEngine=new CompareEngine();
        cEngine.process(historySeq);
        {
            String diffFile="diff.html";
            log.info("Print diff to file \"{}\"", diffFile);
            DiffVisualization vizualization=new DiffVisualization(historySeq, true);
            try (FileOutputStream fos=new FileOutputStream(diffFile)) {
                vizualization.writeToHTML(fos);
            }
        }
        
        int repairId=historySeq.size()-1;
        log.info("Try repair item [{}] \"{}\", encoded as \"{}\".", 
                repairId, historySeq.get(repairId).name, historySeq.get(repairId).encoding);
        RepairEngine rEngine=new RepairEngine();
        if (rEngine.needRepair(historySeq, repairId)) {
            List<String> out=rEngine.repair(historySeq, repairId);
             // todo use NIO Path
            File outFile=new File("out.txt");
            String encode=historySeq.get(repairId).encoding;
            log.debug("Repair success. Write output \"{}\" ({}).", outFile, encode);
            FileUtils.writeLines(outFile, encode, out);
        } else {
            log.info("Item {} not need repair.", repairId);
        }
        
        log.info("Done.");
    }
}
