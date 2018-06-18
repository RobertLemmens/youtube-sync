# Sync youtube playback between browsers
This is an application that syncs youtube playback between connected users using websockets. The entire thing is build with scala. Akka on the backend and ScalaJS on the frontend side. 

## Usage
First fire up a SBT session in the root folder. Then:

```scala
project backend
```
followed by:
```scala
reStart
```
The backend should now be running. Next compile the frontend:
```scala
project frontend
```
followed by:
```scala
fastOptJS
```

Opening the index.html file in the src folder of the frontend project will now open the latest compiled version. Enter a name and connect to the server to start adding video's. 

If you are planning on developing the frontend, typing:
```scala
~fastOptJS
```
will automatically compile to the latest javascript when you make code changes.
## Todo
* Create a better build. One that starts backend and serves the generated frontend HTML through the backend for easy deployment.
* Improve functionality
