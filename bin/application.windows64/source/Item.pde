class Item {
  PVector position;
  int itemWidth;
  color colour;
  float[] fitness;
  
  boolean isComplete() {
    return fitness != null && fitness[0] >= 1 && fitness[1] >= 1 && fitness[2] >= 1;
  }
  
  void draw() {
    noStroke();
    
    if(isComplete() && SHOW_FINISHED_ITEMS ) {
      stroke(1);
    }
    
    fill(colour);
    rect(position.x, position.y, itemWidth, itemWidth);
  }
}