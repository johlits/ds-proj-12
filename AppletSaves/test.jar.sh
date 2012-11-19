mv test.jar ../Applet/
cd ../Applet/
jar cf test.jar.jar *.class test.jar Utils XML time
rm test.jar
mv test.jar.jar ../AppletSaves
cd ../AppletSaves
rm test.jar.sh
rm test.jar.bat