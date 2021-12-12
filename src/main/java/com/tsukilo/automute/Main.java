package com.tsukilo.automute;


import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class Main extends JavaPlugin implements Listener {

    public Configuration config;
    public List<String> bannedWords;
    public String webhook;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("AutoMute starting");
        System.out.println("Registering events:");
        try{
            Bukkit.getPluginManager().registerEvents(this, this);
            System.out.println("Events registered");
        }catch (Exception e){
            System.out.println("Error : " + e);
        }
        System.out.println("Setting up config");
        try{
            this.config = this.getConfig();
            config.options().copyDefaults();
            saveDefaultConfig();

            this.bannedWords = config.getStringList("Words");
            this.webhook = config.getString("discord");

            System.out.println("Config setup");
        }catch (Exception e){
            System.out.println("Error : " + e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onChat(ChatEvent e) throws IOException {
        String message = e.message().toString();
        Player player = e.getPlayer();

        for (int i = 0; i < bannedWords.size(); i++) {
            if(message.contains(bannedWords.get(i))){
                player.sendMessage("You have been permanently muted for using a banned word");
                player.sendMessage("An admin will be on shortly to review your case");
                e.setCancelled(true);

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mute " + player.getName() + " 10y Auto");

                final HttpsURLConnection connection = (HttpsURLConnection) new URL(webhook).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setDoOutput(true);
                try (final OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(("{\"content\":\"" + player.getName() + " attempted to break the rules! Offending message: " + bannedWords.get(i) + " current time: " + System.currentTimeMillis() + "\"}").getBytes(StandardCharsets.UTF_8));
                }
                connection.getInputStream();
            }
        }
    }
}
