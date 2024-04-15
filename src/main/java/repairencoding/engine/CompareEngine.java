package repairencoding.engine;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

/**
 * Mark lines, search modify and link files in sequence
 * @author Alexey K. (PlayerO1)
 */
public class CompareEngine {
    protected final Logger log=LoggerFactory.getLogger(getClass());

    protected int searchWindow1 = 70; // when diff < DIFF_TRESHOLD
    protected int searchWindow2 = 14; // when diff < 3
    protected final int DIFF_TRESHOLD_1=68;
    protected final int DIFF_TRESHOLD_2=3;
    
    public void process(List<TFile> historySeq) {
        if (historySeq.size()<2)
            throw new IllegalArgumentException("History sequence should have 2 or more files.");
        for (TFile f:historySeq) {
            markBrokenLines(f);
        }
        for (int i=1;i<historySeq.size();i++) {
            linkFiles(historySeq.get(i-1), historySeq.get(i));
        }
    }

    protected void markBrokenLines(TFile inFile) {
        for (TLine l:inFile.getLines()) {
            l.hasBroken = checkBrokenLine(l.line);
        }
    }

    // TODO hard method. Need more unit test!    
    protected void linkFiles(TFile pre, TFile next) {
        List<TLine> lines1=pre.getLines();
        List<TLine> lines2=next.getLines();
        int p2=0;
        int[] outK=new int[1];
        int seqenceLost=0;
        for (int i=0;i<lines1.size();i++) {
            log.trace("Progress {}/{} history items", i+1, lines1.size());
            TLine myLine=lines1.get(i);
            int p =searchLine(myLine.line, lines2, p2, i, outK);
            int thisK = outK[0];
            if (p!=TConst.UNDEFINED && p>=0) {
                if (p2!=p) {
                    log.trace(" new offset {} > {}", i, p);
                    p2=p;
                }
                myLine.nextPos=p;
                boolean applyPatch = true;
                if (lines2.get(p).prePos!=TConst.UNDEFINED && lines2.get(p).prePos!=i) {
                    if (lines2.get(p).preK > thisK) {
                        log.debug("i={} and pos={} link to already linked: next line {} link to {} with k={} > that new k={} (overwrite)",
                                i,myLine.pos, p, lines2.get(p).prePos, lines2.get(p).preK, thisK);
                    } else if (lines2.get(p).preK < thisK) {
                        log.debug("i={} and pos={} link to already linked: next line {} link to {} with k={} < that new k={} (ignore this backlink)",
                                i,myLine.pos, p, lines2.get(p).prePos, lines2.get(p).preK, thisK);
                        applyPatch = false;
                    } else {
                        log.warn("i={} and pos={} \"{}\" link to already linked: next line {} \"{}\" link to {}. Has same k={}",
                                i, myLine.pos, myLine.line, p, lines2.get(p).line, lines2.get(p).prePos, thisK);
                    }
                }
                if (applyPatch) {
                    lines2.get(p).prePos = i;
                    lines2.get(p).preK = thisK;
                    if (i != myLine.pos)
                        log.warn("i={} and pos={} not match! At line \"{}\"", i, myLine.pos, myLine.line);
                }

                if (seqenceLost>searchWindow1*3) {
                    log.debug("i={}, found p={} after lost search window", i,p);
                }
                seqenceLost=0;
            } else {
                log.trace("i={} pos={} not found position", i, p);
                seqenceLost++;
                if (seqenceLost==searchWindow1*3) {
                    log.debug("i={}, p={} lost search window, next position search may be not correct", i,p);
                }
            }
            p2++;
        }
    }

    // TODO hard method. Need more unit test!    
    private int searchLine(String text, List<TLine> inLines, int nearPos1, int nearPos2, int[] outK) {
        int result=TConst.UNDEFINED, resultD=Integer.MAX_VALUE;
        for (int fromPos: new int[]{nearPos1,nearPos2}) {
           if (fromPos==TConst.UNDEFINED) continue;
           if (result!=TConst.UNDEFINED && nearPos1==nearPos2) break;
           for (int d=0; d<searchWindow1 ;d++) { // far = 70
               // todo mark previous interval and do not check against lines.
               int p=fromPos+d;
               if (0<= p && p <inLines.size() ) {
                    int k = stringDiff(text, inLines.get(p).line);
                    if (k<=DIFF_TRESHOLD_1*3) {
                        k+=+Math.min(20,d/40);
                        k+=Math.min(16,Math.min(Math.abs(p-nearPos1),Math.abs(p-nearPos2))/50);
                    }
                    if (resultD>k) {
                        result=p;
                        resultD=k;
                    }
               }
               if (d!=0){
                   p=fromPos-d;
                    if (0<= p && p <inLines.size() ) {
                         int k = stringDiff(text, inLines.get(p).line);
                         if (k<=DIFF_TRESHOLD_1*3) {
                             k+=+Math.min(20,d/40);
                            k+=Math.min(16,Math.min(Math.abs(p-nearPos1),Math.abs(p-nearPos2))/50);
                         }
                         if (resultD>k) {
                             result=p;
                             resultD=k;
                         }
                    }
               }
               if (resultD==0 || resultD<DIFF_TRESHOLD_2 && d>3 && d < searchWindow2) // far = 14
                   break; // stop search - it is best.
           }
        }
        if (outK!=null) {
            outK[0]=resultD;
        }
        if (resultD>DIFF_TRESHOLD_1)
            return -1; // not found
        return result;
    }
    
    
    // --- utils ---
        
    protected char[] allowed;
    {
        allowed=" !\"#$%&'()*,-.//0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz|«»АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхцчшщъыьэюя№".toCharArray();
        Arrays.sort(allowed);
    }
    /**
     * TODO specific method. Need more unit test!
     * @param line 
     */
    protected boolean checkBrokenLine(String line) {
            int wrongChar=0, questionChar=0;
            for (int i=0;i<line.length();i++) {
                char c=line.charAt(i);
                //todo or use ange check
                if (Arrays.binarySearch(allowed, c)<0)
                    wrongChar++;
                if (c=='?') questionChar++;
            }
            return wrongChar>0 || (line.length()>5 && questionChar>7);
    }
    
    /**
     * Soft comparsion of 2 string
     * @param l1
     * @param l2
     * @return  0 - equals, 2 equals ignore case and whitespace, 15 high diff but maybe same, Integer.MAX_VALUE absolutly different
     */
    protected int stringDiff(String l1, String l2) {
        if (l1.equals(l2)) return 0;
        if (l1.equalsIgnoreCase(l2)) return 1;
        l1=l1.replace("  ", " ").trim();
        l2=l2.replace("  ", " ").trim();
        if (l1.equalsIgnoreCase(l2)) return 2;
        //todo see science method of diff string length comparsion.
        // next stupid method
        int lenDiff=Math.abs(l1.length()-l2.length());
        l1=l1.replaceAll("[^(a-zA-Zа-яА-Я0-9 )]", "");
        l2=l2.replaceAll("[^(a-zA-Zа-яА-Я0-9 )]", "");
        if (l1.equalsIgnoreCase(l2)) return 3;
        if (l1.replace(" ","").equalsIgnoreCase(l2.replace(" ",""))) return 3;
        if (lenDiff>7 + l1.length()/70)
            return Integer.MAX_VALUE;
        l1=l1.replaceAll("[^(a-zA-Z0-9 )]", ""); // remove broken symbol
        l2=l2.replaceAll("[^(a-zA-Z0-9 )]", "");
        if (l1.length()<3 || l2.length()<3 || Math.abs(l1.length()-l2.length())>30)
             return Integer.MAX_VALUE;
        if (l1.equalsIgnoreCase(l2)) return 7;
        
        /*
todo count by symbols diff 
Расстояние Левенштейна / сходство Джаро — Винклера / Расстояние Хэмминга / Расстояние Дамерау — Левенштейна 
, с учётом битых символов.
*/

        l1=l1.toUpperCase();
        l2=l2.toUpperCase();
        int deltaV1[]=new int[allowed.length];
        for (char c:l1.toCharArray()) {
            int p=Arrays.binarySearch(allowed, c);
            if (p>=0) deltaV1[p]++;
        }
        int deltaV2[]=new int[allowed.length];
        for (char c:l2.toCharArray()) {
            int p=Arrays.binarySearch(allowed, c);
            if (p>=0) deltaV2[p]++;
        }
        long s=0;
        for (int i=0;i<deltaV2.length;i++) {
            s+= Math.abs(deltaV1[i]-deltaV2[i]);
        }
        if (s>Integer.MAX_VALUE) return Integer.MAX_VALUE;
        s = s *300 / l1.length();
        s = s+10 + lenDiff/3;
        if (s>Integer.MAX_VALUE-1000) return Integer.MAX_VALUE-100;
        assert s>=0 && s<=Integer.MAX_VALUE;
        return (int)s;
    }
}
