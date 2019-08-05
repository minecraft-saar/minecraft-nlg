A NLG component for the Minecraft domain
========================================

Compilation: run `./gradlew shadowJar`

Running: 

 - `java -jar build/libs/minecraft-nlg-0.1-SNAPSHOT.jar -c -m world.json -t minecraft.tirtg`
   and then feed a location to stdin to get an expression on stdout
 - instead of `-c` you can use a positional argument to provide a single location
   in that mode you need to restart the generator for each expression
 - use `--help` for help.


## Versions:

 - inlg 2019: checkout the inlg2019 tag, compile and run
   `java -cp build/libs/minecraft-nlg-0.1.0-SNAPSHOT-all.jar de.saar.coli.minecraft.experiments.Inlg2019`
   It will show the generated sentences on the command line.
