package com.routingServerMaven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.LatLng;
import com.google.code.geocoder.model.LatLngBounds;

import express.Express;

public class App {
	
	public class Addresses {
		
		private String name;
		private Double weight;
		
		public Addresses(String n, double w) {
			this.name = n;
			this.weight = w;
		}
		
		public String getName() {
			return this.name;
		}
		
		public double getWeight() {
			return this.weight;
		}
	}
	
	
	public class Node {

        private String id;
        private String coords;

        private List<Node> shortestPath = new LinkedList<>();

        private Integer distance = Integer.MAX_VALUE;

        Map<Node, Integer> adjacentNodes = new HashMap<>();

        public void addDestination(Node destination, int distance) {
            adjacentNodes.put(destination, distance);
        }

        public Node(String id, String coords) {
            this.id = id;
            this.coords = coords;
        }


        public String getCoords() {
            return this.coords;
        }

        public Map<Node, Integer> getAdjacentNodes() {
            return this.adjacentNodes;
        }

        public void setDistance(int val) {
            this.distance = val;
        }

        public int getDistance() {
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
    
    List<Node> listOfNodes;
    Graph graph;
    
    public static void initGraph() {
    	File labdatafile = new File("/Users/roccopastore/Desktop/NavigationMap/labdata");
    	createWeightedAddresses(labdatafile);
    	/*File edgesfile = new File("/Users/roccopastore/Desktop/Scrivania - MacBook Air di Rocco/Tracker Tesi/edges.csv");
    	File nodesfile = new File("/Users/roccopastore/Desktop/Scrivania - MacBook Air di Rocco/Tracker Tesi/nodes.csv");
    	
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
    				listOfNodes.add(new Node(parts[0], parts[2] + parts[1]));
    			}
    			else {
    				firstline = false;
    			}
    			temp = br.readLine();
    		}
    		
    		fr = new FileReader(edgesfile);
    		br = new BufferedReader(fr);
    		temp = br.readLine();
    		firstline = true;
    		Node nodoSource = null;
    		Node nodoDest = null;
    		while(temp != null) {
    			if(!firstline) {
    				String[] parts = temp.split(",");
    				nodoSource = listOfNodes.stream().filter(s -> {return s.getId().equals(parts[1]);}).findAny().orElse(null); 
    				nodoDest = listOfNodes.stream().filter(s -> {return s.getId().equals(parts[2]);}).findAny().orElse(null); 
    				listOfNodes.remove(nodoSource);	
    			}
    			temp = br.readLine();
    		}
    	
    	} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
    
	private static void createWeightedAddresses(File labdatafile) {
		// TODO Auto-generated method stub
		try {
			FileReader fileReader = new FileReader(labdatafile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String temp = bufferedReader.readLine();
			List<String> listOfAddresses = new ArrayList<>();
			List<Integer> goodPoints = new ArrayList<>();
			List<Integer> badPoints = new ArrayList<>();
			List<Integer> verybadPoints = new ArrayList<>();
			Geocoder gc = new Geocoder();
			
			while(temp != null)
			{
				String[] mesurations = temp.split(",");
				LatLng location = new LatLng();
				System.out.println(new BigDecimal(mesurations[mesurations.length-3]));
			
				location.setLat(new BigDecimal(mesurations[mesurations.length-3]));
				location.setLng(new BigDecimal(mesurations[mesurations.length-2]));
				GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setLocation(location).getGeocoderRequest();
				GeocodeResponse geocoderResponse = gc.geocode(geocoderRequest);
				System.out.println(geocoderResponse);
				temp = bufferedReader.readLine();
			}
			
		} catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	
        }
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		initGraph();
		Express app = new Express();
		app.get("/", (req, res) -> {
			res.send("hello world");
		});
		
		app.listen(4000);
	}

}
