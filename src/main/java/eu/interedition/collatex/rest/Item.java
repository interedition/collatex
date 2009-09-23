package eu.interedition.collatex.rest;

public class Item {

  private final String _name;
  private String _description;

  public Item(String name, String description) {
    this._name = name;
    this._description = description;
  }

  public void setDescription(String description) {
    this._description = description;

  }

  public String getName() {
    return _name;
  }

  public String getDescription() {
    return _description;
  }

}
