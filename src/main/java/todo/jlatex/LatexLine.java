package todo.jlatex;

/**
 * A LatexLine is the base component to build a LaTeX document.
 */
public class LatexLine {
    private final StringBuilder contentBuilder = new StringBuilder();

    /**
     * @param content to be appended
     * @throws IllegalArgumentException If the content contains a new line
     *                                  character, because a LatexLine represents a
     *                                  single line
     */
    protected final void addContent(final String content) {
	if (content.contains("\n")) {
	    throw new IllegalArgumentException("A line must not contain the new line character");
	}
	contentBuilder.append(content);
    }

    /**
     * @return The full content of all appended content
     */
    @Override
    public String toString() {
	return contentBuilder.toString();
    }

    /**
     * @return A LatexLine which corresponds to an empty line
     */
    public static LatexLine emptyLine() {
	return new LatexLine();
    }

    /**
     * Formats the given string by replacing all '%' characters in it by the content
     * of the given commands (in order)
     * 
     * @param string     with '%' characters to be replaced by the content of latex
     *                   commands
     * @param otherLines to be inserted in the string
     * @return LatexLine that contains the content of the given string where each
     *         '%' character got replaced by the content of the corresponding
     *         LatexCommand
     * @throws IllegalArgumentException If the amount of '%' characters in the
     *                                  string is more than the given commands.
     */
    public static LatexLine f(final String string, final LatexLine... otherLines) {
	final LatexLine formattedLine = new LatexLine();
	int commandCounter = 0;
	for (final char character : string.toCharArray()) {
	    if (character != '%') {
		formattedLine.addContent(String.valueOf(character));
	    } else if (commandCounter >= otherLines.length) {
		throw new IllegalArgumentException("Not enough commands");
	    } else {
		formattedLine.addContent(otherLines[commandCounter].toString());
		commandCounter++;
	    }
	}
	return formattedLine;
    }
}
