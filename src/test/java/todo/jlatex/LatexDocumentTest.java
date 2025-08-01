package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class LatexDocumentTest {
    @Test
    void test() {
	final LatexDocument d = new LatexDocument("article");
	d.usePackage("babel")
		.usePackage("amsmath")
		.beginDocument()
		.line(LatexCommand.get("frac", "1", "2"))
		.emptyLine()
		.line("I like Spaghetti with Tomato Sauce.")
		.line(LatexLine.format("My favourite command is either this $%$ or that $%$", LatexCommand.get("frac", "2\\pi", "3"), LatexCommand.get("textbf", "abcd")))
		.beginEnvironment("figure", Optional.of("h"))
		.line(LatexCommand.get("includegraphics", Optional.of("width=0.9\\textwidth"), "./path/to/figure.png"))
		.endEnvironment("figure")
		.endDocument();
	final String latex = d.toString();
	assertTrue(latex.contains("\\usepackage{babel}"));
	assertTrue(latex.contains("\\usepackage{amsmath}"));
	assertTrue(latex.contains("\\frac{1}{2}"));
	assertTrue(latex.contains("My favourite command is either this $\\frac{2\\pi}{3}$ or that $\\textbf{abcd}$"));
    }
}
