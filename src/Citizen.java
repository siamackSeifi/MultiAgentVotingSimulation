import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.DecimalFormat;
import java.util.*;

public class Citizen extends Agent {
    private int nRounds = Constants.N_ROUNDS;
    private final Simulator sim;
    private final double immutability, charisma, initialVoteResult;
    private double currentVoteResult;
    private double additionalValues = 0;
    // list of neighbors
    private final List<Citizen> neighbors = new ArrayList<>();

    public Citizen(Simulator simulator) {
        sim = simulator;
        // We assume these won't change throughout the simulation!
        final double currentSatisfaction = twoDecimalPoints(Math.random() * (1.0 - 0.0) + 0.0);
        final double jobWeight = twoDecimalPoints(Math.random());
        final double taxLoweringWeight = twoDecimalPoints(Math.random() * (1 - jobWeight));
        final double constructionWeight = twoDecimalPoints(1 - (taxLoweringWeight + jobWeight));

        immutability = twoDecimalPoints(Math.random());
        charisma = twoDecimalPoints(Math.random());

        // calculate initial voteResult
        initialVoteResult = twoDecimalPoints((currentSatisfaction +
                (jobWeight * (Constants.MAYOR_JOB_WEIGHT - Constants.CANDIDATE_JOB_WEIGHT)) +
                (taxLoweringWeight * (Constants.MAYOR_TAXLOWERING_WEIGHT - Constants.CANDIDATE_TAXLOWERING_WEIGHT)) +
                (constructionWeight * (Constants.MAYOR_CONSTRUCTION_WEIGHT - Constants.CANDIDATE_CONSTRUCTION_WEIGHT))) /
                (jobWeight + taxLoweringWeight + constructionWeight));

        currentVoteResult = twoDecimalPoints(initialVoteResult);
    }

    protected void setup() {
        addBehaviour(new DailyCommunication());
    }

    protected void takeDown() {
        sim.checkTermination(this);
    }

    public void addNeighbor(Citizen neighbor) {
        neighbors.add(neighbor);
    }

    public double getVoteResult() {
        return currentVoteResult;
    }

    private class DailyCommunication extends CyclicBehaviour {
        public void action() {
            if (nRounds <= 0) {
                Citizen.this.doDelete();
                block(500);
                return;
            }
            // Send message to n other agents
            sendMessages();
            // Read messages received in this round
            readMessages();

            currentVoteResult = (initialVoteResult + (additionalValues / (Constants.N_ROUNDS - nRounds + 1)));
            currentVoteResult = twoDecimalPoints(currentVoteResult);

            System.out.println(getLocalName() + "'s voteResult is: " + currentVoteResult);
            sim.updateSimulation(Citizen.this);

            nRounds--;
            block(3000);
        }

        private void sendMessages() {
            Citizen person;
            for (int i = 0; i < 1; i++) {
                person = neighbors.get((new Random()).nextInt(neighbors.size()));
                if (person != null) {
                    Map<String, Double> data = new HashMap<>();
                    data.put("charisma", charisma);
                    data.put("voteResult", currentVoteResult);
                    ACLMessage message = new ACLMessage(ACLMessage.INFORM);
                    message.addReceiver(person.getAID());
                    message.setContent(mapToJson(data));
                    send(message);
                }
            }
        }

        private void readMessages() {
            // Receive and process incoming messages
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            while (true) {
                ACLMessage message = receive(template);
                if (message != null) {
                    String content = message.getContent();
                    Map<String, Double> data = jsonToMap(content);
                    double otherCharisma = data.get("charisma");
                    double otherVoteResult = data.get("voteResult");

                    double changeAmount = (1 - immutability) * (otherCharisma * Math.abs(otherVoteResult - currentVoteResult)) / 2;

                    if (!(otherVoteResult >= Constants.VOTE_LOWER_BOUND && otherVoteResult <= Constants.VOTE_UPPER_BOUND)) { // other is not indifferent
                        if (currentVoteResult < Constants.VOTE_LOWER_BOUND && otherVoteResult < Constants.VOTE_LOWER_BOUND) // both blue
                            additionalValues -= changeAmount;
                        else if (currentVoteResult > Constants.VOTE_UPPER_BOUND && otherVoteResult > Constants.VOTE_UPPER_BOUND) // both red
                            additionalValues += changeAmount;
                        else if (otherVoteResult > currentVoteResult) // other is red, I am blue
                            additionalValues += changeAmount;
                        else if (otherVoteResult < currentVoteResult)
                            additionalValues -= changeAmount;
                        else {
                            System.out.println("\n\n\nsomething strange happened here!");
                        }

                    } // else ignore

                } else {
                    break; // No more messages in the queue
                }
            }
        }

        private String mapToJson(Map<String, Double> data) {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                String key = entry.getKey();
                double value = entry.getValue();
                jsonBuilder.append("\"").append(key).append("\":").append(value).append(",");
            }
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1); // Remove the trailing comma
            jsonBuilder.append("}");
            return jsonBuilder.toString();
        }

        private Map<String, Double> jsonToMap(String json) {
            Map<String, Double> data = new HashMap<>();
            json = json.trim();
            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1);
                String[] keyValuePairs = json.split(",");
                for (String pair : keyValuePairs) {
                    String[] keyValue = pair.split(":");
                    if (keyValue.length == 2) {
                        String key = keyValue[0].trim().replace("\"", "");
                        double value = Double.parseDouble(keyValue[1].trim());
                        data.put(key, value);
                    }
                }
            }
            return data;
        }
    }

    public static Double twoDecimalPoints(Double d) {
        return Math.min(1, Math.max(0, Double.parseDouble((new DecimalFormat("0.00")).format(d))));
    }
}
