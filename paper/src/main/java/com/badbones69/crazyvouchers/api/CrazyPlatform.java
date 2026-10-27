package com.badbones69.crazyvouchers.api;

import com.badbones69.crazyvouchers.api.adapters.PaperSenderAdapter;
import com.ryderbelserion.crazyvouchers.CrazyVouchersPlugin;
import com.ryderbelserion.fusion.paper.FusionPaper;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NonNull;
import java.nio.file.Path;

public class CrazyPlatform extends CrazyVouchersPlugin<Audience, FusionPaper> {

    private PaperSenderAdapter senderAdapter;

    public CrazyPlatform(FusionPaper fusion, Path path) {
        super(fusion, path);
    }

    @Override
    public void init() {
        super.init();

        this.senderAdapter = new PaperSenderAdapter();

        loadMessages();
    }

    @Override
    public @NonNull PaperSenderAdapter getSenderAdapter() {
        return this.senderAdapter;
    }
}