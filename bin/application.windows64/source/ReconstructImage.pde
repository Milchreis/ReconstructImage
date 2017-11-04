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
float DIE_THRESHOLD = 0.4;

/** Sets the maximum width/height window for loaded images (takes larges side of the image) */
int MAX_WINDOW = 800;

/** Painting method */
ItemPainting PAINTING = ItemPainting.RECT;

Brain brain;

void setup() {
  size(800, 600);
  textFont(createFont("Monospaced", 11));
}

void draw() {
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

void print(String text, int x, int y) {
  fill(0);
  text(text, x+1, y+1);
  fill(255);
  text(text, x, y);
}

void mousePressed() {
  selectInput("Select an image", "fileSelected");
}

void keyPressed() {
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

void fileSelected(File selection) {
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