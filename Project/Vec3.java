class Vec3
{
    public Vec3()
    {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(Vec3 v)
    {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public double x, y, z;
} // end class Vec3
