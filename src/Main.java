import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        // set up the runtime and create the main container
        ContainerController container = initRT();
        try {
            container.acceptNewAgent("startSim", new Simulator(container)).start();
        } catch (ControllerException e) {
            e.printStackTrace();
        }
    }

    private static ContainerController initRT() {
        ProfileImpl profile = new BootProfileImpl();
        profile.setParameter(ProfileImpl.CONTAINER_NAME, "mainContainer");
        profile.setParameter(ProfileImpl.MAIN_HOST, "localhost");
        Runtime rt = Runtime.instance();
        return rt.createMainContainer(profile);
    }
}