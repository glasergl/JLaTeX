package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LatexCommandTest {
    @Test
    void testNewPageCommand() {
	final LatexCommand newPageCommand = LatexCommand.command("newpage");
	assertEquals("\\newpage", newPageCommand.toString());
    }
}
