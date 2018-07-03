![gui1](https://raw.githubusercontent.com/RobertLemmens/youtube-sync/master/backend/src/main/resources/youtube-sync.PNG)
# :clock130: Sync youtube playback between browsers :clock10:
This is an application that syncs youtube playback between connected users using websockets. The entire thing is build with scala. Akka on the backend and ScalaJS on the frontend side. 
## How it works
When a room is created, the creator is granted the "leader" status. The server will now take his status and will force every other 
user to sync to this status. Every client loads his own youtube player so theres no real overhead on playing the videos themself. If someone is loading slow and not able to keep up
this has no impact on other users. The server will just keep requesting the slow loading player to adjust to the leader, until it catches up. If the room leader is slow / has buffering problems 
this will reflect back on the other users in the room as they will be synced back to where the leader is. This in practice wont result in stuttering as playstate is also taken into consideration.
If the playstate of the leader is buffering, the rest will also be set to buffering rather than keep playing and syncing back to the leader every few seconds.

Theres a small offset allowed in synchronisation between clients and leader (2 seconds) because in practice keeping sync on milliseconds is not worth the potential gains for our application. This offset can be changed in checkForUpdate method in Frontend.scala.


The server status ticks are every 5 second. This means every 5 seconds all users are checked for status and are candidate for potential updates.

If autoplay is on or next is called and there is no more video in the playlist, elevator music will be played.

Running version can be found at www.youtube-sync.nl
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
