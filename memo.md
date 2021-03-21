# Memo
## Basic Introduction

This is a key - value store system with a single parition. 
There are three operations: get, put and cas. My implementation 
uses Sequence Paxos with Gossip Leader Election for
 synchronizing the replicas. Currently, there is no
 Failure detector or Broadcast component. 
The ballot leader election can take
the responsibility of the Failure detector. 
In addition, in Sequence Paxos, there is no need 
of broadcast. 

You need to run 4 servers to start up. (need 4 servers in a partition) 

## To run

in the terminal:
```
sbt
compile //compile the program
test //run test: operation test, lin test, server crash test
server/assembly //generate server jar files
client/assembly //generate client jar files
```

run the server
```
java -jar server/target/scala-2.13/server.jar -p 45678 \\main server
java -jar server/target/scala-2.13/server.jar -p 45679 -s localhost:45678 \\server connecting to 45678
java -jar server/target/scala-2.13/server.jar -p 45680 -s localhost:45678 \\server connecting to 45678
java -jar server/target/scala-2.13/server.jar -p 45681 -s localhost:45678 \\server connecting to 45678

java -jar client/target/scala-2.13/client.jar -p 56787 -s localhost:45678 \\run client connecting to server 45678
```

## To do list
- [x] client kv store
- [x] connecting the component of SC and BLE
- [x] adjust overlay manager
- [x] server kv store
- [x] adjustment before testing
- [x] simple operation test
- [x] server crash test 
- [] LIN operation test