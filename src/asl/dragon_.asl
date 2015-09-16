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
//  Spinning
////////////////////////////////////////////////////////////////////////////////

//We are currently spinning, but we see food. Stop spinning and target the food.
+time(_) : spin(_) & food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) <-
	+target(Fx,Fy);
	-spin(_);
	!move(D).

//The last spin
+time(_) : spin(0) & mydir(D) <-
	-spin(_);
	!move(D).

//We are currently spinning.
+time(_) : spin(N) & mydir(D) <-
	-spin(_);
	+spin(N - 1);
	if(D == 3) {
		turn(0);
	} else {
		turn(D+1);
	}.

////////////////////////////////////////////////////////////////////////////////
//  Moving
////////////////////////////////////////////////////////////////////////////////

//Go to the nearest starting point of the loop
+time(_): init & mypos(0, 0) & mydir(D) <-
	-init;
	if (D = 2) {
		!move(2);
	} else {
		turn(2);
	}.
	
+time(_): init & mypos(49,0) & mydir(D) <-
	-init;
	if (D = 2) {
		!move(2);
	} else {
		turn(2);
	}.
	
+time(_): init & mypos(0,59) & mydir(D) <-
	-init;
	if (D = 1) {
		!move(1);
	} else {
		turn(1);
	}.
	
+time(_): init & mypos(59,59) & mydir(D) <-
	-init;
	if (D = 3) {
		!move(3);
	} else {
		turn(3);
	}.

// If we are unable to move (our last position is the same as our position now),
// forget the target and schedule a random turn to avoid geting stucked together 
// with an agent moving from the opposite direction
-!move(Dir): mydir(D) & .random(R) <-
	-target(_,_);
	+avoid(Dir).
	

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
	!eat_at_my_pos.
	
// If we get at the target cell, but there is no food there anymore (because if 
// there would be a food, the plan above would have been executed) let's sadly  
// forget about our target. But we see food ahead, then target it instead of
// spinning.
+time(_): target(X,Y) & mypos(X,Y) & mydir(D) & food(Food) & .min(Food,[_,_,Fx,Fy]) <-
	-target(_,_);
	+target(Fx,Fy);
	!move(D).
	
// If we get at the target cell, but there is no food there anymore (because if 
// there would be a food, the plan above would have been executed) let's sadly  
// forget about our target. Start spinning to see if there is any food around
// us.
+time(_): target(X,Y) & mypos(X,Y) & mydir(D) <-
	-target(_,_);
	+spin(3);
	if(D == 3) {
		turn(0);
	} else {
		turn(D+1);
	}.

// Agent thinks it needs to avoid a obstacle. Strafe to avoid the obstacle and
// avoid being stuck forever.
+time(_): avoid(D) & .random(R) <-
    -avoid(_);
	if (D = 0 | D = 2) {
		!move(1);
	} else {
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

+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) & mypos(X, Y) <-
	+target(Fx,Fy);
	!move(D).
	
//Turn when reaching these special points of the loop
+time(_): mypos(10,10) & mydir(3) <-
	turn(2).
	
+time(_): mypos(10,49) & mydir(2) <-
	turn(1).
	
+time(_): mypos(30,49) & mydir(1) <-
	turn(0).
	
+time(_): mypos(30,10) & mydir(0) <-
	turn(1).
	
+time(_): mypos(49,10) & mydir(1) <-
	turn(2).
	
+time(_): mypos(49,49) & mydir(2) <-
	turn(3).

+time(_): mypos(10,49) & mydir(3) <-
	turn(0).
	
+time(_): mypos(10,30) & mydir(0) <-
	turn(1).

+time(_): mypos(49,30) & mydir(1) <-
	turn(0).
	
+time(_): mypos(49,10) & mydir(0) <-
	turn(3).
	
//Continue on the path of the loop
+time(_): mypos(10,Y) & Y < 30 & not mydir(2) & not mydir(0)<-
	turn(2).

+time(_): mypos(10,_) & mydir(3)<-
	turn(0).
	
+time(_): mypos(49,Y) & Y > 20 & Y < 40 & not mydir(0) & not mydir(2)<-
	turn(0).

+time(_): mypos(49,_) & mydir(1)<-
	turn(2).
	
+time(_): mypos(X,10) & X > 20 & X < 40 & not mydir(1) & not mydir(3)<-
	turn(1).

+time(_): mypos(_,10) & mydir(0)<-
	turn(3).
	
+time(_): mypos(X,49) & X > 30 & not mydir(3) & not mydir(1)<-
	turn(3).

+time(_): mypos(_,49) & mydir(2)<-
	turn(1).

////////////////////////////////////////////////////////////////////////////////
//  Time triggered random roaming of the agent
////////////////////////////////////////////////////////////////////////////////		
	
// The rules bellow are executed only, if the agent has no target and it can't 
// see any food right now.

//Move back to the inner circle
+time(_): mypos(X,Y) & Y > 49<-
	!move(0).
	
+time(_): mypos(X,Y) & X < 10 <-
	!move(1).
	
+time(_): mypos(X,Y) & Y < 10 <-
	!move(2).
	
+time(_): mypos(X,Y) & X > 49 <-
	!move(3).

// Otherwise we should just move forward...
+time(_): mypos(X,Y) & mydir(0) & Y > 0<-
	!move(0).
	
+time(_): mypos(X,Y) & mydir(1) & X < 59 <-
	!move(1).
	
+time(_): mypos(X,Y) & mydir(2) & Y < 59 <-
	!move(2).
	
+time(_): mypos(X,Y) & mydir(3) & X > 0 <-
	!move(3).

