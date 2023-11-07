package main;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


class Pet{
	int x=(int)(Math.random()*500);
	int y=(int)(Math.random()*500);
	int size=0;
	String mode = "idle";
	String doing = "standing";
	String[] idleList = {"none", "none2", "lay", "sit", "swing",
			"standing","sitting","dancing","behind"};
	String[] goList = {"walkslow","walk","walk", "slide", "slide" };
 	boolean isJump = false;
	boolean front = false;
	boolean back = false;
	boolean flip = false;
	boolean slideEnd = false;
	boolean dragging = false;
	int tx = 0;
	int ty = 0;
	int jty = -5;
	int dx = 0;
	int dy = 0;
	boolean doend = false;
	boolean actChange = true;
	int needAct = 20;
	
	int imgcropx = 0;
	int imgcropy = 0;
	Random rd = new Random(System.currentTimeMillis());
	
	public void acting() {
		if(dragging) {
			mode = "idle";
			doing = "none";
			needAct = 1;

			
			
			
		}
		else
		{
			
		double n = Math.random();
		if(tx<0) {
			tx=0;
		}
		if(tx>Screen.res.width-100) {
			tx=Screen.res.width-100;
		}
		if(ty<0) {
			ty=0;
		}
		if(ty>Screen.res.height-100) {
			ty=Screen.res.height-100;
		}
		
		
		rd.setSeed(System.currentTimeMillis());
		if(needAct<1) {
			if(doend) {
				n=1;
				needAct = (int) (rd.nextInt(100))+5;
			}
		}
		else {
			needAct--;
		}

		if(isJump) {
			if(jty>-5) {
				
				y-=5*jty;
				jty--;
			}
			else {
				isJump = false;
				actChange = true;
			}
		}
		
			if(Math.random()>0.99) {
				jty=4;
				isJump = true;
				actChange = true;
			}
			if(mode == "idle") {
				if(n>0.99) {
					needAct = (int) (rd.nextInt(100))+5;
					if(Math.random()>0.5) {
						doing = idleList[rd.nextInt(9)];
						actChange = true;
						doend = false;
					}
					else{
						mode = "move";
						doing = "none";
					}
				}
				else {
					if(doend) {
						if(Math.random()>0.99) {
							doing = idleList[rd.nextInt(idleList.length)];
							actChange = true;
							doend = false;
						}
						else if(Math.random()>0.99) {
							
							mode = "move";
							doing = "none";
						}
					}
				}
			}
			else if(mode == "move") {
				if(doing == "none") {
					slideEnd = false;
					doend = false;
					actChange = true;
					if(Math.random()>4) {
						tx = rd.nextInt(200) - 100;
						ty = rd.nextInt(10) - 5;
					}
					else if(Math.random()>4) {
						tx = x + rd.nextInt(30) - 15;
						ty = y + rd.nextInt(100) - 50;
					}
					else {
						tx = x + rd.nextInt(300) - 150;
						ty = y + rd.nextInt(300) - 150;
					}
					
					
					if(tx<0) {
						tx=0;
					}
					if(tx>Screen.res.width) {
						tx=Screen.res.width;
					}
					if(ty<0) {
						ty=0;
					}
					if(ty>Screen.res.height) {
						ty=Screen.res.height;
					}
					
					if(y-ty<=0 && Math.abs(x-tx)<20) {
						front = true;
					}
					else if(y-ty>0 && Math.abs(x-tx)<20) {
						back = true;
					}
					else {
						if(x-tx>0) {
							flip = true;
						}
						else {
							flip = false;
						}
					}
					
					doing = goList[rd.nextInt(goList.length)];
					if(doing != "slide") {
						dx = (int)( 4 * Math.cos(Math.atan2((ty - y), (tx - x))) );
						dy = (int)( 4 * Math.sin(Math.atan2((ty - y), (tx - x))) );
					}
				}
				else if(doing == "slide") {
					x = x + (tx-x)/8;
					y = y + (ty-y)/8;
				}
				else if(doing == "walk") {
					dx = (int)( 4 * Math.cos(Math.atan2((ty - y), (tx - x))) );
					dy = (int)( 4 * Math.sin(Math.atan2((ty - y), (tx - x))) );
					x = x + dx;
					y = y + dy;
				}
				else if(doing == "walkslow") {
					dx = (int)( 4 * Math.cos(Math.atan2((ty - y), (tx - x))) );
					dy = (int)( 4 * Math.sin(Math.atan2((ty - y), (tx - x))) );
					x = x + dx/2;
					y = y + dy/2;
				}
				
				if(Math.abs(tx-x)<10 && Math.abs(ty-y)<10) {
					x=tx;
					y=ty;
					front = false;
					back = false;
					flip = false;
					mode = "idle";
					doend = true;
					doing = idleList[rd.nextInt(9)];
					slideEnd = true;
				}
			}
		}
	}
}


class Screen extends JFrame{
	

	public int fcount = 0;
	static Dimension res = Toolkit.getDefaultToolkit().getScreenSize();

	static int imgdx = 128;
	static int imgdy = 128;
	
	static boolean mouseDown = false;

	
	List<Pet> pets = new ArrayList<Pet>();
	Image petImg;
	Image cropImg;
	Image buffImg = new BufferedImage(res.width, res.height, BufferedImage.TYPE_INT_ARGB);
	Image cimg = new BufferedImage(imgdx, imgdy, BufferedImage.TYPE_INT_ARGB);
	Graphics Gc;
	JPanel p1 = new JPanel();
	 
	 
	public void init() {
		petImg = Toolkit.getDefaultToolkit().getImage("./src/main/imgs/penguin.png");
		
		
		Pet pet = new Pet();
		Pet pet2 = new Pet();
        pets.add(pet);
        pets.add(pet2);
	}
	Screen(){
    	init();
    	
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(null);
        this.setSize(res.width,res.height);
        this.setBackground(new Color(0, 0, 0, 0));
        this.setVisible(true);
        this.setLocation(0, 0);
        
       
        p1.setBounds(0, 0, res.width, res.height);
        p1.setBackground(new Color(0, 0, 0, 0));
        p1.setVisible(true);
        this.add(p1);
        
        
        JLabel jb1 = new JLabel("Press {esc} to exit.");
        jb1.setForeground(Color.WHITE);
        jb1.setBounds(0, 0, res.width, 30);
        jb1.setVisible(true);
        p1.add(jb1);
        
        Panel1 pn1 = new Panel1();
        pn1.setBounds(0, 0, res.width, res.height);
        pn1.setBackground(new Color(0, 0, 0, 0));
        pn1.setVisible(true);
        p1.add(pn1);
        
        
        fpsUpdate fpu = new fpsUpdate();
        fpu.start();

        
        System.out.println(pets);
    	while(true) {
    		for(Pet p : pets) {
    			if(p.dragging) {
        			p.x = ScreenPet.nowX-imgdx/4+5;
    				p.y = ScreenPet.nowY-imgdy/4+10;
        		}
    		}
    		if(fcount>1) {
    			pn1.repaint();
    			for(Pet p : pets) {
    				
    				p.acting();
        			setFrame(p);
        			dragCheck(p);
    			}
    			
    			fcount=0;
    		}	
    	}
    }
	public void dragCheck(Pet p) {
		if(mouseDown) {
			if(p.x < ScreenPet.nowX && ScreenPet.nowX < p.x + imgdx/2 && 
					p.y < ScreenPet.nowY && ScreenPet.nowY < p.y + imgdy/2) {
				if(!p.dragging) {
					p.actChange = true;
				}
				p.dragging = true;
			}
		}
		else {
			p.dragging = false;
		}
	}
	public void setFrame(Pet p, int x, int y, int tx) {
		if(p.actChange) {
			p.doend = false;
			p.imgcropx = x;
			p.imgcropy = y;
			p.actChange=false;
		}
		else {
			p.imgcropx ++;
			if(p.imgcropx>tx) {
				if(p.mode == "move") {
					if(p.doing == "slide") {
						if(!p.slideEnd) {
							p.imgcropx = tx;
						}
					}
					else {
						p.imgcropx=x;
					}
				}
				else {
					p.doend = true;
					p.imgcropx=x;
				}
				
			}
		}
	}
	public void setFrame(Pet p) {
		
		if(!p.dragging) {
		if(p.isJump) {
			setFrame(p, 0,9,8);
		}
		else {
			if(p.mode == "idle") {
				if(p.doing == "none") {
					setFrame(p, 0,8,0);
				}
				else if(p.doing == "none2") {
					setFrame(p, 0,9,0);
				}
				else if(p.doing == "lay") {
					setFrame(p, 9,0,9);
				}
				else if(p.doing == "sit") {
					setFrame(p, 7,8,7);
				}
				else if(p.doing == "swing") {
					
					setFrame(p, 0,3,7);
				}
				else if(p.doing == "standing") {
					setFrame(p, 0,8,6);
				}
				else if(p.doing == "sitting") {
					setFrame(p, 7,8,14);
				}
				else if(p.doing == "dancing") {
					setFrame(p, 0,6,5);
				}
				else if(p.doing == "behind") {
					setFrame(p, 0,7,9);
				}
			}
			else if(p.mode == "move") {
				if(p.doing == "slide") {
					setFrame(p, 0,1,2);
				}
				else 
				{
					if(p.back) {
						setFrame(p, 0,5,5);
					}
					else if(p.front) {
						setFrame(p, 6,5,10);
					}
					else {
						if(p.doing == "walk") {
							setFrame(p, 0,0,8);
						}
						else if(p.doing == "walkslow") {
							setFrame(p, 0,4,10);
						}
					}
				}
			}
		}
	}
		else {
			
			setFrame(p, 10,0,14);
		}
	}
    public class fpsUpdate extends Thread {
    	public void run() {  
            while(true) {
            	try {
        			Thread.sleep(33);
        		} catch (InterruptedException e) {
        			e.printStackTrace();
        		}
            	fcount++;
            }
        }
    }
    class Panel1 extends JPanel{
        public void paintComponent(Graphics g){
        	cimg = createImage(imgdx/2, imgdy/2);
        	cimg = new BufferedImage(imgdx/2, imgdy/2, BufferedImage.TYPE_INT_ARGB);
        	buffImg = createImage(res.width, res.height);
        	buffImg = new BufferedImage(res.width, res.height, BufferedImage.TYPE_INT_ARGB);
    		
            
    		for(Pet p:pets) {
    			Gc = cimg.getGraphics();
    			((Graphics2D) Gc).setBackground(new Color(0, 0, 0, 0));
    			Gc.clearRect(0, 0, imgdx/2, imgdy/2);
    			if(p.flip) {
        			Gc.drawImage(petImg, imgdx/2, 0, -imgdx/2, imgdy/2, 
        					(p.imgcropx) * imgdx, p.imgcropy * imgdy, (p.imgcropx+2) * imgdx, p.imgcropy * imgdy + imgdy, null);   
        		}
        		else {
        			Gc.drawImage(petImg, 0, 0, imgdx/2, imgdy/2, p.imgcropx * imgdx, 
                		p.imgcropy * imgdy, p.imgcropx * imgdx + imgdx, p.imgcropy * imgdy + imgdy, null);   
        		}
        		
        		Gc = buffImg.getGraphics();
        		Gc.drawImage(cimg, p.x, p.y, null);
    		}
    		
            
            super.paintComponent(g);
            
            ((Graphics2D) g).setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, res.width, res.height);
            
    		g.drawImage(buffImg, 0, 0, null);
        }
    }
	
}

public class ScreenPet implements NativeKeyListener, NativeMouseInputListener {
	static int nowX = 0;
	static int nowY = 0;
	public void nativeMousePressed(NativeMouseEvent e) {
		Screen.mouseDown = true;
	}
	public void nativeMouseReleased(NativeMouseEvent e) {
		Screen.mouseDown = false;
	}
	
	public void nativeMouseMoved(NativeMouseEvent e) {
		nowX = 95*e.getX()/120;
		nowY = 95*e.getY()/120;
        
    }

    public void nativeMouseDragged(NativeMouseEvent e) {
    	nowX = 95*e.getX()/120;
    	nowY = 95*e.getY()/120;
    	
    }

	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_S) {
			
		}
		if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
			System.exit(0);
		}
	}
	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_A) {
			
		}
	}
	public void nativeKeyTyped(NativeKeyEvent e) {
		
	}
	public static void main(String[] args) {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}
		ScreenPet example = new ScreenPet();
		GlobalScreen.addNativeKeyListener(example);
		GlobalScreen.addNativeMouseListener(example);
		GlobalScreen.addNativeMouseMotionListener(example);
	    
		nowX=0;
		nowY=0;
		new Screen();
	}
}

