package com.github.charlyb01.music_control.mixin;

import com.github.charlyb01.music_control.categories.Music;
import com.github.charlyb01.music_control.categories.MusicCategories;
import com.github.charlyb01.music_control.client.MusicControlClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.WorldEventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldEventHandler.class)
public class WorldEventHandlerMixin {
    @WrapOperation(method = "playJukeboxSong", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setRecordPlayingOverlay(Lnet/minecraft/text/Text;)V"))
    private void useRightRecordName(InGameHud instance, Text description, Operation<Void> original,
                                    @Local(ordinal = 0) SoundInstance soundInstance) {
        Identifier soundId = soundInstance.getSound().getIdentifier();
        original.call(instance, Text.translatable(soundId.toString()));

        if (MusicControlClient.currentCategory.equals(Music.ALL_MUSICS)
                || MusicControlClient.currentCategory.equals(Music.ALL_MUSIC_DISCS)) {
            MusicCategories.PLAYED_MUSICS.add(soundId);
        }

        // Handle jukebox fade out functionality
        onJukeboxStart();
    }

    
    private void onJukeboxStart() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getSoundManager() == null) return;

        // Always store the current music state for resumption
        MusicControlClient.shouldResumeAfterJukebox = true;
        if (MusicControlClient.currentMusic != null) {
            MusicControlClient.musicBeforeJukebox = MusicControlClient.currentMusic;
            // Store current position (in milliseconds) to continue from where we left off
            MusicControlClient.musicPositionBeforeJukebox = System.currentTimeMillis();
        } else {
            // If no custom music was playing, store a default so we can trigger resume
            MusicControlClient.musicBeforeJukebox = Identifier.of("minecraft:music.overworld.cherry_grove");
            MusicControlClient.musicPositionBeforeJukebox = 0;
        }

        // Trigger fade out by setting jukebox flag
        MusicControlClient.isJukeboxPlaying = true;
    }

    private void onJukeboxEnd() {
        // Reset jukebox state to trigger fade in
        MusicControlClient.isJukeboxPlaying = false;
    }
}
