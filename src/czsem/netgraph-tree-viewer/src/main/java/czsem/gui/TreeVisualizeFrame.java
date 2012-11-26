package czsem.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class TreeVisualizeFrame extends TreeVisualize {
	
	protected JFrame frame;
	
	public JFrame getFrame() {
		return frame;
	}

	public TreeVisualizeFrame() {
		frame = new JFrame("TreeVisualize");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
		frame.setSize(700, 500);
		
		frame.add(this);
        
	}
	


	public static TreeVisualizeFrame showTree(String[] attrs, String forest, int selectedNode) {
		TreeVisualizeFrame tv = showTree(attrs, forest);
		
		tv.selectNode(selectedNode);
		
		return tv;		
	}


	public static TreeVisualizeFrame showTree(String[] attrs, String forest) {
		TreeVisualizeFrame tv = new TreeVisualizeFrame();
		tv.initComponents();
		
		tv.setForest(attrs, forest);
//		tv.addShownAttribute("string");		
//		tv.addShownAttribute("form");		
		tv.addShownAttribute("t_lemma");
		tv.addShownAttribute("functor");		
		
		JFrame fr = tv.getFrame();
		fr.pack();
		fr.setVisible(true);

		return tv;
	}

	public static void showTreeAndWait(String[] attrs, String forest, int selectedNode) throws InterruptedException {
		TreeVisualizeFrame tv = showTree(attrs, forest, selectedNode);
		
		final CountDownLatch signal = new CountDownLatch(1);
		
		tv.getFrame().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				System.err.println("closed");
				signal.countDown();
			}
			
		});
		
		signal.await();
		System.err.println("RETURNED");
	}

	public static void main(String [] args)
	{
		String attrs [] = {
				"category", "orth", "dependencies", "string", "length", "kind",
				"form",
				"sentence_order",
				"hidden",
			};

		String attrs2 [] = {
				"string"
			};

		showTree(attrs2, "[string=a]([string=\\\\b],[string=c])");
		showTree(attrs, "[string=visualized,kind=word,dependencies=\\[nsubjpass(3)\\, aux(5)\\, auxpass(7)\\, prep(11)\\],orth=lowercase,category=VBN,length=10,sentence_order=4]([string=annotations,kind=word,dependencies=\\[amod(1)\\],orth=lowercase,category=NNS,length=11,sentence_order=1]([string=Dependency,kind=word,orth=upperInitial,category=JJ,length=10,sentence_order=0]),[string=can,kind=word,orth=lowercase,category=MD,length=3,sentence_order=2],[string=be,kind=word,orth=lowercase,category=VB,length=2,sentence_order=3],[string=by,kind=word,dependencies=\\[pobj(15)\\],orth=lowercase,category=IN,length=2,sentence_order=5]([string=tool,kind=word,dependencies=\\[det(13)\\],orth=lowercase,category=NN,length=4,sentence_order=7]([string=this,kind=word,orth=lowercase,category=DT,length=4,sentence_order=6])))");
		
		System.err.println("end");
	}

}
