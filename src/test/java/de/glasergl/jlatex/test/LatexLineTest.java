package de.glasergl.jlatex.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import de.glasergl.jlatex.LatexLine;

class LatexLineTest {
	@Test
	void testInitialization() {
		final LatexLine latexLine = new LatexLine();
		assertEquals("", latexLine.toString());
	}

	@Test
	void testAddContent() {
		final String testContent = "test 123 42 hello";
		final LatexLine latexLine = new LatexLine();
		latexLine.addContent(testContent);
		assertEquals(testContent, latexLine.toString());
	}

	@Test
	void testAddContentWithLineBreak() {
		final LatexLine latexLine = new LatexLine();
		assertThrows(IllegalArgumentException.class, () -> {
			latexLine.addContent("abcd\n");
		});
	}
}
