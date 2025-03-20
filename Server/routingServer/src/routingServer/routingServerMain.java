package routingServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import express.Express;

public class routingServerMain {
	
	public class PointOfRoute{
		private String Lat;
		private String Lon;
		
		public PointOfRoute(String lat, String lon)
		{
			this.Lat = lat;
			this.Lon = lon;
		}
		
		public String getLat() {
			return this.Lat;
		}
		
		public String getLon() {
			return this.Lon;
		}
		
		public String getStringLatLon() {
			return this.Lat + "," + this.Lon;
		}
		
		public String getStringLonLat() {
			return this.Lon + "," + this.Lat;
		}
	}
	
	public class Addresses {
		
		private String name;
		private double sum;
		private int count;
		
		public Addresses(String n, double s, int c) {
			this.name = n;
			this.sum = s;
			this.count = c;
		}
		
		public String getName() {
			return this.name;
		}
		
		public double getSum() {
			return this.sum;
		}
		
		public int getCount() {
			return this.count;
		}
		
		public void incrementCount() {
			this.count += 1;
		}
		
		public void addSum(double x) {
			this.sum += x;
		}
		
		public String getString()
		{
			return this.name + "," + this.sum + "," + this.count + ", " + this.sum/this.count;
		}
	
	}
	
	
	public class Node {

        private String id;
        private String coords;

        private List<Node> shortestPath = new LinkedList<>();

        private double distance = Double.MAX_VALUE;

        Map<Node, Double> adjacentNodes = new HashMap<>();

        public void addDestination(Node destination, double distance) {
            adjacentNodes.put(destination, distance);
        }

        public Node(String id, String coords) {
            this.id = id;
            this.coords = coords;
        }


        public String getCoords() {
            return this.coords;
        }

        public Map<Node, Double> getAdjacentNodes() {
            return this.adjacentNodes;
        }

        public void setDistance(double val) {
            this.distance = val;
        }

        public double getDistance() {
            return this.distance;
        }

        public void setShortestPath(List<Node> sp) {
            this.shortestPath = sp;
        }

        public List<Node> getShortestPath() {
            return this.shortestPath;
        }

        public String getId(){
            return this.id;
        }

    }

    public class Graph {

        private Set<Node> nodes = new HashSet<>();

        public void addNode(Node nodeA) {
            nodes.add(nodeA);
        }

        public Set<Node> getNodes() {
            return this.nodes;
        }

    }
    
    
    public List<Addresses> initAddresses()
    {
    	File labdatafile = new File("../../../data/labelledData.csv");
    	List<Addresses> listOfAddresses = new ArrayList<>();
    	List<Graph> graphs = new ArrayList<>();
    	try {
			FileReader fileReader = new FileReader(labdatafile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String temp = bufferedReader.readLine();
			
			int count = 0;
			int total = 0;
			
			while(temp != null)
			{
				String[] mesurations = temp.split(",");
				String toCompare = mesurations[mesurations.length-2].substring(1);
				if(toCompare.contains("San") && !toCompare.contains("Sant"))
				{
					toCompare = toCompare.replace("San", "S.");
				}
				
				
				
				final String finalToCompare = toCompare;
				Addresses ad = listOfAddresses.stream().filter(s -> {return s.getName().equals(finalToCompare);}).findAny().orElse(null);
				
				
				total+=1;
				if(ad == null)
				{
					ad = new Addresses(toCompare,0,0);
				}
				else {
					count+=1;
					listOfAddresses.remove(ad);
				}
				if(mesurations[mesurations.length-1].equals("0"))
				{
					//good
					ad.addSum(1);
				}
				else if(mesurations[mesurations.length-1].equals("1"))
				{
					//verybad
					ad.addSum(500);
				}
				else {
					//bad
					ad.addSum(250);
				}
				
				ad.incrementCount();
				listOfAddresses.add(ad);
				temp = bufferedReader.readLine();
			}
			

		
			
						
		} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	
        }
    	
    	return listOfAddresses;
    }
    

    Graph graph;
    List<Node> listOfNodes = new ArrayList<>();
    
    public Graph initGraph(double q, List<Addresses> listOfAddresses) {
    	
    	File edgesfile = new File("../../../data/edgeswAdd.csv");
    	File nodesfile = new File("/../../../data/nodes.csv");
    	
    	listOfNodes = new ArrayList<>();
    	
   
    	
	    	//creo l'array dei nodi semplice
	    	try {
	    		FileReader fr = new FileReader(nodesfile);
	    		BufferedReader br = new BufferedReader(fr);
	    		String temp = br.readLine();
	    		boolean firstline = true;
	    		while(temp != null)
	    		{
	    			if(!firstline)
	    			{
	    				String[] parts = temp.split(",");
	    	
	    				if(parts.length > 1)
	    				{
	    					listOfNodes.add(new Node(parts[0], parts[2] + "," + parts[1]));
	    				}
	    				
	    			}
	    			else {
	    				firstline = false;
	    			}
	    			temp = br.readLine();
	    		}
	    		
	    	} catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
	    	System.out.println("nodi" + listOfNodes.size());
	    	
	   
	    	System.out.println("q " + q);
	    	//creo i collegamenti tra nodi pesati con qualità asfalto
	    	
	    	int count = 0;
	    	int total = 0;
	    	
	    	
	    	Graph graphwWeight = new Graph();
	    	double sum4medium = 0;
	   
	    	try {
	    		FileReader fr = new FileReader(edgesfile);
	    		BufferedReader br = new BufferedReader(fr);
	    		String temp = br.readLine();
	    		boolean firstline = true;
	    		Node nodoSource = null;
	    		Node nodoDest = null;
	    	
	   
	    	
	    		while(temp != null) {
	    			
	    			double tempq = q;
	    			total+=1;
	    			String[] parts = temp.split(",");
	    			nodoSource = listOfNodes.stream().filter(s -> {return s.getId().equals(parts[1]);}).findAny().orElse(null); 
	    			nodoDest = listOfNodes.stream().filter(s -> {return s.getId().equals(parts[2]);}).findAny().orElse(null); 
	    			
	    			if(nodoDest == null)
	    			{
	    				System.out.println("null");
	    			}
	    			listOfNodes.remove(nodoSource);
	    			Addresses relativead = listOfAddresses.stream().filter(s->{return s.getName().equalsIgnoreCase(parts[parts.length-1]);}).findAny().orElse(null);
	    			double multiplier = 0.0;
	    			if(relativead == null)
	    			{
	    				multiplier = 81;
	    			}
	    			else {
	    				count+=1;
	    				multiplier = (relativead.getSum() / relativead.getCount());
	    				sum4medium += multiplier;
	    				
	    			}
	    			
	    			double distance = Double.parseDouble(parts[3]);

	    		
	    
	    			
	    			nodoSource.addDestination(nodoDest, (distance * (1-q) + multiplier * q));
	    		
	    			listOfNodes.add(nodoSource);
	    			temp = br.readLine();
	    		}
	    		
	    		System.out.println("Media : " + sum4medium / count);
	    		
    		
	    		for(int i=0; i<listOfNodes.size(); i++)
	    		{
	    			graphwWeight.addNode(listOfNodes.get(i));
	    		}
	    	
	    		
	    
	    	
	    	} catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
	    	return graphwWeight;

    
    }
    
    
    public static Graph calculateShortestPathFromSource(Graph graph, Node source, Node destination) {
        source.setDistance(0);

        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Map.Entry<Node, Double> adjacencyPair :
                    currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                if(adjacentNode != null) {
                    double edgeWeight = adjacencyPair.getValue();
                    if (!settledNodes.contains(adjacentNode)) {
                        calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                        unsettledNodes.add(adjacentNode);
                    }
                }
            }
            settledNodes.add(currentNode);
            if(currentNode == destination)
            {
            	break;
            }
        }
        return graph;
    }


    private static Node getLowestDistanceNode(Set<Node> unsettledNodes) {
        Node lowestDistanceNode = null;
        double lowestDistance = Double.MAX_VALUE;
        for (Node node : unsettledNodes) {
            double nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode, double edgeWeigh, Node sourceNode) {
        double sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeigh < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeigh);
            LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }
    
    public List<PointOfRoute> startSearch(String start, String stop, Graph chosen)
    {
    	
    	Node source = listOfNodes.stream().filter(s -> {return s.getCoords().equals(start);}).findAny().orElse(null); 
    	Node destination = listOfNodes.stream().filter(s -> {return s.getCoords().equals(stop);}).findAny().orElse(null); 
    	PointOfRoute firstNodeFix = null;
    	PointOfRoute prelastNodeFix = null;
    	PointOfRoute lastNodeFix = null;
    	
    
    	
    	List<PointOfRoute> pathInPoints = new ArrayList<>();
    	
    	if(source == null)
    	{
    		firstNodeFix = new PointOfRoute(start.split(",")[0], start.split(",")[1]);
    		pathInPoints.add(firstNodeFix);
    		source = getClosestPoint(start);
    		
    	}
    	if(destination == null)
    	{
    		destination = getClosestPoint(stop);
    		lastNodeFix = new PointOfRoute(stop.split(",")[0], stop.split(",")[1]);
    	}
    	Graph result = calculateShortestPathFromSource(chosen,source,destination);
   
    	List<Node> calculatedNodes = new ArrayList<>(result.getNodes());
    	final Node destinationFinal = destination;
    	Node newdestinationnode = calculatedNodes.stream().filter(s -> {return s.getCoords().equals(destinationFinal.getCoords());}).findAny().orElse(null);
    	List<Node> path = newdestinationnode.getShortestPath();
    
    	if(path.size() == 0) {
    		path = getclosestPointwPath(destination.getCoords(), calculatedNodes);
    		prelastNodeFix = new PointOfRoute(newdestinationnode.getCoords().split(",")[0], newdestinationnode.getCoords().split(",")[1]);
    	}
    	
    	
    	for(int i=0; i<path.size(); i++)
    	{
    		pathInPoints.add(new PointOfRoute((path.get(i).getCoords().split(",")[0]), path.get(i).getCoords().split(",")[1]));
    	}
    	
    	if(prelastNodeFix != null)
    	{
    		pathInPoints.add(prelastNodeFix);
    	}
    	
    	if(lastNodeFix != null)
    	{
    		pathInPoints.add(lastNodeFix);
    	}
    	    	
    	return pathInPoints;
    
    }
    
    public Node getClosestPoint(String coords) {
    	
    	Double lat1 = Double.parseDouble(coords.split(",")[0]);
        Double lon1 = Double.parseDouble(coords.split(",")[1]);

        Double min = Double.MAX_VALUE;
        Node returnNode = null;

        for(int i=0; i<listOfNodes.size();i++)
        {
            if(listOfNodes.get(i).getAdjacentNodes().size() > 0) {
                Double lat2 = Double.parseDouble(listOfNodes.get(i).getCoords().split(",")[0]);
                Double lon2 = Double.parseDouble(listOfNodes.get(i).getCoords().split(",")[1]);
                Double distance = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * 6371;
                if (distance < min) {
                    min = distance;
                    returnNode = listOfNodes.get(i);
                }
            }
        }
        
        
        return returnNode;
    	
    }
    
    public List<Node> getclosestPointwPath(String coords, List<Node> calculatedNodes)
    {

        Double lat1 = Double.parseDouble(coords.split(",")[0]);
        Double lon1 = Double.parseDouble(coords.split(",")[1]);

        Double min = Double.MAX_VALUE;
        List<Node> returnPath = null;

        for(int i=0; i<calculatedNodes.size(); i++)
        {

            if(calculatedNodes.get(i).getShortestPath().size() > 0)
            {
                
                Double lat2 = Double.parseDouble(calculatedNodes.get(i).getCoords().split(",")[0]);
                Double lon2 = Double.parseDouble(calculatedNodes.get(i).getCoords().split(",")[1]);
                Double distance = Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*6371;
                if(distance < min)
                {
                    min = distance;
                    returnPath = calculatedNodes.get(i).getShortestPath();
                }
            }

        }
        
        System.out.println("distance : " + min);
        return returnPath;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<Graph> graphs = new ArrayList<>();
		routingServerMain rsm = new routingServerMain();
		
		List<Addresses> listOfAddresses = rsm.initAddresses();
		Express app = new Express();
		app.get("/", (req, res) -> {
			res.send("hello world");
		});
		
		app.get("/route", (req, res) -> {
			System.out.println("New request");
			String lat = req.query("start");
			String lon = req.query("stop");
			String alg = req.query("algorithm");
			
			System.out.println("latitude, longitude and alg " + lat + "," + lon + "," + Double.parseDouble(alg));
			
			Graph graphInstance = rsm.initGraph(Double.parseDouble(alg), listOfAddresses);
			
			
			List<PointOfRoute> path = rsm.startSearch(lat, lon, graphInstance);
			
			System.out.println("Algoritmo : " + alg);
			
			String toReturn = "";
			for(int i=0; i<path.size(); i++)
			{
				toReturn += path.get(i).getStringLonLat() + ";";
			}
			res.send(toReturn);
		});
		
		app.listen(4000);
	}

}