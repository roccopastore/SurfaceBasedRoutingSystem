# RoadSurfaceBasedRouting4Cyclists
RoadSurfaceBasedRouting4Cyclists is an MCS Navigation System Based on Road Surface Quality for Bicycle Riders.

The /Server folder contains the Java backend system which develops an instance of a Dijkstra algorithm that considers the best route based on some road surface data previously collected through the sensors of a smartphone. It has to be started to allow the correct working of the entire system.

The /Client folder contains the Android App. It can be used by an user to select a starting and a destination point on a map and request the best path that connects them to the server. The answer will be a set of coordinates that will be joined in a path through some APIs provided by Mapbox.

The script randomforest.py is the Python supervised algorithm which classifies the points found during the data collection phase in good, bad and very bad classes. It is based on an unsupervised initial classification.

![diagrammaSecondaParte](https://github.com/roccopastore/SurfaceBasedRoutingSystem/blob/main/Images/diagrammaSecondaParte.png)
