package czsem.gate.plugins;

import gate.Gate;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;
import gate.gui.MainFrame;

import java.awt.BorderLayout;

import czsem.gate.learning.PRSetup;
import czsem.gate.utils.GateUtils;
import czsem.gui.NgQueryDesigner;

@CreoleResource(name = "Visual resource for NetgraphQueryEval", 
                guiType = GuiType.LARGE, 
                resourceDisplayed = "czsem.gate.plugins.NetgraphQueryEval", 
                mainViewer = true) 
public class NetgraphQueryEvalViewer extends AbstractVisualResource {
	
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

		
		return super.init();
	}

	@Override
	public void setTarget(Object target) {
		this.target = (NetgraphQueryEval) target;
	    qd.setQueryString(this.target.getQueryString());

	}

	private static final long serialVersionUID = 3490034837910242834L;

	public static void main(String[] args) throws Exception {
		GateUtils.initGate();
		
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEval.class);
		Gate.getCreoleRegister().registerComponent(NetgraphQueryEvalViewer.class);
		
		MainFrame.getInstance().setVisible(true);
		
		new PRSetup.SinglePRSetup(NetgraphQueryEval.class)
		.putFeature("outputASName", "ng").createPR();


	}

}
