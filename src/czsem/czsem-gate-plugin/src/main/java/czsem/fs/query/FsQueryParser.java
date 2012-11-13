package czsem.fs.query;

import java.util.List;

import czsem.fs.FSTokenizer;

public class FsQueryParser {

	protected List<Character> chars;
	protected List<String> strings;
	
	protected int charIndex = 0;
	protected int stringIndex = 0;
	
	protected FsQueryBuilder builder;

	public FsQueryParser(FsQueryBuilder builder) {
		this.builder = builder;
	}
	
	public static class SyntaxError extends Exception {
		public SyntaxError(String message) {
			super(message);
		}

		private static final long serialVersionUID = 595782365757384397L;		
	}

	public void parse(String input) throws SyntaxError {
		FSTokenizer tokenizer = new FSTokenizer(input);
		chars = tokenizer.getCharList();
		strings = tokenizer.getStringList();
		
		parseNode();
	}
	protected boolean nextCharIs(Character next) {
		if (next == getChar()) return true; //mainly if both are null
		return next.equals(getChar());
	}

	protected void parseNode() throws SyntaxError {
		expectChar('[');
		
		builder.addNode();
		
		parseRestrictions();
		
		expectChar(']');
		
		if (nextCharIs('('))
		{
			parseChildren();					
		}
	}

	private void parseChildren() throws SyntaxError {
		expectChar('(');		
		builder.beginChildren();
		
		for (;;)
		{
			parseNode();
			if (! nextCharIs(',')) break;
			expectChar(',');			
		}
				
		expectChar(')');		
		builder.endChildren();
	}

	private void parseRestrictions() throws SyntaxError {
		for (;;)
		{
			parseRestriction();
			if (! nextCharIs(',')) break;
			expectChar(',');			
		}
	}

	protected void parseRestriction() throws SyntaxError {
		if (nextCharIs(']')) return;
		expectChar(null);
		
		StringBuilder comparator = new StringBuilder();
		comparator.append(expectComprator());
		
		if (! nextCharIs(null))
		{
			comparator.append(expectComprator());			
		}

		expectChar(null);
		
		builder.addRestriction(comparator.toString(), getStringPlusPlus(), getStringPlusPlus());
		
	}

	protected char expectComprator() throws SyntaxError {
		Character ch = getChar();
		charIndex++;
		
		if (	ch == null ||
				FSTokenizer.isSpecialChar(ch) != FSTokenizer.SpecialChar.EVEN_STRING_COMPARATOR)
			
			throw new SyntaxError(String.format("Comparator expected but '%c' found!", ch));
		
		return ch;
	}

	protected void expectChar(Character expected) throws SyntaxError {
		Character ch = getChar();
		charIndex++;
		
		if (expected == ch) return; //mainly if both are null - return ok;
		
		if (!expected.equals(ch)) throw new SyntaxError(String.format("Character '%c' expected but '%c' found!", expected, ch));		
	}

	protected Character getChar() {
		return chars.get(charIndex); 
	}

	protected String getStringPlusPlus() {
		return strings.get(stringIndex++); 
	}
}
