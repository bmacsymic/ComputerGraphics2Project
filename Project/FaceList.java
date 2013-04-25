
class FaceList
{
  public Vec3[][] faces;
  public int numFaces;

  public FaceList()
  {
      faces = new Vec3[][]
  }

  public void addFace(Vec3[] newFace)
  {
    faces[numFaces] = newFace;
    numFaces++;
  }

}
