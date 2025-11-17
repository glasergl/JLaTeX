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
}
