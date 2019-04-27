package Door.Task;


import Door.events.PlayerNearDoorEvent;
import Door.load;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.scheduler.Task;

import java.util.LinkedList;

public class playerMoveTask extends Task{
    @Override
    public void onRun(int i) {
        for(Player player: Server.getInstance().getOnlinePlayers().values()){
           LinkedList<String> names = load.getNameAll();
            if(names != null){
                for(String name:names){
                    Position position = load.getApi().getDoorPos(name);
                    Position playerPos = player.getPosition();
                    double r = load.getApi().getConfig(name).getDouble(load.ConfigType.size.getName());
                    if(playerPos.level.getFolderName().equals(position.level.getFolderName())){
                        if(load.getApi().getConfig(name).getBoolean(load.ConfigType.Dir.getName())){// true x false z
                            if((position.x - r <= playerPos.x  && playerPos.x <= position.x + r)
                                    && (position.y - r <= playerPos.y + 1 && position.y + r >= playerPos.y + 1 )
                                    && (position.z - 0.5 <= playerPos.z && playerPos.z <= position.z + 0.5)){
                                PlayerNearDoorEvent event = new PlayerNearDoorEvent(player,player.getPosition(),name);
                                Server.getInstance().getPluginManager().callEvent(event);
                            }
                        }else{
                            if((position.x - 0.5 <= playerPos.x  && playerPos.x <= position.x + 0.5 )
                                    && (position.y - r <= playerPos.y + 1 && position.y + r >= playerPos.y + 1 )
                                    && (position.z - r <= playerPos.z && playerPos.z <= position.z + r)){
                                PlayerNearDoorEvent event = new PlayerNearDoorEvent(player,player.getPosition(),name);
                                Server.getInstance().getPluginManager().callEvent(event);
                            }
                        }
                    }
                }
            }
        }
    }
}
