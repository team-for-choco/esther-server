package com.juyoung.estherserver.mixin;

import com.juyoung.estherserver.EstherServerMod;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInfo;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class EstherServerMixin extends ReentrantBlockableEventLoop<TickTask> implements ServerInfo, ChunkIOErrorReporter, CommandSource, AutoCloseable {

    public EstherServerMixin(String pName) {
        super(pName);
    }

    @Inject(method = "loadLevel", at = @At("TAIL"))
    public void estherserver$loadLevel(CallbackInfo ci) {
        System.out.println("Esther Server Mixin loaded! (modid: " + EstherServerMod.MODID + ")");
    }
}
