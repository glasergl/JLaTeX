package de.glasergl.jlatex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.glasergl.jlatex.GeneratePdf;
import de.glasergl.jlatex.LatexCommand;
import de.glasergl.jlatex.LatexDocument;
import de.glasergl.jlatex.ProcessResult;

class GeneratePdfTest {
	@Test
	void testNormalExecution() {
		final LatexDocument d = new LatexDocument("article");
		d.usePackage("babel", "english").usePackage("amsmath").usePackage("graphicx").beginDocument().format("$%$", LatexCommand.command("frac", "1", "2")).emptyLine().format("I like Spaghetti with Tomato Sauce.")
				.format("My favourite command is either this $%$ or that $%$", LatexCommand.command("frac", "2\\pi", "3"), LatexCommand.command("textbf", "abcd")).endDocument();
		final GeneratePdf l = new GeneratePdf(d);

		for (final ProcessResult latexCompilerProcessResult : l.getProcessResults()) {
			assertEquals(0, latexCompilerProcessResult.exitCode());
		}
		assertEquals(3, l.getProcessResults().size());
		assertTrue(l.wasSuccessful());
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
		d.beginDocument().format("Hello").endDocument();
		assertThrows(IllegalStateException.class, () -> {
			new GeneratePdf(d, "generated-document", "pdflatex42", 4, "build");
		});
		assertFalse(Files.exists(Path.of("./generated-document.pdf")));
		assertFalse(Files.exists(Path.of("./build/")));
		assertFalse(Files.exists(Path.of("./generated-document.tex")));
	}

	@Test
	void testErrorExecutionWithIllegalLatexCode() {
		final LatexDocument d = new LatexDocument("article").beginDocument().emptyLine();
		final GeneratePdf l = new GeneratePdf(d);
		assertEquals(1, l.getProcessResults().size());
		for (final ProcessResult latexCompilerProcessResult : l.getProcessResults()) {
			final int exitCode = latexCompilerProcessResult.exitCode();
			assertNotEquals(0, exitCode);
		}
		assertFalse(Files.exists(Path.of("./generated-document.pdf")));
		assertFalse(Files.exists(Path.of("./build/")));
		assertFalse(Files.exists(Path.of("./generated-document.tex")));
	}

	@Test
	void testLatexCompilerIsInstalled() {
		assertTrue(GeneratePdf.latexCompilerIsInstalled("pdflatex"));
		assertFalse(GeneratePdf.latexCompilerIsInstalled("abcdlatex"));
		assertThrows(IllegalArgumentException.class, () -> {
			GeneratePdf.latexCompilerIsInstalled("");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			GeneratePdf.latexCompilerIsInstalled("     ");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			GeneratePdf.latexCompilerIsInstalled("\t   ");
		});
	}
}
