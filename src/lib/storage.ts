import AsyncStorage from "@react-native-async-storage/async-storage";
import type { DayStats, ScrollSnapshot, ScrollStats } from "./types";

const STORAGE_KEY = "insta_scroll_stats_v1";

const emptyStats = (): ScrollStats => ({
  totalScrolls: 0,
  totalDistance: 0,
  byDay: {},
});

export function todayKey(date = new Date()): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function toSnapshot(stats: ScrollStats): ScrollSnapshot {
  const key = todayKey();
  const today = stats.byDay[key] ?? { scrolls: 0, distance: 0 };
  return {
    todayKey: key,
    todayScrolls: today.scrolls,
    todayDistance: today.distance,
    totalScrolls: stats.totalScrolls,
    totalDistance: stats.totalDistance,
    byDay: stats.byDay,
  };
}

export async function loadStats(): Promise<ScrollSnapshot> {
  try {
    const raw = await AsyncStorage.getItem(STORAGE_KEY);
    if (!raw) return toSnapshot(emptyStats());
    const parsed = JSON.parse(raw) as ScrollStats;
    return toSnapshot({
      totalScrolls: parsed.totalScrolls ?? 0,
      totalDistance: parsed.totalDistance ?? 0,
      byDay: parsed.byDay ?? {},
    });
  } catch {
    return toSnapshot(emptyStats());
  }
}

async function saveStats(stats: ScrollStats): Promise<void> {
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(stats));
}

export async function recordScroll(
  scrolls = 1,
  distance = 0
): Promise<ScrollSnapshot> {
  const current = await loadStats();
  const stats: ScrollStats = {
    totalScrolls: current.totalScrolls,
    totalDistance: current.totalDistance,
    byDay: { ...current.byDay },
  };
  const key = todayKey();
  const day: DayStats = stats.byDay[key] ?? { scrolls: 0, distance: 0 };
  day.scrolls += scrolls;
  day.distance += distance;
  stats.byDay[key] = day;
  stats.totalScrolls += scrolls;
  stats.totalDistance += distance;
  await saveStats(stats);
  return toSnapshot(stats);
}

export async function resetToday(): Promise<ScrollSnapshot> {
  const current = await loadStats();
  const key = current.todayKey;
  const today = current.byDay[key] ?? { scrolls: 0, distance: 0 };
  const stats: ScrollStats = {
    totalScrolls: Math.max(0, current.totalScrolls - today.scrolls),
    totalDistance: Math.max(0, current.totalDistance - today.distance),
    byDay: { ...current.byDay, [key]: { scrolls: 0, distance: 0 } },
  };
  await saveStats(stats);
  return toSnapshot(stats);
}

export function recentDays(
  byDay: Record<string, DayStats>,
  limit = 7
): Array<{ day: string; stats: DayStats }> {
  const out: Array<{ day: string; stats: DayStats }> = [];
  const now = new Date();
  for (let i = 0; i < limit; i += 1) {
    const d = new Date(now);
    d.setDate(now.getDate() - i);
    const key = todayKey(d);
    out.push({ day: key, stats: byDay[key] ?? { scrolls: 0, distance: 0 } });
  }
  return out;
}

export function formatDistance(px: number): string {
  if (px < 1000) return `${Math.round(px)} px`;
  if (px < 100_000) return `${(px / 1000).toFixed(1)}k px`;
  return `${Math.round(px / 1000)}k px`;
}
