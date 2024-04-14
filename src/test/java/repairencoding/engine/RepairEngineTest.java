package repairencoding.engine;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

public class RepairEngineTest {
    
    public RepairEngineTest() {
    }

    List<TFile> makeTestData() {
        ArrayList<TFile> historySeq = new ArrayList();
        historySeq.add(new TFile("file 1.txt", 1000L));
        historySeq.add(new TFile("file 2.txt", 1230L));
        {
            List<TLine> lines=historySeq.get(0).lines=new ArrayList<>();
            for (int i=0;i<12;i++) {
                TLine l=new TLine();
                l.hasBroken=false;
                if (i>=5 && i<7) {
                    l.line=" sample "+i+" часть,текст.";
                } else {
                    l.line=" sample "+i;
                }
                l.prePos=TConst.UNDEFINED;
                l.pos=i;
                l.nextPos=i;
                lines.add(l);
            }
        }
        {
            List<TLine> lines=historySeq.get(1).lines=new ArrayList<>();
            for (int i=0;i<12;i++) {
                TLine l=new TLine();
                l.prePos=i;
                l.pos=i;
                l.nextPos=TConst.UNDEFINED;
                if (i==1) {
                    l.hasBroken=false;
                    l.line=" fixed line . i="+i;
                } else if (i>=5 && i<7) {
                    l.hasBroken=true;
                    l.line=" sample "+i+" ?????,????? ";
                } else {
                    l.hasBroken=false;
                    l.line="sample "+i;
                }
                lines.add(l);
            }
        }
        return historySeq;
    }
    
    /**
     * Test of needRepair method, of class RepairEngine.
     */
    @Test
    public void testNeedRepair_List_int() {
        System.out.println("needRepair");
        RepairEngine instance = new RepairEngine();
        List<TFile> historySeq= makeTestData();
        
        assertFalse(instance.needRepair(historySeq, 0));
        assertTrue(instance.needRepair(historySeq, 1));
    }


    /**
     * Test of repair method, of class RepairEngine.
     */
    @Test
    public void testRepair() {
        System.out.println("repair");
        RepairEngine instance = new RepairEngine();
        List<TFile> historySeq= makeTestData();
        
        List<String> result = instance.repair(historySeq, 1);
        assertEquals("[sample 0,  fixed line . i=1, sample 2, sample 3, sample 4,  sample 5 часть,текст.,  sample 6 часть,текст., sample 7, sample 8, sample 9, sample 10, sample 11]",
                result.toString());
    }
    
}
