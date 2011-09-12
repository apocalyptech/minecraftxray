#!/bin/sh
LOGFILE=minecraft_xray_output_log.txt
cd "`dirname "$0"`"
echo >> ${LOGFILE}
echo "Launching Minecraft X-Ray..." >> ${LOGFILE}
java -Xms256m -Xmx1024m -Djava.library.path=lib/native -jar xray.jar 2>&1 | tee -a ${LOGFILE}

echo
echo "X-Ray log saved to ${LOGFILE}"
echo
