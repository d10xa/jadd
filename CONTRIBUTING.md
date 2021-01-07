## REPL debug with intellij idea

Run terminal supporting autocompletion, then run sbt shell

```
sbt -jvm-debug 5005
```

In intellij idea:

Edit Configurations... -> Add New Configuration -> Remote JVM Debug

run task `debug5005`

Execute sbt task `run` manually.

