#/bin/sh
NUM_ARGS=$#;
if [ $NUM_ARGS != 1 ] 
then
  echo "Usage: run.sh <socket.io server url>" 
  exit 1
fi
java -classpath ./bin:./socketio.jar ChatDemo $1
