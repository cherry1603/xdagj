package io.xdag.net.libp2p.gossip;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GossipTopicsScoringConfig {
    private final Map<String, GossipTopicScoringConfig> topicConfigs;

    private GossipTopicsScoringConfig(final Map<String, GossipTopicScoringConfig> topicConfigs) {
        this.topicConfigs = topicConfigs;
    }

    public boolean isEmpty() {
        return topicConfigs.isEmpty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, GossipTopicScoringConfig> getTopicConfigs() {
        return topicConfigs;
    }

    public static class Builder {
        private final Map<String, GossipTopicScoringConfig.Builder> topicScoringConfigBuilders =
                new HashMap<>();

        public GossipTopicsScoringConfig build() {
            final Map<String, GossipTopicScoringConfig> topicConfig =
                    topicScoringConfigBuilders.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build()));
            return new GossipTopicsScoringConfig(topicConfig);
        }

        public Builder topicScoring(
                final String topic, final Consumer<GossipTopicScoringConfig.Builder> consumer) {
            GossipTopicScoringConfig.Builder builder =
                    topicScoringConfigBuilders.computeIfAbsent(
                            topic, __ -> GossipTopicScoringConfig.builder());
            consumer.accept(builder);
            return this;
        }

        public Builder clear() {
            topicScoringConfigBuilders.clear();
            return this;
        }
    }
}
