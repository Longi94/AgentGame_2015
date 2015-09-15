/*******************************************************************************
	
	Copyright Peter Dienes 2013.
	
	pearl_.asl - 70K+ bot 
	
*******************************************************************************/

////////////////////////////////////////////////////////////////////////////////
//  Being out of energy
////////////////////////////////////////////////////////////////////////////////

+time(_) :  myenergy(Energy) & Energy < 10 <-
	wait.

////////////////////////////////////////////////////////////////////////////////
//  Eating related things
////////////////////////////////////////////////////////////////////////////////
-!eat_at_my_pos : mydir(D) 
	<- 
	true.

+!eat_at_my_pos 
	<- 
	eat.

////////////////////////////////////////////////////////////////////////////////
//  Eated all the food - now look around
////////////////////////////////////////////////////////////////////////////////

+time(_) : lookAround(Last) & food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D)
	<-
	+target(Fx,Fy);
	-lookAround(_);
	!move(D).
	
+time(_) : lookAround(Last) & mydir(D) & D == Last
	<-
	-lookAround(_);
	!move(D).

+time(_) : lookAround(Last) & mydir(D)
	<-
	if(D == 3) {
		turn(0);
	} else {
		turn(D+1);
	}.

////////////////////////////////////////////////////////////////////////////////
//  Moving
////////////////////////////////////////////////////////////////////////////////

// If we are unable to move (our last position is the same as our position now),
// forget the target and schedule a random turn to avoid geting stucked together 
// with an agent moving from the opposite direction
-!move(_) : mypos(X,Y) & mydir(D) <-
	-target(_,_);
	if(secondtarget(A,B)) {
		+target(A,B);
		-secondtarget(_,_);
	}
	if(D < 3) {
		step(D+1);
	} else {
		step(0);
	}.

// If we are not stucked, we just have to keep moving
+!move(Dir)
	<-
	step(Dir).

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
// If yes, let's eat it! 
+time(_): food(Food) & .min(Food,[0,V,X,Y]) <-
	!eat_at_my_pos.
	
// If we get at the target cell, but there is no food there anymore (because if 
// there would be a food, the plan above would have been executed) let's sadly  
// forget about our target.
+time(_): target(X,Y) & mypos(X,Y) & mydir(Dir) & secondtarget(A,B) <-
	-target(X,Y);
	+target(A,B);
	-secondtarget(A,B);
	!move(Dir).

+time(_): target(X,Y) & mypos(X,Y) & mydir(D) <-
	-target(X,Y);
	if(D == 0) {
		+lookAround(3);
	} else {
		+lookAround(D);
	}
	
	if(D == 3) {
		turn(0);
	} else {
		turn(D+1);
	}.
	
+time(_): food(Food) & .min(Food,[_,V,Fx,Fy]) & target(A,B) & mypos(Mx,My)
			& A \== Fx & B \== Fy & not (secondtarget(C,D) )
	<-
	+secondtarget(Fx,Fy);
	if(A > Mx) {
		!move(1);
	}
	if(A < Mx) {
		!move(3);
	}
	if(B > My) {
		!move(2);
	}
	if(B < My) {
		!move(0);
	}.
	               
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
// plans above would fit it, thus here we are sure that we had no plans earlier.

+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) <-
	+target(Fx,Fy);
	!move(D).		

// if it is on the corner of the map, step to the core's direction

// left-top -> go right
+time(_) : mypos(X,Y) & X < 10 & Y < 10
	<-
	if(mydir(2)) {
		// do nothing
	} else {
		turn(2);
	}
	!move(2).

// right-top -> move left
+time(_) : mypos(X,Y) & X > 49 & X > 49
	<-
	if(mydir(3)) {
		// do nothing
	} else {
		turn(3);
	}
	!move(3).

// left-bottom -> move right
+time(_) : mypos(X,Y) & Y > 49 & X < 10
	<-
	if(mydir(1)) {
		// do nothing
	} else {
		turn(1);
	}
	!move(1).
	
// right-bottom -> move left
+time(_) : mypos(X,Y) & Y < 10 & X > 49
	<-
	if(mydir(0)) {
		// do nothing
	} else {
		turn(0);
	}
	!move(0).

// if it is on the edge of the map, turn to the core's direction

// top -> turn down if X > 30, and turn right else
+time(_) : mypos(X,Y) & Y < 10 & X > 30 & (mydir(0) | mydir(1) | mydir(3))
	<-
	turn(2).
	
+time(_) : mypos(X,Y) & Y < 10 & X <= 30 & (mydir(0) | mydir(2) | mydir(3))
	<-
	turn(1).

// left -> turn right if Y < 30, and turn up else
+time(_) : mypos(X,Y) & X < 10 & Y <= 30 & (mydir(0) | mydir(2) | mydir(3))
	<-
	turn(1).
	
+time(_) : mypos(X,Y) & X < 10 & Y > 30 & (mydir(1) | mydir(2) | mydir(3))
	<-
	turn(0).
	
// bottom -> turn up if X < 30, and turn left else
+time(_) : mypos(X,Y) & Y > 49 & X <= 30 & (mydir(1) | mydir(2) | mydir(3))
	<-
	turn(0).
	
+time(_) : mypos(X,Y) & Y > 49 & X > 30 & (mydir(0) | mydir(1) | mydir(2))
	<-
	turn(3).
	
// right -> turn left if Y > 30, and turn down else
+time(_) : mypos(X,Y) & X > 49 & Y > 30 & (mydir(0) | mydir(1) | mydir(2))
	<-
	turn(3).
	
+time(_) : mypos(X,Y) & X > 49 & Y <= 30 & (mydir(0) | mydir(1) | mydir(3))
	<-
	turn(2).
	
////////////////////////////////////////////////////////////////////////////////
//  Time triggered random roaming of the agent
////////////////////////////////////////////////////////////////////////////////
	
// The rules bellow are executed only, if the agent has no target and it can't 
// see any food right now.

// Otherwise we should just move forward...
+time(_): mypos(X,Y) & mydir(0) & Y > 10
	<-
	!move(0).
	
+time(_): mypos(X,Y) & mydir(1) & X < 49 
	<-
	!move(1).
	
+time(_): mypos(X,Y) & mydir(2) & Y < 49 
	<-
	!move(2).
	
+time(_): mypos(X,Y) & mydir(3) & X > 10 
	<-
	!move(3).
	
// ... and turn back when reaching the wall                         
+time(_): mydir(D) & D < 3 <-
	turn(D+1).

+time(_): mydir(D) & D = 3 <-
	turn(0).
	
+time(_)
	<-
	wait.
