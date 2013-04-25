
Bradley Macsymic
Term Project
COMP 4490 A01

Important Source Files handed in are:
-------------------------------------
**ProjectMain.java was started with the solution to A2Q2 since my
A2Q2 program wasn't working properly.

Object1.dat - A supplied shape I use in my program (A triangle, green)
Object2.dat - A supplied shape I use in my program (A triangle, yellow)
object3.dat - A supplied shape I use in my program (A triangle, blue)
FaceList.java - Supplied code
projectMain - My main program for the term project
StdG.java - Supplied Code
Vec3.java - Supplied Code
Vec3Ops.java - Supplied Code
VertFace.java - Supplied Code
predefinedBSpline.txt - The initial B-spline path for my program
readme.txt - This document

------------------------------------------------------------------
*Each of these files will have various .class files that go along
with them. Running each of the files is pretty self explanitory
and does not need any modification.
------------------------------------------------------------------

Features of ProjectMain.java (Also documented in top of program)
----------------------------------------------------------------
My main program preloads in 3 shapes, all triangles (taken from the Object1.dat, Object2.dat, and 
Object3.dat files).

My shape inspiration was the tri-force from the popular legend of Zelda game series.

My program preloads an initial linear and bspline path. They are populated on a 2D plane,
in which the user can specify a new linear path (by drawing) and a new b-spline path (again, my drawing).
Note: You can only draw in the new paths if you are currently 2D projecting. It cannot be done in
3D as this is outside the confines of our course (I believe).

My b-spline was calculated using my A3Q5 answer and does end point interpolation.

You user can Reset (File -> Reset) to reset the paths back to the initial linear and b-spline paths.

Upon showing the 2D projecting, the user can then specify a new Linear or B-spline path using the 
appropriate methods that pop up in the navigation menu at the top of the window.

My program can do xy, and xz projections.

The delay time and number of animations frames can be changes as well.

For the B-spline the user can change the number of points per segment, or they can display/undisplay
the control polygon for the b-spline. The predefined (initial) b-spline path can not show it's inital
control polygon, since it has none. It also cannot display/undisplay it's control polygon since it doesn't
have one.

I have added in System.out.println comments when the user changes functions on the program to show the user
that is it actually changed.