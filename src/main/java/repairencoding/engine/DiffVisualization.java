package repairencoding.engine;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

/**
 * Show Diff from versions of files
 * @author Alexey K. (PlayerO1)
 */
public class DiffVisualization {
    protected boolean useAnchor;
    protected List<TFile> historySeq;

    public DiffVisualization(List<TFile> historySeq, boolean useAnchor) {
        this.historySeq = historySeq;
        this.useAnchor=useAnchor;
    }


    public void writeToHTML(OutputStream os) {
        Map<String,String> anhorIndex=null;
        if (useAnchor) {
            anhorIndex=historySeq.stream()
                    .collect(Collectors.toMap(TFile::getName, (TFile f)->toAnchorStr(f.getName())));
        }
        try (PrintWriter w=new PrintWriter(os, false, Charset.forName(TConst.DEFAULT_ENODING))) {
            w.println("<!DOCTYPE html>"); // todo maybe HTML4?
            w.println("<html>");
            w.println("<head>");
            w.println("<title>Diff history item report</title>");
            w.println("  <style type=\"text/css\">");
            w.println("    .file_content{border: 2px solid powderblue;padding: 4pt;}");
            w.println("    table.file_content tr:hover{background-color:lightgray;}");
            w.println("    tr:target {background-color: #ffb;}");
            w.println("    .broken_line{color:red;}");
            w.println("  </style>");
            //todo style
            w.println("</head><body>");
            w.println("<div>");
            w.println("<h1>Diff report.</h1>");
            w.println("<p>Show diff between files, linked line number.</p>");
            w.println("<p>Files: <ul>");
            for (TFile item:historySeq) {
                String name=escape(item.name);
                if (useAnchor) {
                    name=String.format("<a href='#_table_%s'>%s</a>", toAnchorStr(name), name);
                }
                w.println(String.format("<li>%s   (modified %s, %s encoding)</li>",
                        name, item.time, escape(item.encoding)));
            }
            w.println("</ul></p>");
            w.println("</div>");
            
            w.println("<div>");
            //todo data
            for (int itemI=0;itemI<historySeq.size();itemI++) {
                TFile item=historySeq.get(itemI);
                String preFileName=itemI>0?historySeq.get(itemI-1).name:null;
                String nextFileName=itemI+1<historySeq.size()?historySeq.get(itemI+1).name:null;
                if (useAnchor) {
                    w.println(String.format("File <span id=\"_table_%s\">&laquo;%s&raquo;</span> (%s)"
                            , anhorIndex.get(item.name),item.name, item.encoding));
                } else {
                    w.println(String.format("File &laquo;%s&raquo; (%s)", item.name, item.encoding));
                }
                w.println("<table class=\"file_content\">"
                        + "<tr><th>pre n</th><th>n</th><th>text line</th><th>next n</th></tr>");
                for (int i=0;i<item.lines.size();i++) {
                    TLine lineI=item.lines.get(i);
                    if (useAnchor) {
                        String anhor=anhorIndex.get(item.name)+"_"+(lineI.pos+1);
                        if (lineI.hasBroken) {
                            w.print(String.format("<tr id=\"%s\" class=\"broken_line\">", anhor));
                        } else {
                            w.print(String.format("<tr id=\"%s\">", anhor));
                        }
                    } else {
                        if (lineI.hasBroken) {
                            w.print("<tr class=\"broken_line\">");
                        } else {
                            w.print("<tr>");
                        }
                    }
                    w.print(String.format("<td title=\"%s\">", preFileName));
                    if (lineI.prePos!=TConst.UNDEFINED) {
                        if (useAnchor) {
                            int p=lineI.prePos+1;
                            w.print(String.format("<a href='#%s_%d'>%d</a>", anhorIndex.get(preFileName),p,p));
                        } else {
                            w.print(lineI.prePos+1);
                        }
                    }
                    w.print("</td><td title=\"this\">");
                    w.print(lineI.pos+1);
                    if (lineI.pos!=i) {
                        w.print("("+i+1+")");
                    }
                    w.print("</td>");
                    
                    w.print("<td>");w.print(escape(lineI.line));w.print("</td>");
                    
                    w.print(String.format("<td title=\"%s\">", nextFileName));
                    if (lineI.nextPos!=TConst.UNDEFINED) {
                        if (useAnchor) {
                            int p=lineI.nextPos+1;
                            w.print(String.format("<a href='#%s_%d'>%d</a>", anhorIndex.get(nextFileName),p,p));
                        } else {
                            w.print(lineI.nextPos+1);
                        }
                    }
                    w.println("</td></tr>");
                }
                w.println("</table>");
            }
            w.println("</div>");
            
            w.println("</body></html>");
        }
    }
        
    private String toAnchorStr(String name) {
        name=name.replace(" ","_").replace(".","_").replace("<","_").replace(">","_").replace("&","_")
                .replace("'","_").replace("\"","_");
        name=name.replace("_", "");
        return name;
    }

    private String escape(String s) {
        s=s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        s=s.replace("\"", "&quot;").replace("'", "&quot;"); // todo check HTML
        return s;
    }
}
