package todo.jlatex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Invokes the latex compiler to build the PDF for a LatexDocument
 */
public final class GeneratePdf {
    private static final String DEFAULT_PATH_TO_LATEX_COMPILER = "pdflatex";
    private static final String DEFAULT_PDF_FILE_NAME = "generated-document";
    private static final int DEFAULT_NUMBER_OF_LATEX_COMPILER_INVOCATIONS = 3;
    private static final String DEFAULT_BUILD_DIRECTORY_NAME = "pdf-generation";

    private final LatexDocument document;
    private final String pdfFileName;
    private final String pathToLatexCompiler;
    private final int numberOfLatexCompilerInvocations;
    private final String outputBuildDirectoryName;
    private final long timeoutSeconds;
    private final List<ProcessResult> latexCompilerProcessResults;

    /**
     * @param document                         to create a PDF for
     * @param pdfFileName                      for the output PDF file
     * @param pathToLatexCompiler              which will be invoked during the
     *                                         constructor
     * @param numberOfLatexCompilerInvocations the number of times the latex
     *                                         compiler will be invoked to, e.g.,
     *                                         correctly resolve references
     * @param outputBuildDirectoryName         where the compiler will produce
     *                                         output files, too, will be deleted
     *                                         after the compiler invocations
     * @param timeoutSeconds                   after the process is terminated
     *                                         automatically, because it is assumed
     *                                         to be an error
     * @see #invokeLatexCompiler()
     */
    public GeneratePdf(final LatexDocument document, final String pdfFileName, final String pathToLatexCompiler, final int numberOfLatexCompilerInvocations, final String outputBuildDirectoryName, final long timeoutSeconds) {
	if (pdfFileName.isBlank() || pdfFileName.isEmpty() || pathToLatexCompiler.isEmpty() || pathToLatexCompiler.isBlank() || numberOfLatexCompilerInvocations < 1 || outputBuildDirectoryName.isBlank() || outputBuildDirectoryName.isEmpty() || timeoutSeconds < 1) {
	    throw new IllegalArgumentException();
	}
	this.document = document;
	this.pdfFileName = pdfFileName;
	this.pathToLatexCompiler = pathToLatexCompiler;
	this.numberOfLatexCompilerInvocations = numberOfLatexCompilerInvocations;
	this.outputBuildDirectoryName = outputBuildDirectoryName;
	this.timeoutSeconds = timeoutSeconds;
	this.latexCompilerProcessResults = invokeLatexCompiler();
    }

    /**
     * Overloaded constructor using pdflatex as compiler assumed to be globally
     * available, i.e., the compiler's .exe is in the PATH environment variable with
     * three invocations and a default build directory name
     * 
     * @param document
     * @param pdfFileName
     * @param timeoutSeconds
     * @see #GeneratePdf(LatexDocument, String, String, int, String, long)
     */
    public GeneratePdf(final LatexDocument document, final String pdfFileName, final long timeoutSeconds) {
	this(document, pdfFileName, DEFAULT_PATH_TO_LATEX_COMPILER, DEFAULT_NUMBER_OF_LATEX_COMPILER_INVOCATIONS, DEFAULT_BUILD_DIRECTORY_NAME, timeoutSeconds);
    }

    /**
     * Further overloaded constructor with default output file name
     * "generated-document.pdf"
     * 
     * @param document
     * @param timeoutSeconds
     * @see #GeneratePdf(LatexDocument, String, long)
     */
    public GeneratePdf(final LatexDocument document, final long timeoutSeconds) {
	this(document, DEFAULT_PDF_FILE_NAME, timeoutSeconds);
    }

    /**
     * Creates a temporary tex file of the LatexDocument's content, invokes the
     * LaTeX compiler on it (as often as configured), copies the pdf to the current
     * working directory and deletes the temporary tex file, as well as the latex
     * build files.
     * 
     * @return Exit codes of the latex compiler process (multiple invocations)
     */
    private List<ProcessResult> invokeLatexCompiler() {
	final String pathOfTemporaryTexFile = String.format("./%s.tex", pdfFileName);
	try {
	    boolean allLatexCompilerInvocationsSuccessful = true;
	    final List<ProcessResult> processResults = new ArrayList<>();
	    Files.writeString(Path.of(pathOfTemporaryTexFile), document.toString());
	    for (int i = 0; i < numberOfLatexCompilerInvocations; i++) {
		final ProcessResult processResult = startLatexCompilerProcess(pathOfTemporaryTexFile);
		processResults.add(processResult);
		if (!processResult.successful()) {
		    allLatexCompilerInvocationsSuccessful = false;
		    break;
		}
	    }
	    if (allLatexCompilerInvocationsSuccessful) {
		Files.move(Path.of(String.format("./%s/%s.pdf", outputBuildDirectoryName, pdfFileName)), Path.of(String.format("./%s.pdf", pdfFileName)), StandardCopyOption.REPLACE_EXISTING);
	    }
	    return processResults;
	} catch (final IOException | InterruptedException e) {
	    return List.of();
	} finally {
	    deleteLatexCompilerFiles(Path.of(String.format("./%s/", outputBuildDirectoryName)), Path.of(pathOfTemporaryTexFile));
	}
    }

    /**
     * Invokes the LaTeX compiler configured to work on the given file, using the
     * configured output build directory for latex files. Note that the argument
     * "-interaction=nonstopmode" is automatically added to prevent interaction
     * which would block the process from terminating.
     * 
     * @param pathOfTemporaryTexFile
     * @return Exit code of the LaTeX compiler process.
     * @throws IOException
     * @throws InterruptedException
     */
    private ProcessResult startLatexCompilerProcess(final String pathOfTemporaryTexFile) throws IOException, InterruptedException {
	final List<String> latexCompilerInvocationCommands = List.of(pathToLatexCompiler, "-interaction=nonstopmode", String.format("-output-directory=%s", outputBuildDirectoryName), pathOfTemporaryTexFile);
	final ProcessBuilder latexCompilerInvocationBuilder = new ProcessBuilder(latexCompilerInvocationCommands);
	final Process latexCompilerInvocation = latexCompilerInvocationBuilder.start();
	final boolean successfulExecution = latexCompilerInvocation.waitFor(timeoutSeconds, TimeUnit.SECONDS);
	if (!successfulExecution) {
	    latexCompilerInvocation.destroyForcibly();
	}
	final Optional<Integer> exitCode = successfulExecution ? Optional.of(latexCompilerInvocation.exitValue()) : Optional.empty();
	return new ProcessResult(exitCode, getStandardOutputStreamContent(latexCompilerInvocation));
    }

    /**
     * @param process of which the entire standard output should be read
     * @return The content of the standard outputstream of the given process
     */
    private String getStandardOutputStreamContent(final Process process) {
	try {
	    final InputStream standardOutputStream = process.getInputStream(); // Outputstream of the process is an inputstream on Java side
	    final byte[] content = standardOutputStream.readAllBytes();
	    return new String(content, StandardCharsets.UTF_8);
	} catch (final IOException e) {
	    return "";
	}
    }

    /**
     * Recursively iterates over each file in the given directory to delete them and
     * then the overall folder, too
     * 
     * @param pathToLatexCompilerFiles
     */
    private void deleteLatexCompilerFiles(final Path pathToLatexCompilerFiles, final Path pathOfTemporaryTexFile) {
	try {
	    if (Files.exists(pathOfTemporaryTexFile)) {
		Files.delete(pathOfTemporaryTexFile);
	    }
	    if (Files.exists(pathToLatexCompilerFiles)) {
		Files.walk(pathToLatexCompilerFiles).sorted(Comparator.reverseOrder()).forEach(path -> {
		    try {
			Files.delete(path);
		    } catch (IOException e) {
			throw new RuntimeException("Unable to delete LaTeX compiler files", e);
		    }
		});
	    }
	} catch (final IOException e) {
	    throw new RuntimeException("Unable to delete LaTeX compiler files", e);
	}
    }

    /**
     * @return The integer codes returned by the latex compiler process after
     *         invoking it on the latex document (possibly multiple times)
     */
    public List<ProcessResult> getProcessResults() {
	return latexCompilerProcessResults;
    }

    /**
     * Class to represent the required information of a process execution
     */
    public record ProcessResult(Optional<Integer> exitCode, String outputStreamContent) {
	/**
	 * @return Whether there was an exit code (process did not timeout) and the exit
	 *         code is 0
	 */
	public boolean successful() {
	    return exitCode.isPresent() && exitCode.get() == 0;
	}
    }
}
