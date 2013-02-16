package czsem.gate.plugins;

import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.GuiType;

import java.awt.BorderLayout;

import czsem.gui.NgQueryConfig;

@SuppressWarnings("serial")
@CreoleResource(name = "Query Config", 
	guiType = GuiType.LARGE, 
	resourceDisplayed = "czsem.gate.plugins.NetgraphQueryEval", 
	mainViewer = false) 
public class NetgraphQueryEvalConfig extends AbstractVisualResource {
	
	@Override
	public void setTarget(Object target) {}

	@Override
	public Resource init() throws ResourceInstantiationException {
	    NgQueryConfig queryConfig = new NgQueryConfig();
		queryConfig.initComponents();
		
		setLayout(new BorderLayout());
		add(queryConfig, BorderLayout.CENTER);
		
		return super.init();
	}

}
