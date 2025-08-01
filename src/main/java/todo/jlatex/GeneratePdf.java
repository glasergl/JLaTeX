package todo.jlatex;

import java.io.IOException;
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
    private final List<Integer> latexCompilerExitCodes;

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
	this.document = document;
	this.pdfFileName = pdfFileName;
	this.pathToLatexCompiler = pathToLatexCompiler;
	this.numberOfLatexCompilerInvocations = numberOfLatexCompilerInvocations;
	this.outputBuildDirectoryName = outputBuildDirectoryName;
	this.latexCompilerExitCodes = invokeLatexCompiler();

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
    private List<Integer> invokeLatexCompiler() {
	try {
	    final List<Integer> exitCodes = new ArrayList<>();
	    final String pathOfTemporaryTexFile = String.format("./%s.tex", pdfFileName);
	    Files.writeString(Path.of(pathOfTemporaryTexFile), document.toString());
	    for (int i = 0; i < numberOfLatexCompilerInvocations; i++) {
		final int exitCode = startLatexCompilerProcess(pathOfTemporaryTexFile);
		exitCodes.add(exitCode);
	    }
	    Files.move(Path.of(String.format("./%s/%s.pdf", outputBuildDirectoryName, pdfFileName)), Path.of(String.format("./%s.pdf", pdfFileName)), StandardCopyOption.REPLACE_EXISTING);
	    deleteLatexCompilerFiles(Path.of(String.format("./%s/", outputBuildDirectoryName)));
	    Files.delete(Path.of(pathOfTemporaryTexFile));
	    return exitCodes;
	} catch (final IOException | InterruptedException e) {
	    throw new RuntimeException("Unable to create PDF", e);
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
    private int startLatexCompilerProcess(final String pathOfTemporaryTexFile) throws IOException, InterruptedException {
	final List<String> latexCompilerInvocationCommands = List.of(pathToLatexCompiler, "-interaction=nonstopmode", String.format("-output-directory=%s", outputBuildDirectoryName), pathOfTemporaryTexFile);
	final ProcessBuilder latexCompilerInvocationBuilder = new ProcessBuilder(latexCompilerInvocationCommands);
	final Process latexCompilerInvocation = latexCompilerInvocationBuilder.start();
	final int exitCode = latexCompilerInvocation.waitFor();
	return exitCode;
    }

    /**
     * Recursively iterates over each file in the given directory to delete them and
     * then the overall folder, too
     * 
     * @param pathToLatexCompilerFiles
     * @throws IOException
     */
    private void deleteLatexCompilerFiles(final Path pathToLatexCompilerFiles) throws IOException {
	Files.walk(pathToLatexCompilerFiles).sorted(Comparator.reverseOrder()).forEach(path -> {
	    try {
		Files.delete(path);
	    } catch (IOException e) {
		throw new RuntimeException("Unable to delete LaTeX compiler files", e);
	    }
	});
    }

    /**
     * @return The integer codes returned by the latex compiler process after
     *         invoking it on the latex document (possibly multiple times)
     */
    public List<Integer> getExitCodes() {
	return latexCompilerExitCodes;
    }
}
