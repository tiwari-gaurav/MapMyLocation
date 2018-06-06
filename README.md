# MarkMyLocation
This is a simple application which has a background service which will give location updates and those locations will be marked on map.
There is a service class call BackgroundService. This class is using GoogleLocationAPI for location update.Everytime location is changing, it will call onLocationChange() method where we will get latitude and longitude and these lat long will be send to the Activiy by calling method sendMessageToUI(). Using LocalBroadcastManager we will be sending the values to the UI.
In the Main class we will get the values in onRecieve() and will add those lat, long values to an arrayList.
FIrst we need to intialise map and set the marker at the current location. If the location is not provided by the service then we can request to network provider and get the last known location and put a markeron that point.
DrawMarker() method will mark the path once you stop the service using toggle button.
