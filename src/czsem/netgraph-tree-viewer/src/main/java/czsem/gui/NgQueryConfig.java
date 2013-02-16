package czsem.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import czsem.fs.DependencyConfiguration;
import czsem.fs.FSSentenceWriter;
import czsem.gate.utils.Config;
import czsem.utils.AbstractConfig.ConfigLoadException;
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

	public void initComponents() {
		setLayout(new BorderLayout());

		JPanel panel_center = new JPanel(new GridLayout(1, 2));
        add(panel_center, BorderLayout.CENTER);

        JPanel panel_south = new JPanel();
        add(panel_south, BorderLayout.SOUTH);
		

        DependencyConfig deps = DependencyConfiguration.getDependencyConfig();
        
        final AddRemoveListsManager depMan = new AddRemoveListsManager();
        panel_center.add(embedDependencyManager(depMan, "Dependencies"));
        depMan.addLeftModelSynchronization(deps.getSelected().getDependencyTypes());
        depMan.addRightModelSynchronization(deps.getAvailable().getDependencyTypes());
		depMan.synchronizeModels();

        final AddRemoveListsManager tocDepMan = new AddRemoveListsManager();
        panel_center.add(embedDependencyManager(tocDepMan, "Token Dependencies"));
        tocDepMan.addLeftModelSynchronization(deps.getSelected().getTokenDependencies());
        tocDepMan.addRightModelSynchronization(deps.getAvailable().getTokenDependencies());
		tocDepMan.synchronizeModels();
        
        
		JButton buttonDefaults = new JButton("Defaults");
		buttonDefaults.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DependencyConfiguration.defaultConfigSelected.putToConfig(
							Config.getConfig().getDependencyConfig().getSelected());
					DependencyConfiguration.defaultConfigAvailable.putToConfig(
							Config.getConfig().getDependencyConfig().getAvailable());
					depMan.synchronizeModels();
					tocDepMan.synchronizeModels();
				} catch (ConfigLoadException ex) {
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
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				depMan.synchronizeModels();
				tocDepMan.synchronizeModels();				
			}			
		});

	}

}
