package cuni.mff.intlib.law;

import org.testng.Assert;
import org.testng.annotations.Test;

import cuni.mff.intlib.law.LawDatabse.LawHeadAnalyssis;

public class LawHeadAnalysisTest {

	public static void testHead(String head, String num, int year, String suffix) {
		LawHeadAnalyssis h = new LawDatabse.LawHeadAnalyssis(head);
		Assert.assertEquals(h.getYear(), year);
		Assert.assertEquals(h.getPrefix(), num);
		Assert.assertEquals(h.getSuffix(), suffix);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)  
	public void testLawHeadAnalysisWrong() {
		testHead("safdf", null, 0, null);		
	}

	
	@Test
	public void testLawHeadAnalysis() {
		
		
		
		testHead("01/2011", "01", 2011, "");
		testHead("01/2011 sb", "01", 2011, "sb");
		testHead("080575/2012/KUSK", "080575", 2012, "KUSK");
		testHead("100/2011 Sb.m.s.", "100", 2011, "Sb.m.s.");		
		testHead("10/1/2006 (UD)", "10/1", 2006, "(UD)");
		
		testHead("2013/2012", "2013", 2012, "");
		testHead("1/2013/2012", "1/2013", 2012, "");
		
		testHead("102598/2011-MZE-15000", "102598", 2011, "MZE-15000");
		testHead("172599/2011 - MZE-17210", "172599", 2011, "MZE-17210");
		testHead("20013/2012-OVZ-30.0-4.6.12/2", "20013", 2012, "OVZ-30.0-4.6.12/2");
		
		testHead("080575/2012/KUSK", "080575", 2012, "KUSK");
		testHead("080575/2012/KUSK", "080575", 2012, "KUSK");
		testHead("080575/2012/KUSK", "080575", 2012, "KUSK");
		
		
	}
}
