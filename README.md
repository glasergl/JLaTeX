# JLaTeX
This tool allows creating latex code in Java to ultimately create dynamic PDF documents.
For example
```java
LatexDocument d = new LatexDocument("scrartcl", "a4paper, 12pt");
d.usePackage("babel", "ngerman")
 .beginDocument()
 .plain("test1212")
 .line(command("LARGE"))
 .format("I like math mode like this $%$", command("frac", 1, 2))
 .endDocument();
```
represents
```latex
\documentclass[a4paper, 12pt]{scrartcl}
\usepackage[ngerman]{babel}
\begin{document}
test1212
\LARGE
I like math mode like this $\frac{1}{2}$
\end{document}
```
Furthermore, the library provides functionality to serialize the LaTeX code into a file on the system, call a LaTeX compiler on the file (possibly multiple times) and clean up the compiler files such that only the PDF remains.
If you have `pdflatex`, e.g., via [MikTex](https://miktex.org/), globally installed on your system, then it is sufficient to call `new GenerateDocument(d);` such that the PDF file is created at the current working directory of the java application.

A more sophisticated example is a [cook book generator](https://github.com/glasergl/CustomCookBook) where recipes are represented with abstract data and then a PDF is generated out of the data.
It contains a title page, table of contents, each recipe with ingredients + steps and more.

# Maven Central
Currently, this library is not in any dependency repository.
Nevertheless, it is planned to do eventually.
In order to try the library, clone this repository, run `mvn install` and then the import
```xml
<dependency>
  <groupId>todo</groupId>
  <artifactId>jlatex</artifactId>
  <version>1.0.0</version>
</dependency>
```
is resolved locally for your project.
