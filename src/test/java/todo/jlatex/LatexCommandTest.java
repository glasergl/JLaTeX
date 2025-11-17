package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LatexCommandTest {
    @Test
    void testNewPageCommand() {
	final LatexCommand newPageCommand = LatexCommand.command("newpage");
	assertEquals("\\newpage", newPageCommand.toString());
    }

    @Test
    void testCommandWithMaximalArguments() {
	final LatexCommand latexCommand = LatexCommand.command("abcd", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	assertEquals("\\abcd{1}{2}{3}{4}{5}{6}{7}{8}{9}", latexCommand.toString());
    }
    
    @Test
    void testCommandWithBackslashPrependedAlready() {
	final LatexCommand latexCommand = LatexCommand.command("\\test", "1");
	assertEquals("\\test{1}", latexCommand.toString());
    }

    @Test
    void testIllegalCommands() {
	assertThrows(IllegalArgumentException.class, () -> {
	    LatexCommand.command("abcd", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
	});
	assertThrows(IllegalArgumentException.class, () -> {
	    LatexCommand.command("", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	});
	assertThrows(IllegalArgumentException.class, () -> {
	    LatexCommand.command("     ", "1", "2", "3", "4", "5", "6", "7", "8", "9");
	});
	assertThrows(IllegalArgumentException.class, () -> {
	    LatexCommand.command("", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
	});
    }
}
