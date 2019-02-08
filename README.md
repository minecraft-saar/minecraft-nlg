A NLG component for the Minecraft domain
========================================

Compilation: run `./gradlew shadowJar`

Running: 

 - `java -jar build/libs/minecraft-nlg-0.1-SNAPSHOT.jar -c -m world.json -t minecraft.tirtg`
    and then feed a location to stdin to get an expression on stdout
 - instead of `-c` you can use a positional argument to provide a single location
   in that mode you need to restart the generator for each expression
 - use `--help` for help.
