package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class GeneratePdfTest {
    @Test
    void test() {
	final LatexDocument d = new LatexDocument("article");
	d.usePackage("babel", "english")
		.usePackage("amsmath")
		.usePackage("graphicx")
		.beginDocument()
		.line(LatexCommand.format("$%$", LatexCommand.get("frac", "1", "2")))
		.emptyLine()
		.line("I like Spaghetti with Tomato Sauce.")
		.line(LatexLine.format("My favourite command is either this $%$ or that $%$", LatexCommand.get("frac", "2\\pi", "3"), LatexCommand.get("textbf", "abcd")))
		.endDocument();
	final GeneratePdf l = new GeneratePdf(d);

	assertEquals(List.of(0, 0, 0), l.getExitCodes());
	assertTrue(Files.exists(Path.of("./generated-document.pdf")));
	try {
	    Files.delete(Path.of("./generated-document.pdf"));
	} catch (final IOException e) {
	    fail("Unabel to delete generated document during test");
	}
    }
}
