import java.util.*;

class Vec3Ops
{
    public static Vec3 add(Vec3 V, Vec3 W)
    {
        return new Vec3(V.x + W.x, V.y + W.y, V.z + W.z);
    }

    public static Vec3 sub(Vec3 V, Vec3 W)
    {
        return new Vec3(V.x - W.x, V.y - W.y, V.z - W.z);
    }

    public static Vec3 crs(Vec3 V, Vec3 W)
    {
        return new Vec3(V.y * W.z - V.z * W.y, V.z * W.x - V.x * W.z, V.x * W.y
                - V.y * W.x);
    }

    public static double dot(Vec3 V, Vec3 W)
    {
        return V.x * W.x + V.y * W.y + V.z * W.z;
    }

    public static Vec3 mul(double scalar, Vec3 V)
    {
        return new Vec3(scalar * V.x, scalar * V.y, scalar * V.z);
    }

    public static double norm(Vec3 V)
    {
        return Math.sqrt(dot(V, V));
    }

    public static Vec3 normalize(Vec3 V)
    {
        double d = norm(V);
        Vec3 W = new Vec3();
        if (d >= Double.MIN_VALUE)
        {
            W.x = V.x / d;
            W.y = V.y / d;
            W.z = V.z / d;
        }
        return W;
    }

    public static Vec3 rotate(Vec3 V, char axis, double angle)
    {
        double a, b, x = V.x, y = V.y, z = V.z, c = Math.cos(angle), s = Math
                .sin(angle);
        if (axis == 'x' || axis == 'X')
        {
            a = c * y - s * z;
            b = s * y + c * z;
            y = a;
            z = b;
        } else if (axis == 'y' || axis == 'Y')
        {
            a = c * z - s * x;
            b = s * z + c * x;
            z = a;
            x = b;
        } else if (axis == 'z' || axis == 'Z')
        {
            a = c * x - s * y;
            b = s * x + c * y;
            x = a;
            y = b;
        } else
            ;
        return new Vec3(x, y, z);
    }

    public static double findAzimuth(Vec3 V)
    {
        double theta = Math.atan2(V.y, V.x);
        if (theta < 0)
            theta += 2 * Math.PI;
        return theta;
    }

    public static double findColatitude(Vec3 V)
    {
        double phi = 0, d = norm(V);
        if (d > Double.MIN_VALUE)
            phi = Math.acos(V.z / d);
        return phi;
    }

    public static Vec3 stringToVec3(String s)
    {
        StringTokenizer t = new StringTokenizer(s);
        double x = Double.parseDouble(t.nextToken());
        double y = Double.parseDouble(t.nextToken());
        double z = Double.parseDouble(t.nextToken());
        return new Vec3(x, y, z);
    }

    public static Vec3 intersectLines_xy(Vec3 P0, Vec3 T0, Vec3 P1, Vec3 T1)
    {
        Vec3 R = new Vec3(0, 0, 0);
        Vec3 D = Vec3Ops.sub(P1, P0);
        double a11 = Vec3Ops.dot(T0, T1), a12 = -1, a21 = 1, a22 = -a11, b1 = Vec3Ops
                .dot(D, T1), b2 = Vec3Ops.dot(D, T0);
        double denom = a11 * a22 - a12 * a21;
        intersectLines_xy_found = Math.abs(denom) > Double.MIN_VALUE;
        if (intersectLines_xy_found)
        {
            double t = (b1 * a22 - b2 * a12) / denom;
            R = Vec3Ops.add(P0, Vec3Ops.mul(t, T0));
        }
        return R;
    }

    public static boolean intersectLines_xy_found = false;

} // end class Vec3Ops
