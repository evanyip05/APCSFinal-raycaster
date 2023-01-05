This is a classic raycasting program done in search of a way to render 3d geometry without involving matricies,
and to explore "final class" project structure (I've found its not great amd packages are better)
This was a first attempt, found out its highly limited or would need a lot more work to make it suitable for more 3d geometry

It works using a 2d point moves around a maze like structure and casts rays too the different features of the maze

Depending on how far and at what angle the ray was cast,
a location is choosen to draw a bit of the feature from the perspective of the point

The program is organized into a few helper classes (calc, gui, and utilities) containing the math and graphics classes,
and a game class to contain a level, do the actual ray casting process, and handle movement and controls

many of the classes here are early prototypes of code I use today, more notably ExtendableThread, Panel, Frame, and Listener,
featured in (newer code) github.com/evanyip05/Storyboard2 (older code) .../APCSFinal-nonMatrix3DRenderer
