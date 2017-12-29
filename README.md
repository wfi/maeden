# M&AElig;DEN

## Synopsis

M&aelig;den is a simulated grid-world environment
designed to support multi-agent problem-solving.
Agent controllers may be written in any language 
that supports basic sockets.

Details and documentation may be 
[found here](http://www.westmont.edu/~iba/maeden/).

## To Do

1. [in-process] when simulation ends 
(in the case where eating the food is the ultimate goal),
send disconnect signals to any other agents currently connected
1. track down problem with stochastic failure giving incorrect status; 
problem starts in `processAgentActions()`
where `STOCHASTICISM`/`STOCHASTIC_RATE` is sometimes exercised
1. [in-process] upgrade documentation in Java sources to support javadoc
1. add shield object or armor or chain mail, etc.
1. create wrapper server that will fork a simulation in specified world 
(do we really want this?)
1. modify assignment of agent IDs to re-use IDs that have been released
1. ~~eliminate base/helper distinction~~
1. ~~for sending visual info from the server to agent-controllers,
use something such as JSON or other that is more readily parsed by all languages~~
1. ~~resolve conflict when two agents want to move into the same square~~
1. ~~replace iterator with for-each loops~~
1. ~~add command-line argument to control EAT_FOOD_ENDS_IT~~
1. ~~fix the foundBase and killGrid interaction that is happening in Grid.java~~
1. ~~create an abstract agent-controller class that KeyboardController extends
and that other classes could extend as well~~
1. ~~refactor into simulator proper and agent controller folders~~
1. ~~put everything into a Java package; the challenge, however, is for controllers and
the simulator proper to live and play nicely together with a minimum of hassle~~
1. ~~create map to store action costs, indexed by action character as key and associating energy cost as value~~
1. ~~in server's processAgentActions() loop, if no action sent by an agent, let nextCommand be "wait" (or whatever)~~
1. ~~adjust simulation time units to correspond to 'wait cost'
and then deduct wait-cost every time-slice when agents do not act~~
1. ~~remove restriction of single-digit agent IDs (i.e., limit of 10 agents)~~
1. ~~make To Do list numbered instead of bullets~~
