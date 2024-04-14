package repairencoding.types;

import java.util.List;

/**
 * File representation
 * TODO: make more abstraction and API: getter/setter
 * 
 * @author Alexey K. (PlayerO1)
 */
public class TFile {
    public String name;
    public long time;
    public String encoding;
    public List<TLine> lines;
    
    
    
    // --- API ---

    public TFile(String name, long time) {
        this.name = name;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<TLine> getLines() {
        return lines;
    }

    public void setLines(List<TLine> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "TFile{name=\"" + name + "\", time=" + time + ", encoding=\"" + encoding + "\", "+
                (lines==null?null:lines.size())+" lines}";
    }
    
    
}
