#!/bin/sh
BINDIR=$(dirname "$(readlink -fn "$0")")
cd "$BINDIR"
exec java -Xmx1024M -cp clojure-1.6.0-alpha1.jar:craftbukkit.jar org.bukkit.craftbukkit.Main -o true
