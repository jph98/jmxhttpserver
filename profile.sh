#
# Profile script
#

#export LD_LIBRARY_PATH="./linux-x86-32/libyjpagent.so:$LD_LIBRARY_PATH"
#echo $LD_LIBRARY_PATH

java -agentpath:./linux-x86-32/libyjpagent.so -jar target/jmxline-app.jar standalone
