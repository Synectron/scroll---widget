import { useCallback, useEffect, useState, type ComponentType } from "react";
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import WebView from "react-native-webview";
import { ScrollBadge } from "../components/ScrollBadge";
import { StatsModal } from "../components/StatsModal";
import { MOBILE_USER_AGENT, SCROLL_BRIDGE_JS } from "../lib/scrollBridge";
import { loadStats, recordScroll, resetToday } from "../lib/storage";
import type { ScrollSnapshot } from "../lib/types";

const INSTAGRAM_URL = "https://www.instagram.com/";

// React 19 + current webview typings resolve props to `never` under tsc.
const InstagramWebView = WebView as unknown as ComponentType<Record<string, unknown>>;

const emptySnapshot = (): ScrollSnapshot => ({
  todayKey: "",
  todayScrolls: 0,
  todayDistance: 0,
  totalScrolls: 0,
  totalDistance: 0,
  byDay: {},
});

export default function HomeScreen() {
  const [snapshot, setSnapshot] = useState<ScrollSnapshot>(emptySnapshot);
  const [loading, setLoading] = useState(true);
  const [minimized, setMinimized] = useState(false);
  const [statsOpen, setStatsOpen] = useState(false);
  const [showTip, setShowTip] = useState(true);

  useEffect(() => {
    let alive = true;
    (async () => {
      const stats = await loadStats();
      if (alive) setSnapshot(stats);
    })();
    return () => {
      alive = false;
    };
  }, []);

  const onMessage = useCallback(async (event: { nativeEvent: { data: string } }) => {
    try {
      const data = JSON.parse(event.nativeEvent.data) as {
        type?: string;
        scrolls?: number;
        distance?: number;
      };
      if (data.type !== "scroll") return;
      const next = await recordScroll(data.scrolls ?? 1, data.distance ?? 0);
      setSnapshot(next);
    } catch {
      // ignore malformed messages
    }
  }, []);

  const onReset = useCallback(async () => {
    const next = await resetToday();
    setSnapshot(next);
  }, []);

  return (
    <SafeAreaView style={styles.safe} edges={["top", "left", "right"]}>
      <View style={styles.header}>
        <View>
          <Text style={styles.brand}>Insta Scroll</Text>
          <Text style={styles.headerSub}>Browse Instagram · count every scroll</Text>
        </View>
        <Pressable style={styles.statsBtn} onPress={() => setStatsOpen(true)}>
          <Text style={styles.statsBtnText}>Stats</Text>
        </Pressable>
      </View>

      <View style={styles.webWrap}>
        {loading ? (
          <View style={styles.loader}>
            <ActivityIndicator color="#FF4D6D" size="large" />
            <Text style={styles.loaderText}>Loading Instagram…</Text>
          </View>
        ) : null}

        <InstagramWebView
          source={{ uri: INSTAGRAM_URL }}
          style={styles.webview}
          userAgent={MOBILE_USER_AGENT}
          injectedJavaScript={SCROLL_BRIDGE_JS}
          injectedJavaScriptBeforeContentLoaded={SCROLL_BRIDGE_JS}
          onMessage={onMessage}
          onLoadEnd={() => setLoading(false)}
          allowsBackForwardNavigationGestures
          sharedCookiesEnabled
          thirdPartyCookiesEnabled
          setSupportMultipleWindows={false}
          javaScriptEnabled
          domStorageEnabled
          mediaPlaybackRequiresUserAction
        />

        <ScrollBadge
          scrolls={snapshot.todayScrolls}
          distance={snapshot.todayDistance}
          minimized={minimized}
          onToggleMinimize={() => setMinimized((v) => !v)}
          onOpenStats={() => setStatsOpen(true)}
        />

        {showTip ? (
          <View style={styles.tip}>
            <Text style={styles.tipText}>
              Log into Instagram here, then scroll your feed. The bubble tracks today’s scrolls.
            </Text>
            <Pressable onPress={() => setShowTip(false)}>
              <Text style={styles.tipDismiss}>Got it</Text>
            </Pressable>
          </View>
        ) : null}
      </View>

      <StatsModal
        visible={statsOpen}
        snapshot={snapshot}
        onClose={() => setStatsOpen(false)}
        onResetToday={onReset}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: "#0F0F10",
  },
  header: {
    paddingHorizontal: 16,
    paddingBottom: 10,
    paddingTop: 4,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  brand: {
    color: "#FF4D6D",
    fontSize: 18,
    fontWeight: "700",
  },
  headerSub: {
    marginTop: 2,
    color: "#A7A29B",
    fontSize: 12,
  },
  statsBtn: {
    paddingHorizontal: 14,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: "#1F1518",
    borderWidth: 1,
    borderColor: "rgba(255,77,109,0.35)",
  },
  statsBtnText: {
    color: "#FF4D6D",
    fontWeight: "600",
    fontSize: 13,
  },
  webWrap: {
    flex: 1,
    overflow: "hidden",
    borderTopLeftRadius: 18,
    borderTopRightRadius: 18,
    backgroundColor: "#000",
  },
  webview: {
    flex: 1,
    backgroundColor: "#000",
  },
  loader: {
    ...StyleSheet.absoluteFill,
    zIndex: 5,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "#0F0F10",
    gap: 12,
  },
  loaderText: {
    color: "#A7A29B",
  },
  tip: {
    position: "absolute",
    left: 14,
    right: 14,
    top: 14,
    zIndex: 15,
    padding: 14,
    borderRadius: 14,
    backgroundColor: "rgba(20,20,22,0.95)",
    borderWidth: 1,
    borderColor: "rgba(255,255,255,0.1)",
  },
  tipText: {
    color: "#F6F4F1",
    fontSize: 13,
    lineHeight: 18,
  },
  tipDismiss: {
    marginTop: 10,
    color: "#FF4D6D",
    fontWeight: "700",
  },
});
