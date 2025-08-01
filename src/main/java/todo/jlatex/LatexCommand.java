package todo.jlatex;

import java.util.Optional;

/**
 * Specific LatexLine that represents a LaTeX command.
 */
public final class LatexCommand extends LatexLine {
    /**
     * @see #get(String, String...)
     * @see #get(String, Optional, String...)
     */
    private LatexCommand(final String commandName, final Optional<String> optionalArgument, final String... arguments) {
	if (arguments.length > 9) {
	    throw new IllegalArgumentException("At most 9 arguments for a LaTeX command");
	}
	if (commandName.isEmpty() || commandName.isBlank()) {
	    throw new IllegalArgumentException("Command name cannot be empty");
	}
	addContent((commandName.charAt(0) == '\\' ? "" : "\\") + commandName);
	if (optionalArgument.isPresent()) {
	    addContent(String.format("[%s]", optionalArgument.get()));
	}
	for (final String argument : arguments) {
	    addContent(String.format("{%s}", argument));
	}
    }

    /**
     * @param commandName the first element of a command, i.e., \commandName, you
     *                    may prepend the backslash yourself, but it would also be
     *                    automatically prepended
     * @param arguments   for each, a {argument} will be appended
     * @return Representation of \commandName{argument1}{argument2}...
     */
    public static LatexCommand get(final String commandName, final String... arguments) {
	return new LatexCommand(commandName, Optional.empty(), arguments);
    }

    /**
     * Adds an optional argument in square brackets before the first required
     * argument. If the optional argument is empty, this behaves exactly as
     * {@link #get(String, String...)}.
     * 
     * @see #get(String, String...)
     * @return Representation of
     *         \commandName[optionalArgument]{argument1}{argument2}...
     */
    public static LatexCommand get(final String commandName, final Optional<String> optionalArgument, final String... arguments) {
	return new LatexCommand(commandName, optionalArgument, arguments);
    }
}
