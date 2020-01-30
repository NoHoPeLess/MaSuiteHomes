package fi.matiaspaavilainen.masuitehomes.bungee.controllers;

import fi.matiaspaavilainen.masuitecore.bungee.chat.Formator;
import fi.matiaspaavilainen.masuitecore.core.configuration.BungeeConfiguration;
import fi.matiaspaavilainen.masuitecore.core.objects.Location;
import fi.matiaspaavilainen.masuitecore.core.models.MaSuitePlayer;
import fi.matiaspaavilainen.masuitehomes.bungee.MaSuiteHomes;
import fi.matiaspaavilainen.masuitehomes.core.models.Home;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.UUID;

public class SetController {

    private MaSuiteHomes plugin;

    public SetController(MaSuiteHomes plugin) {
        this.plugin = plugin;
    }

    private Formator formator = new Formator();
    private BungeeConfiguration config = new BungeeConfiguration();

    public void set(ProxiedPlayer player, String home, Location loc, int maxGlobalHomes, int maxServerHomes) {
        setHome(player, home, loc, player.getUniqueId(), maxGlobalHomes, maxServerHomes);
    }

    public void set(ProxiedPlayer player, String name, String home, Location loc, int maxGlobalHomes, int maxServerHomes) {
        MaSuitePlayer msp = plugin.api.getPlayerService().getPlayer(name);
        if (msp == null) {
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("player-not-found"));
            return;
        }
        setHome(player, home, loc, msp.getUniqueId(), maxGlobalHomes, maxServerHomes);
    }

    private void setHome(ProxiedPlayer player, String homeName, Location loc, UUID uniqueId, int maxGlobalHomes, int maxServerHomes) {
        Home home = plugin.homeService.getHomeExact(uniqueId, homeName);
        List<Home> homes = plugin.homeService.getHomes(uniqueId);
        loc.setServer(player.getServer().getInfo().getName());
        if (home != null) {
            home.setLocation(loc);
            plugin.homeService.updateHome(home);
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home.updated").replace("%home%", home.getName()));
            plugin.listHomes(player);
            return;
        }

        long serverHomeCount = homes.stream().filter(filteredHome -> filteredHome.getLocation().getServer().equalsIgnoreCase(player.getServer().getInfo().getName())).count();

        System.out.println(serverHomeCount);

        if ((homes.size() < maxGlobalHomes || maxGlobalHomes == -1) && (serverHomeCount < maxServerHomes || maxServerHomes == -1)) {
            Home h = plugin.homeService.createHome(new Home(homeName, uniqueId, loc));
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home.set").replace("%home%", h.getName()));
        } else {
            formator.sendMessage(player, config.load("homes", "messages.yml").getString("home-limit-reached"));
        }

        plugin.listHomes(player);
    }
}
