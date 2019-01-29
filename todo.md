
- A set of default libraries?
    Would be nice to have known set of java libraries that are always available for JAS
- Faster startup with nailgun or simalear
- Have a compile cache. MD5 of source
- Error message for require that is wrong
- Document how to build/contribute

# Before sharing
- JarInstaller should give the user a commandline that adds ~/.jars/bin to the path for the current bash instance
    - New release of jar installer that prints out how to update the current $PATH    
    
- Simple download and install from github
    - Creata a release

    curl -O https://github.com/bjornblomqvist/jas/releases/download/0.2.0/jas-0.2.0.jars
    java -jar jas-0.2.0.jar --install
    export PATH=$PATH:$HOME/.jars/bin
    jas

# 2019-01-26

- Get dependencies to work
    require("./test/bla.jar")
    Write code to create a classLoader with all require statements we can find. Use it as classLoader for the compiler =)

# 2019-01-20

//- Get shebang working
//- Print class source on error
//- Make import work
//- Make args available in script
//- Package with jar installer

# 2019-01-18
// - Get it up and running 
    1. Read given file path.
    2. Wrap it in a class
    3. Compile
    4. Run

# 2019-01-17
- Get it up and running 
    1. Read given file path.
    2. Wrap it in a class
    3. Compile
    4. Run