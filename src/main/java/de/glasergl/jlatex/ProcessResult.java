package de.glasergl.jlatex;

/**
 * Class to represent the required information of a process execution
 */
public record ProcessResult(int exitCode, String outputStreamContent) {
	/**
	 * @return Whether the exit code is 0
	 */
	public boolean successful() {
		return exitCode == 0;
	}
}
