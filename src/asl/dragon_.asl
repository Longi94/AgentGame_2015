////////////////////////////////////////////////////////////////////////////////
//  INIT 
////////////////////////////////////////////////////////////////////////////////

init.

////////////////////////////////////////////////////////////////////////////////
//  Being out of energy
////////////////////////////////////////////////////////////////////////////////

+time(_) :  myenergy(Energy) & Energy<100 <-
	wait.



////////////////////////////////////////////////////////////////////////////////
//  Eating related things
////////////////////////////////////////////////////////////////////////////////
-!eat_at_my_pos <- 
	true.

+!eat_at_my_pos <- 
	eat.

////////////////////////////////////////////////////////////////////////////////
//  Moving
////////////////////////////////////////////////////////////////////////////////

//Go to the nearest starting point of the loop
+time(_): init & mypos(X,Y) & mydir(D) & X = 0 & Y = 0 <-
	+target_low(10, 10);
	if (D = 2) {
		!move(2);
	} else {
		turn(2);
	}.
	
+time(_): init & mypos(X,Y) & mydir(D) & X = 59 & Y = 0 <-
	+target_low(49, 10);
	if (D = 2) {
		!move(2);
	} else {
		turn(2);
	}.
	
+time(_): init & mypos(X,Y) & mydir(D) & X = 0 & Y = 59 <-
	+target_low(10, 49);
	if (D = 1) {
		!move(1);
	} else {
		turn(1);
	}.
	
+time(_): init & mypos(X,Y) & mydir(D) & X = 59 & Y = 59 <-
	+target_low(49, 49);
	if (D = 0) {
		!move(0);
	} else {
		turn(0);
	}.
	
//Start looping when reaching the nearest point of the loop
+time(_): init & mypos(X,Y) & X = 10 & Y = 10 <-
	-target_low(_,_);
	-init;
	!move(2).
	
+time(_): init & mypos(X,Y) & X = 49 & Y = 10 <-
	-target_low(_,_);
	-init;
	!move(2).
	
+time(_): init & mypos(X,Y) & X = 10 & Y = 49 <-
	-target_low(_,_);
	-init;
	!move(1).
	
+time(_): init & mypos(X,Y) & X = 49 & Y = 49 <-
	-target_low(_,_);
	-init;
	!move(0).

// If we are unable to move (our last position is the same as our position now),
// forget the target and schedule a random turn to avoid geting stucked together 
// with an agent moving from the opposite direction
-!move(_) <-
	-target(_,_);
	+scheduled_random_turn.

// If we are not stucked, we just have to keep moving
+!move(Dir)  <-
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
// If yes, let's eat it and forget about it as a target! 
+time(_): food(Food) & .min(Food,[0,V,X,Y]) <-
	-target(_,_);
	!eat_at_my_pos.
	
// If we get at the target cell, but there is no food there anymore (because if 
// there would be a food, the plan above would have been executed) let's sadly  
// forget about our target.
+time(_): target(X,Y) & mypos(X,Y) & mydir(Dir) <-
	-target(_,_);
	!move(Dir).
	
+time(_): target_low(X,Y) & mypos(X,Y) & mydir(Dir) & not target(Fx, Fx) <-
	-target_low(_,_);
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
	
//Lower priority target
+time(_): target_low(Fx,Fy) & mypos(Mx,My) & Fx>Mx <-
	!move(1).
	
+time(_): target_low(Fx,Fy) & mypos(Mx,My) & Fx<Mx <-
	!move(3).
	
+time(_): target_low(Fx,Fy) & mypos(Mx,My) & Fy>My <-
	!move(2).	
    
+time(_): target_low(Fx,Fy) & mypos(Mx,My) & Fy<My <-
	!move(0).
	
//Turn when reaching these special points of the loop
+time(_): mypos(X,Y) & mydir(D) & X = 10 & Y = 10 & D = 3 <-
	turn(2).
	
+time(_): mypos(X,Y) & mydir(D) & X = 10 & Y = 49 & D = 2 <-
	turn(1).
	
+time(_): mypos(X,Y) & mydir(D) & X = 30 & Y = 49 & D = 1 <-
	turn(0).
	
+time(_): mypos(X,Y) & mydir(D) & X = 30 & Y = 10 & D = 0 <-
	turn(1).
	
+time(_): mypos(X,Y) & mydir(D) & X = 49 & Y = 10 & D = 1 <-
	turn(2).
	
+time(_): mypos(X,Y) & mydir(D) & X = 49 & Y = 49 & D = 2 <-
	turn(3).

+time(_): mypos(X,Y) & mydir(D) & X = 10 & Y = 49 & D = 3 <-
	turn(0).
	
+time(_): mypos(X,Y) & mydir(D) & X = 10 & Y = 30 & D = 0 <-
	turn(1).

+time(_): mypos(X,Y) & mydir(D) & X = 49 & Y = 30 & D = 1 <-
	turn(0).
	
+time(_): mypos(X,Y) & mydir(D) & X = 49 & Y = 10 & D = 0 <-
	turn(3).

// If we see some food and we had no target before, we select the closest 
// food and move towards it. (Note: if we would have a target, one of the 
// plans above would fit it, thus here we are sure that we had no plans earlier.
+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) & mypos(X, Y) <-
	+target(Fx,Fy);
	+target_low(X,Y);
	/*if (D = 0) {
		if (Fy < 10) {
			+target_low(X, 10);
		} else {
			+target_low(X, Fy);
		}
	}
	if (D = 2) {
		if (Fy > 49) {
			+target_low(X, 49);
		} else {
			+target_low(X, Fy);
		}
	}
	if (D = 1) {
		if (Fx > 49) {
			+target_low(49, Y);
		} else {
			if (Fx > 30){
				+target_low(30, Y);
			} else {
				+target_low(Fx, Y);
			}
		}
	}
	if (D = 3) {
		if (Fx < 10) {
			+target_low(10, Y);
		} else {
			if (Fx < 30){
				+target_low(30, Y);
			} else {
				+target_low(Fx, Y);
			}
		}
	}*/
	!move(D).
	
	
	
////////////////////////////////////////////////////////////////////////////////
//  Time triggered random roaming of the agent
////////////////////////////////////////////////////////////////////////////////		
	
// The rules bellow are executed only, if the agent has no target and it can't 
// see any food right now.

// Otherwise we should just move forward...
+time(_): mypos(X,Y) & mydir(0) & Y>0<-
	!move(0).
	
+time(_): mypos(X,Y) & mydir(1) & X<59 <-
	!move(1).
	
+time(_): mypos(X,Y) & mydir(2) & Y<59 <-
	!move(2).
	
+time(_): mypos(X,Y) & mydir(3) & X>0 <-
	!move(3).
	
// ... and turn back when reaching the wall
+time(_): mydir(D) & D < 2 <-
	turn(D+2).

+time(_): mydir(D) & D >= 2 <-
	turn(D-2).
