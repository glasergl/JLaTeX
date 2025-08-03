package todo.jlatex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import todo.jlatex.GeneratePdf.ProcessResult;

class GeneratePdfTest {
    @Test
    void testNormalExecution() {
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
	final GeneratePdf l = new GeneratePdf(d, 20L);

	for (final ProcessResult latexCompilerProcessResult : l.getProcessResults()) {
	    assertEquals(Optional.of(0), latexCompilerProcessResult.exitCode());
	}
	assertTrue(Files.exists(Path.of("./generated-document.pdf")));
	assertFalse(Files.exists(Path.of("./build/")));
	assertFalse(Files.exists(Path.of("./generated-document.tex")));
	try {
	    Files.delete(Path.of("./generated-document.pdf"));
	} catch (final IOException e) {
	    fail("Unable to delete generated document during test");
	}
    }

    @Test
    void testErrorExecutionWithNonExistingCompiler() {
	final LatexDocument d = new LatexDocument("article");
	d.beginDocument().line("Hello").endDocument();
	final GeneratePdf l = new GeneratePdf(d, "generated-document", "pdflatex42", 4, "build", 7L);
	assertTrue(l.getProcessResults().isEmpty());
	assertFalse(Files.exists(Path.of("./generated-document.pdf")));
	assertFalse(Files.exists(Path.of("./build/")));
	assertFalse(Files.exists(Path.of("./generated-document.tex")));
    }

    @Test
    void testErrorExecutionWithTooShortTimeout() {
	final LatexDocument d = new LatexDocument("article");
	d.beginDocument();
	for (int i = 0; i < 1_000_000; i++) {
	    d.line(LatexLine.format("$i=%$", LatexCommand.get("frac", "1", String.valueOf(i))));
	}
	d.endDocument();
	final GeneratePdf l = new GeneratePdf(d, 1L);
	assertEquals(1, l.getProcessResults().size());
	assertTrue(l.getProcessResults().get(0).exitCode().isEmpty());
	assertFalse(Files.exists(Path.of("./generated-document.pdf")));
	assertFalse(Files.exists(Path.of("./build/")));
	assertFalse(Files.exists(Path.of("./generated-document.tex")));
    }

    @Test
    void testErrorExecutionWithIllegalLatexCode() {
	final LatexDocument d = new LatexDocument("article").beginDocument().emptyLine();
	final GeneratePdf l = new GeneratePdf(d, 10L);
	assertEquals(1, l.getProcessResults().size());
	for (final ProcessResult latexCompilerProcessResult : l.getProcessResults()) {
	    final Optional<Integer> exitCode = latexCompilerProcessResult.exitCode();
	    assertTrue(exitCode.isEmpty() || exitCode.get() != 0);
	}
	assertFalse(Files.exists(Path.of("./generated-document.pdf")));
	assertFalse(Files.exists(Path.of("./build/")));
	assertFalse(Files.exists(Path.of("./generated-document.tex")));
    }
}
