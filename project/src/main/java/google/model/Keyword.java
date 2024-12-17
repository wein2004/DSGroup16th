package google.model;

public class Keyword {
	public String name;
    public double weight;
    
    public Keyword(String name, double weight){
      this.name = name;
      this.weight = weight;
    }

    public String getName(){
    	return name;
    }
    
    public double getWeight(){
    	return weight;
    }
}
