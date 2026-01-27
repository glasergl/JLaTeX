[![build](https://github.com/glasergl/JLaTeX/actions/workflows/build.yml/badge.svg?branch=development)](https://github.com/glasergl/JLaTeX/actions/workflows/build.yml)

# JLaTeX
This tool allows creating LaTeX code in Java to ultimately create dynamic PDF documents.
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

In my opinion Java (or any high level programming language) synergizes well with LaTeX, because one can use the control flow structures and datastructures of the high level programming language as well as the precise PDF layout control of LaTeX.  

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
