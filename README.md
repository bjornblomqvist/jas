# Java as script POC

The idea is to be able to write and use java as if was a script language. Run
source files use shebang.

## Goals

**V1**

//- A commandline runner
//- Support for shebang
- Support for import statements
- Make it installable with bash script like RVM and others
  Use jarinstaller =)

**V2**

- Simple way to include decencies (require)

**V3**

- Support declaring methods
- Support declaring classes

## Implementation ideas

My plan is to create class from the script file where all methods definitions end up
as static methods in a class and all the code is run in the static context of the class.

To make it really useful it must be easy to include other libraries.


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

_test.jas_
```
    #!/usr/bin/jas
    
    System.out.println("hello world");
```

```
    ~/dev$ ./test.jas
    hello world
``` 

## V2 feature examples

compile group: 'log4j', name: 'log4j', version: '1.2.17'

_test.jas_
```
    require("log4j:log4j:1.2.17");
    import org.apache.log4j.Logger;
    
    Logger logger = Logger.getLogger("Test.js");
    logger.info("hello world");
```

_test.jas_
```
    require("./lib/log4j-1.2.17.jar");
    import org.apache.log4j.Logger;
    
    Logger logger = Logger.getLogger("Test.js");
    logger.info("hello world");
```