package us.crazycrew.crazyvouchers.api.enums.messages;

import com.ryderbelserion.fusion.api.objects.FusionKey;
import com.ryderbelserion.fusion.core.api.config.properties.PropertyManager;
import com.ryderbelserion.fusion.core.api.config.properties.objects.enums.PropertyType;
import com.ryderbelserion.fusion.core.api.registry.message.MessageRegistry;
import com.ryderbelserion.fusion.core.api.registry.message.adapter.YamlMessageAdapter;
import com.ryderbelserion.fusion.core.utils.StringUtils;
import com.ryderbelserion.fusion.files.types.configurate.YamlCustomFile;
import net.kyori.adventure.audience.Audience;
import org.jspecify.annotations.NullMarked;
import org.spongepowered.configurate.CommentedConfigurationNode;
import us.crazycrew.crazyvouchers.VouchersProvider;
import us.crazycrew.crazyvouchers.api.CrazyVouchers;
import us.crazycrew.crazyvouchers.api.adapters.sender.ISenderAdapter;
import us.crazycrew.crazyvouchers.api.config.ConfigManager;
import us.crazycrew.crazyvouchers.api.config.types.RootKeys;
import java.util.List;
import java.util.Map;
import static us.crazycrew.crazyvouchers.api.CrazyVouchers.namespace;

@NullMarked
public enum Message {

    ;

    private final CrazyVouchers platform = VouchersProvider.api();

    private final ISenderAdapter senderAdapter = this.platform.getSenderAdapter();

    private final ConfigManager configManager = this.platform.getConfigManager();

    private final PropertyManager pluginConfig = this.configManager.getConfig();

    private final String defaultValue;
    private final PropertyType type;
    private final Object[] path;
    private final FusionKey id;

    Message(final String oldPath, final String id, final String defaultValue, final Object... path) {
        this.defaultValue = defaultValue;
        this.id = FusionKey.key(namespace, id);
        this.type = PropertyType.STRING;
        this.path = path;
    }

    Message(final String oldPath, final String id, final List<String> defaultValue, final Object... path) {
        this.defaultValue = StringUtils.toString(defaultValue);
        this.id = FusionKey.key(namespace, id);
        this.type = PropertyType.STRING_LIST;
        this.path = path;
    }

    public void addKey(final MessageRegistry registry, final YamlCustomFile customFile, final CommentedConfigurationNode configuration, final FusionKey id) {
        final YamlMessageAdapter adapter = new YamlMessageAdapter(configuration, this.defaultValue, this.path);

        /*if (!configuration.hasChild(this.path)) {
            final CommentedConfigurationNode section = configuration.node(this.path);

            switch (this.type) {
                case STRING_LIST -> {
                    try {
                        section.set(toList(this.defaultValue));

                        customFile.save();
                    } catch (final SerializationException exception) {
                        exception.printStackTrace();
                    }
                }

                case STRING -> {
                    try {
                        section.set(this.defaultValue);

                        customFile.save();
                    } catch (final SerializationException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }*/

        registry.addKey(
                id,
                this.id,
                adapter
        );
    }

    public List<String> toList(final String value) {
        return value.lines().toList();
    }

    public void sendMessage(final Audience audience, final Map<String, String> placeholders) {
        switch (this.pluginConfig.getProperty(RootKeys.message_state)) {
            case send_message -> this.senderAdapter.sendMessage(audience, this.id, placeholders);
            case send_actionbar -> this.senderAdapter.sendActionBar(audience, this.id, placeholders);
        }
    }

    public void sendMessage(final Audience audience, final String placeholder, final String value) {
        sendMessage(audience, Map.of(placeholder, value));
    }

    public void sendMessage(final Audience audience) {
        sendMessage(audience, Map.of());
    }

    public String getMessage(final Audience audience, final Map<String, String> placeholders) {
        return this.senderAdapter.getMessage(audience, this.id, placeholders);
    }

    public String getMessage(final Audience audience, final String placeholder, final String value) {
        return getMessage(audience, Map.of(placeholder, value));
    }

    public String getMessage(final Audience audience) {
        return this.senderAdapter.getMessage(audience, this.id);
    }

    public FusionKey getKey() {
        return this.id;
    }
}