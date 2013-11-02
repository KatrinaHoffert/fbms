package cmpt370.fbms.GUI;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MainMenu extends JMenuBar {

	
	private static final long serialVersionUID = 1L;
	
	private JMenuItem newAction = new JMenuItem(" New");
	private JMenuItem openAction = new JMenuItem(" Open");
	private JMenuItem saveAction = new JMenuItem(" Save");
	private JMenuItem saveAsAction = new JMenuItem(" Save as");
	private JMenuItem printAction = new JMenuItem(" Print");
	private JMenuItem exitAction = new JMenuItem("Exit");
	
	private JMenuItem undoAction = new JMenuItem("undo");
	private JMenuItem redoAction = new JMenuItem("Redo");
	private JMenuItem cutAction = new JMenuItem("Cut");
	private JMenuItem copyAction = new JMenuItem("Copy");
	private JMenuItem pasteAction = new JMenuItem("Paste");
	
public MainMenu(){
	JMenu fileMenu = new JMenu("File");
	JMenu editMenu = new JMenu("Edit");
	
	this.add(fileMenu);
	this.add(editMenu);
	

	
	fileMenu.add(newAction);
	fileMenu.add(openAction);
	fileMenu.add(saveAction);
	fileMenu.add(saveAsAction);
	fileMenu.add(printAction);
	fileMenu.addSeparator();
	fileMenu.add(exitAction);
	
	editMenu.add(undoAction);
	editMenu.add(redoAction);
	editMenu.addSeparator();
	editMenu.add(cutAction);
	editMenu.add(copyAction);
	editMenu.add(pasteAction);
	initFileActions();
	initEditActions();
}

private void initFileActions(){
	newAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	openAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	saveAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	printAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	exitAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
		System.exit(0);	
		}
	});
	
}
private void initEditActions() {
	undoAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	redoAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	cutAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	copyAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	pasteAction.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e){
			
		}
	});
	
}
}

