package InputControl;

import Networking.Client;
import Packet.InputPacket;
import Player.Player;
import Render.PlayerView;
import javafx.scene.Scene;

public class UpdateInputController extends InputController{

    private boolean lastUp, lastDown, lastLeft, lastRight;

    public UpdateInputController(Scene scene){
        super(scene);
    }

    public void handleAction(Player player)
    {

    }

    public void applyMovement(Player player, Client clientNetworking)
    {
        //reset
        player.setVx(0);
        player.setVy(0);

        double currentSpeed = 0;

        if (isMovingUp())    player.setVy(-currentSpeed);
        if (isMovingDown())  player.setVy(currentSpeed);
        if (isMovingLeft())  player.setVx(-currentSpeed);
        if (isMovingRight()) player.setVx(currentSpeed);

        boolean curUp = isMovingUp();
        boolean curDown = isMovingDown();
        boolean curLeft = isMovingLeft();
        boolean curRight = isMovingRight();

        if(curUp != lastUp || curDown != lastDown || curLeft != lastLeft || lastRight != curRight) {
            InputPacket ip = new InputPacket();

            ip.up = isMovingUp();
            ip.down = isMovingDown();
            ip.left = isMovingRight();
            ip.right =  isMovingLeft();

            clientNetworking.send(ip);
        }

        lastUp = curUp;
        lastDown = curDown;
        lastLeft = curLeft;
        lastRight = curRight;



    }
}
