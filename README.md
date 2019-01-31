# Java as script POC

The idea is to be able to write and use java as if it was a script language. This should enable java developers
to use java in places like build scripts, startup scripts, configuration.

## Implementation idea

Create class from the script file where all methods definitions end up as static methods in a class and all the 
code is run in the static context of the class.

## Installation

    curl -OL https://github.com/bjornblomqvist/jas/releases/download/0.3.0/jas-0.3.0.jar
    java -jar jas-0.3.0.jar --install
    export PATH=$PATH:$HOME/.jars/bin
    jas --help

## Usage example v1

__Run a script__

_test.jas_
```
System.out.println("hello world");
```

```
~/dev$ jas test.jas
hello world
```


__Run with shebang__

_test2.jas_
```
#!/usr/bin/jas
    
System.out.println("hello world");
```

```
~/dev$ ./test.jas
hello world
```


## V2 feature examples

_test3.jas_
```
import static se.bjornblomqvist.Require.require;

require("/tmp/log4j-1.2.17.jar");
import org.apache.log4j.Logger;

Logger logger = Logger.getLogger("Test.js");
logger.info("hello world");
```