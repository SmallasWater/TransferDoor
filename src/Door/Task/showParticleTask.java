package Door.Task;


import Door.load;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.Task;
import java.util.LinkedList;
import java.util.Map;

public class showParticleTask extends Task{

    @Override
    public void onRun(int i) {
        LinkedList<String> linkedList = load.getNameAll();
        if(linkedList != null){
            for (String name: linkedList){
                Position position = load.getApi().getDoorPos(name);
                if(position != null){
                    boolean bool = load.getApi().getConfig(name).getBoolean(load.ConfigType.Dir.getName());
                    LinkedList<double[]> pos = load.getApi().getDoor(name,position,bool);
                    Map mp = (Map) load.getApi().getConfig(name).get(load.ConfigType.Color.getName());
                    int[] rgb = new int[]{(int)mp.get("R"),(int)mp.get("G"),(int)mp.get("B")};
                    for (double[] array:pos){
                        DustParticle genericParticle = new DustParticle(new Vector3(array[0],array[1],array[2]),rgb[0],rgb[1],rgb[2]);
                        Level level = position.level;
                        level.addParticle(genericParticle);
                    }
                }
            }
        }
    }
}
