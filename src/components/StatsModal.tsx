import {
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { formatDistance, recentDays } from "../lib/storage";
import type { ScrollSnapshot } from "../lib/types";

type Props = {
  visible: boolean;
  snapshot: ScrollSnapshot;
  onClose: () => void;
  onResetToday: () => void;
};

export function StatsModal({ visible, snapshot, onClose, onResetToday }: Props) {
  const week = recentDays(snapshot.byDay, 7);

  return (
    <Modal visible={visible} animationType="slide" transparent onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <Pressable style={StyleSheet.absoluteFill} onPress={onClose} />
        <View style={styles.sheet}>
          <Text style={styles.brand}>Insta Scroll</Text>
          <Text style={styles.title}>Your scroll stats</Text>

          <View style={styles.hero}>
            <Text style={styles.heroLabel}>Today</Text>
            <Text style={styles.heroCount}>{snapshot.todayScrolls}</Text>
            <Text style={styles.heroSub}>
              {formatDistance(snapshot.todayDistance)} · all-time {snapshot.totalScrolls}
            </Text>
          </View>

          <Text style={styles.section}>Last 7 days</Text>
          {week.map(({ day, stats }) => (
            <View key={day} style={styles.row}>
              <Text style={styles.day}>{day}</Text>
              <Text style={styles.dayCount}>{stats.scrolls}</Text>
            </View>
          ))}

          <Pressable style={styles.reset} onPress={onResetToday}>
            <Text style={styles.resetText}>Reset today</Text>
          </Pressable>
          <Pressable style={styles.close} onPress={onClose}>
            <Text style={styles.closeText}>Close</Text>
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: "rgba(0,0,0,0.55)",
    justifyContent: "flex-end",
  },
  sheet: {
    backgroundColor: "#141416",
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    paddingHorizontal: 22,
    paddingTop: 22,
    paddingBottom: 34,
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.08)",
  },
  brand: {
    color: "#FF4D6D",
    fontSize: 12,
    letterSpacing: 1,
    textTransform: "uppercase",
    fontWeight: "600",
  },
  title: {
    marginTop: 6,
    color: "#F6F4F1",
    fontSize: 26,
    fontWeight: "700",
  },
  hero: {
    marginTop: 18,
    padding: 18,
    borderRadius: 18,
    backgroundColor: "#1F1518",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.08)",
  },
  heroLabel: {
    color: "#A7A29B",
    fontSize: 11,
    textTransform: "uppercase",
    letterSpacing: 0.8,
  },
  heroCount: {
    marginTop: 2,
    color: "#F6F4F1",
    fontSize: 48,
    fontWeight: "700",
  },
  heroSub: {
    marginTop: 4,
    color: "#FF8A3D",
    fontSize: 13,
  },
  section: {
    marginTop: 20,
    marginBottom: 8,
    color: "#F6F4F1",
    fontSize: 16,
    fontWeight: "700",
  },
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    paddingVertical: 8,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: "rgba(255,255,255,0.08)",
  },
  day: {
    color: "#A7A29B",
    fontSize: 14,
  },
  dayCount: {
    color: "#F6F4F1",
    fontSize: 14,
    fontWeight: "600",
  },
  reset: {
    marginTop: 20,
    alignItems: "center",
    paddingVertical: 12,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: "#FF4D6D",
  },
  resetText: {
    color: "#FF4D6D",
    fontWeight: "600",
  },
  close: {
    marginTop: 10,
    alignItems: "center",
    paddingVertical: 12,
  },
  closeText: {
    color: "#A7A29B",
  },
});
