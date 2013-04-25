import java.io.*;
import java.util.*;

class VertFace
{
    private int nV, nF;
    private Vec3[] v;
    private int[][] f;

    VertFace(String fileName) throws FileNotFoundException
    {
        FileReader faces = new FileReader(fileName);
        Scanner inputFile = new Scanner(faces);
        nV = inputFile.nextInt();
        v = new Vec3[nV];
        for (int i = 0; i < v.length; i++)
        {
            double x = inputFile.nextDouble();
            double y = inputFile.nextDouble();
            double z = inputFile.nextDouble();
            v[i] = new Vec3(x, y, z);
        }
        nF = inputFile.nextInt();
        f = new int[nF][];
        for (int i = 0; i < f.length; i++)
        {
            int n = inputFile.nextInt();
            f[i] = new int[n];
            for (int j = 0; j < f[i].length; j++)
                f[i][j] = inputFile.nextInt();
        }
        inputFile.close();
    } // end FaceList constructor

    public int getNumVerts()
    {
        return v.length;
    }

    public Vec3 getVert(int i)
    {
        return v[i];
    }

    public int getNumFaces()
    {
        return f.length;
    }

    public int getNumVertsOnFace(int face)
    {
        return f[face].length;
    }

    public Vec3 getVertOnFace(int face, int vert)
    {
        return v[f[face][vert]];
    }

    public Vec3 getNormal(int face)
    {
        Vec3 A = Vec3Ops.sub(v[f[face][2]], v[f[face][1]]);
        Vec3 B = Vec3Ops.sub(v[f[face][0]], v[f[face][1]]);
        return Vec3Ops.crs(A, B);
    }

    public Vec3 getUnitNormal(int face)
    {
        Vec3 N = getNormal(face);
        return Vec3Ops.normalize(N);
    }
} // end FaceList
