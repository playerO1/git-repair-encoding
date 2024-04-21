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
import repairencoding.types.TUtil;

/**
 * Main class. Programm launcher.
 * @author Alexey K. (github.com/PlayerO1)
 */
public class Main {
    static final Logger log=LoggerFactory.getLogger(Main.class);
    
    static void printHelpAndExit() {
        log.warn("Required path with text files in CMD argument");
        log.info(" Sample: java -jar git-repair-encoding.jar path_with_text_files");
        log.info(" Sample 2: java -jar git-repair-encoding.jar --thread 4 path_with_text_files");
        log.info(" Use --thread=-1 for use all available processors");
        System.exit(-1);
    }
    public static void main(String[] args) throws IOException {
        String folder=null;
        // parse CMD
        String cpuN=null;
        if (1==args.length) { // todo use Apache CLI parser
            folder=args[0];
        } else if (2==args.length) {
            cpuN=args[0];
            if (cpuN.toLowerCase().startsWith("--thread=")) {
                cpuN=cpuN.substring("--thread=".length());
            } else
                printHelpAndExit();
            folder=args[1];
        } else if (3==args.length) {
            if (!args[0].toLowerCase().startsWith("--thread"))
                printHelpAndExit();
            cpuN=args[1];
            folder=args[2];
        } else {
            //todo variant with file list except path_with_text_files
            printHelpAndExit();
        }
        if (cpuN!=null) {
            TUtil.MT_THREAD_COUNT=Integer.parseInt(cpuN);
            if (TUtil.MT_THREAD_COUNT<0) {
                TUtil.MT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();
            }
            log.info("CPU thread count set to {}.", TUtil.MT_THREAD_COUNT);
        }
        
        // process
        List<TFile> historySeq;
        log.info("Load history from \"{}\"", folder);
        long timeToLoad=System.currentTimeMillis();
        try (ALoader loader=new FileLoader(Paths.get(folder))) {
            historySeq=loader.load();
        }
        timeToLoad=System.currentTimeMillis() - timeToLoad;
        log.info("Analyzing {} item content...", historySeq.size());
        long timeToRepair=System.currentTimeMillis();
        CompareEngine cEngine=new CompareEngine();
        cEngine.process(historySeq);
        timeToRepair=System.currentTimeMillis() - timeToRepair;
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
        long timeToRepair2=System.currentTimeMillis();
        RepairEngine rEngine=new RepairEngine();
        if (rEngine.needRepair(historySeq, repairId)) {
            List<String> out=rEngine.repair(historySeq, repairId);
            timeToRepair += System.currentTimeMillis() - timeToRepair2;
             // todo use NIO Path
            File outFile=new File("out.txt");
            String encode=historySeq.get(repairId).encoding;
            log.debug("Repair has finished. Write output \"{}\" ({}).", outFile, encode);
            FileUtils.writeLines(outFile, encode, out);
        } else {
            timeToRepair += System.currentTimeMillis() - timeToRepair2;
            log.info("Item {} not need repair.", repairId);
        }
        
        log.info("Done. Time: load {} ms, repair {} ms.", timeToLoad, timeToRepair);
    }
}
