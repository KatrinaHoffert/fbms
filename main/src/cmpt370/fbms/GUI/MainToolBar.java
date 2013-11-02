package cmpt370.fbms.GUI;



import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

public class MainToolBar extends JToolBar{

	
	private static final long serialVersionUID = 1L;


	private JButton upbut = new JButton("up");
	private JButton refresh = new JButton("refresh");
	private JTextField write = new JTextField(5);
	
	public MainToolBar(){
	

	
	refresh.addActionListener(new ActionListener(){
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			}
		
		
	});
	
	upbut.addActionListener(new ActionListener(){
				@Override
		public void actionPerformed(ActionEvent e) {
		
			
		}
	});
	this.setFloatable(false);
	this.add(upbut);
	this.add(refresh);
	this.add(write);
	
	}
}
