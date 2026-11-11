import { Pressable, StyleSheet, Text, View } from "react-native";
import { formatDistance } from "../lib/storage";

type Props = {
  scrolls: number;
  distance: number;
  minimized: boolean;
  onToggleMinimize: () => void;
  onOpenStats: () => void;
};

export function ScrollBadge({
  scrolls,
  distance,
  minimized,
  onToggleMinimize,
  onOpenStats,
}: Props) {
  return (
    <View style={styles.wrap} pointerEvents="box-none">
      <View style={styles.card}>
        <Pressable
          style={styles.toggle}
          onPress={onToggleMinimize}
          hitSlop={8}
          accessibilityRole="button"
          accessibilityLabel={minimized ? "Expand counter" : "Minimize counter"}
        >
          <Text style={styles.toggleText}>{minimized ? "+" : "–"}</Text>
        </Pressable>

        <Pressable onPress={onOpenStats} accessibilityRole="button">
          {!minimized ? <Text style={styles.label}>Insta scrolls today</Text> : null}
          <Text style={styles.count}>{scrolls}</Text>
          {!minimized ? (
            <Text style={styles.sub}>{formatDistance(distance)} scrolled</Text>
          ) : null}
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: "absolute",
    right: 14,
    bottom: 28,
    zIndex: 20,
  },
  card: {
    minWidth: 132,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 16,
    backgroundColor: "rgba(18,18,18,0.92)",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.12)",
    shadowColor: "#000",
    shadowOpacity: 0.35,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 8 },
    elevation: 8,
  },
  toggle: {
    position: "absolute",
    top: 8,
    right: 8,
    width: 22,
    height: 22,
    borderRadius: 11,
    backgroundColor: "rgba(255,255,255,0.08)",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 2,
  },
  toggleText: {
    color: "#B8B0A8",
    fontSize: 14,
    lineHeight: 16,
  },
  label: {
    color: "#B8B0A8",
    fontSize: 10,
    letterSpacing: 0.8,
    textTransform: "uppercase",
    marginBottom: 4,
    paddingRight: 20,
  },
  count: {
    color: "#FF6B84",
    fontSize: 28,
    fontWeight: "700",
    letterSpacing: -0.5,
  },
  sub: {
    marginTop: 4,
    color: "#B8B0A8",
    fontSize: 11,
  },
});
