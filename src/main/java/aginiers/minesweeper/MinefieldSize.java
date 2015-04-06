package aginiers.minesweeper;

public enum MinefieldSize {
  SMALL("SMALL", 8, 6, 10, 2), //
  MEDIUM("MEDIUM", 16, 12, 35, 3), //
  BIG("BIG", 24, 18, 70, 4), //
  GIANT("GIANT", 32, 24, 140, 5);//

  private final String code;
  private final int width;
  private final int height;
  private final int mines;
  private final int lives;

  private MinefieldSize(String code, int width, int height, int mines, int lives) {
    this.code = code;
    this.width = width;
    this.height = height;
    this.mines = mines;
    this.lives = lives;
  }

  /**
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  /**
   * @return the height
   */
  public int getHeight() {
    return height;
  }

  /**
   * @return the mines
   */
  public int getMines() {
    return mines;
  }

  /**
   * @return the lives
   */
  public int getLives() {
    return lives;
  }

}
