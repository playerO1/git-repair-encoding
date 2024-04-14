package repairencoding.engine;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import repairencoding.types.TConst;
import repairencoding.types.TFile;
import repairencoding.types.TLine;

public class CompareEngineTest {
    
    public CompareEngineTest() {
    }

    /**
     * Test of markBrokenLines method, of class CompareEngine.
     */
    @Test
    public void testMarkBrokenLines() {
        System.out.println("markBrokenLines");
        CompareEngine instance = new CompareEngine();
        TFile inFile = new TFile("name", 0);
        inFile.encoding=TConst.DEFAULT_ENODING;
        inFile.lines=Arrays.asList(new TLine(),new TLine(),new TLine());


        inFile.lines.get(0).line=" test of ?????? ?? ?";
        inFile.lines.get(1).line=" test 2 ????? ??????";
        inFile.lines.get(2).line=" test ????? ??? ??";
        
        instance.markBrokenLines(inFile);
        
        assertTrue(inFile.lines.get(0).hasBroken);
        assertTrue(inFile.lines.get(1).hasBroken);
        assertTrue(inFile.lines.get(2).hasBroken);



        inFile.lines.get(0).line=" test of Кодировка UTF-8";
        inFile.lines.get(1).line=" test 2 или виндовс 1251";
        inFile.lines.get(2).line=" test правильная строка";

        instance.markBrokenLines(inFile);
        
        assertFalse(inFile.lines.get(0).hasBroken);
        assertFalse(inFile.lines.get(1).hasBroken);
        assertFalse(inFile.lines.get(2).hasBroken);
    }


    /**
     * Test of checkBrokenLine method, of class CompareEngine.
     */
    @Test
    public void testCheckBrokenLine() {
        System.out.println("checkBrokenLine");
        CompareEngine instance = new CompareEngine();
        assertFalse(instance.checkBrokenLine(""));
        assertFalse(instance.checkBrokenLine(" "));
        assertFalse(instance.checkBrokenLine("  -- comment --"));
        assertFalse(instance.checkBrokenLine("<!DOCTYPE HTML> <!-- comment -->"));
        assertFalse(instance.checkBrokenLine("<li><a href='#_table_1txt'>1.txt</a>"));
        assertFalse(instance.checkBrokenLine(" insert into user (id,name) values (1,'ПОЛЬЗОВАТЕЛЬ первый');"));
        assertFalse(instance.checkBrokenLine(" insert into user (id,name,password) values (?,?,?);"));
        assertFalse(instance.checkBrokenLine(" select inot table(values)..."));
        
        assertTrue(instance.checkBrokenLine("\" INSERT  INTO user( id, name ) values (1,'???????????? ??????')\""));
        assertTrue(instance.checkBrokenLine(" select * from table where name like 'Ïðèìåð òåêñòà'"));
        assertTrue(instance.checkBrokenLine("test ������ symbols not pass"));
        
        //todo more test case with broken encoding
    }

    /**
     * Test of stringDiff method, of class CompareEngine.
     */
    @Test
    public void testStringDiff() {
        System.out.println("stringDiff");
        CompareEngine instance = new CompareEngine();

        assertEquals(0, instance.stringDiff("", ""));
        assertEquals(0, instance.stringDiff(" ", " "));
        assertEquals(2, instance.stringDiff("   ", ""));
        assertEquals(2, instance.stringDiff("", "   "));
        assertEquals(0, instance.stringDiff("test", "test"));
        assertEquals(2, instance.stringDiff(" test", "test "));
        assertEquals(1, instance.stringDiff("test", "Test"));
        assertEquals(3, instance.stringDiff(" T esT", "  t ES t"));
        
        assertEquals(177, instance.stringDiff(" test case", " test "));
        assertEquals(391, instance.stringDiff("SELECT FROM", "   UPDATE INTO"));
        assertEquals(Integer.MAX_VALUE, instance.stringDiff("Hello world!", "Привет мир!"));
        assertEquals(10, instance.stringDiff("select inot table(values)", "select into table(values)"));
        assertEquals(85, instance.stringDiff("  delete from employer;", "   select * from employer;"));
        
        assertEquals(33, instance.stringDiff("  SELCT * from user where id=2", " select * from user where id =2"));
        
        // with broket char's, should ignore broken char's:
        assertEquals(26, instance.stringDiff(" insert into user (id,name) values (1,'ПОЛЬЗОВАТЕЛЬ первый');", " INSERT  INTO user( id, name ) values (1,'???????????? ??????');"));
        
        // barrier test
        
        assertTrue(instance.DIFF_TRESHOLD_1<instance.stringDiff("  create table employeer();", "   drop table employeer;"));
        assertTrue(instance.DIFF_TRESHOLD_1<instance.stringDiff("  delete from employer;", "   select * from employer;"));
        assertTrue(instance.stringDiff("  SELCT * from user where id=2", " select * from user where id =2")<instance.DIFF_TRESHOLD_1);
        assertTrue(instance.stringDiff("  insert into user (id,name) values (1,'ПОЛЬЗОВАТЕЛЬ первый')", " INSERT  INTO user( id, name ) values (1,'???????????? ??????')")<instance.DIFF_TRESHOLD_1);
        
    }
    
}
