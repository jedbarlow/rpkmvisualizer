all: rpkmvisualizer_manual.pdf

rpkmvisualizer_manual.pdf: manual.tex
	pdflatex --halt-on-error manual.tex &&\
	pdflatex --halt-on-error manual.tex &&\
	mv manual.pdf rpkmvisualizer_manual.pdf
