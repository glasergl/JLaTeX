package todo.jlatex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class that represents an entire LaTeX document consisting of a list of
 * LatexLine objects. One may extend this class to add further convenience
 * methods for your use case.
 */
public class LatexDocument {
    /**
     * List of lines where between each a new line character will be placed when the
     * document is built
     */
    protected final List<LatexLine> lines = new ArrayList<>();

    /**
     * Initializes an empty LaTeX document with the given document class
     * 
     * @param documentClass
     */
    public LatexDocument(final String documentClass) {
	this(documentClass, Optional.empty());
    }

    /**
     * Initializes an empty LaTeX document with the given document class and adds
     * the options in square brackets
     * 
     * @param documentClass
     * @param documentClassOptions
     */
    public LatexDocument(final String documentClass, final Object documentClassOptions) {
	this(documentClass, Optional.of(documentClassOptions));
    }

    /**
     * @param documentClass
     * @param documentClassOptions
     * @see #LatexDocument(String)
     * @see #LatexDocument(String, Object)
     */
    protected LatexDocument(final String documentClass, final Optional<Object> documentClassOptions) {
	line(LatexCommand.command("documentclass", documentClassOptions, documentClass));
    }

    /**
     * @param command - to be appended
     * @return This for method chaining
     */
    public final LatexDocument line(final LatexCommand command) {
	lines.add(command);
	return this;
    }

    /**
     * @param string          - will be split according to \n and then all %
     *                        replaced by the given objects, e.g., commands, numbers
     *                        etc.
     * @param insertedObjects which will be inserted for % symbols, i.e., their
     *                        toString() representation
     * @return This for method chaining
     */
    public final LatexDocument format(final String string, final Object... insertedObjects) {
	final String[] lines = string.split("\n");
	int commandCounter = 0;
	for (final String line : lines) {
	    final LatexLine formattedLine = new LatexLine();
	    for (final char character : line.toCharArray()) {
		if (character != '%') {
		    formattedLine.addContent(String.valueOf(character));
		} else if (commandCounter >= insertedObjects.length) {
		    throw new IllegalArgumentException("Not enough commands");
		} else {
		    formattedLine.addContent(insertedObjects[commandCounter].toString());
		    commandCounter++;
		}
	    }
	    this.lines.add(formattedLine);
	}
	return this;
    }

    /**
     * @return This for method chaining
     */
    public final LatexDocument emptyLine() {
	this.lines.add(LatexLine.emptyLine());
	return this;
    }

    /**
     * @param packageName to be added with usepackage (without optional argument)
     * @return This for method chaining
     */
    public final LatexDocument usePackage(final String packageName) {
	line(LatexCommand.command("usepackage", packageName));
	return this;
    }

    /**
     * Overloaded method with optional argument to be added in square brackets
     * 
     * @param packageName
     * @param optionalArgument
     * @return This for method chaining
     * @see #usePackage(String)
     */
    public final LatexDocument usePackage(final String packageName, final Object optionalArgument) {
	line(LatexCommand.command("usepackage", Optional.of(optionalArgument), packageName));
	return this;
    }

    /**
     * Adds the \begin{environmentName}{requiredArgument1}{requiredArgument2}...
     * command
     * 
     * @param environmentName which must not be empty
     * @param arguments       list of argument added in curly brackets
     * @return This for method chaining
     * @throws IllegalArgumentException If the environmentName is empty
     */
    public final LatexDocument beginEnvironment(final String environmentName, final Object... arguments) {
	if (environmentName.isEmpty() || environmentName.isBlank()) {
	    throw new IllegalArgumentException("Environment name must not be empty");
	}
	line(LatexCommand.command(String.format("begin{%s}", environmentName), arguments));
	return this;
    }

    /**
     * Adds an optional argument after the environment name, i.e.,
     * \begin{environmentName}[optionalArgument]{requiredArgument1} etc. If the
     * optional argument is empty, the method behaves exactly as
     * {@link #beginEnvironment(String, String...)}.
     * 
     * @param environmentName
     * @param optionalArgument
     * @param arguments
     * @return This for method chaining
     * @see #beginEnvironment(String, String...)
     */
    public final LatexDocument beginEnvironment(final String environmentName, final Optional<Object> optionalArgument, final Object... arguments) {
	if (environmentName.isEmpty() || environmentName.isBlank()) {
	    throw new IllegalArgumentException("Environment name must not be empty");
	}
	line(LatexCommand.command(String.format("begin{%s}", environmentName), optionalArgument, arguments));
	return this;
    }

    /**
     * Adds the \end{environmentName} command to the document
     * 
     * @param environmentName which must not be empty
     * @return This for method chaining
     */
    public final LatexDocument endEnvironment(final String environmentName) {
	if (environmentName.isEmpty() || environmentName.isBlank()) {
	    throw new IllegalArgumentException("Environment name must not be empty");
	}
	line(LatexCommand.command("end", environmentName));
	return this;
    }

    /**
     * Convenience method to add the \begin{document} command
     * 
     * @return This for method chaining
     */
    public final LatexDocument beginDocument() {
	return beginEnvironment("document");
    }

    /**
     * Convenience method to add the \end{document} command
     * 
     * @return This for method chaining
     */
    public final LatexDocument endDocument() {
	return endEnvironment("document");
    }

    /**
     * @return Entire document as string by appending the content of each line (in
     *         order) and placing the system's default line separator in between.
     */
    @Override
    public final String toString() {
	final StringBuilder content = new StringBuilder();
	for (final LatexLine line : lines) {
	    content.append(line.toString() + System.lineSeparator());
	}
	return content.toString();
    }
}
