A NLG component for the Minecraft domain
========================================

This repository contains a sentence generator (and a grammar).  Based
on a 3d representation of a world and a target object to describe, the
system 1) translates the representation to a relation-based one and 2)
computes the optimal referring expression for the target object in the
world, based on (learned or otherwise generated) grammar weights.


Compilation: run `./gradlew shadowJar`

Running: 

 - `java -jar build/libs/minecraft-nlg-0.1-SNAPSHOT.jar -c -m world.json -t minecraft.tirtg`
   and then feed a verb and a location to stdin (e.g. `build:loc25`) to get an expression on stdout
 - instead of `-c` you can use a positional argument to provide a single location
   in that mode you need to restart the generator for each expression
 - use `--help` for help.


## Versions:

 - inlg 2019: checkout the inlg2019 tag, compile and run
   `java -cp build/libs/minecraft-nlg-0.1.0-SNAPSHOT-all.jar de.saar.coli.minecraft.experiments.Inlg2019`
   It will show the generated sentences on the command line.

## Example for simple definite referents:

The file `examples/simple-definites.irtg` contains a simple example
for generating referring expression without relations to other objects
in the world.  A corresponding model of a world is in
`examples/simple-definites.json`.  You can run the generator like this:

 `java -jar build/libs/minecraft-nlg-0.1.0-SNAPSHOT-all.jar -c -m examples/simple-definites.json -t examples/simple-definites.irtg`

The program expects input in the form of `verb:object`.

For example, the input `destroy:b1` will yield the following output:
```
{"orange":[["b2"]],"block":[["b1"],["b2"]],"it":[["b1"]]}
destroy the previous block
```

The first line is always the current model of the world to make debugging easier.
