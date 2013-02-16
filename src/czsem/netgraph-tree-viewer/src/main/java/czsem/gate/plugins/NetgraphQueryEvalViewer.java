package czsem.gate.plugins;

import gate.Document;
import gate.Gate;
import gate.Resource;
import gate.annotation.AnnotationSetImpl;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.gui.MainFrame;

import java.awt.BorderLayout;

import czsem.gate.learning.PRSetup;
import czsem.gate.utils.GateUtils;
import czsem.gui.NgQueryDesigner;

@CreoleResource(name = "Query Editor", 
                guiType = GuiType.LARGE, 
                resourceDisplayed = "czsem.gate.plugins.NetgraphQueryEval", 
                mainViewer = true) 
public class NetgraphQueryEvalViewer extends AbstractVisualResource {	
	private static final long serialVersionUID = 3490034837910242834L;
	
	protected NetgraphQueryEval target;
	private NgQueryDesigner qd;
	
	@Override
	public Resource init() throws ResourceInstantiationException {
		
		setLayout(new BorderLayout());
		
	    qd = new NgQueryDesigner() {
			private static final long serialVersionUID = 8912766232005127568L;

	    	@Override
			protected void onUpdateQueryButton() {
				super.onUpdateQueryButton();
				
				target.setQueryString(getQueryString());
				try {
					target.reInit();
				} catch (ResourceInstantiationException e) {
					throw new RuntimeException(e);
				}
			}

	    	
	    };
	    qd.initComponents();
		
		add(qd, BorderLayout.CENTER);
		
		qd.setAs(new AnnotationSetImpl((Document) null));

		
		return super.init();
	}

	@Override
	public void setTarget(Object target) {
		this.target = (NetgraphQueryEval) target;
	    qd.setQueryString(this.target.getQueryString());

	}

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEval.class);
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEvalViewer.class);
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEvalConfig.class);
		
		MainFrame.getInstance().setVisible(true);
		
		new PRSetup.SinglePRSetup(NetgraphQueryEval.class)
		.putFeature("outputASName", "ng").createPR();


	}

}
