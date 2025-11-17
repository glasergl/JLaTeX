package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class LatexDocumentTest {
    @Test
    void test() {
	final LatexDocument d = new LatexDocument("article");
	d.usePackage("babel", "english")
		.usePackage("amsmath")
		.beginDocument()
		.format("$%$", LatexCommand.command("frac", "1", "2"))
		.emptyLine()
		.format("I like Spaghetti with Tomato Sauce.")
		.format("My favourite command is either this $%$ or that $%$", LatexCommand.command("frac", "2\\pi", "3"), LatexCommand.command("textbf", "abcd"))
		.beginEnvironment("figure", Optional.of("h"))
		.line(LatexCommand.command("includegraphics", Optional.of("width=0.9\\textwidth"), "./path/to/figure.png"))
		.endEnvironment("figure")
		.endDocument();
	final String latex = d.toString();
	assertTrue(latex.contains("\\usepackage[english]{babel}"));
	assertTrue(latex.contains("\\usepackage{amsmath}"));
	assertTrue(latex.contains("\\frac{1}{2}"));
	assertTrue(latex.contains("My favourite command is either this $\\frac{2\\pi}{3}$ or that $\\textbf{abcd}$"));
	assertTrue(latex.contains("\\begin{figure}[h]"));
	assertTrue(latex.contains("\\includegraphics[width=0.9\\textwidth]{./path/to/figure.png}"));
	assertTrue(latex.contains("\\end{figure}"));
    }

    @Test
    void testFormatWithMoreLines() {
	final LatexDocument d = new LatexDocument("article");
	d.beginDocument().format("""
		abcd
		abasd123

		$%$
				""", LatexCommand.command("frac", 1, 2));
	d.endDocument();
	final String latex = d.toString();
	final String[] lines = latex.split(System.lineSeparator());
	assertEquals(7, lines.length);
	assertEquals("\\documentclass{article}", lines[0]);
	assertEquals("\\begin{document}", lines[1]);
	assertEquals("abcd", lines[2]);
	assertEquals("abasd123", lines[3]);
	assertEquals("", lines[4]);
	assertEquals("$\\frac{1}{2}$", lines[5]);
	assertEquals("\\end{document}", lines[6]);
    }

    @Test
    void testFormatWithDoublePercent() {
	final LatexDocument d = new LatexDocument("book");
	d.beginDocument().format("""
		%% asd
		%%abcd
		$%$
				""", LatexCommand.command("frac", 1, 2));
	d.endDocument();
	final String latex = d.toString();
	final String[] lines = latex.split(System.lineSeparator());
	assertEquals(6, lines.length);
	assertEquals("\\documentclass{book}", lines[0]);
	assertEquals("\\begin{document}", lines[1]);
	assertEquals("% asd", lines[2]);
	assertEquals("%abcd", lines[3]);
	assertEquals("$\\frac{1}{2}$", lines[4]);
	assertEquals("\\end{document}", lines[5]);
    }

    @Test
    void testFormatWithNotEnoughCommands() {
	final LatexDocument d = new LatexDocument("book");
	assertThrows(IllegalArgumentException.class, () -> {
	    d.beginDocument().format("""
	    	%% asd
	    	$%$
	    			""");
	});
    }

    @Test
    void testFormatWithTooManyCommands() {
	final LatexDocument d = new LatexDocument("scrbook", "12pt");
	d.beginDocument().format("""
		%%abcd
		$%$%%
				""", LatexCommand.command("frac", 1, 2), LatexCommand.command("textbf", "test"));
	d.endDocument();
	final String latex = d.toString();
	final String[] lines = latex.split(System.lineSeparator());
	assertEquals(5, lines.length);
	assertEquals("\\documentclass[12pt]{scrbook}", lines[0]);
	assertEquals("\\begin{document}", lines[1]);
	assertEquals("%abcd", lines[2]);
	assertEquals("$\\frac{1}{2}$%", lines[3]);
	assertEquals("\\end{document}", lines[4]);
    }

    @Test
    void testPlain() {
	final LatexDocument d = new LatexDocument("book");
	d.beginDocument().plain("""
		%%abcd
		$%$
				""");
	d.endDocument();
	final String latex = d.toString();
	final String[] lines = latex.split(System.lineSeparator());
	assertEquals(5, lines.length);
	assertEquals("\\documentclass{book}", lines[0]);
	assertEquals("\\begin{document}", lines[1]);
	assertEquals("%%abcd", lines[2]);
	assertEquals("$%$", lines[3]);
	assertEquals("\\end{document}", lines[4]);
    }

    @Test
    void testIllegalBeginEnvironment() {
	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("");
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("  \t   ");
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("  \t   ", Optional.of("abcd"));
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("", Optional.of("abcd"));
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("", Optional.empty());
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("abcd", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
	});
    }

    @Test
    void testIllegalEndEnvironments() {
	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("abcd", "1", "2", "3", "4", "5", "6", "7", "8", "9").endEnvironment("");
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("abcd", "1", "2", "3", "4", "5", "6", "7", "8", "9").endEnvironment("    ");
	});

	assertThrows(IllegalArgumentException.class, () -> {
	    new LatexDocument("book").beginEnvironment("abcd", "1", "2").endEnvironment("\n");
	});
    }
}
