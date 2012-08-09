package czsem.ILP;

import weka.core.Instances;
import czsem.ILP.Serializer.Relation;
import czsem.ILP.WekaSerializer.Condition;

public class FuzzyILPClassifier extends ILPClassifier
{
	private static final long serialVersionUID = 4088657170809650508L;
	private Relation [] atl_relations;

	@Override
	public void buildClassifier(Instances instances) throws Exception {
		buildClassifier("fuzzy_" + instances.relationName(), instances);		
	}

	@Override
	protected void putModes(WekaSerializer ilp_ser)
	{
		putCripsModes(ilp_ser);
		
		atl_relations = new Relation[crisp_relations.length];
		
		ilp_ser.putCommentLn("");
		ilp_ser.putCommentLn("--- M O N O T O N I Z E D    M O D E S ---");
		
		for (int i = 0; i < crisp_relations.length; i++)
		{			
			atl_relations[i] = ilp_ser.addBinRelation(
					crisp_relations[i].getName() + "_atleast",
					crisp_relations[i].getArgTypeName(0),
					crisp_relations[i].getArgTypeName(1));
			
			ilp_ser.putBinaryMode(atl_relations[i], "*", '+', '#');
		}		
	}

	
	@Override
	protected void putTestingAxioms(WekaSerializer ilp_ser, int class_attribute_index)
	{
		ilp_ser.putCommentLn("");
		ilp_ser.putCommentLn("--- T E S T I N G     M O N O T O N I C I T Y      A X I O M S ---");

		for (int i = 0; i < crisp_relations.length; i++)
		{
			
			if (i == class_attribute_index) continue;

			//monotonicity axioms
			ilp_ser.print(atl_relations[i].getName());
			ilp_ser.print("(ID,N) :- ");
			ilp_ser.print(crisp_relations[i].getName());
			ilp_ser.print("(ID,N), not(number(N)),!.\n");

			ilp_ser.print(atl_relations[i].getName());
			ilp_ser.print("(ID,N) :- ");
			ilp_ser.print(crisp_relations[i].getName());
			ilp_ser.print("(ID,N2), number(N2),");
			ilp_ser.print("number(N), N2>=N.\n\n");
			
		}
		
		//crisp data, monotonic rules
		//serious_test(ID,DEG):-serious_atl(ID,DEG),DEG2 is DEG+1, not(serious_atl(ID,DEG2)).
		ilp_ser.print(crisp_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG):-");
		ilp_ser.print(atl_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG), not((");
		ilp_ser.print(atl_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG2),DEG2 > DEG)).\n\n");		
	}

	@Override
	protected void putLearningAxioms(WekaSerializer ilp_ser, int class_attribute_index)
	{
		ilp_ser.putCommentLn("");
		ilp_ser.putCommentLn("--- L E A R N I N G     M O N O T O N I C I T Y      A X I O M S ---");

		for (int i = 0; i < crisp_relations.length; i++)
		{
			
			if (i == class_attribute_index) continue;

			//monotonicity axioms
			ilp_ser.print(atl_relations[i].getName());
			ilp_ser.print("(ID,N) :- ");
			ilp_ser.print(crisp_relations[i].getName());
			ilp_ser.print("(ID,N), not(number(N)),!.\n");

			ilp_ser.print(atl_relations[i].getName());
			ilp_ser.print("(ID,N) :- ");
			ilp_ser.print(crisp_relations[i].getName());
			ilp_ser.print("(ID,N2), number(N2), ");
			ilp_ser.print(atl_relations[i].getArgTypeName(1));
			ilp_ser.print("(N), number(N), N2>=N.\n\n");
			
		}
/*		
		//crisp data, monotonic rules
		//serious_test(ID,DEG):-serious_atl(ID,DEG),DEG2 is DEG+1, not(serious_atl(ID,DEG2)).
		ilp_ser.print(crisp_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG):-");
		ilp_ser.print(atl_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG),DEG2 is DEG+1, not(");
		ilp_ser.print(atl_relations[class_attribute_index].getName());
		ilp_ser.print("(ID,DEG2)).\n\n");
*/
	}

	@Override
	protected void putDeteminations(WekaSerializer backg_ser, int class_index)
	{
		backg_ser.putCommentLn("");
		backg_ser.putCommentLn("--- M O N O T O N I Z E D      D E T E R M I N A T I O N S ---");
		
		putDeteminations(backg_ser, class_index, atl_relations);
		
	}

	@Override
	protected Condition getPositiveExmplesCondition()
	{
		return new Condition()
		{			
			@Override
			public boolean evaluate(String currentInstanceValue, String alternativeValueFormAttributeDomain)
			{
				return currentInstanceValue.compareTo(alternativeValueFormAttributeDomain) >= 0;
			}
		};
	}

	@Override
	protected Relation getTargetCalssRelation(int class_attribute_index) {
		return atl_relations[class_attribute_index];
	}
	
}
