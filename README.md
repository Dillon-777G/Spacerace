# Spacerace
Orbital Clock
This project is based on a love for astronomers and everything they have accomplished. Designed around kitty terminal, it is a simple clock that also shows a current relative position of the earth by using an
array of characters that is constantly printing to create a dynamic effect on the stars. Future plans for this project are to address compatibility with other terminals as well as a redraw to include the entirety
of the solar system.

**USAGE**
Simply navigate to the hidecodehere directory and input: ./AlphaLaunch(screen3) 
  -this will launch in a window that should work on most computers, currently only on kitty. I plan to implement support for windows, mac, and gnome in a case statment soon.

To run in any terminal:
  compile java
    -javac Spacerace.java
    -java Spacerace

It is possible to delete targeted rows of exccess space with:
  -java Spacerace -m followed by an integer of a range of integers.
      EX: -java Spacerace -m 4-6

The program also currently can work with lolcat, I hope to create a smoother animation effect soon as it currently
slows the animation but creates a very pretty flow of color across the screen(screen2)

  
