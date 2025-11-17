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
     * @see #invokeLatexCompiler()
     */
    public GeneratePdf(final LatexDocument document, final String pdfFileName, final String pathToLatexCompiler, final int numberOfLatexCompilerInvocations, final String outputBuildDirectoryName) {
	if (pdfFileName.isBlank() || pdfFileName.isEmpty() || pathToLatexCompiler.isEmpty() || pathToLatexCompiler.isBlank() || numberOfLatexCompilerInvocations < 1 || outputBuildDirectoryName.isBlank() || outputBuildDirectoryName.isEmpty()) {
	    throw new IllegalArgumentException();
	}
	if (!latexCompilerIsInstalled(pathToLatexCompiler)) {
	    throw new IllegalStateException();
	}
	this.document = document;
	this.pdfFileName = pdfFileName;
	this.pathToLatexCompiler = pathToLatexCompiler;
	this.numberOfLatexCompilerInvocations = numberOfLatexCompilerInvocations;
	this.outputBuildDirectoryName = outputBuildDirectoryName;
	this.latexCompilerProcessResults = invokeLatexCompiler();
    }

    /**
     * Overloaded constructor using pdflatex as compiler assumed to be globally
     * available, i.e., the compiler's .exe is in the PATH environment variable with
     * three invocations and a default build directory name
     * 
     * @param document
     * @param pdfFileName
     * @see #GeneratePdf(LatexDocument, String, String, int, String)
     */
    public GeneratePdf(final LatexDocument document, final String pdfFileName) {
	this(document, pdfFileName, DEFAULT_PATH_TO_LATEX_COMPILER, DEFAULT_NUMBER_OF_LATEX_COMPILER_INVOCATIONS, DEFAULT_BUILD_DIRECTORY_NAME);
    }

    /**
     * Further overloaded constructor with default output file name
     * "generated-document.pdf"
     * 
     * @param document
     * @see #GeneratePdf(LatexDocument, String)
     */
    public GeneratePdf(final LatexDocument document) {
	this(document, DEFAULT_PDF_FILE_NAME);
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
	latexCompilerInvocationBuilder.redirectErrorStream(true);
	final Process latexCompilerInvocation = latexCompilerInvocationBuilder.start();
	final String latexCompilerOutput = getStandardOutputStreamContent(latexCompilerInvocation);
	final int exitCode = latexCompilerInvocation.waitFor();
	return new ProcessResult(exitCode, latexCompilerOutput);
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
			throw new IllegalStateException("Unable to delete LaTeX compiler files", e);
		    }
		});
	    }
	} catch (final IOException e) {
	    throw new RuntimeException("Unable to delete LaTeX compiler files", e);
	}
    }

    /**
     * @return Whether all process results were successful
     */
    public boolean wasSuccessful() {
	return latexCompilerProcessResults.stream().allMatch(ProcessResult::successful);
    }

    /**
     * @return The integer codes returned by the latex compiler process after
     *         invoking it on the latex document (possibly multiple times)
     */
    public List<ProcessResult> getProcessResults() {
	return latexCompilerProcessResults;
    }

    /**
     * Checks existence of latex compiler by invoking it with the --version flag
     * 
     * @param compilerPath - path to latex compiler executable file, or just the
     *                     compiler name if it's globally installed, e.g.,
     *                     "pdflatex"
     * @return Whether the given latex compiler is installed and can be invoked,
     *         assumes false if an exception occurs
     * @throws IllegalArgumentException If the given path is empty or just
     *                                  whitespace
     */
    public static boolean latexCompilerIsInstalled(final String compilerPath) {
	try {
	    if (compilerPath.isEmpty() || compilerPath.isBlank()) {
		throw new IllegalArgumentException();
	    }
	    final List<String> latexVersionCommand = List.of(compilerPath, "--version");
	    final ProcessBuilder latexCompilerInvocationBuilder = new ProcessBuilder(latexVersionCommand);
	    latexCompilerInvocationBuilder.redirectErrorStream(true);
	    final Process latexCompilerInvocation = latexCompilerInvocationBuilder.start();
	    getStandardOutputStreamContent(latexCompilerInvocation); // must be consumed such that process is not frozen by operating system
	    return latexCompilerInvocation.waitFor() == 0;
	} catch (final IOException | InterruptedException e) {
	    return false;
	}
    }

    /**
     * @param process of which the entire standard output should be read
     * @return The content of the standard outputstream of the given process, empty
     *         string when encountering an exception
     */
    private static String getStandardOutputStreamContent(final Process process) {
	try {
	    final InputStream standardOutputStream = process.getInputStream(); // Outputstream of the process is an inputstream on Java side
	    final byte[] content = standardOutputStream.readAllBytes();
	    return new String(content, StandardCharsets.UTF_8);
	} catch (final IOException e) {
	    return "";
	}
    }
}
