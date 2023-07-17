import jade.wrapper.ControllerException;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.Viewer;

public class DynamicGraphHandler {
    private final Graph graph;

    public DynamicGraphHandler() {
        System.setProperty("org.graphstream.ui", "swing");
        graph = new SingleGraph("DynamicGraph");
        graph.setAttribute("ui.stylesheet", "node.blue { fill-color: blue; size: 20px;} node.red { fill-color: red; size: 20px;} node.grey { fill-color: grey; size: 10px;}");
        graph.setAttribute("ui.title", "Visualize Graph");

    }

    public Graph getGraph() {
        return graph;
    }
    public void addNode(String name, double initialValue) {
        Node node = graph.addNode(name);
        setNodeAttr(node, name, initialValue);
    }
    public void updateNodeAttribute(String nodeName, double value) throws ControllerException {
        Node node = graph.getNode(nodeName);
        if (node != null)
            setNodeAttr(node, nodeName, value);
    }

    private void setNodeAttr(Node node, String name, double value) {
        try {
            node.setAttribute("value", value);
            node.setAttribute("ui.label", name + "  v:" + value);


            // Set the initial color based on the initial value
            if (value < Constants.VOTE_LOWER_BOUND) {
                node.setAttribute("ui.class", "blue");
            } else if (value <= Constants.VOTE_UPPER_BOUND) {
                node.setAttribute("ui.class", "grey");
            } else {
                node.setAttribute("ui.class", "red");
            }
        } catch (RuntimeException e) {
        }
    }

    public void displayGraph() {
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.EXIT);
    }
}