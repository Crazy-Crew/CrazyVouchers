package com.badbones69.crazyvouchers.api.builders;

import com.badbones69.crazyvouchers.CrazyVouchers;
import com.ryderbelserion.fusion.paper.FusionPaper;
import net.kyori.adventure.text.event.ClickEvent;
import java.util.ArrayList;
import java.util.List;

public class ComponentBuilder {

    private final CrazyVouchers plugin = CrazyVouchers.get();

    private final FusionPaper fusion = this.plugin.getFusion();

    private final List<String> messages = new ArrayList<>();
    private final ClickEvent<?> event;

    public ComponentBuilder(final ClickEvent<?> event) {
        this.event = event;
    }

    public ComponentBuilder() {
        this(null);
    }

    public void updateMessage(final String message, final int position) {
        this.messages.add(position, message);
    }

    public void addMessage(final String message) {
        this.messages.add(message);
    }
}