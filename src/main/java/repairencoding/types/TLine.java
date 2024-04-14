package repairencoding.types;

/**
 *
 * @author Alexey K. (PlayerO1)
 */
public class TLine {
    public int pos;
    public String line;
    
    public boolean hasBroken;
    public int prePos;
    public int nextPos;

    
    
    /// --- API ---
    @Override
    public String toString() {
        return "TLine{" + "pos=" + pos + ", line=" + line + ", hasBroken=" + hasBroken + ", prePos=" + prePos + ", nextPos=" + nextPos + '}';
    }
    
}
