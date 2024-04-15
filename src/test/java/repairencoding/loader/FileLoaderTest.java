package repairencoding.loader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import repairencoding.types.TFile;

public class FileLoaderTest {
    
    public FileLoaderTest() {
    }


    /**
     * Test of load method, of class FileLoader.
     * Check file order, encoding detection, readed content.
     */
    @Test
    public void testLoad() throws Exception {
        String sampleDir="src/test/resources/sample_text";
        Path p=Paths.get(sampleDir);
        assertTrue(Files.exists(p));
        
        FileLoader instance = new FileLoader(p);
        List<TFile> result = instance.load();
        assertEquals(2, result.size());
        {
            TFile f=result.get(0);
            assertEquals("first 1.txt", f.name);
            assertEquals("UTF-8", f.encoding);
            assertEquals("[TLine{pos=0, line=This is text, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=1, line=Это текст., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=2, line=UTF-8, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=3, line=Line 7/2, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=4, line=Line 5, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=5, line=Line 8., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=6, line=Line 9 Пример текста тут?, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=7, line=10., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}]",
                    f.lines.toString());
        }
        {
            TFile f=result.get(1);
            assertEquals("next 2.txt", f.name);
            assertEquals("windows-1251", f.encoding);
            assertEquals("[TLine{pos=0, line=Пример текста., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=1, line=, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=2, line=windows-1251, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=3, line=Line 7/2, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=4, line=Line 8., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=5, line=Line 9. ?????? ?????? ????, hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}, TLine{pos=6, line=10.., hasBroken=false, prePos=-2147483648, preK=0, nextPos=-2147483648}]",
                    f.lines.toString());
        }
    }
    
}
