@echo. >> minecraft_xray_output_log.txt
@echo Launching Minecraft X-Ray >> minecraft_xray_output_log.txt
java -Xms256m -Xmx1024m -cp AppleJavaExtensions.jar;jinput.jar;lwjgl.jar;lwjgl_test.jar;lwjgl_util.jar;lwjgl_util_applet.jar;lzma.jar;xray.jar;snakeyaml-1.9.jar -Djava.library.path=. com.apocalyptech.minecraft.xray.XRay 1>> minecraft_xray_output_log.txt 2>&1
@echo.
@echo X-Ray log saved to minecraft_xray_output_log.txt
@echo.
@pause
