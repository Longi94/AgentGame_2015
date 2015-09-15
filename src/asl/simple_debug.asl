/*******************************************************************************
	
	AgentGame v1.0.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2011.
	
	simple.asl - Simple agent running for the closest food it sees 
	
*******************************************************************************/


////////////////////////////////////////////////////////////////////////////////
//  INIT 
////////////////////////////////////////////////////////////////////////////////

// No initial beliefs yet
started.

+time(_): started <-
	-started;
	setcolor(0,255,0).


////////////////////////////////////////////////////////////////////////////////
//  Being out of energy
////////////////////////////////////////////////////////////////////////////////

+time(_): myenergy(Energy) & Energy<100 <-
	wait;
	debug.DebugAction.



////////////////////////////////////////////////////////////////////////////////
//  Eating related things
////////////////////////////////////////////////////////////////////////////////
-!eat_at_my_pos <- 
	true.

+!eat_at_my_pos <- 
	eat;
	debug.DebugAction.



////////////////////////////////////////////////////////////////////////////////
//  Moving
////////////////////////////////////////////////////////////////////////////////

// If we are unable to move (our last position is the same as our position now),
// forget the target and take a random turn to avoid geting stucked together 
// with an agent moving from the opposite direction
-!move(_) <-
	-target(_,_);
	.random(R);
	turn(R*4).

// If we are not stucked, we just have to keep moving - remembering our last 
// position to detect possible collisions later
+!move(Dir) : mypos(X,Y) <-
	step(Dir);
	debug.DebugAction.
	


////////////////////////////////////////////////////////////////////////////////
//  Time triggered "intelligence" of the agent
////////////////////////////////////////////////////////////////////////////////	

// Is there food at my position? 
//    - the list of food objects I see is Food 
//    - each food object is represented in the list in the form of 
//      [distance_from_me, food_value, position_x, position_y]
//    - the .min internal function chooses the minimal value in Food
//    - the food objects (=lists) are ordered by their first element (=distance)
//    - the  "[0,..." means that the distance of the closest food is zero
// If yes, let's eat it and forget about it as a target! 
+time(_): food(Food) & .min(Food,[0,V,X,Y]) <-
	-target(_,_);
	!eat_at_my_pos.
	
// If we get at the target cell, but there is no food there anymore (because if 
// there would be a food, the plan above would have been executed) let's sadly  
// forget about our target.
+time(_): target(X,Y) & mypos(X,Y) & mydir(Dir)<-
	-target(_,_);
	!move(Dir).
	
// If we have a target different from our current position, let's move towards 
// it. (Note: if our position would be the target position, one of the plans  
// above would have been selected earlier.)
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx>Mx <-
	!move(1).
	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx<Mx <-
	!move(3).
	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy>My <-
	!move(2).
    
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy<My <-
	!move(0).

// If we see some food and we had no target before, we select the closest 
// food and move towards it. (Note: if we would have a target, one of the 
// plans above would fit it, thus here we are sure that we had no plans earllier.
+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) & mypos(Mx,My) <-
	+target(Fx,Fy);
	!move(D).
	
	
	
////////////////////////////////////////////////////////////////////////////////
//  Time triggered random roaming of the agent
////////////////////////////////////////////////////////////////////////////////		
	
// The rules bellow are executed only, if the agent has no target and it can't 
// see any food right now.

// First we define what a random turn is, and how to make it:
+!randomTurn <-
	.random(R);
	turn(R*4);
	debug.DebugAction.

// With a 5% probability we turn into a random direction
+time(_): .random(P) & P>0.95 & debug.DebugAction <-
	!randomTurn.

// Otherwise we should just move forward...
+time(_): mypos(X,Y) & mydir(0) & Y>0 <-
	!move(0).
	
+time(_): mypos(X,Y) & mydir(1) & X<59 <-
	!move(1).
	
+time(_): mypos(X,Y) & mydir(2) & Y<59 <-
	!move(2).
	
+time(_): mypos(X,Y) & mydir(3) & X>0 <-
	!move(3).
	
// ... and turn back when reaching the wall
+time(_): mydir(D) & D < 2 <-
	turn(D+2);
	debug.DebugAction.

+time(_): mydir(D) & D >= 2 <-
	turn(D-2);
	debug.DebugAction.
	