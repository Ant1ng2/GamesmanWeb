package edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.Timer;

import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Shape3D;

public abstract class TwistyPuzzle extends Shape3D implements ActionListener, PuzzleStateChangeListener {
	//TODO - generalize to an animation simulator
	//generators, solves, pre-moves,
	//color schemes/key layouts
	//enable/disable spin view
	//save state? ask lucas...
	public TwistyPuzzle(double x, double y, double z) {
		super(x, y, z);
		addStateChangeListener(this);
	}

	protected double stickerGap = getDefaultStickerGap();
	public double getStickerGap() {
		return stickerGap;
	}
	protected double getDefaultStickerGap() {
		return 0.1;
	}
	//spacing can range from 0->.5
	public void setStickerGap(double spacing) {
		this.stickerGap = spacing;
		createPolys(true);
	}

	public void resetPuzzle() {
		createPolys(false);
	}

	private ArrayList<PuzzleTurn> turns = new ArrayList<PuzzleTurn>();
	public ArrayList<PuzzleTurn> getTurnHistory() {
		return turns;
	}
	protected void appendTurn(PuzzleTurn nextTurn) {
		nextTurn.updateInternalRepresentation(false);
		PuzzleTurn lastTurn = turns.isEmpty() ? null : turns.remove(turns.size() - 1);
		PuzzleTurn newTurn = nextTurn.mergeTurn(lastTurn);
		if(newTurn == null) {
			turns.add(lastTurn);
			turns.add(nextTurn);
		} else if(!newTurn.isNullTurn())
			turns.add(newTurn);
		
		if(animationQueue.isEmpty())
			animationQueue.add(new TurnAnimation(this));
		TurnAnimation lastAnim = animationQueue.get(animationQueue.size() - 1);
		if(!lastAnim.mergeTurn(nextTurn))
			animationQueue.add(new TurnAnimation(this, nextTurn));
		turner.start();
	}
	
	private Timer turner = new Timer(10, this);
	private ArrayList<TurnAnimation> animationQueue = new ArrayList<TurnAnimation>();
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == turner) {
			TurnAnimation anim = animationQueue.get(0);
			for(PuzzleTurn finished : anim.animate())
				fireStateChanged(finished);
			if(anim.isEmpty()) {
				animationQueue.remove(0);
				if(animationQueue.isEmpty())
					turner.stop();
			}
		} else if(e.getSource() == timer) {
			canvas.setDisplayString(getTime());
			canvas.repaint();
		}
	}

	private int framesPerAnimation = 5; //this is the number of animations per turn (less -> faster animation)
	public int getMaxFramesPerAnimation() {
		return 20;
	}
	public int getFramesPerAnimation() {
		return framesPerAnimation;
	}
	public void setFramesPerAnimation(int newRate) {
		framesPerAnimation = newRate;
	}
	
	private ArrayList<PuzzleStateChangeListener> stateListeners = new ArrayList<PuzzleStateChangeListener>();
	public void addStateChangeListener(PuzzleStateChangeListener l) {
		stateListeners.add(l);
	}
	public void fireStateChanged(PuzzleTurn turn) {
		for(PuzzleStateChangeListener l : stateListeners)
			l.puzzleStateChanged(this, turn);
		fireCanvasChange();
	}

	private String variation;
	{
		String[] vars = getPuzzleVariations();
		if(vars != null && vars.length > 0)
			variation = vars[0];
	}
	public void setPuzzleVariation(String variation) {
		this.variation = variation;
	}
	public final String getPuzzleVariation() {
		return variation;
	}

	protected int[] dimensions = null;
	public int[] getDimensions() {
		return dimensions;
	}
	public void setDimensions(int dimX, int dimY, int dimZ) {
		setDimensions(new int[] { dimX, dimY, dimZ });
	}
	public void setDimensions(int[] dims) {
		dimensions = dims;
		createPolys(false);
	}
	
	//*** To implement a custom twisty puzzle, you must override the following methods and provide a noarg constructor *** 
	//something like a cuboid would have this, whereas square one wouldn't
	protected int[] getDefaultXYZDimensions() {
		return null;
	}
	protected String[] getPuzzleVariations() {
		return null;
	}
	protected void createPolys(boolean copyOld) {
		if(!copyOld) {
			turns.clear();
			animationQueue.clear();
			turner.stop();
		}
		clearPolys();
	}
	public abstract void scramble();
	public abstract boolean isSolved();
	public abstract HashMap<String, Color> getDefaultColorScheme();
	//this sets the default angle to view the puzzle from (using Shape3D's setRotation() method)
	public abstract void resetRotation();
	
	private Properties keyProps = new Properties();
	public Properties getKeyboardLayout() {
		return keyProps;
	}
	public void setKeyboardLayout(Properties props) {
		keyProps = props;
	}
	{
		try {
			keyProps.load(this.getClass().getResourceAsStream("keys.properties"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(NullPointerException e) {
			System.err.println("keys.properties not found!");
			e.printStackTrace();
		}
	}
	public final void doTurn(KeyEvent e) {
		if(e.isAltDown()) return;
		String character = ""+e.getKeyChar();
		String turn = (String) keyProps.get(character);
		if(turn == null)
			turn = (String) keyProps.get(character.toLowerCase());
		if(turn == null) return;
		doTurn(turn);
	}
	public void puzzleStateChanged(TwistyPuzzle src, PuzzleTurn turn) {
		if(src.isSolved() && stop == -1)
			stopTimer();
	}
	private String getTime() {
		if(start == -1)
			return "";
		long time = stop;
		if(stop == -1)
			time = System.currentTimeMillis();
		return (time-start)/1000. + "";
	}
	private long start = -1, stop = -1;
	private void stopTimer() {
		stop = System.currentTimeMillis();
		timer.stop();
		canvas.setDisplayString(getTime());
	}
	private void startTimer() {
		start = System.currentTimeMillis();
		stop = -1;
		timer.start();
	}
	private void resetTimer() {
		stop = start = -1;
		timer.stop();
	}
	private Timer timer = new Timer(100, this);
	//returns true if the String was recognized as a turn
	public final boolean doTurn(String turn) {
		if(turn.equals("scramble")) {
			scramble();
			resetTimer();
			return true;
		} else if(turn.equals("time")) {
			if(!this.isSolved())
				startTimer();
			return true;
		}
		return doTurn2(turn);
	}
	protected abstract boolean doTurn2(String turn);
	public abstract String getState();
}
