package czsem.fs;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.NodeAttributes.IdNodeAttributes;


public class FSTreeWriterTest {

	@Test
	public static void testPrintTree() {
		StringWriter out = new StringWriter();
		
		FSTreeWriter tw = new FSTreeWriter(new PrintWriter(out), new IdNodeAttributes());
		
		tw.addDependency(0,1);
		tw.addDependency(0,2);
		tw.addDependency(1,3);
		tw.addDependency(1,4);
		tw.printTree();
		
		Assert.assertEquals(out.toString(), "[id=0]([id=1]([id=3],[id=4]),[id=2])");
		
	}
}
