class Brain {

  ArrayList<Item> dna; 
  int mutationRate;
  PImage image;
  long generations;
  boolean complete;
  float dieThreshold;
    
  Brain(int number, int mutationRate, float dieThreshold) {
    dna = new ArrayList<Item>();
    this.mutationRate = mutationRate;
    this.dieThreshold = dieThreshold;
    init(number);
  }
  
  void init(int number) {
    dna.clear();
    int w = floor(sqrt(width * height / number));
    
    for(int x = 0; x < width; x += w) {
      for(int y = 0; y < height; y += w) {
        Item item = new Item();
        item.itemWidth = w;
        item.colour = color(random(0, 255), random(0, 255), random(0, 255), 255);
        item.position = new PVector(x, y);
        dna.add(item);
      }
    }
  }
  
  void setImage(PImage img) {
    image = img;
    image.resize(width, height);
    generations = 0;
    complete = false;
    if(dna.size() > 0 && dna.get(0).fitness != null) {
      dna.get(0).fitness[0] = 0;
    }
  }
  
  int getAvgColor(float x, float y, int w) {
    
    int[] sums = new int[3];
    
    if(x+w > width || y+w > height) {
      return 0;
    }
        
    for(int xx = (int)x; xx < x+w; xx++) {
      for(int yy = (int)y; yy < y+w; yy++) {
        color pixel = image.get(xx, yy);
        sums[0] += red(pixel); 
        sums[1] += green(pixel); 
        sums[2] += blue(pixel); 
      }
    }
    
    int area = (w * w);
    return color(sums[0]/area, sums[1]/area, sums[2]/area);
  }
  
  float[] fitness(Item item) {
    color avgColor = getAvgColor(item.position.x, item.position.y, item.itemWidth);
        
    float rDiff = abs(red(avgColor) - red(item.colour));
    float gDiff = abs(green(avgColor) - green(item.colour));
    float bDiff = abs(blue(avgColor) - blue(item.colour));
    
    return new float[]{1-(rDiff/256), 1-(gDiff/256), 1-(bDiff/256)};
  }
  
  void mutate(Item i, float[] fit) {
      // Red 
      if(fit[0] < dieThreshold) {
        i.colour = color(random(0, 255), green(i.colour), blue(i.colour));
      } else if(fit[0] < 1.0){
        i.colour = color(red(i.colour) + random(-mutationRate, mutationRate), green(i.colour), blue(i.colour));
      }
      
      // Green 
      if(fit[1] < dieThreshold) {
        i.colour = color(red(i.colour), random(0, 255), blue(i.colour));
      } else if(fit[1] < 1.0) {
        i.colour = color(red(i.colour), green(i.colour) + random(-mutationRate, mutationRate), blue(i.colour));
      }
      
      // Blue 
      if(fit[2] < dieThreshold) {
        i.colour = color(red(i.colour), green(i.colour), random(0, 255));
      } else if(fit[2] < 1.0){
        i.colour = color(red(i.colour), green(i.colour), blue(i.colour) + random(-mutationRate, mutationRate));
      }
  }
  
  void reconstruct() {    
     
    complete = true;
     for(Item i : dna) {
      if(!i.isComplete()) {
        complete = false;
        break;
      }
    }
    
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