import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.image.*;

/***********************************
* projectMain.java
*
* COMP 4490
* INSTRUCTOR Dr. Desmond Walton
* Term Project
* Bradley Macsymic
*
* This is my term project. It preloads in 3 shapes, all triangles. My shape inspiration was the tri-force
* from the popular legend of Zelda game series. My program preloads an initial linear and bspline path.
* they are populated on a 2D plane, in which the user can specify a new linear path (by drawing) and a new
* b-spline path (again, my drawing). My b-spline was calculated using my A3Q5 answer and does end point
* interpolation. You user can Reset (File -> Reset) to reset the paths back to the initial linear and b-spline
* paths. Upon showing the 2D projecting, the user can then specify a new Linear or B-spline path using the
* appropriate methods that pop up in the navigation menu at the top of the window. My program can do xy, and
* xz projections. The delay time and number of animations frames can be changes as well. For the B-spline
* the user can change the number of points per segment, or they can display/undisplay the control polygon for the
* b-spline. The predefined (initial) b-spline path can not show it's inital control polygon, since it has none.
* It also cannot display/undisplay it's control polygon since it doesn't have one.

** I have added in System.out.println comments when the user changes functions on the program to show the user
* that is it actually changed.
***********************************/
//----------------- main class --------------------
public class projectMain
{
    public static void main(String[] args)
    {
        new Project_Frame();
    }
} //end ProjectMain

// ----------------- frame (window) class --------------------
class Project_Frame
{
    private JFrame frame;
    private JTextField inputField, msgField;
    private JMenuBar menuBar;
    private JMenu crtlPntsBsplineMenu, BsplinePathOptions;
    private JMenu crtlPntsLinearMenu;
    private JMenuItem exitMenuItem, resetMenuItem,
            numAnimFramesMenuItem, delayMenuItem, path1MenuItem, path2MenuItem,
            doAnimMenuItem;
    private JMenuItem showProjMenuItem, xyProjMenuItem, xzProjMenuItem, newPolyMenuItem,
            polyColourMenuItem, mvPtMenuItem, rmvPtMenuItem, addVertMenuItem, showHideVertices;
    private JMenuItem numberPointsPerSegment, showHideControlPolygon;
    private JMenuItem newLinearLineMenuItem;
    private boolean getFile = false, fileErr = false, anim = false,
            changeNumAnimFrames = false, changeDelay = false;
    private boolean reset = false, inputNumPolyPts = false,
            inputPolyPt = false, inputPolyColour = false, mvPt = false,
            reposition = false, rmvPt = false, addPt = false;
    private boolean displayVerts = false;
    private boolean inputPointsPerSeg = false;
    private boolean showControlPoly = true;
    private boolean showProjection = false;
    private boolean inputLinearPath = false;
    private boolean doBasicLinearPath = true;
    private boolean doBasicBsplinePath = true;
    private String input;
    private String projectionType = "xy";
    private final int FW = 500;
    private final int FH = 500;
    private final int MIN_NUM_POLY_PTS = 3;
    private final int MAX_NUM_POLY_PTS = 10;
    private final int MAX_NUM_KNOTS = 20;
    private int flyBy = 0, numAnimFrames = 75, flyPath = 1;
    private int nF;
    private int numPolyPts = 0, iPolyPts = 0;
    private int numPointsPerSeg = 64;
    private int currLinearPoint = 0;
    private int CPoints = 0;
    private long delayTime = 75;// milliseconds
    private double dt = 1.0 / numAnimFrames;
    private Color bg;
    private Color polyColour = Color.cyan;
    private Vec3[] faceNml, faceC;
    private Vec3 massCentre;
    private Vec3[] P = new Vec3[MAX_NUM_POLY_PTS];
    private Vec3[] C;
    private Vec3[] VArray = new Vec3[4];
    private Vec3[][] xyFaces;
    private Vec3[][] xzFaces;
    private Vec3 linearPnt1 = new Vec3(6.25, -1.5,  1.5);
    private Vec3 linearPnt2 = new Vec3(0.0,   4.75, 1.5);
    private VertFace fL, fL2, fL3;
    private Polygon[] model;
    private Polygon[] model2;
    private Polygon[] model3;
    private Project_Panel flyByPanel;
    private double[][] zList;
    private int[][] faceRenderOrder;

    Project_Frame()
    {
        // Create and set up the window.
        frame = new JFrame("COMP 4490 --- Term Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FW, FH);

        //Set frame to be at the exact middle of the user's screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setBounds( ((int)screenSize.getWidth()/2)-(FW/2), ((int)screenSize.getHeight()/2)-(FH/2), FW, FH);

        // make menus
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenu animMenu = new JMenu("3D Animation");
        menuBar.add(animMenu);

        JMenu ProjectionMenu = new JMenu("2D Projection");
        menuBar.add(ProjectionMenu);
        MenuListener listener = new MenuListener();
        crtlPntsBsplineMenu = new JMenu("Ctrl Pnts B-spline");
        BsplinePathOptions = new JMenu("B-spline Camera Path");
        crtlPntsLinearMenu = new JMenu("Ctrl Pnts Linear");

        // File menu items
        resetMenuItem = new JMenuItem("Reset");
        fileMenu.add(resetMenuItem);
        resetMenuItem.addActionListener(listener);
        exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(listener);

        // Animation menu items
        numAnimFramesMenuItem = new JMenuItem("Change # of animation frames");
        animMenu.add(numAnimFramesMenuItem);
        numAnimFramesMenuItem.addActionListener(listener);
        delayMenuItem = new JMenuItem("Change delay time");
        animMenu.add(delayMenuItem);
        delayMenuItem.addActionListener(listener);
        path1MenuItem = new JMenuItem("Path 1 (Linear)");
        animMenu.add(path1MenuItem);
        path1MenuItem.addActionListener(listener);
        path2MenuItem = new JMenuItem("Path 2 (B-Spline)");
        animMenu.add(path2MenuItem);
        path2MenuItem.addActionListener(listener);
        doAnimMenuItem = new JMenuItem("Do animation");
        animMenu.add(doAnimMenuItem);
        doAnimMenuItem.addActionListener(listener);

        //2d Projection Menu Items
        showProjMenuItem = new JMenuItem("Show/Hide 2D Projection");
        ProjectionMenu.add(showProjMenuItem);
        showProjMenuItem.addActionListener(listener);
        xyProjMenuItem = new JMenuItem("x-y Projection");
        ProjectionMenu.add(xyProjMenuItem);
        xyProjMenuItem.addActionListener(listener);
        xzProjMenuItem = new JMenuItem("x-z Projection");
        ProjectionMenu.add(xzProjMenuItem);
        xzProjMenuItem.addActionListener(listener);

        //Ctrl Pnts for B-spline
        newPolyMenuItem = new JMenuItem("New Polyline");
        crtlPntsBsplineMenu.add(newPolyMenuItem);
        newPolyMenuItem.addActionListener(listener);
        showHideVertices = new JMenuItem("Show/Hide Vertices");
        crtlPntsBsplineMenu.add(showHideVertices);
        showHideVertices.addActionListener(listener);
        polyColourMenuItem = new JMenuItem("Polyline Colour");
        crtlPntsBsplineMenu.add(polyColourMenuItem);
        polyColourMenuItem.addActionListener(listener);
        mvPtMenuItem = new JMenuItem("Move a vertex");
        crtlPntsBsplineMenu.add(mvPtMenuItem);
        mvPtMenuItem.addActionListener(listener);
        rmvPtMenuItem = new JMenuItem("Remove a vertex");
        crtlPntsBsplineMenu.add(rmvPtMenuItem);
        rmvPtMenuItem.addActionListener(listener);
        addVertMenuItem = new JMenuItem("Add a vertex (to the end)");
        crtlPntsBsplineMenu.add(addVertMenuItem);
        addVertMenuItem.addActionListener(listener);

        //B-spline Camera Path(End Pt Interp)
        numberPointsPerSegment = new JMenuItem("Number Of Points per Segment");
        BsplinePathOptions.add(numberPointsPerSegment);
        numberPointsPerSegment.addActionListener(listener);
        showHideControlPolygon = new JMenuItem("Show/Hide Control Polygon");
        BsplinePathOptions.add(showHideControlPolygon);
        showHideControlPolygon.addActionListener(listener);

        //Crtl Pnts for Linear camera path
        newLinearLineMenuItem = new JMenuItem("New Linear Line");
        crtlPntsLinearMenu.add(newLinearLineMenuItem);
        newLinearLineMenuItem.addActionListener(listener);

        // create a message box
        msgField = new JTextField();
        msgField.setText("");
        msgField.setEditable(false);

        // create a text input box
        inputField = new JTextField();
        inputField.addActionListener(new TextFieldListener());

        // organize window
        Container contentPane = frame.getContentPane();
        flyByPanel = new Project_Panel();
        contentPane.add(msgField, "North");
        contentPane.add(flyByPanel, "Center");
        contentPane.add(inputField, "South");
        frame.setVisible(true);
        bg = frame.getBackground();

        //Load in shapes now
        readFiles();

    } //end Project_Frame

    //------- (inner) class to manage menu calls ----------

    private class MenuListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            double maxW = 500;
            double maxH = 500;

            Object source = event.getSource();
            if (source == resetMenuItem)
            {
                C = loadInPredefinedBspline();
                resetLinearPath();
                System.out.println("Resetting to default linear and b-spline paths.");
                flyBy = 0;
                doBasicBsplinePath = true;
                doBasicLinearPath = true;
                currLinearPoint = 0;
                numPolyPts = 0;
                iPolyPts = 0;
            }
            if (source == exitMenuItem)
                System.exit(0);
            if (source == numAnimFramesMenuItem && !getFile && !changeDelay)
                changeNumAnimFrames = true;
            if (source == delayMenuItem && !getFile && !changeNumAnimFrames)
                changeDelay = true;
            if (source == path1MenuItem)
            {
                flyPath = 1;
                if(showProjection)
                {
                    menuBar.add(crtlPntsLinearMenu);
                    menuBar.remove(crtlPntsBsplineMenu);
                    menuBar.remove(BsplinePathOptions);
                    menuBar.revalidate();
                }
                System.out.println("Now changing to linear camera path.");


                menuBar.revalidate();
            }
            if (source == path2MenuItem)
            {
                //Initialize the predefined b-spline
                if(doBasicBsplinePath)
                    C = loadInPredefinedBspline();

                flyPath = 2;
                if(showProjection)
                {
                    menuBar.remove(crtlPntsLinearMenu);
                    menuBar.add(crtlPntsBsplineMenu);
                    menuBar.add(BsplinePathOptions);
                    menuBar.revalidate();
                }
                System.out.println("Now changing to cubic b-spline camera path.");
            }
            if (source == showProjMenuItem)
            {
                if(showProjection == true)
                {
                    showProjection = false;
                    System.out.println("Now hiding " + projectionType + " 2D projection.");

                    if(flyPath == 1)
                    {
                        menuBar.remove(crtlPntsLinearMenu);
                        menuBar.revalidate();
                    }

                    if(flyPath == 2)
                    {
                        if(showControlPoly == true)
                        {
                            System.out.println("Hiding control polygon...");
                        }
                        menuBar.remove(crtlPntsLinearMenu);
                        menuBar.remove(crtlPntsBsplineMenu);
                        menuBar.remove(BsplinePathOptions);
                        menuBar.revalidate();
                    }
                }
                else
                {
                    showProjection = true;
                    System.out.println("Now showing " + projectionType + " 2D projection.");

                    if(flyPath == 1)
                    {
                        menuBar.add(crtlPntsLinearMenu);
                        menuBar.remove(crtlPntsBsplineMenu);
                        menuBar.remove(BsplinePathOptions);
                        menuBar.revalidate();
                    }

                    if(flyPath == 2)
                    {
                        if(showControlPoly == true)
                        {
                            System.out.println("Showing control polygon...");
                        }
                        menuBar.remove(crtlPntsLinearMenu);
                        menuBar.add(crtlPntsBsplineMenu);
                        menuBar.add(BsplinePathOptions);
                        menuBar.revalidate();

                        //Initialize the predefined b-spline
                        if(doBasicBsplinePath)
                            C = loadInPredefinedBspline();
                    }

                }
            }
            if (source == xyProjMenuItem)
            {
                projectionType = "xy";
                System.out.println("Projection changed to " + projectionType + " coordinates.");
            }
            if (source == xzProjMenuItem)
            {
                projectionType = "xz";
                System.out.println("Projection changed to " + projectionType + " coordinates.");
            }
            if (source == doAnimMenuItem)
            {
                if(showProjection == true)
                {
                    showProjection = false;
                    System.out.println("Now hiding " + projectionType + " 2D projection.");
                    if(flyPath == 1)
                    {
                        menuBar.remove(crtlPntsLinearMenu);
                        menuBar.revalidate();
                    }
                    if(flyPath == 2)
                    {
                        menuBar.remove(crtlPntsBsplineMenu);
                        menuBar.remove(BsplinePathOptions);
                        menuBar.revalidate();
                    }
                }
                if(flyPath == 1)
                {
                    if(doBasicLinearPath)
                        System.out.println("Doing predefined Linear Path.");
                    else
                        System.out.println("Doing custom Linear Path.");
                }

                if(flyPath == 2)
                {
                    if(doBasicBsplinePath)
                    {
                        System.out.println("Doing predefined B-spline Path.");
                        C = loadInPredefinedBspline();
                    }
                    else
                        System.out.println("Doing custom B-spline Path.");
                }

                // this is where the animation is done - it keeps calling
                // paint()
                Graphics g = flyByPanel.getGraphics();
                anim = true;
                showProjection = false;
                flyBy = -1;
                for (int i = 0; i < numAnimFrames; i++)
                {
                    flyBy++;
                    delay(delayTime);
                    flyByPanel.paint(g);
                } // an animation frame i
            } // do animation

            else if (source == newPolyMenuItem)
            {
                inputNumPolyPts = true;
                inputPolyColour = false;
                numPolyPts = 0;
                iPolyPts = 0;
                mvPt = false;
                reposition = false;
                rmvPt = false;
                addPt = false;
                inputPointsPerSeg = false;
                doBasicBsplinePath = false;
            }

            else if (source == newLinearLineMenuItem)
            {
                inputLinearPath = true;
                currLinearPoint = 0;
                doBasicLinearPath = false;
            }
            else if (source == showHideVertices)
            {
                if (displayVerts == true)
                {
                    System.out.println("Hiding vertices...");
                    displayVerts = false;
                }
                else
                {
                    System.out.println("Showing vertices...");
                    displayVerts = true;
                }
            }
            else if (source == polyColourMenuItem)
            {
                inputNumPolyPts = false;
                inputPolyColour = true;
                mvPt = false;
                reposition = false;
                rmvPt = false;
                addPt = false;
                inputPointsPerSeg = false;
            }
            else if (source == mvPtMenuItem && !inputNumPolyPts
                    && iPolyPts > 0 && !reposition && !rmvPt)
            {
                mvPt = true;
                inputPolyPt = false;
                rmvPt = false;
                reposition = false;
                addPt = false;
                inputPointsPerSeg = false;
            }
            else if (source == rmvPtMenuItem && !inputNumPolyPts
                    && iPolyPts > 0 && !reposition && !mvPt)
            {
                rmvPt = true;
                inputPolyPt = false;
                mvPt = false;
                reposition = false;
                addPt = false;
                inputPointsPerSeg = false;
            }
            else if (source == addVertMenuItem
                    && numPolyPts < MAX_NUM_POLY_PTS)
            {
                rmvPt = false;
                inputPolyPt = true;
                mvPt = false;
                reposition = false;
                numPolyPts++;
                addPt = true;
                inputPointsPerSeg = false;
            }
            else if (source == numberPointsPerSegment)
            {
                inputPointsPerSeg = true;
            }

            else if (source == showHideControlPolygon)
            {
                if(showControlPoly == true)
                {
                    showControlPoly = false;
                    System.out.println("Hiding control polygon...");
                }
                else
                {
                    showControlPoly = true;
                    System.out.println("Showing control polygon...");
                }
            }

            flyBy = 0;
            frame.repaint();
        } // end actionPerformed
        public Vec3 [] loadInPredefinedBspline()
        {
            Vec3[] newCArray = new Vec3[316];
            int arrayPos = 0;
            FileReader file;
            BufferedReader buff;
            String line;
            String[] tokens;

            try
            {
                file = new FileReader("predefinedBSpline.txt");
                buff = new BufferedReader(file);

                while ((line = buff.readLine()) != null)
                {
                    tokens = line.split(",");
                    newCArray[arrayPos] = new Vec3(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]),
                                                   Double.parseDouble(tokens[2]));
                    arrayPos++;
                }

                CPoints = arrayPos;
            }
            catch(IOException ioe)
            {
                ioe.printStackTrace();
            }


            return newCArray;
        }//loadInPredefinedBspline

        public void resetLinearPath()
        {
            linearPnt1 = new Vec3(6.25, -1.5,  1.5);
            linearPnt2 = new Vec3(0.0,   4.75, 1.5);
        }

    } // end class MenuListener

    // ------- (inner) class to manage text input ------------

    class TextFieldListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            input = inputField.getText();
            if (changeNumAnimFrames)
            {
                numAnimFrames = Integer.parseInt(input);
                dt = 1.0 / numAnimFrames;
                changeNumAnimFrames = false;
            }
            else if (changeDelay)
            {
                delayTime = (long) Integer.parseInt(input);
                changeDelay = false;
            }
            if (inputNumPolyPts && !inputPolyColour)
            {
                numPolyPts = Integer.parseInt(input);
                if (numPolyPts < MIN_NUM_POLY_PTS)
                    numPolyPts = MIN_NUM_POLY_PTS;
                if (numPolyPts > MAX_NUM_POLY_PTS)
                    numPolyPts = MAX_NUM_POLY_PTS;
                inputNumPolyPts = false;
                inputPolyPt = true;
            }
            else if (inputPolyColour && !inputNumPolyPts)
            {
                int iColour = Integer.parseInt(input);
                if (iColour == 0)
                    polyColour = Color.black;
                else if (iColour == 1)
                    polyColour = Color.red;
                else if (iColour == 2)
                    polyColour = Color.green;
                else
                    polyColour = Color.cyan;
                inputPolyColour = false;
            }
            else if (inputPointsPerSeg)
            {
                if(Integer.parseInt(input) < 64)
                    numPointsPerSeg = 64;
                else if (Integer.parseInt(input) > 100)
                    numPointsPerSeg = 100;
                else
                    numPointsPerSeg = Integer.parseInt(input);

                inputPointsPerSeg = false;
                System.out.println("Number of points per segment is now " + numPointsPerSeg + ".");
            }

            else
                ;
            inputField.setText("");
            frame.repaint();
        } // end actionPerformed
    } // end TextFieldListener

    public Vec3 findNmlsC(VertFace fLCurrent)
    {
        Vec3 massCentreCurrent = new Vec3();
        for (int i = 0; i < fLCurrent.getNumFaces(); i++)
        {
            Vec3 S = new Vec3();
            int n = fLCurrent.getNumVertsOnFace(i);
            for (int j = 1; j < n; j++)
            {
                Vec3 P = fLCurrent.getVertOnFace(i, j);
                S = Vec3Ops.add(S, P);
            }
            faceC[i] = Vec3Ops.mul(1.0 / n, S);
            faceNml[i] = fLCurrent.getUnitNormal(i);
        }
        for (int i = 0; i < fLCurrent.getNumVerts(); i++)
            massCentreCurrent = Vec3Ops.add(massCentreCurrent, fLCurrent.getVert(i));
        massCentreCurrent = Vec3Ops.mul(1.0 / fLCurrent.getNumVerts(), massCentreCurrent);

        return massCentreCurrent;
    }//findNmlsC

    void readFiles()
    {
        Vec3 centerOfFirst;
        Vec3 centerOfSecond;
        Vec3 centerOfThird;
        try
        {
            fL = new VertFace("object1.dat");
            fL2 = new VertFace("object2.dat");
            fL3 = new VertFace("object3.dat");
        }
        catch (FileNotFoundException exception)
        {
            fileErr = true;
        }

        faceNml = new Vec3[fL.getNumFaces()];
        faceC = new Vec3[fL.getNumFaces()];

        //Calculate the center of all masses, that is all triangles
        centerOfFirst = findNmlsC(fL);
        centerOfSecond = findNmlsC(fL);
        centerOfThird = findNmlsC(fL);

        massCentre = new Vec3();
        massCentre = Vec3Ops.add(centerOfFirst, centerOfSecond);
        massCentre = Vec3Ops.add(centerOfSecond, centerOfThird);

        //Divide by 1/3, since three objects
        massCentre = Vec3Ops.mul((1.0/3.0), massCentre);

        getFile = false;
        flyBy = 0;
    }//readFile

    public void delay(long millisec)
    {
        // Purpose: produce a delay in milliseconds
        // Input: number of millisecond delay, clock
        long time1, time2;

        time1 = System.currentTimeMillis();
        time2 = time1;
        while (time2 - time1 < millisec)
            time2 = System.currentTimeMillis();
    }//delay

    public void drawVec3Poly(Graphics2D g2, Color colour, int n, Vec3[] P, boolean showVerts)
    {
        g2.setColor(colour);
        //System.out.println(n);
        for (int i = 0; i < n; i++)
        {
            //gets the size of the dot
            double dotX = 0.5 * StdG.DOT_SIZE * StdG.get_xScale();
            double dotY = 0.5 * StdG.DOT_SIZE * StdG.get_yScale();
            //Display the verts if the menu item was recently changed to do so, otherwise don't
            if(showVerts)
                g2.fill(new Ellipse2D.Double(StdG.sX(P[i].x - dotX), StdG
                    .sY(P[i].y + dotY), StdG.DOT_SIZE, StdG.DOT_SIZE));

            //Draws the line based on the standard math coordinates
            if (i > 0)
            {
                g2.draw(new Line2D.Double(StdG.sX(P[i - 1].x), StdG
                        .sY(P[i - 1].y), StdG.sX(P[i].x), StdG.sY(P[i].y)));
            }
        }
    }//drawVec3Poly

    // ------- (inner) class to manage drawing ------------
    class Project_Panel extends JPanel
    {
        private Vec3 U, V, N, r;
        double e = 0.25;
        private Vec3 L = new Vec3(1, 0.8, 0.6);
        private double Wdu, Wdv, Wl = -0.25, Wt = -0.3, Wr = 0.375, Wb = 0.325;
        private final double X_MIN = -5, X_MAX = 5, Y_MIN = -5, Y_MAX = 5;
        private double[] shade;
        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics2D buffer;
        Rectangle area;


        private String colourPrompt1 = "Select colour by number: ";
        private String colourPrompt2 = "0 = black, 1 = red, 2 = green";
        private int select;
        private double Px, Py;

        Project_Panel()
        {

            // handle (non-menu, non-text box) mouse calls
            addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent event)
                {
                    double maxW = getWidth() - 1;
                    double maxH = getHeight() - 1;
                    StdG.initG(X_MIN, X_MAX, Y_MIN, Y_MAX, maxW, maxH, true);
                    Px = (double) event.getX();
                    Py = (double) event.getY();
                    if (inputPolyPt && iPolyPts < numPolyPts)
                    {
                        doBasicBsplinePath = false;
                        P[iPolyPts] = new Vec3(StdG.fx(Px), StdG.fy(Py), 0);
                        iPolyPts++;
                        if (iPolyPts >= numPolyPts)
                            inputPolyPt = false;
                    }
                    else if (mvPt && !inputPolyPt)
                    {
                        select = findVertex(P);
                        if (select >= 0 && select < iPolyPts)
                        {
                            mvPt = false;
                            reposition = true;
                        }
                    }
                    else if (reposition)
                    {
                        P[select] = new Vec3(StdG.fx(Px), StdG.fy(Py), 0);
                        reposition = false;
                    }
                    else if (rmvPt)
                    {
                        select = findVertex(P);
                        if (select >= 0 && select < numPolyPts)
                        {
                            for (int i = select + 1; i < numPolyPts; i++)
                                P[i - 1] = P[i];
                            numPolyPts--;
                            iPolyPts--;
                            rmvPt = false;
                        }//if
                    }//elseif

                    else if (inputLinearPath)
                    {
                        if(currLinearPoint == 0)
                        {
                            linearPnt1 = new Vec3(StdG.fx(Px), StdG.fy(Py), 0);
                        }//if

                        if(currLinearPoint == 1)
                        {
                            linearPnt2 = new Vec3(StdG.fx(Px), StdG.fy(Py), 0);
                            inputLinearPath = false;
                            doBasicLinearPath = false;
                        }//if
                        currLinearPoint++;
                    }//elseif

                    repaint();
                } //mousePressed
            }); //addMouseListener
        }//constructor Project_Panel

        public void paint(Graphics g)
        {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Dimension dim = getSize();
            int sWidth = dim.width;
            int sHeight = dim.height;

            // initialize (standard) math coordt system
            double maxW = getWidth() - 1;
            double maxH = getHeight() - 1;
            StdG.initG(X_MIN, X_MAX, Y_MIN, Y_MAX, maxW, maxH, true);

            // draw polyline
            if(flyPath == 2 && showProjection && showControlPoly == true && doBasicBsplinePath == false)
                drawVec3Poly(g2, polyColour, iPolyPts, P, displayVerts);

            // create off-screen buffer
            bi = (BufferedImage) createImage(sWidth, sHeight);
            buffer = bi.createGraphics();
            area = new Rectangle(dim);

            // mark point to be moved
            if (reposition)
            {
                g2.setColor(Color.blue);
                g2.fill(new Ellipse2D.Double(Px - 0.75 * StdG.DOT_SIZE, Py
                        - 0.75 * StdG.DOT_SIZE, 1.5 * StdG.DOT_SIZE,
                        1.5 * StdG.DOT_SIZE));
                repaint();
            }

            // mark end of polyline for new vertex to be added
            if (addPt)
            {
                g2.setColor(Color.blue);
                g2.fill(new Ellipse2D.Double(StdG.sX(P[iPolyPts - 1].x) - 0.75
                        * StdG.DOT_SIZE, StdG.sY(P[iPolyPts - 1].y) - 0.75
                        * StdG.DOT_SIZE, 1.5 * StdG.DOT_SIZE,
                        1.5 * StdG.DOT_SIZE));
                addPt = false;
                repaint();
            }
            if(inputLinearPath && (currLinearPoint == 1))
            {
                doBasicLinearPath = false;
                g2.setColor(Color.blue);
                g2.fill(new Ellipse2D.Double(Px - 0.75 * StdG.DOT_SIZE, Py
                        - 0.75 * StdG.DOT_SIZE, 1.5 * StdG.DOT_SIZE,
                        1.5 * StdG.DOT_SIZE));
                repaint();
            }

            // update message box
            if (numPolyPts == 0 && !inputNumPolyPts && !inputPolyColour && !inputPointsPerSeg && !inputLinearPath &&
                !changeNumAnimFrames && !changeDelay)
                msgField.setText("Select a menu item.");
            //else if
            else if (inputNumPolyPts && !inputPolyColour)
                msgField.setText("Enter number of points for polyline");
            else if (inputPolyColour && !inputNumPolyPts)
                msgField.setText(colourPrompt1 + colourPrompt2);
            else if (iPolyPts < numPolyPts && !inputNumPolyPts
                    && !inputPolyColour)
                msgField.setText("Click point P[" + iPolyPts + "]");
            else if (inputLinearPath)
                msgField.setText("Click point P[" + currLinearPoint + "]");
            else if (mvPt)
                msgField.setText("Click vertex to be moved");
            else if (reposition)
                msgField.setText("Click new position of vertex");
            else if (rmvPt)
                msgField.setText("Click vertex to be removed");

            else if (inputPointsPerSeg)
                msgField.setText("Enter a number of points per segment (64 <= n <= 100)");

            else if (fileErr)
                msgField.setText("File not found");
            else if (changeNumAnimFrames)
                msgField.setText("Enter new number of animation frames (was "
                        + numAnimFrames + ")");
            else if (changeDelay)
                msgField.setText("Enter new delay time in milliseconds (was "
                        + delayTime + ")");
            else
                msgField.setText(" ");

            //Show 2d projection
            if(showProjection && fL != null)
            {

                //draw axis
                g2.setColor(Color.gray);
                g2.draw(new Line2D.Double(StdG.sX(0), StdG.sY(Y_MIN), StdG.sX(0), StdG.sY(Y_MAX)));
                g2.draw(new Line2D.Double(StdG.sX(X_MIN), StdG.sY(0), StdG.sX(X_MAX), StdG.sY(0)));

                if(projectionType.equals("xy"))
                {
                    g2.drawString("Y Axis", (int)StdG.sX(0)+5, (int)StdG.sY(Y_MIN));
                    g2.drawString("X Axis", (int)StdG.sX(X_MAX)+5, (int)StdG.sY(0));
                }
                else if(projectionType.equals("xz"))
                {
                    g2.drawString("Z Axis", (int)StdG.sX(0)+5, (int)StdG.sY(Y_MIN));
                    g2.drawString("X Axis", (int)StdG.sX(X_MAX)+5, (int)StdG.sY(0));
                }

                //Draws all three shapes/objects on the 2D projection plane
                drawFaces2DProjection(fL, g2);
                drawFaces2DProjection(fL2, g2);
                drawFaces2DProjection(fL3, g2);

                //Draw the liner camera path
                if(flyPath == 1)
                {
                    if(doBasicLinearPath)
                    {
                        linearPnt1 = new Vec3(6.25, -1.5,  1.5);
                        linearPnt2 = new Vec3(0.0,   4.75, 1.5);
                        g2.draw(new Line2D.Double(StdG.sX(linearPnt1.x), StdG.sY(linearPnt1.y),
                                                  StdG.sX(linearPnt2.x), StdG.sY(linearPnt2.y)));
                    }

                    else if(currLinearPoint == 2)
                    {
                        g2.draw(new Line2D.Double(StdG.sX(linearPnt1.x), StdG.sY(linearPnt1.y),
                                                  StdG.sX(linearPnt2.x), StdG.sY(linearPnt2.y)));
                    }
                }

                //Draw the cubic b-spline camera path (Going to do end point interpolation)
                else if(flyPath == 2)
                {
                    //Still draw out the predefined bspline
                    if(doBasicBsplinePath)
                    {
                        drawVec3Poly(g2, Color.black, C.length-1, C, false);
                    }
                    // draw polyline
                    if(showControlPoly == true && !doBasicBsplinePath)
                        drawVec3Poly(g2, polyColour, iPolyPts, P, displayVerts && !doBasicBsplinePath);

                    //No more points to add, generate uniform cubic b-spline curve
                    if(numPolyPts >= 3 && iPolyPts == numPolyPts && !doBasicBsplinePath)
                    {
                        int L = numPolyPts-1;
                        int tPos = 0;
                        int m = 4;
                        int numOfSegments = L-m+2;
                        int numOfKnots = L+1+m;
                        //number of knots = #control verts + order
                        double T[] = new double[numOfKnots];
                        //(2) generate knot set
                        for(int i = 0; i <= L-2; i++)
                        {
                            //4 of the 0 knots
                            if(i == 0)
                            {
                                T[tPos++] = 0;
                                T[tPos++] = 0;
                                T[tPos++] = 0;
                                T[tPos++] = 0;
                            }

                            //4 of the L-2 knots
                            else if(i == (L-2))
                            {
                                T[tPos++] = L-2;
                                T[tPos++] = L-2;
                                T[tPos++] = L-2;
                                T[tPos++] = L-2;
                            }

                            else
                            {
                                T[tPos++] = i;
                            }
                        }
                        //(3) generate theta
                        int n = numPointsPerSeg;
                        double delta = 1.0 / (n - 1.0); // n points to be generated for each segment
                        //(4)
                        C = new Vec3 [numOfSegments*numPointsPerSeg];
                        C[0] = P[0];
                        //(5)
                        int k = 1;

                        //(6)
                        for(int i = 0; i <= L-3; i++)
                        {
                            for(int j = 0; j <= 3; j++)
                            {
                                VArray[j] = P[i+j];
                            }

                            for(int j = 1; j <= (n-1); j++)
                            {
                                C[k] = evalPtOnBspline((i+(j*delta)), i, 4, T, VArray);
                                k++;
                            }
                        }

                        //draw B-spline curve
                        CPoints = k;
                        drawVec3Poly(g2, Color.black, k, C, false);
                    }//
                }
                repaint();

            }//if showProjection

            else if (anim)
            // do a frame
            {
                buffer.setColor(bg);
                buffer.clearRect(0, 0, area.width, area.height);

                Wdu = sWidth / (Wr - Wl);
                Wdv = sHeight / (Wb - Wt);
                shade = new double[fL.getNumFaces()];
                model = new Polygon[fL.getNumFaces()];
                model2 = new Polygon[fL2.getNumFaces()];
                model3 = new Polygon[fL3.getNumFaces()];
                L = Vec3Ops.normalize(L);

                // find camera position
                camera(flyPath, flyBy * dt, massCentre);

                // do the transformations to display the model
                displayModel(fL, model);
                displayModel(fL2, model2);
                displayModel(fL3, model3);

                //Initialize Ordering list
                //I have all the faces in one order, as I have three shapes and sometimes
                //the shapes can be 'in front' of each other. So I have to implement the Painter's
                //algorithm to combat this issue.
                zList = new double[nF*3][2];
                faceRenderOrder = new int[nF*3][2];
                int pos = 0;
                //First triangle
                for(int i = 0; i < nF; i++)
                {
                    int numVertsOnFace = fL.getNumVertsOnFace(i);
                    double averageZ = 0.0;
                    for(int j = 0; j < numVertsOnFace; j++)
                    {
                        averageZ = averageZ + fL.getVertOnFace(i, j).z;
                    }
                    averageZ = averageZ / numVertsOnFace;
                    zList[pos][0] = averageZ;
                    zList[pos][1] = 0;
                    faceRenderOrder[pos][0] = i;
                    faceRenderOrder[pos][1] = 0;
                    pos++;
                }
                //Second triangle
                for(int i = 0; i < nF; i++)
                {
                    int numVertsOnFace = fL2.getNumVertsOnFace(i);
                    double averageZ = 0.0;
                    for(int j = 0; j < numVertsOnFace; j++)
                    {
                        averageZ = averageZ + fL2.getVertOnFace(i, j).z;
                    }
                    averageZ = averageZ / numVertsOnFace;
                    zList[pos][0] = averageZ;
                    zList[pos][1] = 1;
                    faceRenderOrder[pos][0] = i;
                    faceRenderOrder[pos][1] = 1;
                    pos++;
                }
                //Third triangle
                for(int i = 0; i < nF; i++)
                {
                    int numVertsOnFace = fL3.getNumVertsOnFace(i);
                    double averageZ = 0.0;
                    for(int j = 0; j < numVertsOnFace; j++)
                    {
                        averageZ = averageZ + fL3.getVertOnFace(i, j).z;
                    }
                    averageZ = averageZ / numVertsOnFace;
                    zList[pos][0] = averageZ;
                    zList[pos][1] = 2;
                    faceRenderOrder[pos][0] = i;
                    faceRenderOrder[pos][1] = 2;
                    pos++;
                }

                //Sort, decreasing order, Painters Algorithm
                zList = insertionSort(zList, faceRenderOrder);

                // projection of the model to the off-screen buffer
                for (int i = 0; i < faceRenderOrder.length; i++)
                {
                    int c = (int) (255 * shade[faceRenderOrder[i][0]]);

                    //Triangle 1
                    if(faceRenderOrder[i][1] == 0)
                    {
						//Left Triangle = Green
						buffer.setColor(new Color(0, c, 0));
						buffer.fillPolygon(model[faceRenderOrder[i][0]]);
					}
                    //Triangle 2
                    else if(faceRenderOrder[i][1] == 1)
                    {
						//Top Triangle = Yellow
						buffer.setColor(new Color(c, c, 0));
						buffer.fillPolygon(model2[(faceRenderOrder[i][0])]);
					}
                    //Triangle 3
                    else if(faceRenderOrder[i][1] == 2)
                    {
						//Right Triangle = Blue
						buffer.setColor(new Color(0, 0, c));
						buffer.fillPolygon(model3[faceRenderOrder[i][0]]);
					}
                }//for

                // display the off-screen buffer
                g2.drawImage(bi, 0, 0, this);
            }
        }//paintComponent

        //Sorts in descending order
        public double[][] insertionSort(double[][] zListCurr, int[][] faceRenderOrder)
        {
            int tempPos;
            double tempVal;
            double tempVal2;

            int tempValA;
            int tempValB;

            //takes the value at pos, and puts it in it's proper place to the left of pos
            for(int i = 0; i < zListCurr.length; i++)
            {
                tempPos = i;
                while((tempPos > 0) && (zListCurr[tempPos-1][0] >= zListCurr[tempPos][0]))
                {
					//Save pos stuff
                    tempVal = zListCurr[tempPos][0];
                    tempVal2 = zListCurr[tempPos][1];
                    tempValA = faceRenderOrder[tempPos][0];
                    tempValB = faceRenderOrder[tempPos][1];

                    //Move left pos to current pos
                    zListCurr[tempPos][0] = zListCurr[tempPos-1][0];
                    zListCurr[tempPos][1] = zListCurr[tempPos-1][1];
                    faceRenderOrder[tempPos][0] = faceRenderOrder[tempPos-1][0];
                    faceRenderOrder[tempPos][1] = faceRenderOrder[tempPos-1][1];

                    //Move save poss stuff to left pos
                    zListCurr[tempPos-1][0] = tempVal;
                    zListCurr[tempPos-1][1] = tempVal2;
                    faceRenderOrder[tempPos-1][0] = tempValA;
                    faceRenderOrder[tempPos-1][1] = tempValB;
                    tempPos--;
                }//close while
            }//close for

            //It is by default in ascending order, so this just reverses the order
            return reverse(zListCurr, faceRenderOrder);
        }//close sortInsertion


        //This reverses ascending order to descending order
        public double[][] reverse(double[][] list, int[][] faceRenderOrderCurr)
        {
            double[][] newList = new double[list.length][2];
            int[][] newRenderOrder = new int[faceRenderOrder.length][2];

            //Reverse the order from ascending or descending.
            for(int i = 0; i < list.length; i++)
            {
                newList[list.length-i-1][0] = list[i][0];
                newList[list.length-i-1][1] = list[i][1];
                newRenderOrder[faceRenderOrderCurr.length-i-1][0] = faceRenderOrderCurr[i][0];
                newRenderOrder[faceRenderOrderCurr.length-i-1][1] = faceRenderOrderCurr[i][1];
            }

            faceRenderOrder = newRenderOrder;

            return newList;
        }

        public int findVertex(Vec3[] P)
        {
            int i = 0;
            while (i < iPolyPts
                    && (Math.abs(Px - StdG.sX(P[i].x)) > StdG.DOT_SIZE || Math
                            .abs(Py - StdG.sY(P[i].y)) > StdG.DOT_SIZE))
                i++;
            return i;
        }//findVertex

        //returns a point on a uniform cubic B-spline with control vertices V[0], V[1], V[2] and V[3]
        //at the parameter value t where 0 <= t <= 1
        public Vec3 ptOnUCBspline(double t, Vec3[] V)
        {
            Vec3 Q = new Vec3 (0,0,0);
            double b;

            for(int i = 0; i <= 3; i++)
            {
                b = 0.0;
                if(i == 0)
                {
                    b = Math.pow((1.0 - t), 3.0);
                }

                else if(i == 1)
                {
                    b = 4.0 + (-6.0)*(Math.pow(t, 2.0)) + (3.0)*(Math.pow(t, 3.0));
                }

                else if(i == 2)
                {
                    b = 1.0 + (3.0 * t) + (3.0)*(Math.pow(t, 2.0)) + (-3.0)*(Math.pow(t, 3.0));
                }

                else if(i == 3)
                {
                    b = Math.pow(t, 3.0);
                }

                b = (1.0/6.0) * b;
                Q = Vec3Ops.add(Q, Vec3Ops.mul(b, V[i]));
            }
            return Q;
        }//ptOnUCBspline

        public Vec3 evalPtOnBspline(double t, int k, int m, double[] T, Vec3[] V)
        {
            Vec3 Q = new Vec3(0, 0, 0);
            double b;

            for(int i = 0; i <= (m-1); i++)
            {
                b = deBoorCox(t, (k+i), m, T);
                Vec3 temp  = Vec3Ops.mul(b, V[i]);
                Q = Vec3Ops.add(Q, temp);
            }

            return Q;
        }//evalPtOnBspline

        public double deBoorCox(double t, int k, int m, double[] T)
        {
            double a, b;
            double f = 0, g = 0;

            if(m == 1)
            {
                if((T[k] < t) && (t <= T[k+1]))
                    return 1;
                else
                    return 0;
            }

            else
            {
                a = deBoorCox(t, k, (m-1), T);
                if(a != 0)  //use |a| > min_value
                    f = ((t - T[k])*a) / (T[k+m-1] - T[k]);
                else
                    f = 0;

                b = deBoorCox(t, (k+1), (m-1), T);
                if(b != 0)  //use |b| > min_value
                    g = ((T[k+m] - t)*b) / (T[k+m] - T[k+1]);
                else
                    g = 0;
            }

            return (f + g);
        }//deBoorCox

        // find UVN coordinate system
        public void findUV()
        {
            double Ntol = 0.01;
            V = new Vec3(0, 0, 0);
            if (Math.abs(1.0 - N.z) < Ntol)
                V.y = N.z;
            else if (Math.abs(1.0 + N.z) < Ntol)
                V.y = -N.z;
            else
                V.z = -1;
            U = Vec3Ops.crs(V, N);
            U = Vec3Ops.normalize(U);
            V = Vec3Ops.crs(N, U);
        }//findUV

        // the camera path
        public void camera(int flyPath, double t, Vec3 CVert)
        {
            Vec3 FPa0;
            Vec3 FPa1;
            Vec3 FPb0;
            Vec3 FPb1;
            Vec3 FPb2;

            if (flyPath == 1)
            {
                //xy linear path
                if(projectionType.equals("xy"))
                {
                    FPa0 = new Vec3(linearPnt1.x, linearPnt1.y, 0.0);
                    FPa1 = new Vec3(linearPnt2.x, linearPnt2.y, 0.0);
                }

                //xz linear path
                else
                {
                    FPa0 = new Vec3(linearPnt1.x, 0.0, linearPnt1.y);
                    FPa1 = new Vec3(linearPnt2.x, 0.0, linearPnt2.y);
                }

                r = Vec3Ops.add(Vec3Ops.mul(1 - t, FPa0), Vec3Ops.mul(t, FPa1));
                N = new Vec3(-1, -1, 0);
            }
            else if (flyPath == 2)
            {
                double temp = (1 - t) * (CPoints);
                int convertedTemp = (int)Math.round(temp);
                //Prevents it from being outside the confines of the C array
                if(convertedTemp < 0)
                    convertedTemp = 0;
                if(convertedTemp >= CPoints)
                    convertedTemp = CPoints-1;
                //xy B-spline path
                if(projectionType.equals("xy"))
                {
                    r = new Vec3(C[(C.length-1)-convertedTemp].x, C[(C.length-1)-convertedTemp].y,
                                 C[(C.length-1)-convertedTemp].z);
                }
                //xz B-spline path
                else if(projectionType.equals("xz"))
                    r = new Vec3(C[(C.length-1)-convertedTemp].x, C[(C.length-1)-convertedTemp].z,
                                 C[(C.length-1)-convertedTemp].y);

                N = Vec3Ops.sub(CVert, r);
            }
            N = Vec3Ops.normalize(N);
            findUV();
        }//camera

        public void drawFaces2DProjection(VertFace fLCurrent, Graphics2D g2)
        {
            //check all faces, if either has at least one x and at least one y value, it will be
            //on the XY place, so add it to a list of faces that need to be projected onto the XY plane
            int totalNumFaces = fLCurrent.getNumFaces();
            boolean hasX;
            boolean hasY;
            boolean hasZ;
            int totalVertsOnFace;
            xyFaces = new Vec3[15][15];
            int xyPos = 0;
            xzFaces = new Vec3[15][15];
            int xzPos = 0;

            for(int i = 0; i < totalNumFaces; i++)
            {
                hasX = false;
                hasY = false;
                hasZ = false;
                totalVertsOnFace = fLCurrent.getNumVertsOnFace(i);

                for(int j = 0; j < totalVertsOnFace; j++)
                {
                    Vec3 tempVert = fLCurrent.getVertOnFace(i, j);
                    if(tempVert.x != 0)
                        hasX = true;
                    if(tempVert.y != 0)
                        hasY = true;
                    if(tempVert.z != 0)
                        hasZ = true;
                }//for

                //On the XY plane, add the face
                if(hasX && hasY)
                {
                    Vec3[] tempFaceList = new Vec3[totalVertsOnFace];
                    for(int j = 0; j < totalVertsOnFace; j++)
                        tempFaceList[j] = fLCurrent.getVertOnFace(i, j);
                    xyFaces[xyPos++] = tempFaceList;
                }

                //On the XZ plane, add the face
                if(hasX && hasZ)
                {
                    Vec3[] tempFaceList = new Vec3[totalVertsOnFace];
                    for(int j = 0; j < totalVertsOnFace; j++)
                    {
                        tempFaceList[j] = new Vec3(fLCurrent.getVertOnFace(i, j).x, fLCurrent.getVertOnFace(i, j).z, 0);
                        //tempFaceList[j].y = fL.getVertOnFace(i, j).y;
                    }
                    xzFaces[xzPos++] = tempFaceList;
                }
            }//for
            //draw all the faces on the XY plane
            if(projectionType.equals("xy"))
                {
                for(int i = 0; i < xyPos; i++)
                {
                    drawVec3Poly(g2, Color.black, xyFaces[i].length, xyFaces[i], false);
                }
            }

            else if(projectionType.equals("xz"))
            {
                //draw all the faces on the XZ plane
                for(int i = 0; i < xzPos; i++)
                {
                    drawVec3Poly(g2, Color.black, xzFaces[i].length, xzFaces[i], false);
                }
            }
        }//drawFaces2DProjection

        // transformation of a face from world coordinate system to UVN system
        public void projectToView(int iFace, Vec3[] View, VertFace fLCurrent)
        {
            int n = fLCurrent.getNumVertsOnFace(iFace);
            double temp;
            Vec3 vertex;
            double[] q = new double[4];
            double[] w = new double[4];
            double[][] M = new double[4][4];
            M[0][0] = U.x;
            M[1][0] = U.y;
            M[2][0] = U.z;
            M[3][0] = -Vec3Ops.dot(r, U);
            M[0][1] = V.x;
            M[1][1] = V.y;
            M[2][1] = V.z;
            M[3][1] = -Vec3Ops.dot(r, V);
            M[0][2] = N.x;
            M[1][2] = N.y;
            M[2][2] = N.z;
            M[3][2] = -Vec3Ops.dot(r, N);
            M[0][3] = N.x / e;
            M[1][3] = N.y / e;
            M[2][3] = N.z / e;
            M[3][3] = 1 - Vec3Ops.dot(r, N) / e;

            for (int i = 0; i < n; i++)
            {
                vertex = fLCurrent.getVertOnFace(iFace, i);
                q[0] = vertex.x;
                q[1] = vertex.y;
                q[2] = vertex.z;
                q[3] = 1;
                for (int j = 0; j < 4; j++)
                {
                    temp = 0;
                    for (int k = 0; k < 4; k++)
                        temp += q[k] * M[k][j];
                    w[j] = temp;
                }
                View[i] = new Vec3(w[0] / w[3], w[1] / w[3], w[2] / w[3]);
            }
        }//projectToView

        public void viewFace(int n, double[] FU, double FV[], int iFace, Polygon[] modelCurrent)
        {
            modelCurrent[iFace] = new Polygon();
            for (int i = 0; i < n; i++)
            {
                int Vh = (int) Math.round(FU[i]);
                int Vv = (int) Math.round(FV[i]);
                modelCurrent[iFace].addPoint(Vh, Vv);
            }
        }//viewFace

        public void displayModel(VertFace fLCurrent, Polygon[] modelCurrent)
        {
            Vec3 eN = Vec3Ops.mul(-e, N);
            Vec3 eyeWorld = Vec3Ops.add(eN, r);
            int numFaces = fLCurrent.getNumFaces();
            nF = 0;
            for (int i = 0; i < numFaces; i++)
            {
                //This is the backface culling, and was free to use as this was included in the
                //solution for A2Q2. If our program didn't work properly for A2Q2 we could use the
                //solution as a starting point, which I have done since mine didn't work properly.
                double backFaceCullingValue = Vec3Ops.dot(faceNml[i],
                        Vec3Ops.sub(eyeWorld, faceC[i]));

                ///Backface culling, ignore face if (e-c)*N < 0
                //e = eyeWorld
                //c = centroid of face = faceC[i]
                //n = face outward normal = faceNml[i]
                if (backFaceCullingValue > 0)
                {
                    int numFaceVerts = fLCurrent.getNumVertsOnFace(i);
                    Vec3[] View = new Vec3[numFaceVerts];
                    double[] faceU = new double[numFaceVerts];
                    double[] faceV = new double[numFaceVerts];
                    projectToView(i, View, fLCurrent);
                    for (int j = 0; j < numFaceVerts; j++)
                    {
                        faceU[j] = Wdu * (View[j].x - Wl);
                        faceV[j] = Wdv * (View[j].y - Wt);
                    }//end j

                    //Compute shade of face, if N dot L < 0, then use 0 since it is on the other
                    //side of the shape
                    shade[nF] = Vec3Ops.dot(L, faceNml[i]);
                    if (shade[nF] < 0)
                        shade[nF] = 0;
                    viewFace(numFaceVerts, faceU, faceV, nF, modelCurrent);
                    nF++;
                }//if
            }//for
        }//displayModel
    } //-------------------- end (inner) class Project_Panel --------------------
}//class Project_Frame
