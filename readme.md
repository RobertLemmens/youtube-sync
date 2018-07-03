![gui1](https://raw.githubusercontent.com/RobertLemmens/youtube-sync/master/backend/src/main/resources/youtube-sync.PNG)
# :clock130: Sync youtube playback between browsers :clock10:
This is an application that syncs youtube playback between connected users using websockets. The entire thing is build with scala. Akka on the backend and ScalaJS on the frontend side. 

## Usage
### Developing
First fire up a SBT session in the root folder. Then:
```scala
project backend
```
followed by:
```scala
reStart
```
The backend should now be running. The frontend will also be build and served through akka.
Access the site by going to localhost:8080


If you want to build only the JS files do:
```scala
project frontend
```
followed by:
```scala
fastOptJS
```
You will need a copy of the index.html in the resource folder of the backend project and point that copy towards the build JS if you want to build the frontend without the backend.

### Build a jar
First fire up a SBT session in the root folder. Then:
```scala
project backend
```
followed by:
```scala
assembly
```
This will build a standalone jar ready for deployment.

## Todo
* DONE -  Create a better build. One that starts backend and serves the generated frontend HTML through the backend for easy deployment.
* Improve functionality
