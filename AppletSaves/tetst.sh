mv tetst ../Applet/
cd ../Applet/
jar cf tetst.jar *.class tetst Utils XML time
rm tetst
mv tetst.jar ../AppletSaves
cd ../AppletSaves
rm tetst.sh
rm tetst.bat