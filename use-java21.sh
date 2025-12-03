#!/bin/bash
# Helper script to switch to Java 21 for this project
# Usage: source use-java21.sh

if [ -f ~/.sdkman/bin/sdkman-init.sh ]; then
    source ~/.sdkman/bin/sdkman-init.sh
    sdk use java 21.0.1-tem 2>/dev/null || sdk install java 21.0.1-tem
    export JAVA_HOME=$HOME/.sdkman/candidates/java/current
    export PATH=$JAVA_HOME/bin:$PATH
    echo "✅ Switched to Java 21"
    java -version
else
    echo "❌ SDKMAN not found. Please install SDKMAN first."
    echo "   Visit: https://sdkman.io/install"
fi

