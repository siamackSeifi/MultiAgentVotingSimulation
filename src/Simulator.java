import jade.core.Agent;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import java.util.*;

public class Simulator extends Agent {

    private final ContainerController container;
    private DynamicGraphHandler graphHandler;

    private Map<String, Integer> electionResult = new HashMap<>();


    public Simulator(ContainerController mainContainer) {
        container = mainContainer;
    }
    // setup and start the simulation
    public void setup() {
        Map<String, Citizen> citizens = new HashMap<>();
        electionResult.put("initialBlue", 0);
        electionResult.put("initialRed", 0);
        electionResult.put("initialWhite", 0);
        electionResult.put("finalBlue", 0);
        electionResult.put("finalRed", 0);
        electionResult.put("finalWhite", 0);

        graphHandler = new DynamicGraphHandler();
        // Create citizen agents in the main container
        try {
            Citizen tmp;
            for (int i = 0; i < Constants.POPULATION; i++) {
                // create the agent
                tmp = new Citizen(this);
                citizens.put("Citizen" + (i+1), tmp);
                // create corresponding node to the agent in graph
                graphHandler.addNode("Citizen" + (i+1), tmp.getVoteResult());

                if (tmp.getVoteResult() < Constants.VOTE_LOWER_BOUND) {
                    electionResult.put("initialBlue", electionResult.get("initialBlue") + 1);
                } else if (tmp.getVoteResult() <= Constants.VOTE_UPPER_BOUND) {
                    electionResult.put("initialWhite", electionResult.get("initialWhite") + 1);
                } else {
                    electionResult.put("initialRed", electionResult.get("initialRed") + 1);
                }
            }
            // add random neighbors to each agent
            Graph graph = graphHandler.getGraph();
            // Displaying the graph
            graphHandler.displayGraph();
            for (Node node : graph) {
                List<Node> otherNodes = new ArrayList<>();
                for (Node otherNode : graph) {
                    if (!otherNode.equals(node)) {
                        otherNodes.add(otherNode);
                    }
                }
//             Randomly select 4 other nodes to connect with
                int index;
                for (int i = 0; i < Constants.N_CONNECTIONS; i++) {
                    index = (new Random()).nextInt(otherNodes.size());
                    if (graph.getEdge(node.getId() + "-" + otherNodes.get(index).getId()) == null &&
                            graph.getEdge(otherNodes.get(index).getId() + "-" + node.getId()) == null) {
                        graph.addEdge(node.getId() + "-" + otherNodes.get(index).getId(), node, otherNodes.get(index));
                        // add neighbor to the agent
                        citizens.get(node.getId()).addNeighbor(citizens.get(otherNodes.get(index).getId()));
                        citizens.get(otherNodes.get(index).getId()).addNeighbor(citizens.get(node.getId()));
                    }
                }
                // start agents
                container.acceptNewAgent(node.getId(), citizens.get(node.getId())).start();
            }
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }
    public void updateSimulation(Citizen citizen) {
        try {
            graphHandler.updateNodeAttribute(citizen.getName().split("@")[0], citizen.getVoteResult());
        } catch (ControllerException e) {
        }
    }
    public void checkTermination(Citizen citizen) {
        if (citizen.getVoteResult() < Constants.VOTE_LOWER_BOUND) {
            electionResult.put("finalBlue", electionResult.get("finalBlue") + 1);
        } else if (citizen.getVoteResult() <= Constants.VOTE_UPPER_BOUND) {
            electionResult.put("finalWhite", electionResult.get("finalWhite") + 1);
        } else {
            electionResult.put("finalRed", electionResult.get("finalRed") + 1);
        }
        if (getAgentList() <= 5) { // there are three primitive agents + this + the last agent to be
            double initialBlue = (electionResult.get("initialBlue")*100.0)/
                    ((electionResult.get("initialBlue")+ electionResult.get("initialRed") == 0)
                            ? 1
                            : electionResult.get("initialBlue")+ electionResult.get("initialRed"));
            double initialRed = (electionResult.get("initialRed")*100.0)/
                    ((electionResult.get("initialBlue")+ electionResult.get("initialRed") == 0)
                            ? 1
                            : electionResult.get("initialBlue")+ electionResult.get("initialRed"));
            double initialWhite = (electionResult.get("initialWhite")*100.0)/Constants.POPULATION;

            System.out.println("simulation finished");
            System.out.println("At first, the election results was the following:");
            System.out.println("Blue: " + electionResult.get("initialBlue") + " votes (" + initialBlue + " %)");
            System.out.println("Red: " + electionResult.get("initialRed") + " votes (" + initialRed + " %)");
            System.out.println("The number of people who don't plan on voting: " +
                    electionResult.get("initialWhite") + " (" + initialWhite + "%)\n");

            double finalBlue = (electionResult.get("finalBlue")*100.0)/
                    ((electionResult.get("finalBlue")+ electionResult.get("finalRed") == 0)
                            ? 1
                            :electionResult.get("finalBlue")+ electionResult.get("finalRed"));
            double finalRed = (electionResult.get("finalRed")*100.0)/
                    ((electionResult.get("finalBlue")+ electionResult.get("finalRed") == 0)
                            ? 1
                            :electionResult.get("finalBlue")+ electionResult.get("finalRed"));
            double finalWhite = (electionResult.get("finalWhite")*100.0)/Constants.POPULATION;
            System.out.println("The election results are the following:");
            System.out.println("Blue: " + electionResult.get("finalBlue") + " votes (" + finalBlue + " %)\n");
            System.out.println("Red: " + electionResult.get("finalRed") + " votes (" + finalRed + " %)\n");
            System.out.println("The number of people who didn't participate the election: " +
                    electionResult.get("finalWhite") + " (" + finalWhite + "%)");
        }
    }

    private int getAgentList() {
        AMSAgentDescription[] agents;
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults((long) -1);

        try {
            agents = AMSService.search(this, new AMSAgentDescription(), sc);
            return agents.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -10;
        }
    }
}
