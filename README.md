**Election Simulation**

# 1. Introduction

Using multi-agent systems in computer simulations has been really effective in studying how dynamic systems behave. Therefore, leveraging its power to comprehend one of the most intricate systems in existence—human behavior in a society—becomes a logical step forward. To this end, I have implemented a model which simulates a city election where there are two candidates: one wants to be re-elected, and the other wants to replace the current mayor. The purpose of creating this model is to check how much the voter's preference is affected throughout the days leading to election day.

# 2. Basic Definitions

## 2.1. Citizens

The main components of our system, which represent the community of a city, are Citizens. They are the agents that represent a Citizen. These Citizens possess specific attributes that define their characteristics and preferences, including:

- currentSatisfaction: which indicates their level of satisfaction with the current mayor. Range = [0, 1]
- jobWeight: the level of importance they put on plans the candidates have for creating jobs. Range = [0, 1]
- taxLoweringWeight: the level of importance they put on plans the candidates have for lowering the taxes. Range = [0, 1]
- constructionWeight: the level of importance they put on plans the candidates have for urban development. Range = [0, 1]
- charisma: How much they can influence other people to change their opinion. Range = [0, 1]
- immutability: How much susceptible the citizen is to other people's opinions. Range = [0, 1]. With 0 being completely susceptible to ideas and 1 being very hard to change this citizen's mind
- neighbors: The list of citizens who are acquaintances of the citizen.
- initialVoteResult: The initial opinion of a citizen on candidates. Range = [0, 1]. With 1 being completely in favor of the candidate who is the current mayor and 0 being completely in favor of his opponent.
- currentVoteResult: The updated voteResult of the citizen at the end of each round.

## 2.2. Candidates

There are two candidates in this simulation. One being the current mayor who is trying to get elected again and the other being his opponent. The candidates are represented by three constants in our simulation:

- {MAYOR/CANDIDATE}\_JOB\_WEIGHT: which represents the weight of job generation plans in their proposal
- {MAYOR/CANDIDATE}\_TAXLOWERING\_WEIGHT: which represents the weight of tax reduction plans in their proposal
- {MAYOR/CANDIDATE}\_CONSTRUCTION\_WEIGHT: which represents the weight of urban development plans in their proposal

For each of the candidates, the accumulation of these three indicators sums to 1.

## 2.3. Constants

- POPULATION: The number of citizens in our city
- N\_ROUNDS: The days left until the election day
- N\_CONNECTIONS: The number of neighbors each citizen has
- VOTE\_LOWER\_BOUND: The lower bound of the voteResult range in which a citizen decides not to vote
- VOTE\_UPPER\_BOUND: The upper bound of the voteResult range in which a citizen decides not to vote
- In the visualization part, citizens with voteResult lower than the VOTE\_LOWER\_BOUND will be color coded blue, and citizens with voteResult higher than the VOTE\_UPPER\_BOUND will be color coded red. Those in the range will be color coded White.

# 3. Mathematics

To establish a correlation between the attributes and the "voteResult," we must formulate two mathematical expressions as outlined below:

- Initial voteResult calculation:

``        initialVoteResult = (currentSatisfaction +
(jobWeight * (Constants.MAYOR_JOB_WEIGHT - Constants.CANDIDATE_JOB_WEIGHT)) +
(taxLoweringWeight * (Constants.MAYOR_TAXLOWERING_WEIGHT - Constants.CANDIDATE_TAXLOWERING_WEIGHT)) +
(constructionWeight * (Constants.MAYOR_CONSTRUCTION_WEIGHT - Constants.CANDIDATE_CONSTRUCTION_WEIGHT)))
/(jobWeight + taxLoweringWeight + constructionWeight);``

- Updated voteResult calculation:

For this calculation, we first determine the amount of change for the citizen in a round of communication with the following:

``                    double changeAmount =
(1 - immutability) * (otherCharisma * Math.abs(otherVoteResult - currentVoteResult)) / 2;``

And Based on the opinions the two communicating party have:

- If both are blue: changeAmount will be subtracted from the additionalValue
- If both are red: changeAmount will be added to the additionalValue
- Else, the voteResult of the citizen on the receiving end of the communication will get closer to the other citizen.

By calculating the additionalValue, we will divide it by the number of days to normalize it and add it to the initial voteResult in each round.

``            currentVoteResult = (initialVoteResult + (additionalValues / (Constants.N_ROUNDS - nRounds + 1)));
``

# 4. Implementation

## 4.1. Prerequisites

In order to run the project, the following libraries must be added to the project:

- JAVA Agent DEvelopment Framework
- GraphStream

Once the libraries are added, everything is set to run the project.

## 4.2. Classes

### 4.2.1. Main

The entry point of the project is Main class. The mainContainer is created and passed to the Simulator agent, which is in charge of the simulation process.

### 4.2.2. DynamicGraphHandler

This class is in charge of handling the visualization.

### 4.2.3. Simulator

In setup of Simulator agent, the citizen agents are created based on the population of the city, the list of neighbors for each citizen is determined and the visualization is being set up. This agent has two other functions:

updateSimulation: which is called by each citizen at the end of each round to update the visualization of that particular agent.

checkTermination: which is called by each citizen at the end of its lifecycle (takeDown) to check if the simulation has finished or not.

### 4.2.4. Citizen

At initialization point, initialVoteResult is calculated.

Citizens have a CyclicBehaviour in which they communicate by sending and reading messages with their neighbors, and their currentVoteResult is updated based on the communications (Section 4). This CyclicBehaviour ends when number of rounds reach zero.


As we can see in this particular simulation, the blue party lost some votes, possibly due to its unsatisfying proposed plans and the opponent's charismatic believers. But it was not enough for them to lose the election.

**References**

1. Wilensky, U. (1998). NetLogo Voting model. http://ccl.northwestern.edu/netlogo/models/Voting. Center for Connected Learning and Computer-Based Modeling, Northwestern University, Evanston, IL.

[1]https://jade.tilab.com/download/jade/license/jade-download/

[2] https://graphstream-project.org/download/