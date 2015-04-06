package aginiers.minesweeper;

public enum BoxType {

  FLAG("flag"),//
  BOMB("bomb"),//
  UNEXPOSED("unexposed"),//
  EXPOSED("exposed");//

  private String code;

  private BoxType(String c) {
    code = c;
  }

  public String getCode() {
    return code;
  }

}
