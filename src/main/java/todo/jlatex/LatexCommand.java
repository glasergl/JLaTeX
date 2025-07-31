package todo.jlatex;

import java.util.Optional;

public final class LatexCommand extends LatexLine {
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

    public static LatexCommand get(final String commandName, final String... arguments) {
	return new LatexCommand(commandName, Optional.empty(), arguments);
    }

    public static LatexCommand get(final String commandName, final Optional<String> optionalArgument,
	    final String... arguments) {
	return new LatexCommand(commandName, optionalArgument, arguments);
    }
}
