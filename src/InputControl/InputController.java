package InputControl;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputController {

    private Set<KeyCode> keys = new HashSet<>();

    InputController(Scene scene)
    {
        scene.setOnKeyPressed(e -> keys.add(e.getCode()));
        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));
    }

    public boolean isMovingUp(){return keys.contains(KeyCode.W);}
    public boolean isMovingDown(){return keys.contains(KeyCode.S);}
    public boolean isMovingLeft(){return keys.contains(KeyCode.A);}
    public boolean isMovingRight(){return keys.contains(KeyCode.D);}

    public void clear(){
        keys.clear();
    }

}
