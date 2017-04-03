# Overview

Example LaTeX document showcasing main features of the `csse-fcs` class.

Its content is completely random and does not hold any meaning what-so-ever.
If you decide to present this dissertation unchanged, we hold no responsibility
for your guaranteed and utter failure.

Commands to compile the report (the last one is optional):
```
pdflatex -synctex=1 -interaction=nonstopmode %.tex
makeindex %.nlo -s nomencl.ist -o %.nls
bibtex %
pdflatex -synctex=1 -interaction=nonstopmode %.tex
pdflatex -synctex=1 -interaction=nonstopmode %.tex
"<path to pdf viewer>" %.pdf
```