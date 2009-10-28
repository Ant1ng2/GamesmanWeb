package edu.berkeley.gcweb.gui.gamescubeman.PuzzleUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.Timer;

import edu.berkeley.gcweb.gui.gamescubeman.Cuboid.Cuboid;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Canvas3D;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.Polygon3D;
import edu.berkeley.gcweb.gui.gamescubeman.ThreeD.RotationMatrix;

public class CornerChooser extends RollingJPanel implements MouseListener, MouseMotionListener, ComponentListener, KeyListener, ActionListener {
	private static final int PREFERRED_HEIGHT = 50;
	private static final int STICKER_LENGTH = (int) (.3* PREFERRED_HEIGHT);
	private JButton nullset;
	private HashMap<String, Color> colors;
	private Canvas3D paintCanvas;
	private AppletSettings settings;
	private HashMap<GeneralPath, String> StickerColor;
	private String selectedCorner = null;
	private HashMap<String, Rectangle2D> colorRectangles;
	private PuzzleCanvas puzzlecanvas;
	private HashMap<String,String> cornermap;
	private Cuboid cuboid;
	
	public CornerChooser(AppletSettings settings, HashMap<String, Color> colorScheme, Canvas3D paintCanvas, PuzzleCanvas puzzlecanvas){
		this.paintCanvas = paintCanvas;
		this.settings = settings;
		this.puzzlecanvas = puzzlecanvas;
		this.cuboid = (Cuboid) puzzlecanvas.getPuzzle();
		cornermap = new HashMap<String,String>();
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(100, PREFERRED_HEIGHT));
		setOpaque(true);
		
		
		nullset = new JButton("Clear");
		nullset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uniColor();
			}
		});
		nullset.setFocusable(false);
		nullset.setBounds(0, 0, 40, 20);
		add(nullset, BorderLayout.LINE_START);
		
		colors = colorScheme;
		addMouseListener(this);
		addMouseMotionListener(this);
		addComponentListener(this);
		paintCanvas.addKeyListener(this);
		setOpaque(true);
	}
//	private void getInput(){
//		
//	    String CurLine = ""; // Line read from standard in
//	       System.out.println("Enter a line of text (type 'quit' to exit): ");
//			       InputStreamReader converter = new InputStreamReader(System.in);
//			       BufferedReader in = new BufferedReader(converter);
//			     while (!(CurLine.equals("quit"))){
//			            try {
//							CurLine = in.readLine();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//			            if (!(CurLine.equals("quit"))){
//			              System.out.println("You typed: " + CurLine);
//			            }
//			      }
//	}
	private void uniColor(){
		for (PuzzleSticker[][] a : cuboid.cubeStickers)
			for(PuzzleSticker[] b: a)
				for (PuzzleSticker c: b){
					c.setFace(null);
				}
		RotationMatrix rm = new RotationMatrix(1, -45);
		rm = new RotationMatrix(0, -45).multiply(rm);
		cuboid.setRotation(rm);
		cuboid.fireStateChanged(null);
	}
	

	private GeneralPath drawSticker(Graphics2D g2d, float x, float y, double theta, Color c){
		GeneralPath p = new GeneralPath();
		p.moveTo(x,y);
		double t =Math.sin((Math.PI)/3);
		float a = (float) (STICKER_LENGTH*Math.sin(Math.PI/3*2));
		float b = (float) (STICKER_LENGTH*Math.sin(Math.PI/3));
		p.lineTo((float)(x-STICKER_LENGTH*Math.cos(Math.PI/6)),(float)(y-STICKER_LENGTH*Math.sin(Math.PI/6)));
		p.lineTo((float)(x),(float)(y-2*STICKER_LENGTH*Math.sin(Math.PI/6)));
		p.lineTo((float)(x+STICKER_LENGTH*Math.cos(Math.PI/6)),(float)(y-STICKER_LENGTH*Math.sin(Math.PI/6)));
		p.closePath();
		p.transform(AffineTransform.getRotateInstance(theta, x, y));
		g2d.setColor(c);
		g2d.fill(p);
		g2d.setColor(Color.WHITE);
		g2d.draw(p);
		
		return p;
	}
	private void drawCorner(Graphics2D g2d, float x, float y, String a, String b, String c){
		GeneralPath p;
		Color[] stickers = new Color[3];
		String faces = a+","+b+","+c;
		stickers[0]=colors.get(a);
		stickers[1]=colors.get(b);
		stickers[2]=colors.get(c);
		p=drawSticker(g2d,x,y,0,stickers[0]);
		StickerColor.put(p, faces);
		p=drawSticker(g2d,x,y,Math.PI/3*2,stickers[1]);
		StickerColor.put(p, faces);
		p=drawSticker(g2d,x,y,Math.PI/3*4,stickers[2]);
		StickerColor.put(p, faces);
		
		
	}
	private void paintkeyChar(String k, int x, int y, Graphics2D g2d){
		g2d.setColor(Color.WHITE);
		g2d.drawString(k, x-2, y);
	}
	
	private void cornertableUpdate(String stickers, String idx){

		cornermap.put(idx, stickers);
		
	}
	protected void paintComponent(Graphics g) {
		StickerColor = new HashMap<GeneralPath, String>();
		cornermap = new HashMap<String, String>();
		
		Graphics2D g2d = (Graphics2D) g;
		if(isOpaque()) {
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
		}
		double gap = (double) getWidth() / 15;
		int x = 40;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","F","L");
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","R","F");
		paintkeyChar("A", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("U,R,F","a");
		
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","B","R");
		paintkeyChar("S", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("U,B,R","s");
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"U","L","B");
		paintkeyChar("D", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("U,L,B","d");
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","L","F");
		paintkeyChar("J", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("D,L,F","j");
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","F","R");
		paintkeyChar("K", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("D,F,R","k");
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","R","B");
		paintkeyChar("L", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("D,R,B","l");
		
		x +=STICKER_LENGTH+gap;
		drawCorner(g2d,x,PREFERRED_HEIGHT/2,"D","F","L");
		paintkeyChar(";", x, PREFERRED_HEIGHT, g2d);
		cornertableUpdate("D,F,L",";");
		
		colorRectangles = new HashMap<String, Rectangle2D>();
		for(String face : colors.keySet()) {
			colorRectangles.put(face, new Rectangle2D.Double());
		}
		colorRectangles.put("null", new Rectangle2D.Double());
		
	}

	private GeneralPath getClickedGP(){
		Point p = getMousePosition();
		if(p == null) return null;
		for(GeneralPath face : StickerColor.keySet())
			if(face.contains(p)){
				return face;
			}
		return null;
	}
	public String getClickedFace() {
		GeneralPath g = getClickedGP();
		if (g == null) return null;
		return StickerColor.get(g);
		/*
		Point p = getMousePosition();
		System.out.println(p);
		if(p == null) return null;
		for(GeneralPath face : StickerColor.keySet())
			if(face.contains(p)){
				System.out.println(StickerColor.get(face));
				return StickerColor.get(face);
			}
		return null;*/
	}
	
	private void pieceRotate(GeneralPath g){
		if(StickerColor.containsKey(g)){
			String[] swap = StickerColor.get(g).split(",");
			System.out.print(StickerColor.get(g));
			StickerColor.put(g, swap[1]+","+swap[2]+","+swap[0]);
			System.out.print(StickerColor.get(g));
		}	
	}
	private void refreshCursor() {
		Cursor c = selectedCorner == null ? Cursor.getDefaultCursor() : createCursor(selectedCorner);
		this.setCursor(c);
		paintCanvas.setCursor(c);
		repaint();
	}
	
	private static final int CURSOR_SIZE = 32;
	private Cursor createCursor(String c) {
		String[] faces =getClickedFace().split(",");
		BufferedImage buffer = new BufferedImage((int) (2*CURSOR_SIZE*Math.cos(Math.PI/6)), (int) (3*CURSOR_SIZE*Math.sin(Math.PI/6)), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) buffer.createGraphics();
		
		drawCorner(g2d, (float)(CURSOR_SIZE*Math.cos(Math.PI/6)), (float)(CURSOR_SIZE*Math.sin(Math.PI/6)), faces[0],faces[1], faces[2]);
		
		Toolkit tool = Toolkit.getDefaultToolkit();
		return tool.createCustomCursor(buffer, new Point(0, 0), "bucket");
	}

	public void mouseClicked(MouseEvent e) {
		String face = getClickedFace();
		if(face != null)/*
		String[] faces = getClickedFace().split(",");
		Color[] face = new Color[3];
		face[0]=colors.get(faces[0]);
		face[1]=colors.get(faces[1]);
		face[2]=colors.get(faces[2]);		
		if(face != null) */{
			System.out.println("face is "+face);
			if (!face.equals(selectedCorner))
				selectedCorner = face;
			else{
				pieceRotate(getClickedGP());
				selectedCorner = getClickedFace();
			}
			refreshCursor();
			System.out.println("Is corner changed?"+ getClickedFace());
		}
	}
	
	public String getSelectedFace() {
		return selectedCorner;
	}
	
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseDragged(MouseEvent e) {
		repaint();
	}
	public void mouseMoved(MouseEvent e) {
		repaint();
	}
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void keyPressed(KeyEvent e) {
		String s = e.getKeyChar() + "";
		if (cornermap.containsKey(s)) {
			String[] cs = cornermap.get(s).split(",");
			PuzzleSticker[] ps = cuboid.getCorner(currentCorner);
			for (int i = 0; i < ps.length; i++) {
				ps[i].setFace(cs[i]);
			}
			cuboid.fireCanvasChange();
		} else if(e.getKeyCode() == KeyEvent.VK_ENTER)
			currentCorner++;
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			currentCorner--;
		System.out.println(e.getKeyCode() + " " + KeyEvent.VK_TAB);
		if(currentCorner < 0)
			currentCorner = 0;
		else if(currentCorner > 7)
			currentCorner = 7;
	
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent arg0) {}

	private Timer glowTimer = new Timer(100, this);
	private int currentCorner;
	public void setVisible(boolean visible){
		super.setVisible(visible);
		if(visible) {
			glowTimer.start();
			currentCorner = 0;
			uniColor();
			cuboid.setDisabled(true);
		} else {
			glowTimer.stop();
			cuboid.setRotation(cuboid.getPreferredViewAngle());
			cuboid.setDisabled(false);
			cuboid.fireStateChanged(null);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == glowTimer) {
			for(int i = 0; i < 8; i++) {
				for(PuzzleSticker poly : cuboid.getCorner(i)) {
					poly.setBorderColor(i == currentCorner ? new Color((int) System.currentTimeMillis()) : Color.BLACK);
					cuboid.fireCanvasChange();
				}
			}
		}
	}
}
