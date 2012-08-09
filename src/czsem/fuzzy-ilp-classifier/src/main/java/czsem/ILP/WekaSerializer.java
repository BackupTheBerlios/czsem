package czsem.ILP;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import weka.core.Instance;

public class WekaSerializer extends Serializer {

	public WekaSerializer(String output_filename) throws FileNotFoundException, UnsupportedEncodingException
	{
		super(output_filename);
	}
	
	public WekaSerializer(String output_filename, boolean append) throws FileNotFoundException, UnsupportedEncodingException
	{
		super(output_filename, append);
	}

	public WekaSerializer(OutputStream outputStream) throws UnsupportedEncodingException
	{
		super(outputStream);
	}

	public static interface Condition
	{
		public boolean evaluate(String current_instance_value, String alternative_value_form_attribute_domain);
	}
	
	public static class NegCondition implements Condition
	{
		private Condition orig;
		
		public NegCondition(Condition orig) {this. orig = orig;}

		@Override
		public boolean evaluate(String currentInstanceValue, String alternativeValueFormAttributeDomain)
		{			
			return ! orig.evaluate(currentInstanceValue, alternativeValueFormAttributeDomain);
		}		
	}
	
	
	protected void putInstanceExamples(Instance instance, String [] class_values, String id, Relation rel, Condition condition)
	{
		String value = getStringValue(instance, instance.classIndex()); 
				
		for (int a=0; a<class_values.length; a++)
		{			
//			if (class_values[a].equals(value)) continue;
			
			if (condition.evaluate(value, class_values[a]))
				putBinTuple(rel, id, class_values[a]);
		}
	}

	protected static String getStringValue(Instance instance, int attr_index)
	{
		String value = instance.toString(attr_index);
//		if (value.compareTo("?") == 0) continue;
		if (value.equals("?")) value = "unknown";
		
		return value;		
	}
	
	protected void assertBkgTuplesForInstance(Instance instance, String id, Relation [] relations)
	{
		for (int r = 0; r < relations.length; r++)
		{				
			if (r == instance.classIndex()) continue;
			
			output.print("assert((");		
			
			putInlineBinTuple(relations[r], id, getStringValue(instance, r));
//			System.err.printf("assert((%s(%s,%s))).\n", relations[r].getName(), id, getStringValue(instance, r));
			
			output.println(")).");		
			output.flush();		
//			output.println();
//			output.flush();		

		}		
	}

	
	protected void putBkgTuplesForInstance(Instance instance, String id, Relation [] relations)
	{
		for (int r = 0; r < relations.length; r++)
		{				
			if (r == instance.classIndex()) continue;
			putBinTuple(relations[r], id, getStringValue(instance, r));						
		}		
	}

/*
	protected void assertBkgTuplesForInstance(Instance instance, String id, Relation [] relations)
	{
		for (int r = 0; r < relations.length; r++)
		{				
			if (r == instance.classIndex()) continue;
			putBinTuple(relations[r], id, getStringValue(instance, r));						
		}		
	}
*/	
	protected Relation addAttributeRelation(String attr_name)
	{
		return addBinRelation("has_" + attr_name, "id", attr_name);						
	}

	protected Relation [] addAttributeRelations(Instance instance)
	{
		Relation [] relations = new Relation[instance.numAttributes()];

		for (int i = 0; i < relations.length; i++)
		{			
			relations[i] = addAttributeRelation(instance.attribute(i).name());
		}
		
		return relations;
	}
	
	protected void putTestClassPredicate(String test_id, Relation rel)
	{
//		output.print(":-");
		renderInlineTupleWithoutValueCheck(rel.getName(), new String[] {test_id, "X"});
		output.println(",print(X),print('\\n'),!.");
		output.println("print('END\\n').");
		output.println(); //acknowledge the first solution
		output.flush();
				
	}
}
