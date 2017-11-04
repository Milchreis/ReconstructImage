enum ItemPainting {
  CIRCLE, RECT, HEXAGONE;
  
  public ItemPainting next() {
    return values()[(this.ordinal()+1) % values().length];
  }
  
  public ItemPainting previous() {
    return values()[(this.ordinal()-1) % values().length];
  }
}

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
    
    if(PAINTING.equals(ItemPainting.CIRCLE)) {
       ellipse(position.x, position.y, itemWidth, itemWidth); 
    
    } else if(PAINTING.equals(ItemPainting.HEXAGONE)) {
      polygon(position.x, position.y, itemWidth*0.55, 6);
      
    } else {
      rect(position.x, position.y, itemWidth, itemWidth);
    }
  }
  
  void polygon(float x, float y, float radius, int npoints) {
    pushMatrix();
    translate(x, y);
    
    float angle = TWO_PI / npoints;
    beginShape();
    for (float a = 0; a < TWO_PI; a += angle) {
      float sx = 0 + cos(a) * radius;
      float sy = 0 + sin(a) * radius;
      vertex(sx, sy);
    }
    endShape(CLOSE);
    popMatrix();
  }
}