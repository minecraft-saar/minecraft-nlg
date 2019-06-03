package de.saar.coli.minecraft.relationextractor;

public class Wall extends BigBlock {

  public enum WallDirection {
    ALONGX,
    ALONGY
  }

  /**
   * A wall is a special cuboid with thickness == 1, standing upright.
   */
  public Wall(String name, int x1, int y1, int z1, int x2, int y2, int z2) {
    super(name, x1, y1, z1, x2, y2, z2);

    /* Assumptions about walls:
     * The have a width of 1
     * Have width and height > 1
     * Therefore: x1 == x2 xor z1 == z2
     * A wall needs a height, otherwise it is just a row (TODO discuss)
     */

    if (x1 == x2 && z1 == z2) {
      throw new RuntimeException("Wall does neither extend over the x nor the z axis!");
    }
    if (y1 == y2) {
      throw new RuntimeException("Wall has height of 1, this is not a proper wall");
    }
  }

  public int height() {
    return y2 - y1;
  }

  /**
   * returns the width along the X axis or the Y axis, whichever the wall is built along.
   */
  public int width() {
    if (x1 == x2) {
      return z2 - z1;
    }
    return x2 - x1;
  }

  /**
   * Computes whether this wall is built along the X or Y axis.
   */
  public WallDirection direction() {
    if (y1 == y2) {
      return WallDirection.ALONGX;
    }
    return WallDirection.ALONGY;
  }

  @Override
  public boolean sameShapeAs(MinecraftObject other) {
    if (!(other instanceof Wall)) {
      return false;
    }
    Wall owall = (Wall) other;
    if (height() != owall.height() || width() != owall.width()) {
      return false;
    }
    return true;
  }
}
