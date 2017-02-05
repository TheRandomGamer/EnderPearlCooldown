package me.therandomgamer.epearlcooldown;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by robin on 18/09/16.
 */
public class Main extends JavaPlugin implements Listener {

    private HashMap<Player,Long> cooldown;
    private List<Player> onEpearl;


    @Override
    public void onEnable() {
        super.onEnable();
        this.getLogger().info("FactionUtilities: EnderPearlCooldown by TheRandomGamer has been enabled");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        cooldown = new HashMap<Player, Long>();
        onEpearl = new ArrayList<Player>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onItemSwitch(PlayerItemHeldEvent e){
        final Player p = e.getPlayer();
        if(p.getInventory().getItem(e.getNewSlot()) != null && p.getInventory().getItem(e.getNewSlot()).getType() == Material.ENDER_PEARL){

            if(!onEpearl.contains(p)){
            onEpearl.add(p);}
            int cdTimer = PlayerHasCooldown(p);
            new BukkitRunnable(){
                @Override
                public void run() {
                    if(onEpearl.contains(p)){
                        int cd = PlayerHasCooldown(p);
                        if(cd == -1){
                            ActionBarAPI.sendActionBar(p, getConfig().getString("ReadyMessage").replaceAll("&","§"));
                        }else{
                            String conf = getConfig().getString("CooldownMessage").replaceAll("&","§");
                            String msg = conf.replaceAll("%t",String.valueOf(cd));
                            ActionBarAPI.sendActionBar(p,msg);
                        }
                    }else{
                        cancel();
                    }
                }
            }.runTaskTimer(this,0,20);


        }else if(onEpearl.contains(p)){
            onEpearl.remove(p);
            ActionBarAPI.sendActionBar(p, "");
        }
    }

    @EventHandler
    public void onEpearl(PlayerTeleportEvent e){
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){
            Player p = e.getPlayer();
            if(PlayerHasCooldown(p) != -1){
                e.setCancelled(true);
            }else{
                cooldown.put(p, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onProjectileThrow(ProjectileLaunchEvent e){
        if(e.getEntityType() == EntityType.ENDER_PEARL && e.getEntity().getShooter() instanceof Player && PlayerHasCooldown((Player) e.getEntity().getShooter()) != -1){
            ((Player) e.getEntity().getShooter()).getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
            e.setCancelled(true);
        }
    }


    private int PlayerHasCooldown(Player p){
        int cdtimer = -1;
        if(cooldown.containsKey(p)){
            Long lastThrow = cooldown.get(p);
            if(System.currentTimeMillis()-lastThrow <= getConfig().getLong("Cooldown")){
                long cdTimerMillis = getConfig().getLong("Cooldown") - System.currentTimeMillis() + lastThrow;
                cdtimer = (int) TimeUnit.MILLISECONDS.toSeconds(cdTimerMillis);
            }else{
                cooldown.remove(p);
            }
        }
        return cdtimer;
    }
}
