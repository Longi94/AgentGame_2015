+time(_): myenergy(Energy) & Energy<100 <-
	wait.
	
+time(_): time(T) & myid(ID) & mypos(X,Y) & mydir(D) & food(Food) 
			& proba.Move(T,ID,X,Y,D,Step,Turn,Eat,Food) & Step<4 <-
	step(Step).

+time(_): time(T) & myid(ID) & mypos(X,Y) & mydir(D) & food(Food) 
			& proba.Move(T,ID,X,Y,D,Step,Turn,Eat,Food) & Turn<4 <-
	turn(Turn).
	
+time(_): time(T) & myid(ID) & mypos(X,Y) & mydir(D) & food(Food) 
			& proba.Move(T,ID,X,Y,D,Step,Turn,Eat,Food) & Eat==1 <-
	eat.
