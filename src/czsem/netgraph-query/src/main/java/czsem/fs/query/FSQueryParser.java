package czsem.fs.query;

import java.util.List;

import czsem.fs.FSTokenizer;

public class FSQueryParser {

	protected List<Character> chars;
	protected List<String> strings;
	
	protected int charIndex = 0;
	protected int stringIndex = 0;
	
	protected FSQueryBuilder builder;

	public FSQueryParser(FSQueryBuilder builder) {
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
	protected void parseNode() throws SyntaxError {
		expectChar('[');
		
		builder.addNode();
		
		parseRestrictions();
		
		expectChar(']');
		
		if (moreCharsAvailable() && nextCharIs('('))
		{
			parseChildren();					
		}
	}

	protected void parseChildren() throws SyntaxError {
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

	protected void parseRestrictions() throws SyntaxError {
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

	protected boolean nextCharIs(Character next) {
		if (next == getChar()) return true; //mainly if both are null
		if (next == null) return false; //because of previous
		return next.equals(getChar());
	}
	
	protected void expectChar(Character expected) throws SyntaxError {
		Character ch = getChar();
		charIndex++;
		
		if (expected == ch) return; //mainly if both are null - return ok;
		
		if (expected == null || !expected.equals(ch)) 
			throw new SyntaxError(String.format("Character '%c' expected but '%c' found!", expected, ch));		
	}

	protected Character getChar() {
		return chars.get(charIndex); 
	}

	protected boolean moreCharsAvailable() {
		return charIndex < chars.size();
	}

	protected String getStringPlusPlus() {
		return strings.get(stringIndex++); 
	}
}
