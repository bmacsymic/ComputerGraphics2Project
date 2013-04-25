import java.awt.geom.*;
class StdG
{ public static void initG(double x0, double x1,
        double y0, double y1, double w, double h,
        boolean iso)
  { xMin = x0; xMax = x1;
    yMin = y0; yMax = y1; ht = h;
    double dx = xMax - xMin,
           dy = yMax - yMin;
    if ( dx < Double.MIN_VALUE || w < 1)
      xScale = 1;
    else
      xScale = dx/w;
    if ( dy < Double.MIN_VALUE || h < 1)
      yScale = 1;
    else
      yScale = dy/h;
    if (iso)
    { xScale = Math.max(xScale, yScale);
      yScale = xScale;
    }
  } // end initG

  public static double get_xScale()
  { return xScale; }

  public static double get_yScale()
  { return yScale; }

  public static double sX(double x)
  { return (x - xMin)/xScale; }

  public static double sY(double y)
  { return ht - (y - yMin)/yScale; }
  
  public static Vec3 sV(Vec3 v)
  { return new Vec3( (v.x - xMin) / xScale,
                     ht - (v.y - yMin) / yScale, 0 );
  }

  public static double fx(double X)
  { return xMin + X * xScale; }

  public static double fy(double Y)
  { return yMin + (ht - Y) * yScale; }
  
  public static Vec3 fv(Vec3 V)
  { return new Vec3( xMin + V.x * xScale,
                     yMin + (ht - V.y) * yScale, 0 );
  }

  public static final double DOT_SIZE = 4;
  private static double xMin, xMax,
           yMin, yMax, ht, xScale, yScale;
} // end class StdG
