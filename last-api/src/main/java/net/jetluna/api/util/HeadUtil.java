package net.jetluna.api.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class HeadUtil {

    // Создает голову по ссылке на текстуру (Minecraft-URL)
    public static ItemStack getHead(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (url == null || url.isEmpty()) return head;

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            textures.setSkin(new URL("http://textures.minecraft.net/texture/" + url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        profile.setTextures(textures);
        meta.setPlayerProfile(profile);
        head.setItemMeta(meta);

        return head;
    }
}