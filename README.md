# M&AElig;DEN

## Synopsis

Maeden is a simulated grid-world environment
designed to support multi-agent problem-solving.
Agent controllers may be written in any language 
that supports basic sockets.

Details and documentation may be 
[found here](http://www.westmont.edu/~iba/maeden/).

## To Do

* create an abstract agent-controller class that KeyboardController extends
and that other classes could extend as well
* when simulation ends 
(in the case where eating the food is the ultimate goal),
send disconnect signals to any other agents currently connected
* in server loop, keep track of connected agents' energy 
even if they are deliberating; 
kill them if they run out of energy
* adjust simulation time units to correspond to 'wait cost'
and then deduct wait-cost every time-slice when agents do not act
* action cost incurred should reflect cost of actual action selected by agent
plus wait costs since the last action (or connection if first action);
this item may be obviated by previous item
* fix the foundBase and killGrid interaction that is happening in Grid.java
* create associative array to store action costs, indexed by action character
* resolve conflict when two agents want to move into the same square
* track down problem with stochastic failure giving incorrect status; 
problem starts in `processAgentActions()`
where `STOCHASTICISM`/`STOCHASTIC_RATE` is sometimes exercised
* create wrapper server that will fork a simulation in specified world 
(do we really want this?)
* upgrade documentation in Java sources to support javadoc
* modify assignment of agent IDs to re-use IDs that have been released
* remove restriction of single-digit agent IDs (i.e., limit of 10 agents)
* add shield object or armor or chain mail, etc.
