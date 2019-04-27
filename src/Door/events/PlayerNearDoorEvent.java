package Door.events;


import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import cn.nukkit.level.Position;

public class PlayerNearDoorEvent extends PlayerEvent{

    private static final HandlerList handlers = new HandlerList();
    private Position position;
    private String name;
    public PlayerNearDoorEvent(Player player, Position position,String name){
        this.player = player;
        this.position = position;
        this.name = name;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }
}
