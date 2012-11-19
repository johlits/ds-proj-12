mkdir temp
cp *.class temp
cp Sim/*.class temp
cp Editor/*.class temp
cd temp
mkdir Utils
mkdir XML
mkdir time
mkdir icon
cp ../Utils/*.class Utils
cp ../XML/*.class XML
cp ../time/*.class time
cp ../icon/* icon

jar cf Sim.jar *.class Utils XML time icon
chmod +x Sim.jar

mv Sim.jar ..
cd ..
rm -rf temp

