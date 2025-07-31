package todo.jlatex;

public class LatexLine {
    private final StringBuilder contentBuilder = new StringBuilder();

    protected final void addContent(final String content) {
	if (content.contains("\\n")) {
	    throw new IllegalArgumentException("A line must not contain the new line character");
	}
	contentBuilder.append(content);
    }

    @Override
    public String toString() {
	return contentBuilder.toString();
    }

    public static LatexLine newLine() {
	return new LatexLine();
    }

    public static LatexLine format(final String sentence, final LatexCommand... commands) {
	final LatexLine formattedLine = new LatexLine();
	int commandCounter = 0;
	for (final char character : sentence.toCharArray()) {
	    if (character != '%') {
		formattedLine.addContent(String.valueOf(character));
	    } else if (commandCounter >= commands.length) {
		throw new IllegalArgumentException("Not enough commands");
	    } else {
		formattedLine.addContent(commands[commandCounter].toString());
		commandCounter++;
	    }
	}
	return formattedLine;
    }
}
