:-use_module(library(lists)).

serialize_rule_file(RuleFileName, OutputFileName, OntologyURI, ObjectProperties) :-
	assert(objectProperties(ObjectProperties)),
	open(OutputFileName, 'write', Stream),
	set_output(Stream),
	write('<?xml version="1.0" encoding="UTF-8"?>\n'),
	write('<!DOCTYPE Ontology [ <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#" >\n'),
	write(' <!ENTITY pml "http://ufal.mff.cuni.cz/pdt/pml/" > ]>\n'),
		
	write('<Ontology xmlns="http://www.w3.org/2002/07/owl#"\n'),	
	format(' ontologyIRI="~a">\n',[OntologyURI]),
		
	consult(RuleFileName),

	write('</Ontology>'),			
	close(Stream).



serialize_rule(:-(H, B)) :- 	
%	T =.. L, write(L), write('\n'), 
	numbervars(:-(H, B), 0, _),	
	
	write('<DLSafeRule>\n'),
		
		write('<Body>\n'),
			serialize_term(B),
		write('\n</Body>\n'),	

		write('<Head>\n'),
			serialize_term(H),
		write('\n</Head>\n'),
		
	
	write('</DLSafeRule>\n\n'),!.
	 
	

 

serialize_term('$VAR'(N)):- char_code('a',I), Var is I + N, format('<Variable IRI="urn:swrl#~c"/>\n',[Var]),!.
serialize_term(T):- atom(T), format('<Literal>~a</Literal>\n',[T]),!.
serialize_term(T):- simple(T), format('<Literal>~k</Literal>\n',[T]),!.


serialize_term(','(A,B)):- serialize_term(A), write('\n'), serialize_term(B),!.
	

serialize_term(T):- functor(T,F,N), objectProperties(OProps), member(F, OProps),!,	
	write('<ObjectPropertyAtom>'),
	format('<ObjectProperty IRI="&pml;~a"/>\n', [F]),
	serialize_arg(T, 0, N),		
	write('</ObjectPropertyAtom>').

serialize_term(T):- functor(T,F,N),	
	write('<DataPropertyAtom>'),
	format('<DataProperty IRI="&pml;~a"/>\n', [F]),
	serialize_arg(T, 0, N),		
	write('</DataPropertyAtom>').


serialize_arg(T, _, 0).
serialize_arg(T, N, N).
serialize_arg(T, M, N) :-  M2 is M+1, arg(M2, T, A),
	serialize_term(A),
	serialize_arg(T, M2, N).  





term_expansion(T,T):- serialize_rule(T).
