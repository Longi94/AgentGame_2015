////////////////////////////////////////////////////////////////////////////////
//  INIT 
////////////////////////////////////////////////////////////////////////////////

init.
step_count(0).

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
	-target(_,_);
	+target(Fx,Fy);
	-spin(_);
	.send(dragon_1, untell, spin(4));
	!move(D).

+time(_): spin(0) & original_dir(Do) & mydir(D) & Do \== D & not target(_,_) <-
	-original_dir(_);
	-spin(_);
	.send(dragon_1, untell, spin(4));
	turn(Do).
	
//The last spin
+time(_) : spin(0) & mydir(D) & not target(_,_) <-
	-original_dir(_);
	-spin(_);
	.send(dragon_1, untell, spin(4));
	!move(D).

//We are currently spinning.
+time(_) : spin(N) & mydir(D) & not target(_,_) <-
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
	
+time(_): init & mypos(59,0) & mydir(D) <-
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
	if (D = 0) {
		!move(0);
	} else {
		turn(0);
	}.

// If we are unable to move (our last position is the same as our position now),
// forget the target and schedule a random turn to avoid geting stucked together 
// with an agent moving from the opposite direction
-!move(Dir): mydir(D) & .random(R) <-
	-target(_,_);
	+avoid(Dir).
	

// If we are not stucked, we just have to keep moving
+!move(Dir): step_count(N)  <-
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
+time(_): food(Food) & .min(Food,[0,V,X,Y]) & mydir(D) & original_dir(_)<-
	!eat_at_my_pos.
	
+time(_): food(Food) & .min(Food,[0,V,X,Y]) & mydir(D) <-
	+original_dir(D);
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
+time(_): target(X,Y) & mypos(X,Y) & mydir(D) & teammates(Mates) <-
	-target(_,_);
	+spin(3);
	.send(dragon_1, tell, spin(4));
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
		if (R > 0.5) {
			!move(1)
		} else {
			!move(3);
		}
	} else {
		if (R > 0.5) {
			!move(0)
		} else {
			!move(2);
		}
	}.
	

	
// We see another agent on the food
+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & agent(Agent) & .min(Agent,[_,_,_,_,Fx,Fy,_]) & mydir(D) <-
	-target(_,_);
	!move(D).

+time(_): mypos(X,Y) & Y > 49 & food(Food) & .min(Food,[_,_,Fx,Fy]) & agent(Agent) & .min(Agent,[_,_,_,_,Fx,Fy,_])<-
	-target(_,_);
	!move(0).
	
+time(_): mypos(X,Y) & X < 10 & food(Food) & .min(Food,[_,_,Fx,Fy]) & agent(Agent) & .min(Agent,[_,_,_,_,Fx,Fy,_]) <-
	-target(_,_);
	!move(1).
	
+time(_): mypos(X,Y) & Y < 40 & food(Food) & .min(Food,[_,_,Fx,Fy]) & agent(Agent) & .min(Agent,[_,_,_,_,Fx,Fy,_]) <-
	-target(_,_);
	!move(2).
	
+time(_): mypos(X,Y) & X > 49 & food(Food) & .min(Food,[_,_,Fx,Fy]) & agent(Agent) & .min(Agent,[_,_,_,_,Fx,Fy,_]) <-
	-target(_,_);
	!move(3).

// If we have a target different from our current position, let's move towards 
// it. (Note: if our position would be the target position, one of the plans  
// above would have been selected earlier.)
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx>Mx & (mydir(0) | mydir(2)) <-
	!move(1).
	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx<Mx & (mydir(0) | mydir(2)) <-
	!move(3).
	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy>My & (mydir(0) | mydir(2)) <-
	!move(2).	
    
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy<My & (mydir(0) | mydir(2)) <-
	!move(0).

	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy>My <-
	!move(2).	
    
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fy<My <-
	!move(0).

+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx>Mx <-
	!move(1).
	
+time(_): target(Fx,Fy) & mypos(Mx,My) & Fx<Mx <-
	!move(3).
	
// If we see some food and we had no target before, we select the closest 
// food and move towards it. (Note: if we would have a target, one of the 
// plans above would fit it, thus here we are sure that we had no plans earlier.

+time(_): food(Food) & .min(Food,[_,_,Fx,Fy]) & mydir(D) & mypos(X, Y) <-
	-target(_,_);
	+target(Fx,Fy);
	!move(D).

	
//Turn when reaching these special points of the loop
+time(_): mypos(10,38) & mydir(3) <-
	turn(2).
	
+time(_): mypos(10,49) & mydir(2) <-
	turn(1).
	
+time(_): mypos(49,49) & mydir(1) <-
	turn(0).
	
+time(_): mypos(49,38) & mydir(0) <-
	turn(3).

	
//Continue on the path of the loop
+time(_): mypos(10,Y) & not mydir(2) & Y < 49 <-
	turn(2).

+time(_): mypos(49,Y) & not mydir(0) & Y > 38 <-
	turn(0).

+time(_): mypos(X,38) & not mydir(3) & X > 10 <-
	turn(3).

+time(_): mypos(X,49) & not mydir(1) & X < 49 <-
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
	
+time(_): mypos(X,Y) & Y < 38 <-
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

