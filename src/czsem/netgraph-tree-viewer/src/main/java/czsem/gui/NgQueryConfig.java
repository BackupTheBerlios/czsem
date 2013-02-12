package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import czsem.fs.DependencyConfiguration;
import czsem.fs.FSSentenceWriter;
import czsem.gate.utils.Config;
import czsem.utils.AbstractConfig.ConfigLoadEception;
import czsem.utils.Config.DependencyConfig;

@SuppressWarnings("unused")
public class NgQueryConfig extends Container {
	private static final long serialVersionUID = 8676767227162395664L;

	public static void main(String[] args) throws Exception {
		JFrame fr = new JFrame(NgQueryConfig.class.getName());
	    fr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	
		
	    NgQueryConfig qd = new NgQueryConfig();
		qd.initComponents();
		
		fr.add(qd);
		
		fr.pack();
		fr.setVisible(true);

	}

	protected static JPanel embedDependencyManager(AddRemoveListsManager man, String title) {
        JPanel panel_dependencies = new JPanel(new BorderLayout());
        panel_dependencies.setBorder(
        		BorderFactory.createCompoundBorder(
        				BorderFactory.createEmptyBorder(15, 15, 15, 15), 
        				BorderFactory.createTitledBorder(title)));
        man.initComponents();
		panel_dependencies.add(man);
		
		return panel_dependencies;
	}

	public void initComponents() throws ConfigLoadEception {
		setLayout(new BorderLayout());

		JPanel panel_center = new JPanel(new GridLayout(1, 2));
        add(panel_center, BorderLayout.CENTER);

        JPanel panel_south = new JPanel();
        add(panel_south, BorderLayout.SOUTH);
		

        DependencyConfig deps = DependencyConfiguration.getDependencyConfig();
        
        final AddRemoveListsManager depMan = new AddRemoveListsManager();
        panel_center.add(embedDependencyManager(depMan, "Dependencies"));
        depMan.addLeftModelSynchronization(deps.getDependencyTypesSelected());
        depMan.addRightModelSynchronization(deps.getDependencyTypesAvailable());
		depMan.synchronizeModels();

        final AddRemoveListsManager tocDepMan = new AddRemoveListsManager();
        panel_center.add(embedDependencyManager(tocDepMan, "Token Dependencies"));
        tocDepMan.addLeftModelSynchronization(deps.getTokenDependenciesSelected());
        tocDepMan.addRightModelSynchronization(deps.getTokenDependenciesAvailable());
		tocDepMan.synchronizeModels();
        
        
		JButton buttonDefaults = new JButton("Defaults");
		buttonDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DependencyConfiguration.defaultConfig.putToConfig();
					depMan.synchronizeModels();
					tocDepMan.synchronizeModels();
				} catch (ConfigLoadEception ex) {
					throw new RuntimeException(ex);
				}
			}});
		panel_south.add(buttonDefaults);

		
        JButton buttonSave = new JButton("Save");
		buttonSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Config.getConfig().updateLoadedConfigFile();
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			}
		});
		panel_south.add(buttonSave);		
	}

}
