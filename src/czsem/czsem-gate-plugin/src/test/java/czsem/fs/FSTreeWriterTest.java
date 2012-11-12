package czsem.fs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import czsem.fs.FSTreeWriter.NodeAttributes;

public class FSTreeWriterTest {

	@Test
	public static void testPrintTree() {
		StringWriter out = new StringWriter();
		
		FSTreeWriter tw = new FSTreeWriter(new PrintWriter(out), new NodeAttributes() {
			@Override
			public Iterable<Entry<String, Object>> get(int node_id) {
				List<Entry<String,Object>> ret = new ArrayList<Map.Entry<String,Object>>(1);
				ret.add(new AbstractMap.SimpleEntry<String, Object>("id", node_id));
				return ret;
			}
		});
		
		tw.addDependency(0,1);
		tw.addDependency(0,2);
		tw.addDependency(1,3);
		tw.addDependency(1,4);
		tw.printTree();
		
		Assert.assertEquals(out.toString(), "[id=0]([id=1]([id=3],[id=4]),[id=2])");
		
	}
}
