package Door;


import AwakenSystem.AwakenSystem;
import AwakenSystem.data.baseAPI;
import AwakenSystem.data.defaultAPI;
import Door.Task.playerMoveTask;
import Door.Task.showParticleTask;
import Door.events.PlayerNearDoorEvent;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.*;

public class load extends PluginBase implements Listener{

    private static load load;
    private static LinkedHashMap<Player,LinkedList<Object>> pos = new LinkedHashMap<>();
    private static File dataFolder;
    @Override
    public void onEnable() {
        load = this;
        dataFolder = this.getDataFolder();
        File config = new File(this.getDataFolder()+"/Door");
        if(!config.exists()){
            if(!config.mkdirs()){
                Server.getInstance().getLogger().warning("Door目录创建失败");
            }
        }
        this.getServer().getPluginManager().registerEvents(this,this);
        this.getServer().getScheduler().scheduleDelayedTask(this,()->
                this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
                    public void onRun() {
                        Server.getInstance().getScheduler().scheduleRepeatingTask(new playerMoveTask(), 2);
                        Server.getInstance().getScheduler().scheduleRepeatingTask(new showParticleTask(), 5);
                    }}),20);
    }

    private boolean isExistsConfig(String name){
        return getDoorFile(name).exists();
    }

    private File getDoorFile(String name){
        return new File(this.getDataFolder()+"/Door/"+name+".yml");
    }

    private Config getConfig(String name, boolean isExists){
        if(isExists){
            return isExistsConfig(name)?getConfig(name):null;
        }else{
            return new Config(this.getDataFolder()+"/Door/"+name+".yml",Config.YAML);
        }

    }

    public Config getConfig(String name){
        return getConfig(name,false);
    }

    public static load getApi() {
        return load;
    }

    private boolean playerTransfer(Player player, String name) {
        if (!isExistsConfig(name))
            return false;
        Plugin LevelAwakenSystem = Server.getInstance().getPluginManager().getPlugin("LevelAwakenSystem");
        Config config = getConfig(name);
        if (LevelAwakenSystem != null) {
            int level = defaultAPI.getPlayerAttributeInt(player.getName(), baseAPI.PlayerConfigType.LEVEL);
            String Att = defaultAPI.getPlayerAttributeString(player.getName(), baseAPI.PlayerConfigType.ATTRIBUTE);
            List finalAtt = config.getList(ConfigType.Att.getName());
            int PF = defaultAPI.getPlayerAttributeInt(player.getName(), baseAPI.PlayerConfigType.TALENT);
            if (level < config.getInt(ConfigType.level.getName())) {
                player.sendMessage("§c 抱歉，您的等级不足" + config.getInt(ConfigType.level.getName()) + "级 无法传送 当前等级:" + level);
                return false;
            }
            if (!finalAtt.contains(Att) && !finalAtt.isEmpty()) {
                player.sendMessage("§c 抱歉，您的属性不在" + finalAtt.toString() + "中 无法传送 当前属性:" + Att);
                return false;
            }
            if (PF < config.getInt(ConfigType.PF.getName())) {
                player.sendMessage("§c 抱歉，您的评级低于" + getChatMessageAll().get(config.getInt(ConfigType.PF.getName())) + " 无法传送 当前评分:" + defaultAPI.getChatBySetting(player.getName()));
                return false;
            }
        }
        if (Server.getInstance().getPluginManager().getPlugin("EconomyAPI") != null) {
            int money = config.getInt(ConfigType.money.getName());
            if (EconomyAPI.getInstance().myMoney(player) < money) {
                player.sendMessage("§c 抱歉，您的金钱不足无法传送");
                return false;
            }
            EconomyAPI.getInstance().reduceMoney(player, money);
        }
//            player.sendMessage(config.getString(ConfigType.message.getName()));
        if(getConfig(name).getString(ConfigType.type.getName(),(String) ConfigType.type.getDefaultType()).equals("default")){
            Map pos = (Map) config.get(defaultConfigType.pos2.getName());
            double x = (double) pos.get(LevelType.x.getName());
            double y = (double) pos.get(LevelType.y.getName());
            double z = (double) pos.get(LevelType.z.getName());
            Level level = Server.getInstance().getLevelByName((String) pos.get(LevelType.map.getName()));
            Position position = new Position(x, y, z, level);
            return player.teleport(position);
        }else if(getConfig(name).getString(ConfigType.type.getName(),(String) ConfigType.type.getDefaultType()).equals("Server")){
            String ip = getConfig(name).getString(ServerConfigType.ip.getName(),"127.0.0.1");
            int port = getConfig(name).getInt(ServerConfigType.port.getName(),19132);
            player.transfer(new InetSocketAddress(ip,port));
            return true;
        }
        return false;

    }

   @EventHandler
   public void onNear(PlayerNearDoorEvent event){


        Player player = event.getPlayer();
        if(playerTransfer(player,event.getName())){
            player.sendMessage(getConfig(event.getName()).getString(ConfigType.message.getName()));
        }else{
            player.knockBack(player,0,event.getPosition().x,event.getPosition().z,0.7D);
        }
   }



    public Position getDoorPos(String name){
        Config config = getConfig(name,true);
        if(config != null){
            Map pos = (Map) config.get(ConfigType.pos1.getName());
            double x = (double) pos.get(LevelType.x.getName());
            double y = (double) pos.get(LevelType.y.getName());
            double z = (double) pos.get(LevelType.z.getName());
            Level level = Server.getInstance().getLevelByName((String)pos.get(LevelType.map.getName()));
            return new Position(x,y,z,level);
        }
        return null;
    }

    public static LinkedList<String> getNameAll(){
        LinkedList<String> list = new LinkedList<>();
        File file = new File(dataFolder+"/Door");
        File[] files = file.listFiles();
        if(files != null){
            for (File file1:files){
                if(file1.isFile()){
                   String fileName = file1.getName();
                    int dot = fileName.lastIndexOf('.');
                    if ((dot >-1) && (dot < (fileName.length()))) {
                        fileName = fileName.substring(0, dot);
                    }
                    list.add(fileName);
                }
            }
            return list;
        }
        return null;

    }



    // 获取传送门
    public LinkedList<double[]> getDoor(String name,Position position,boolean Dir){
        LinkedList<double[]> pos = new LinkedList<>();
        double round = getConfig(name).getDouble(ConfigType.size.getName());
        int c = 0;
        double x,y;
        for(double a = 0;a < round; a += 0.1D){
            c++;
        }
        float b = 360/(c * 4);
        if(b > 90 || b < 0)
            b = 3;
        for(int i = 0;i <= 90; i += b){
            x = round * Math.cos(Math.toRadians(i));
            y = round * Math.sin(Math.toRadians(i));
            if(Dir){
                pos.add(new double[]{x + position.x,y + position.y,position.z});
                pos.add(new double[]{-y + position.x,x + position.y,position.z});
                pos.add(new double[]{-x + position.x,-y + position.y,position.z});
                pos.add(new double[]{y + position.x,-x + position.y,position.z});
            }else{
                pos.add(new double[]{position.x,y + position.y,x + position.z});
                pos.add(new double[]{position.x,x + position.y,- y + position.z});
                pos.add(new double[]{position.x,-y + position.y,-x + position.z});
                pos.add(new double[]{position.x,-x + position.y,y + position.z});
            }
        }
        return pos;
    }
    private void saveDoor(String name,Position position,String ip,int port){
        if(!isExistsConfig(name)){
            Config config = getConfig(name);
            LinkedHashMap<String,Object> configs = new LinkedHashMap<>();
            LinkedHashMap<String,Object> pos1 = new LinkedHashMap<>();
            pos1.put(LevelType.x.getName(),position.x);
            pos1.put(LevelType.y.getName(),position.y);
            pos1.put(LevelType.z.getName(),position.z);
            pos1.put(LevelType.map.getName(),position.level.getFolderName());
            configs.put(ConfigType.type.getName(),"Server");
            configs.put(ServerConfigType.ip.getName(),ip);
            configs.put(ServerConfigType.port.getName(),port);
            configs.put(ConfigType.pos1.getName(),pos1);
            for (ConfigType type:ConfigType.values()){
                if(type == ConfigType.type || type == ConfigType.pos1) continue;
                if(type == ConfigType.Color){
                    LinkedHashMap<String,Integer> color = new LinkedHashMap<>();
                    color.put(RGBType.R.getName(),106);
                    color.put(RGBType.G.getName(),90);
                    color.put(RGBType.B.getName(),205);
                    configs.put(type.getName(),color);
                    continue;
                }
                configs.put(type.getName(),type.getDefaultType());
            }
            config.setAll(configs);
            config.save();
        }
    }

    private void saveDoor(String name, Position position, Position tp){
        if(!isExistsConfig(name)){
            Config config = getConfig(name);
            LinkedHashMap<String,Object> configs = new LinkedHashMap<>();
            LinkedHashMap<String,Object> pos1 = new LinkedHashMap<>();
            pos1.put(LevelType.x.getName(),position.x);
            pos1.put(LevelType.y.getName(),position.y);
            pos1.put(LevelType.z.getName(),position.z);
            pos1.put(LevelType.map.getName(),position.level.getFolderName());
            LinkedHashMap<String,Object> pos2 = new LinkedHashMap<>();
            pos2.put(LevelType.x.getName(),tp.x);
            pos2.put(LevelType.y.getName(),tp.y);
            pos2.put(LevelType.z.getName(),tp.z);

            pos2.put(LevelType.map.getName(),tp.level.getFolderName());
            configs.put(ConfigType.type.getName(),"default");
            configs.put(ConfigType.pos1.getName(),pos1);
            configs.put(defaultConfigType.pos2.getName(),pos2);
            for (ConfigType type:ConfigType.values()){
                if(type == ConfigType.Color){
                    LinkedHashMap<String,Integer> color = new LinkedHashMap<>();
                   for(RGBType type1:RGBType.values()){
                       color.put(type1.getName(),type1.getDefaultType());
                   }
                   configs.put(type.getName(),color);
                    continue;
                }
                if(type == ConfigType.pos1) continue;
                configs.put(type.getName(),type.getDefaultType());
            }

            config.setAll(configs);
            config.save();
        }
    }
    public enum LevelType{
        x("x",0.0D),
        y("y",0.0D),
        z("z",0.0D),
        map("level","");
        protected String name;
        protected Object defaultType;
        LevelType(String name,Object defaultType){
            this.name = name;
            this.defaultType = defaultType;
        }
        public String getName() {
            return name;
        }


        public Object getDefaultType() {
            return defaultType;
        }

    }
    public enum RGBType{
        R("R",0),
        G("G",191),
        B("B",255);
        protected String name;
        protected Integer defaultType;
        RGBType(String name,Integer defaultType){
            this.name = name;
            this.defaultType = defaultType;
        }
        public String getName() {
            return name;
        }

        public int getDefaultType() {
            return defaultType;
        }

    }
    public enum defaultConfigType{
        pos2("pos2",new LinkedHashMap<String,Object>());
        protected String name;
        protected Object defaultType;
        defaultConfigType(String name,Object defaultType){
            this.name = name;
            this.defaultType = defaultType;
        }
        public Object getDefaultType() {
            return defaultType;
        }

        public String getName() {
            return name;
        }
    }
    public enum ServerConfigType{
        ip("IP",""),
        port("Port",19132);
        protected String name;
        protected Object defaultType;
        ServerConfigType(String name,Object defaultType){
            this.name = name;
            this.defaultType = defaultType;
        }
        public Object getDefaultType() {
            return defaultType;
        }

        public String getName() {
            return name;
        }
    }

    public enum ConfigType{
        type("类型","default"),
        pos1("pos1",new LinkedHashMap<String,Object>()),
        Dir("方向",true),
        size("半径",2D),
        Color("颜色",new LinkedHashMap<String,Integer>()),
        money("传送需要金币",0),
        message("传送信息",""),
        level("传送需要等级",0),
        PF("传送需要评分",0),
        Att("传送需要属性",new ArrayList<String>());

        protected String name;
        protected Object defaultType;
        ConfigType(String name,Object defaultType){
            this.name = name;
            this.defaultType = defaultType;
        }

        public Object getDefaultType() {
            return defaultType;
        }

        public String getName() {
            return name;
        }
    }
    private static HashMap<Integer,String> getChatMessageAll(){
        HashMap<Integer,String> m = new HashMap<>();
        HashMap map = (HashMap) AwakenSystem.getMain().getConfig().get(baseAPI.ConfigType.SETTING.getName());
        int count = 0;
        for (Object string:map.keySet()){
            m.put(count,String.valueOf(string));
            count++;
        }
        return m;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("pos")){
            if(sender instanceof Player){
                if(sender.isOp()){
                    if(args.length > 0){
                        switch (args[0]){
                            case "help":
                                sender.sendMessage("§e§l§o============传送门使用帮助============");
                                sender.sendMessage("§a§l§o/pos 1 §7设置传送门位置");
                                sender.sendMessage("§a§l§o/pos 2 §7设置传送点位置");
                                sender.sendMessage("§a§l§o/pos t <ip> <port> §7设置跨服传送");
                                sender.sendMessage("§a§l§o/pos save §6<名称> <d>§7保存传送门");
                                sender.sendMessage("§a§l§o/pos remove §6<名称> §7删除传送门");
                                sender.sendMessage("§a§l§o/pos r §6<名称> §7旋转传送门");
                                sender.sendMessage("§a§l§o/pos size §6<名称> §6<数值> §7设置传送门大小");
                                sender.sendMessage("§e§l§o========================§r");
                                break;
                            case "1":
                                Position SavePosition = ((Player) sender).getPosition();
                                if(!pos.containsKey(sender)){

                                    pos.put((Player) sender,new LinkedList<Object>(){
                                        {
                                            add(SavePosition);
                                        }
                                    });
                                }else{
                                    LinkedList<Object> positions = pos.get(sender);
                                    positions.set(0,SavePosition);
                                    pos.put((Player) sender,positions);
                                }
                                sender.sendMessage("§e§l§o[传送门] 传送门位置设置成功 \n§6§l§o坐标: x: "
                                        +(int)SavePosition.x+" y: "+(int)SavePosition.y+" z: "+(int)SavePosition.z+" 世界: "+SavePosition.level.getFolderName());
                                sender.sendMessage("§e§l§o[传送门] §c请输入/pos 2设置传送坐标或者/pos t 设置跨服传送");
                                break;
                            case "t":
                                if(args.length < 2){
                                    return false;
                                }
                                if(!pos.containsKey(sender)){
                                    sender.sendMessage("§e§l§o[传送门] §c请先设置传送门位置");
                                    return false;
                                }
                                LinkedList<Object> position = pos.get(sender);
                                if(position.size() <= 1){
                                    position.add(args[1]+":"+args[2]);
                                }else{
                                    position.set(1,args[1]+":"+args[2]);
                                }
                                pos.put((Player) sender,position);
                                sender.sendMessage("§e§l§o[传送门] 跨服传送位置设置成功 IP:"+args[1]+" 端口: "+args[2]);
                                sender.sendMessage("§e§l§o[传送门] §c请输入/pos save 传送门名称 保存传送门");
                                break;
                            case "2":
                                SavePosition = ((Player) sender).getPosition();
                                if(!pos.containsKey(sender)){
                                    sender.sendMessage("§e§l§o[传送门] §c请先设置传送门位置");
                                    return false;
                                }
                                LinkedList<Object> positions = pos.get(sender);
                                if(positions.size() <= 1){
                                    positions.add(SavePosition);
                                }else{
                                    positions.set(1,SavePosition);
                                }
                                pos.put((Player) sender,positions);
                                sender.sendMessage("§e§l§o[传送门] 传送坐标设置成功 \n§6§l§o坐标: x: "
                                        +SavePosition.x+" y: "+SavePosition.y+" z: "+SavePosition.z+" 世界: "+SavePosition.level.getFolderName());
                                sender.sendMessage("§e§l§o[传送门] §c请输入/pos save 传送门名称 保存传送门");
                                break;
                            case "save":
                                if(!pos.containsKey(sender)){
                                    sender.sendMessage("§e§l§o[传送门] §c请先设置传送门位置");
                                    return false;
                                }
                                if(pos.get(sender).size() <= 1){
                                    sender.sendMessage("§e§l§o[传送门] §c请先设置传送坐标");
                                    return false;
                                }
                                if(args.length > 1){
                                    String name = args[1];
                                     position = pos.get(sender);
                                    if(isExistsConfig(name)){
                                        sender.sendMessage("§e§l§o[传送门] §c"+name+"传送门已存在 换个名字吧");
                                    }else{
                                        sender.sendMessage("§e§l§o[传送门] §7"+name+"传送门构建中");
                                        if(position.get(1) instanceof Position){
                                            this.saveDoor(name,(Position) position.get(0),(Position) position.get(1));
                                        }else{
                                            String ipAndPort = (String) position.get(1);
                                            String ip = ipAndPort.split(":")[0];
                                            int port = Integer.parseInt(ipAndPort.split(":")[1]);
                                            this.saveDoor(name,(Position) position.get(0),ip,port);
                                        }
                                        sender.sendMessage("§e§l§o[传送门] §a"+name+"传送门构建完成");
                                        pos.remove(sender);
                                    }
                                }else{
                                    sender.sendMessage(command.getUsage());
                                }
                                break;
                            case "remove":
                                if(args.length > 1){
                                    String name = args[1];
                                    if(isExistsConfig(name)){
                                        File file = getDoorFile(name);
                                        if(file.delete()){
                                            sender.sendMessage("§e§l§o[传送门] §a传送门"+name+"删除成功");
                                        }else{
                                            sender.sendMessage("§e§l§o[传送门] §c传送门"+name+"删除失败");
                                        }
                                    }else{
                                        sender.sendMessage("§e§l§o[传送门] §c"+name+"传送门不存在");
                                    }
                                }else{
                                    sender.sendMessage(command.getUsage());
                                }
                                break;
                            case "r":
                                if(args.length > 1) {
                                    String name = args[1];
                                    if(isExistsConfig(name)){
                                        Config config = getConfig(name);
                                        config.set(ConfigType.Dir.getName(),!config.getBoolean(ConfigType.Dir.getName()));
                                        config.save();
                                        sender.sendMessage("§e§l§o[传送门] §a"+name+"传送门已旋转!");
                                    }else{
                                        sender.sendMessage("§e§l§o[传送门] §c"+name+"传送门不存在");
                                    }
                                }else{
                                    sender.sendMessage(command.getUsage());
                                }
                                break;
                            case "size":
                                if(args.length > 2){
                                String name = args[1];
                                String sizeString = args[2];
                                double size = Double.parseDouble(sizeString);
                                if(isExistsConfig(name)){
                                    Config config = getConfig(name);
                                    config.set(ConfigType.size.getName(),size);
                                    config.save();
                                    sender.sendMessage("§e§l§o[传送门] §a"+name+"传送门大小已更改!");
                                }else{
                                    sender.sendMessage("§e§l§o[传送门] §c"+name+"传送门不存在");
                                }
                            }else{
                                sender.sendMessage(command.getUsage());
                            }
                                break;
                            default:
                                sender.sendMessage(command.getUsage());
                                break;
                        }
                    }else{
                        sender.sendMessage(command.getUsage());
                    }
                }
            }
        }
        return true;
    }
}

