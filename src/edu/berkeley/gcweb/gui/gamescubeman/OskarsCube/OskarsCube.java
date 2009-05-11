package edu.berkeley.gcweb.gui.gamescubeman.OskarsCube;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import netscape.javascript.JSObject;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;

@SuppressWarnings("serial")
public class OskarsCube extends JApplet implements KeyListener, ActionListener {
	private final static boolean USE_JAVA_SOLVER = true; 
	private static final boolean display_remoteness_default = false;
	private static final boolean display_best_move_default = false;
	private static final boolean random_faces = true;
	private static final boolean display_number_viable_default = false;
	private static final boolean find_best_start_end_default = true; 
	private static final int boardsize = 5;
	private static final int goalremoteness = 10;
	public static int acheivable;
	public static Solver solved_map;
	public MyShape cube;
	private Canvas3D canvas;
	public static JSObject jso;
	
	private JMenuBar menu_bar;
	private JMenu new_puzzle, display;
	private JLabel remoteness;
	private JLabel best_move;
	private JLabel num_viable;
	//private JButton resetViewButton;
	/*
	private JCheckBox display_best_move_box;
	private JCheckBox display_remoteness_box;
	private JCheckBox display_number_viable_box;
	private JCheckBox display_viable_insides_box;
	private JCheckBox display_solution_path_box;
	*/
	private JCheckBoxMenuItem display_best_move;
	private JCheckBoxMenuItem display_remoteness;
	private JCheckBoxMenuItem display_solution_path;
	private JCheckBoxMenuItem display_num_achievable;
	private JCheckBoxMenuItem display_unachievable;
	private JMenuItem display_reset;
	
	private CubeGen cubefaces;
	
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					
					cubefaces = new CubeGen(random_faces, find_best_start_end_default, boardsize);
					
					if (USE_JAVA_SOLVER)
						solved_map = new Solver(cubefaces);
					while(!(solved_map.move_map.containsKey(solved_map.end[0] * boardsize*boardsize*4 + solved_map.end[1] * boardsize*2 + solved_map.end[2]) && solved_map.move_map.containsKey(solved_map.start[0] * boardsize*boardsize*4 + solved_map.start[1]
						* boardsize*2 + solved_map.start[2]))) {
						System.out.println("failed");
						cubefaces = new CubeGen(random_faces, find_best_start_end_default, boardsize);
						solved_map = new Solver(cubefaces);
						
					}
					int tries = 0;
					while (solved_map.getRemoteness(solved_map.start)/2 < goalremoteness) {
							System.out.println("failed " + tries + " " + solved_map.getRemoteness(solved_map.start) );
							cubefaces = new CubeGen(random_faces, find_best_start_end_default, boardsize);
							solved_map = new Solver(cubefaces);
							tries++;
					}
					int zoom = 25 + 2*(5-boardsize)*(5-boardsize);
					canvas = new Canvas3D();
					canvas.setLightBorders(true);
					cube = new MyShape(0, 0, zoom, cubefaces);
					cube.setCanvas(canvas);
					canvas.addShape3D(cube);
					canvas.addKeyListener(OskarsCube.this);
					menu_bar = new JMenuBar();
					new_puzzle = new JMenu("New Game");
					new_puzzle.setFocusable(false);
					new_puzzle.setLayout(new BoxLayout(new_puzzle, BoxLayout.X_AXIS));
					JMenuItem new_make = new JMenuItem("Make New Puzzle",
	                         KeyEvent.VK_T);
					new_puzzle.add(new_make);
					new_puzzle.addSeparator();
					ButtonGroup group = new ButtonGroup();
					JRadioButtonMenuItem new_classic = new JRadioButtonMenuItem("Classic Cube");
					new_classic.setSelected(!random_faces);
					group.add(new_classic);
					new_puzzle.add(new_classic);
					JRadioButtonMenuItem new_random = new JRadioButtonMenuItem("Random Cube");
					group.add(new_random);
					new_random.setSelected(random_faces);
					new_puzzle.add(new_random);
					new_puzzle.addSeparator();
					

					
					display = new JMenu("Options");
					display.setFocusable(false);
					display.setLayout(new BoxLayout(display, BoxLayout.X_AXIS));
					display_reset = new JMenuItem("Reset View",
	                         KeyEvent.VK_T);
					display_reset.addActionListener(OskarsCube.this);
					display.add(display_reset);
					display.addSeparator();
					display_best_move = new JCheckBoxMenuItem("Show Best Move");
					display_best_move.setSelected(display_best_move_default);
					display_best_move.addActionListener(OskarsCube.this);
					display.add(display_best_move);
					display_remoteness = new JCheckBoxMenuItem("Show Remoteness");
					display_remoteness.setSelected(display_remoteness_default);
					display_remoteness.addActionListener(OskarsCube.this);
					display.add(display_remoteness);
					display_solution_path = new JCheckBoxMenuItem("Show Solution Path");
					//display_solution_path.setSelected(display_remoteness_default);
					display_solution_path.addActionListener(OskarsCube.this);
					display.add(display_solution_path);
					display_num_achievable = new JCheckBoxMenuItem("Show Number Achievable");
					display_num_achievable.setSelected(display_number_viable_default);
					display_num_achievable.addActionListener(OskarsCube.this);
					display.add(display_num_achievable);
					display_unachievable = new JCheckBoxMenuItem("Show Unachievable");
					//display_unachievable.setSelected(display_number_viable_default);
					display_unachievable.addActionListener(OskarsCube.this);
					display.add(display_unachievable);
					display.addSeparator();
					
					
					menu_bar.add(new_puzzle);
					menu_bar.add(display);
					menu_bar.setMaximumSize(new Dimension(100000000,400));
					menu_bar.setLayout(new BoxLayout(menu_bar, BoxLayout.X_AXIS));
					
					JPanel full_panel = new JPanel();
					full_panel.setLayout(new BoxLayout(full_panel,
							BoxLayout.PAGE_AXIS));
					JPanel buttons = new JPanel();
					
					buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
					
					
					best_move = new JLabel();
					num_viable = new JLabel();
					remoteness = new JLabel();
					/*
					display_viable_insides_box = new JCheckBox("Show Unachievable Insides");
					cube.setInteriorVisible(display_viable_insides_box.isSelected());
					display_viable_insides_box.setFocusable(false);
					display_viable_insides_box.addActionListener(OskarsCube.this);
					
					display_solution_path_box = new JCheckBox("Show Solution Path");
					cube.setIntSolVisible(display_solution_path_box.isSelected());
					display_solution_path_box.setFocusable(false);
					display_solution_path_box.addActionListener(OskarsCube.this);
					
					display_best_move_box = new JCheckBox("Show Best Move");
					display_best_move_box.setFocusable(false);
					display_best_move_box.addActionListener(OskarsCube.this);
					
					display_remoteness_box = new JCheckBox("Show Remoteness");
					display_remoteness_box.setFocusable(false);
					display_remoteness_box.addActionListener(OskarsCube.this);
					
					display_number_viable_box = new JCheckBox("Show Number Achievable");
					display_number_viable_box.setFocusable(false);
					display_number_viable_box.addActionListener(OskarsCube.this);
					*/
					if (solved_map.getRemoteness(cubefaces.start) !=0) {
						remoteness.setText("The puzzle can be solved in " + (solved_map.getRemoteness(cubefaces.start) / 2 + " moves"));
						remoteness.setVisible(display_remoteness_default);
						best_move.setText("The best move is to slide " + solved_map.getBestMove(cubefaces.start));
						best_move.setVisible(display_best_move_default);
					} else {
						remoteness.setText("The puzzle cannot be solved");
						remoteness.setVisible(display_remoteness_default);
						best_move.setText("There are no moves to win");
						best_move.setVisible(display_best_move_default);
						
					}
					num_viable.setText(acheivable + " positions are achievable and "+ (boardsize*boardsize*boardsize - acheivable) + " are not");
					num_viable.setVisible(display_number_viable_default);
					
					/*
					resetViewButton = new JButton("Reset View");
					resetViewButton.setMnemonic(KeyEvent.VK_R);
					resetViewButton.setFocusable(false);
					resetViewButton.addActionListener(OskarsCube.this);
					*/
					menu_bar.setFocusable(false);
					full_panel.add(menu_bar);
					//buttons.add(resetViewButton);
					buttons.add(remoteness);
					buttons.add(best_move);
					buttons.add(num_viable);
					//buttons.add(display_viable_insides_box);
					//buttons.add(display_number_viable_box);
					//buttons.add(display_best_move_box);
					//buttons.add(display_remoteness_box);
					//buttons.add(display_solution_path_box);
					full_panel.add(buttons);
					full_panel.add(canvas);
					
					getContentPane().add(full_panel);
					cube.setAwayRVisible(solved_map.getBestMove(cube.current_position) == "away from RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,1}), display_best_move.isSelected());
					cube.setTowardRVisible(solved_map.getBestMove( cube.current_position) == "towards RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,-1}), display_best_move.isSelected());
					cube.setAwayBVisible(solved_map.getBestMove(cube.current_position) == "away from BLUE", solved_map.isValidMove(cube.current_position, new int[] {0,1,0}), display_best_move.isSelected());
					cube.setTowardBVisible(solved_map.getBestMove(cube.current_position) == "towards BLUE",solved_map.isValidMove(cube.current_position, new int[] {0,-1,0}), display_best_move.isSelected());
					cube.setTowardWVisible(solved_map.getBestMove(cube.current_position) == "towards WHITE",solved_map.isValidMove(cube.current_position, new int[] {-1,0,0}), display_best_move.isSelected());
					cube.setAwayWVisible(solved_map.getBestMove(cube.current_position) == "away from WHITE", solved_map.isValidMove(cube.current_position, new int[] {1,0,0}), display_best_move.isSelected());
					canvas.fireCanvasChange();
					set_view();
					update_displays();

				}
			});
			// if (!USE_JAVA_SOLVER)
			// jso = JSObject.getWindow(this);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void setBG_FG(JComponent comp, Color bg, Color fg) {
		if (comp instanceof JButton)
			return;
		comp.setBackground(bg);
		comp.setForeground(fg);
		for (Component child : comp.getComponents())  {
			if (child instanceof JComponent)
				setBG_FG((JComponent) child, bg, fg);
			else if (!(child instanceof JButton)) {
				child.setBackground(bg);
				child.setForeground(fg);
			}
		}
	}

	public String getBoardString() {
		MyShape piece_holder = (MyShape) cube;
		String position = Integer.toString(piece_holder.current_position[0]);
		position += Integer.toString(piece_holder.current_position[1]);
		position += Integer.toString(piece_holder.current_position[2]);
		return position;
	}

	private JPanel sideBySide(JComponent... cs) {
		JPanel p = new JPanel();
		p.setBackground(Color.WHITE);
		for (JComponent c : cs)
			p.add(c);
		return p;
	}

	public static void main(String[] args) {
		final OskarsCube a = new OskarsCube();
		a.init();
		a.setPreferredSize(new Dimension(4000, 5000));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JPanel pane = new JPanel();
				f.setContentPane(pane);
				f.add(a);
				f.pack();
				f.setVisible(true);
				a.canvas.requestFocusInWindow();
			}
		});
	}

	private boolean movement_key_held = false;
	private int tW = KeyEvent.VK_W;
	private int aW = KeyEvent.VK_S;
	private int tB = KeyEvent.VK_E;
	private int aB = KeyEvent.VK_D;
	private int tR = KeyEvent.VK_R;
	private int aR = KeyEvent.VK_F;
	

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == aB) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid= solved_map.isValidMove(holder.current_position,
							new int[] { 0, 1, 0 });
				if (valid) {
					holder.big_red_axis.xyholder.translate(0, -2, 0);
					holder.big_red_axis.xzholder.translate(0, -2, 0);
					holder.current_position[1] += 2;
				}
				canvas.fireCanvasChange();
			}
		}
		if (arg0.getKeyCode() == tB) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid = solved_map.isValidMove(holder.current_position,
							new int[] { 0, -1, 0 });
				if (valid) {
					holder.big_red_axis.xyholder.translate(0, 2, 0);
					holder.big_red_axis.xzholder.translate(0, 2, 0);
					holder.current_position[1] -= 2;
				}
				canvas.fireCanvasChange();
			}
		}
		if (arg0.getKeyCode() == aR) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid = solved_map.isValidMove(holder.current_position,
							new int[] { 0, 0, 1 });
				if (valid) {
					holder.big_red_axis.xyholder.translate(0, 0, 2);
					holder.big_red_axis.yzholder.translate(0, 0, 2);
					holder.current_position[2] += 2;
				}
				canvas.fireCanvasChange();
			}
		}
		if (arg0.getKeyCode() == tR) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid = solved_map.isValidMove(holder.current_position,
							new int[] { 0, 0, -1 });
				if (valid) {
					holder.big_red_axis.yzholder.translate(0, 0, -2);
					holder.big_red_axis.xyholder.translate(0, 0, -2);
					holder.current_position[2] -= 2;
				}
				canvas.fireCanvasChange();
			}
		}
		if (arg0.getKeyCode() == aW) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid = solved_map.isValidMove(holder.current_position,
							new int[] { 1, 0, 0 });
				if (valid) {
					holder.big_red_axis.yzholder.translate(2, 0, 0);
					holder.big_red_axis.xzholder.translate(2, 0, 0);
					holder.current_position[0] += 2;
				}
				canvas.fireCanvasChange();
			}
		}
		if (arg0.getKeyCode() == tW) {
			if (!movement_key_held) {
				movement_key_held = true;
				MyShape holder = (MyShape) cube;
				boolean valid= solved_map.isValidMove(holder.current_position,
							new int[] { -1, 0, 0 });
				if (valid) {
					holder.big_red_axis.yzholder.translate(-2, 0, 0);
					holder.big_red_axis.xzholder.translate(-2, 0, 0);
					holder.current_position[0] -= 2;
				}
				canvas.fireCanvasChange();
			}
		}
		cube.setAwayRVisible(solved_map.getBestMove(cube.current_position) == "away from RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,1}), display_best_move.isSelected());
		cube.setTowardRVisible(solved_map.getBestMove( cube.current_position) == "towards RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,-1}), display_best_move.isSelected());
		cube.setAwayBVisible(solved_map.getBestMove(cube.current_position) == "away from BLUE", solved_map.isValidMove(cube.current_position, new int[] {0,1,0}), display_best_move.isSelected());
		cube.setTowardBVisible(solved_map.getBestMove(cube.current_position) == "towards BLUE",solved_map.isValidMove(cube.current_position, new int[] {0,-1,0}), display_best_move.isSelected());
		cube.setTowardWVisible(solved_map.getBestMove(cube.current_position) == "towards WHITE",solved_map.isValidMove(cube.current_position, new int[] {-1,0,0}), display_best_move.isSelected());
		cube.setAwayWVisible(solved_map.getBestMove(cube.current_position) == "away from WHITE", solved_map.isValidMove(cube.current_position, new int[] {1,0,0}),display_best_move.isSelected());
		
		canvas.fireCanvasChange();
		update_displays();
		MyShape holder = (MyShape) cube;
	}

	private void update_displays() {
		MyShape holder = (MyShape) cube;
		// catch if start or end is not valid.
		if (!(solved_map.move_map.containsKey(solved_map.end[0] * boardsize*boardsize*4
				+ solved_map.end[1] * boardsize*2 + solved_map.end[2]) && solved_map.move_map
				.containsKey(solved_map.start[0] * boardsize*boardsize*4 + solved_map.start[1]
						* boardsize*2 + solved_map.start[2]))) {
			System.out.println("Start or end are not achievable");
			return;
		}
		remoteness.setText("The puzzle can be solved in " + (solved_map.getRemoteness(holder.current_position) / 2 + " moves"));
		best_move.setText("The best move is to slide " + solved_map.getBestMove(holder.current_position));
		
	}

	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		movement_key_held = false;
	}

	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == display_reset) {
			set_view();
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_unachievable) {
			cube.setInteriorVisible(display_unachievable.isSelected());
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_num_achievable) {
			num_viable.setVisible(display_num_achievable.isSelected());
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_remoteness) {
			remoteness.setVisible(display_remoteness.isSelected());
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_best_move) {
			best_move.setVisible(display_best_move.isSelected());
			cube.setAwayRVisible(solved_map.getBestMove(cube.current_position) == "away from RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,1}), display_best_move.isSelected());
			cube.setTowardRVisible(solved_map.getBestMove( cube.current_position) == "towards RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,-1}), display_best_move.isSelected());
			cube.setAwayBVisible(solved_map.getBestMove(cube.current_position) == "away from BLUE", solved_map.isValidMove(cube.current_position, new int[] {0,1,0}), display_best_move.isSelected());
			cube.setTowardBVisible(solved_map.getBestMove(cube.current_position) == "towards BLUE",solved_map.isValidMove(cube.current_position, new int[] {0,-1,0}), display_best_move.isSelected());
			cube.setTowardWVisible(solved_map.getBestMove(cube.current_position) == "towards WHITE",solved_map.isValidMove(cube.current_position, new int[] {-1,0,0}), display_best_move.isSelected());
			cube.setAwayWVisible(solved_map.getBestMove(cube.current_position) == "away from WHITE", solved_map.isValidMove(cube.current_position, new int[] {1,0,0}),display_best_move.isSelected());
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_solution_path) {
			cube.setIntSolVisible(display_solution_path.isSelected());
			canvas.fireCanvasChange();
		}
		
	}

	private void set_view() {
		
		cube.setRotation(new RotationMatrix(1,12,3,135));
		
	}
	
}
