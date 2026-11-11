export type DayStats = {
  scrolls: number;
  distance: number;
};

export type ScrollStats = {
  totalScrolls: number;
  totalDistance: number;
  byDay: Record<string, DayStats>;
};

export type ScrollSnapshot = {
  todayKey: string;
  todayScrolls: number;
  todayDistance: number;
  totalScrolls: number;
  totalDistance: number;
  byDay: Record<string, DayStats>;
};
