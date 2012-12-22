package czsem.fs.query;

import java.util.HashSet;
import java.util.Set;

import czsem.fs.query.FSQueryParser.SyntaxError;

public class AttrsCollectorFSQB implements FSQueryBuilder{
	Set<String> attrs = new HashSet<String>();
	
	@Override
	public void addRestriction(String comparartor, String arg1, String arg2) {
		attrs.add(arg1);
	}
	
	public static String[] collectAttributes(String fsString) throws SyntaxError
	{
		AttrsCollectorFSQB builder = new AttrsCollectorFSQB();
		FSQueryParser parser = new FSQueryParser(builder);
		
		parser.parse(fsString);
		
		return builder.getAttributes();
	}

	
	public String[] getAttributes() {
		return attrs.toArray(new String[attrs.size()]);
	}

	@Override
	public void beginChildren() {}
	@Override
	public void endChildren() {}
	@Override
	public void addNode() {}
}
