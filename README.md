## About
My study version of 2048 game.   

## Build and Run
For build and run app you need: JDK 8+
Go to the project and run scripts:
        
    javac -d out src/game/*
    java -classpath ./out game.Starter
       
Also, you can pack project to execute jar file:
    
    jar -cmf manifest.mf 2048game.jar  -C out .    
That's all.

## Author
* Mykola Kostyshyn - [mikkiko](https://github.com/mikkiko)