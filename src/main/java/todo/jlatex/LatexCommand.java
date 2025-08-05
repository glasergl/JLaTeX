package todo.jlatex;

import java.util.Optional;

/**
 * Specific LatexLine that represents a LaTeX command.
 */
public final class LatexCommand extends LatexLine {
    /**
     * @param command          where toString() evaluates to the command name
     * @param optionalArgument where, when given, toString() on the value evaluates
     *                         to the option argument
     * @param arguments        where the toString() representation corresponds to
     *                         the argument
     * @see #command(Object, Object...)
     * @see #command(Object, Optional, Object...)
     */
    private LatexCommand(final Object command, final Optional<Object> optionalArgumentObject, final Object... arguments) {
	if (arguments.length > 9) {
	    throw new IllegalArgumentException("At most 9 arguments for a LaTeX command");
	}
	final String commandName = command.toString();
	final Optional<String> optionalArgument = optionalArgumentObject.isPresent() ? Optional.of(optionalArgumentObject.get().toString()) : Optional.empty();
	if (commandName.isEmpty() || commandName.isBlank()) {
	    throw new IllegalArgumentException("Command name cannot be empty");
	}
	addContent((commandName.charAt(0) == '\\' ? "" : "\\") + commandName);
	if (optionalArgument.isPresent()) {
	    addContent(String.format("[%s]", optionalArgument.get()));
	}
	for (final Object argument : arguments) {
	    addContent(String.format("{%s}", argument.toString()));
	}
    }

    /**
     * @param command   the first element of a command, i.e., \command, you may
     *                  prepend the backslash yourself, but it would also be
     *                  automatically prepended
     * @param arguments for each, a {argument} will be appended
     * @return Representation of \command{argument1}{argument2}...
     */
    public static LatexCommand command(final Object command, final Object... arguments) {
	return new LatexCommand(command, Optional.empty(), arguments);
    }

    /**
     * Adds an optional argument in square brackets before the first required
     * argument. If the optional argument is empty, this behaves exactly as
     * {@link #command(Object, Object...)}.
     * 
     * @param command
     * @param optionalArgumentObject
     * @param arguments
     * @return Representation of
     *         \commandName[optionalArgument]{argument1}{argument2}...
     * @see #command(Object, Object...)
     */
    public static LatexCommand command(final Object command, final Optional<Object> optionalArgumentObject, final Object... arguments) {
	return new LatexCommand(command, optionalArgumentObject, arguments);
    }
}
