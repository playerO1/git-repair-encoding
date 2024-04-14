package repairencoding.engine;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

/**
 * Repair broken lines on last file of sequence by history.
 * 
 * @see CompareEngine
 * @author Alexey K. (PlayerO1)
 */
public class RepairEngine {
    protected final Logger log=LoggerFactory.getLogger(getClass());
    
    public boolean needRepair(List<TFile> historySeq, int i) {
        return needRepair(historySeq.get(i));
    }

    public boolean needRepair(TFile historyItem) {
        int brokenLines=0;
        int noHistoryBrokenLines=0;
        log.debug("Check for repair item \"{}\" with {} lines", historyItem.name, historyItem.lines.size());
        for (TLine line:historyItem.lines) {
            if (line.hasBroken) {
                brokenLines++;
                if (line.prePos==TConst.UNDEFINED) {
                    noHistoryBrokenLines++;
                } else {
                   // todo watch full history                  
                }
            }
        }
        log.debug("Item contain {} brokenLines, {} noHistoryBrokenLines", brokenLines, noHistoryBrokenLines);
        return brokenLines>0;
    }
    
    public List<String> repair(List<TFile> historySeq, int itm) {
        if (itm==0)
            throw new IllegalArgumentException("Can not repair first item[0] from future!");
        log.debug("Start repair history[{}] \"{}\"", itm, historySeq.get(itm).name);
        List<TLine> lines=historySeq.get(itm).lines;
        List<String> out=new ArrayList<>(lines.size());
        int statRepair=0, statHistoryUsedDepth=0, statHistoryWatchDepth=0, statNotFound=0;
        for (TLine line:lines) {
            if (line.hasBroken) {
                int oldP=line.prePos;
                int depth=0;
                for (int i=itm-1; oldP!=TConst.UNDEFINED && i>=0 ;i--) {
                    depth++;
                    statHistoryWatchDepth=Math.max(statHistoryWatchDepth, depth);
                    TLine oldL=historySeq.get(i).lines.get(oldP);
                    if (!oldL.hasBroken) {
                        out.add(oldL.line);
                        log.trace(" [{}][{}] line {}({})> repair from \"{}\" to \"{}\"",
                                itm, i, line.pos, oldP, line.line, oldL.line);
                        statRepair++;
                        statHistoryUsedDepth=Math.max(statHistoryUsedDepth, depth);
                        break;
                    } else {
                        log.trace(" [{}][{}] line {}({})> go deep", itm, i, line.pos, oldP);
                    }
                    oldP=oldL.prePos;
                }
                if (oldP==TConst.UNDEFINED) {
                    out.add(line.line); // no modify
                    log.trace(" [{}] line {}> no good history", itm, line.pos);
                    statNotFound++;
                }
            } else {
                out.add(line.line);
            }
        }
        log.debug("Repair complete: repair {} lines, max history depth = {}, max history watch depth = {}, not found {} lines.",
                statRepair, statHistoryUsedDepth, statHistoryWatchDepth, statNotFound);
        return out;
    }
}
