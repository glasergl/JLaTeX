package todo.jlatex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LatexDocument {
    protected final List<LatexLine> lines = new ArrayList<>();

    public LatexDocument(final String documentClass) {
	this(documentClass, Optional.empty());
    }

    public LatexDocument(final String documentClass, final String documentClassOptions) {
	this(documentClass, Optional.of(documentClassOptions));
    }

    protected LatexDocument(final String documentClass, final Optional<String> documentClassOptions) {
	addLine(LatexCommand.get("documentclass", documentClassOptions, documentClass));
    }

    public final LatexDocument addLine(final LatexLine line) {
	lines.add(line);
	return this;
    }

    public final LatexDocument addLine(final String line) {
	final LatexLine sentence = new LatexLine();
	sentence.addContent(line);
	addLine(sentence);
	return this;
    }

    public final LatexDocument usePackage(final String packageName) {
	addLine(LatexCommand.get("usepackage", packageName));
	return this;
    }

    public final LatexDocument usePackage(final String packageName, final String options) {
	addLine(LatexCommand.get("usepackage", Optional.of(options), packageName));
	return this;
    }

    public final LatexDocument beginDocument() {
	addLine(LatexCommand.get("begin", "document"));
	return this;
    }

    public final LatexDocument emptyLine() {
	addLine(LatexLine.newLine());
	return this;
    }

    public final LatexDocument endDocument() {
	addLine(LatexCommand.get("end", "document"));
	return this;
    }

    @Override
    public final String toString() {
	final StringBuilder content = new StringBuilder();
	for (final LatexLine line : lines) {
	    content.append(line.toString() + System.lineSeparator());
	}
	return content.toString();
    }
}
