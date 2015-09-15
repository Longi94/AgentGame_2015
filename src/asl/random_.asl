/*******************************************************************************
	
	AgentGame v1.0.
	Copyright Peter Eredics (BUTE-DMIS) 2010-2011.
	
	random_.asl - Randomly crawling agent.
	
*******************************************************************************/

+time(_): food(Food) & .min(Food,[0,V,X,Y]) <-
	eat.


+time(_): .random(R) &  mypos(X,Y) & (R>0.95 |  lastpos(X,Y))<-
	-+lastpos(-1,-1);
	!randomTurn.



+time(_): mypos(X,Y) & mydir(Direction)<-
	-+lastpos(X,Y);
	step(Direction).


	
+!randomTurn <-
	.random(R);
	turn(R*4).
