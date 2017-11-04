import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ReconstructImage extends PApplet {

// ReconstructImage by milchreis

// Settings
// ==================================

/** Sets the number of items/tiles for the complete image */
int ELEMENTS = 3000;

/** Sets the range for variation of the color value (per color channel 0-255) */
int MUTATION_RATE = 30;

/** Toggle the visualisation for finished items */
boolean SHOW_FINISHED_ITEMS = false;

/** Sets the threshold lowest fitness for items which are underneth will die 
 * and reborn as complete new item (random color) */
float DIE_THRESHOLD = 0.4f;

/** Sets the maximum width/height window for loaded images (takes larges side of the image) */
int MAX_WINDOW = 800;

/** Painting method */
ItemPainting PAINTING = ItemPainting.RECT;

Brain brain;

public void setup() {
  
  textFont(createFont("Monospaced", 11));
}

public void draw() {
  background(30);
  
  if(brain == null || brain.image == null)  {
    // View instructions
    textSize(22); 
    print("1. Click to load an image", 10, 30);
    print("2. Press '+' or '-' to change the mutation rate", 10, 70);
    print("3. Press 'Up' or 'Down' to change the number of elements", 10, 110);
    print("4. Press 'Space' to mark finished elements", 10, 150);
    print("5. Press 'Right' or 'Left' to change the shape", 10, 190);
 
  } else {
    // Process image
    // Draw
    for(Item i : brain.dna) {
      i.draw();
    }  
    
    if(brain.image != null) {
      brain.reconstruct();
    }
    
    // Draw infobar
    textSize(14);
    fill(color(0, 0, 0, 180));
    rect(0, 0, width, 30);
    print("Generation: " + brain.generations, 5, 20);
    textSize(10);
    print("Mutationrate: +/-" + MUTATION_RATE, width-140, 15);
    print("Elements: " + brain.dna.size(), width-140, 25);
  }
}

public void print(String text, int x, int y) {
  fill(0);
  text(text, x+1, y+1);
  fill(255);
  text(text, x, y);
}

public void mousePressed() {
  selectInput("Select an image", "fileSelected");
}

public void keyPressed() {
  if(key == '+') {
    MUTATION_RATE = MUTATION_RATE + 1 > Integer.MAX_VALUE ? MUTATION_RATE : MUTATION_RATE + 1;
    brain.mutationRate = MUTATION_RATE;
  }
  
  if(key == '-') {
    MUTATION_RATE = MUTATION_RATE - 1 <= 0 ? MUTATION_RATE : MUTATION_RATE - 1;
    brain.mutationRate = MUTATION_RATE;
  }
  
  if(key == ' ') {
    SHOW_FINISHED_ITEMS = !SHOW_FINISHED_ITEMS;
  }
  
  if (keyCode == UP) {
    ELEMENTS = ELEMENTS + 50 > width*height ? ELEMENTS : ELEMENTS + 10;
    brain.number = ELEMENTS;
    brain.init();
  }
  
  if (keyCode == DOWN) {
    ELEMENTS = ELEMENTS - 10 <= 0 ? ELEMENTS : ELEMENTS - 10;
    brain.number = ELEMENTS;
    brain.init();
  }
  
  if (keyCode == RIGHT) {
    PAINTING = PAINTING.next();
  }
  
  if (keyCode == LEFT) {
    PAINTING = PAINTING.previous();
  }
}

public void fileSelected(File selection) {
  if (selection != null) {
    if(brain == null) {
      brain = new Brain(ELEMENTS, MUTATION_RATE, DIE_THRESHOLD);
    }
    
    PImage img = loadImage(selection.getAbsolutePath());
    
    if(img.width < img.height) {
      img.resize(0, MAX_WINDOW);
    } else {
      img.resize(0, MAX_WINDOW);
    }
    
    surface.setSize(img.width, img.height);
    brain.setImage(img);
  }  
}
class Brain {

  ArrayList<Item> dna; 
  int mutationRate;
  PImage image;
  long generations;
  boolean complete;
  float dieThreshold;
  int number;
    
  Brain(int number, int mutationRate, float dieThreshold) {
    dna = new ArrayList<Item>();
    this.mutationRate = mutationRate;
    this.dieThreshold = dieThreshold;
    this.number = number;
  }
  
  public void init() { 
    dna.clear();
    
    int w = floor(sqrt(image.width * image. height / number));
    int cols = width / w;
    
    for(int i=0; i<number; i++) {
      
      int x = (i % cols) * w;
      int y = (i / cols) * w;
      
      Item item = new Item();
      item.itemWidth = w;
      item.colour = color(random(0, 255), random(0, 255), random(0, 255), 255);
      item.position = new PVector(x, y);
      dna.add(item);   
    }
  }
  
  public void setImage(PImage img) {
    image = img;
    image.resize(width, height);
    generations = 0;
    complete = false;
  }
  
  public int getAvgColor(float x, float y, int w) {
    
    int[] sums = new int[3];
    
    if(x+w > image.width || y+w > image.height) {
      return 0;
    }
        
    for(int xx = (int)x; xx < x+w; xx++) {
      for(int yy = (int)y; yy < y+w; yy++) {
        int pixel = image.get(xx, yy);
        sums[0] += red(pixel); 
        sums[1] += green(pixel); 
        sums[2] += blue(pixel); 
      }
    }
    
    int area = (w * w);
    return color(sums[0]/area, sums[1]/area, sums[2]/area);
  }
  
  public float[] fitness(Item item) {
    int avgColor = getAvgColor(item.position.x, item.position.y, item.itemWidth);
        
    float rDiff = abs(red(avgColor) - red(item.colour));
    float gDiff = abs(green(avgColor) - green(item.colour));
    float bDiff = abs(blue(avgColor) - blue(item.colour));
    
    return new float[]{1-(rDiff/256), 1-(gDiff/256), 1-(bDiff/256)};
  }
  
  public void mutate(Item i, float[] fit) {
      // Red 
      if(fit[0] < dieThreshold) {
        i.colour = color(random(0, 255), green(i.colour), blue(i.colour));
      } else if(fit[0] < 1.0f){
        i.colour = color(red(i.colour) + random(-mutationRate, mutationRate), green(i.colour), blue(i.colour));
      }
      
      // Green 
      if(fit[1] < dieThreshold) {
        i.colour = color(red(i.colour), random(0, 255), blue(i.colour));
      } else if(fit[1] < 1.0f) {
        i.colour = color(red(i.colour), green(i.colour) + random(-mutationRate, mutationRate), blue(i.colour));
      }
      
      // Blue 
      if(fit[2] < dieThreshold) {
        i.colour = color(red(i.colour), green(i.colour), random(0, 255));
      } else if(fit[2] < 1.0f){
        i.colour = color(red(i.colour), green(i.colour), blue(i.colour) + random(-mutationRate, mutationRate));
      }
  }
  
  public void reconstruct() {   
    // DNA size has been changed -> recrate the dna
    if(dna.size() != number) {
      init();
    }
     
    // Check the completness of the reconstruction
    complete = true;
    for(Item i : dna) {
      if(!i.isComplete()) {
        complete = false;
        break;
      }
    }
    
    // Stop calculation if it is complete
    if(complete) {
      return;
    }
    
    for(Item i : dna) {      
      // Calculate fitness
      float[] fit = fitness(i);
      // Mutate depends on fitness
      mutate(i, fit);
      // Set fitness for drawing
      i.fitness = fit;
    }
    
    if(!complete) 
      generations++;
  }
}
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
  int colour;
  float[] fitness;
  
  public boolean isComplete() {
    return fitness != null && fitness[0] >= 1 && fitness[1] >= 1 && fitness[2] >= 1;
  }
  
  public void draw() {
    noStroke();
    
    if(isComplete() && SHOW_FINISHED_ITEMS ) {
      stroke(1);
    }
    
    fill(colour);
    
    if(PAINTING.equals(ItemPainting.CIRCLE)) {
       ellipse(position.x, position.y, itemWidth, itemWidth); 
    
    } else if(PAINTING.equals(ItemPainting.HEXAGONE)) {
      polygon(position.x, position.y, itemWidth*0.55f, 6);
      
    } else {
      rect(position.x, position.y, itemWidth, itemWidth);
    }
  }
  
  public void polygon(float x, float y, float radius, int npoints) {
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
  public void settings() {  size(800, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ReconstructImage" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
