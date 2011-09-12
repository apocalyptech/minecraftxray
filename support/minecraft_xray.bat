@echo. >> minecraft_xray_output_log.txt
@echo Launching Minecraft... X-Ray >> minecraft_xray_output_log.txt
java -Xms256m -Xmx1024m -Djava.library.path=lib/native -jar xray.jar 1>> minecraft_xray_output_log.txt 2>&1
@echo.
@echo X-Ray log saved to minecraft_xray_output_log.txt
@echo.
@pause
