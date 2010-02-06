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
//import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import netscape.javascript.JSObject;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;

@SuppressWarnings("serial")
public class OskarsCube extends JApplet implements KeyListener, ActionListener {
	private final static boolean USE_JAVA_SOLVER = true; 
	private boolean display_remoteness_default = false;
	private boolean display_best_move_default = false;
	private boolean display_number_viable_default = false;
	private static final boolean find_best_start_end_default = true;
	private static final boolean find_best_subcomponent = true;
	private boolean random_faces = true;
	
	private int boardsize = 5;
	private int goalRemoteness = 0;
	private int goalBushiness = 0; //Too large and maze has too many open lines == easy routes to visualize
	private int goalSubcomponents = 100; //NOTE, this is at most!!
	private int goalBranches = 0;
	private int goalBranchbyDegree =0;
	private int goalMaxBrDegree = 0;
	private int goalTurns = 0;
	private int goalPlaneTurns = 0;
	
	public boolean topdownview = true;
	
	public static int acheivable;
	public static Solver solved_map;
	public MyShape cube;
	private CubeGen cubefaces;
	private Canvas3D canvas;
	public static JSObject jso;
	
	private JMenuBar menu_bar;
	private JMenu new_puzzle, display;
	private JLabel remoteness, best_move, num_viable;
	private JCheckBoxMenuItem display_best_move, display_remoteness, display_solution_path, display_num_achievable, display_unachievable;
	private JMenuItem display_reset;
	private JMenuItem new_make;
	private JRadioButtonMenuItem new_random, new_saved;
	private JRadioButtonMenuItem top_down_view,side_view;
	private ButtonGroup view_group;
	private JTextField saved_game_b, saved_game_w, saved_game_r;
	private JLabel saved_game;
	//for if decide to implement searchable random in GUI
	/*
	private JSlider board_size;
	private JSlider goal_remoteness;
	private JSlider goal_bushiness;
	private JSlider goal_components;
	private JSlider goal_branches;
	private JSlider goal_br_degree;
	private JSlider goal_plane_turns;
	*/

	
	public void init() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					canvas = new Canvas3D();
					make_new_puzzle();
					
					//menu set up
					menu_bar = new JMenuBar();
					new_puzzle = new JMenu("Game");
					new_puzzle.setFocusable(false);
					new_puzzle.setLayout(new BoxLayout(new_puzzle, BoxLayout.X_AXIS));
					new_make = new JMenuItem("New Puzzle", KeyEvent.VK_T);
					new_puzzle.add(new_make);
					new_make.addActionListener(OskarsCube.this);
					new_puzzle.addSeparator();
					ButtonGroup group = new ButtonGroup();
					JRadioButtonMenuItem new_classic = new JRadioButtonMenuItem("Classic Cube");
					new_classic.setSelected(!random_faces);
					group.add(new_classic);
					new_puzzle.add(new_classic);
					new_random = new JRadioButtonMenuItem("Random Cube");
					group.add(new_random);
					new_random.setSelected(random_faces);
					new_saved = new JRadioButtonMenuItem("Load Saved Cube");
					new_saved.setSelected(false);
					group.add(new_saved);
					new_puzzle.add(new_saved);
					new_puzzle.add(new_random);
					new_puzzle.addSeparator();
					saved_game = new JLabel("B/W/R Face Values");
					new_puzzle.add(saved_game);
					saved_game_b = new JTextField(cubefaces.BlueInt + "", 1);
					new_puzzle.add(saved_game_b);
					saved_game_w = new JTextField(cubefaces.WhiteInt + "", 1);
					new_puzzle.add(saved_game_w);
					saved_game_r = new JTextField(cubefaces.RedInt + "", 1);
					new_puzzle.add(saved_game_r);
					new_puzzle.addSeparator();
					//Add Sliders Here
					
					
					display = new JMenu("Options");
					display.setFocusable(false);
					display.setLayout(new BoxLayout(display, BoxLayout.X_AXIS));
					display_reset = new JMenuItem("Reset View",KeyEvent.VK_T);
					display_reset.addActionListener(OskarsCube.this);
					display.add(display_reset);
					display.addSeparator();
					//View Button Group
					view_group = new ButtonGroup();
					top_down_view = new JRadioButtonMenuItem("Top Down View");
					view_group.add(top_down_view);
					side_view = new JRadioButtonMenuItem("Side View");
					view_group.add(side_view);
					display.add(top_down_view);
					display.add(side_view);
					top_down_view.addActionListener(OskarsCube.this);
					side_view.addActionListener(OskarsCube.this);
					side_view.setSelected(!topdownview);
					top_down_view.setSelected(topdownview);
					display.addSeparator();
					display_best_move = new JCheckBoxMenuItem("Show Move Value");
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
					full_panel.setLayout(new BoxLayout(full_panel,BoxLayout.PAGE_AXIS));
					JPanel buttons = new JPanel();
					buttons.setLayout(new BoxLayout(buttons, BoxLayout.PAGE_AXIS));
					best_move = new JLabel();
					num_viable = new JLabel();
					remoteness = new JLabel();
					
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
					
					
					menu_bar.setFocusable(false);
					full_panel.add(menu_bar);
					
					buttons.add(remoteness);
					buttons.add(best_move);
					buttons.add(num_viable);
					
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
		
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/* Make a new puzzle with int values in CubeGen set to input, default to original if not valid */
	private void make_new_puzzle(int blue, int white, int red) {
		cubefaces = new CubeGen(blue,white,red);
		if (USE_JAVA_SOLVER)
			solved_map = new Solver(cubefaces);
		int zoom = (25 + 2*(5-boardsize)*(5-boardsize))/17*12;
		canvas.setLightBorders(true);
		cube = new MyShape(0, 0, zoom, cubefaces);
		cube.setCanvas(canvas);
		canvas.addShape3D(cube);
		canvas.addKeyListener(OskarsCube.this);
		
	}
	
	private void make_new_puzzle() {
		cubefaces = new CubeGen(random_faces, find_best_start_end_default, find_best_subcomponent, boardsize);
		
		if (USE_JAVA_SOLVER)
			solved_map = new Solver(cubefaces);
		/*while(!(solved_map.move_map.containsKey(solved_map.end[0] * boardsize*boardsize*4 + solved_map.end[1] * boardsize*2 + solved_map.end[2]) && solved_map.move_map.containsKey(solved_map.start[0] * boardsize*boardsize*4 + solved_map.start[1]
			* boardsize*2 + solved_map.start[2]))) {
			System.out.println("failed");
			cubefaces = new CubeGen(random_faces, find_best_start_end_default, find_best_subcomponent, boardsize);
			solved_map = new Solver(cubefaces);
		}
		*/
		int tries = 0;
		int maxremotenessseen =0;
		int maxbushinessseen = 0;
		boolean found = true;
		while (found) {
			if (maxremotenessseen < solved_map.getRemoteness(solved_map.start)/2) {
				maxremotenessseen = solved_map.getRemoteness(solved_map.start)/2;
			}
			if (maxbushinessseen < cubefaces.bushiness) {
				maxbushinessseen = cubefaces.bushiness;
			}
				System.out.println("failed " + tries + " " + solved_map.getRemoteness(solved_map.start)/2 + " " + maxremotenessseen + " " + maxbushinessseen);
				cubefaces = new CubeGen(random_faces, find_best_start_end_default, find_best_subcomponent, boardsize);
				solved_map = new Solver(cubefaces);
				tries++;
				found = (solved_map.getRemoteness(solved_map.start)/2 < goalRemoteness) || (cubefaces.branches < goalBranches) ||
					(cubefaces.brfactor < goalBranchbyDegree) || (cubefaces.maxbrfactor < goalMaxBrDegree) || (cubefaces.bushiness < goalBushiness) 
					|| (cubefaces.subcomponents > goalSubcomponents) || (cubefaces.turns < goalTurns) || (cubefaces.planeTurns < goalPlaneTurns);
				//System.out.println("found" + found + " " + cubefaces.maxbrfactor);
		}
		int zoom = (25 + 2*(5-boardsize)*(5-boardsize))/17*12;
		canvas.setLightBorders(true);
		cube = new MyShape(0, 0, zoom, cubefaces);
		cube.setCanvas(canvas);
		canvas.addShape3D(cube);
		canvas.addKeyListener(OskarsCube.this);
		
		
		
		
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
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
	private int tW2 = KeyEvent.VK_RIGHT;
	private int aW2 = KeyEvent.VK_LEFT;
	private int tB2 = KeyEvent.VK_I; //IN
	private int aB2 = KeyEvent.VK_O; //OUT
	private int tR2 = KeyEvent.VK_UP; 
	private int aR2 = KeyEvent.VK_DOWN;
	

	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == aB || arg0.getKeyCode() == aB2) {
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
		if (arg0.getKeyCode() == tB || arg0.getKeyCode() == tB2) {
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
		if (arg0.getKeyCode() == aR || arg0.getKeyCode() == aR2) {
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
		if (arg0.getKeyCode() == tR || arg0.getKeyCode() == tR2) {
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
		if (arg0.getKeyCode() == aW || arg0.getKeyCode() == aW2) {
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
		if (arg0.getKeyCode() == tW || arg0.getKeyCode() == tW2) {
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
		cube.setSecondFacesVisible(topdownview);
		
		canvas.fireCanvasChange();
		update_displays();
	}

	private void update_displays() {
		// catch if start or end is not valid.
		if (!(solved_map.move_map.containsKey(solved_map.end[0] * boardsize*boardsize*4
				+ solved_map.end[1] * boardsize*2 + solved_map.end[2]) && solved_map.move_map
				.containsKey(solved_map.start[0] * boardsize*boardsize*4 + solved_map.start[1]
						* boardsize*2 + solved_map.start[2]))) {
			System.out.println("Start or end are not achievable");
			return;
		}
		remoteness.setText("The puzzle can be solved in " + (solved_map.getRemoteness(cube.current_position) / 2 + " moves"));
		best_move.setText("The best move is to slide " + solved_map.getBestMove(cube.current_position));
		
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
			topdownview = top_down_view.isSelected();
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
			cube.setSecondFacesVisible(topdownview);
			canvas.fireCanvasChange();
		} else if (e.getSource() == display_solution_path) {
			cube.setIntSolVisible(display_solution_path.isSelected());
			canvas.fireCanvasChange();
		} else if (e.getSource() == new_make) {
			random_faces = new_random.isSelected();
			//set cube size
			//set minimum remoteness
			//redo puzzle
			if(new_saved.isSelected()){
				make_new_puzzle(java.lang.Integer.parseInt(saved_game_b.getText()),java.lang.Integer.parseInt( saved_game_w.getText()),java.lang.Integer.parseInt( saved_game_r.getText()));
			} else{
			make_new_puzzle();
			
			}
			saved_game_b.setText(cubefaces.BlueInt + "");
			saved_game_w.setText(cubefaces.WhiteInt + "");
			saved_game_r.setText(cubefaces.RedInt + "");
			set_view();
			cube.setAwayRVisible(solved_map.getBestMove(cube.current_position) == "away from RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,1}), display_best_move.isSelected());
			cube.setTowardRVisible(solved_map.getBestMove( cube.current_position) == "towards RED", solved_map.isValidMove(cube.current_position, new int[] {0,0,-1}), display_best_move.isSelected());
			cube.setAwayBVisible(solved_map.getBestMove(cube.current_position) == "away from BLUE", solved_map.isValidMove(cube.current_position, new int[] {0,1,0}), display_best_move.isSelected());
			cube.setTowardBVisible(solved_map.getBestMove(cube.current_position) == "towards BLUE",solved_map.isValidMove(cube.current_position, new int[] {0,-1,0}), display_best_move.isSelected());
			cube.setTowardWVisible(solved_map.getBestMove(cube.current_position) == "towards WHITE",solved_map.isValidMove(cube.current_position, new int[] {-1,0,0}), display_best_move.isSelected());
			cube.setAwayWVisible(solved_map.getBestMove(cube.current_position) == "away from WHITE", solved_map.isValidMove(cube.current_position, new int[] {1,0,0}), display_best_move.isSelected());
			cube.setSecondFacesVisible(topdownview);
			canvas.fireCanvasChange();
			update_displays();
			canvas.fireCanvasChange();
		} else if (e.getSource() == side_view || e.getSource() == top_down_view) {
			topdownview = top_down_view.isSelected();
			set_view();
			canvas.fireCanvasChange();
		}
		
	}

	private void set_view() {
		if(!topdownview){
		//This one has blue ceiling
		cube.setRotation(new RotationMatrix(1,12,3,135));
		} else {
		//For looking down into the box
		cube.setRotation(new RotationMatrix(1000,1000,1000,120));
		cube.setRotation(new RotationMatrix(1,0,0,90));
		}
		cube.setSecondFacesVisible(topdownview);
		
		//This one has blue floor *DOES NOT WORK*
		//cube.setRotation(new RotationMatrix(20000000,20000000,.0000005,90));
	}
	
}
