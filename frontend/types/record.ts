export type RecordInput = {
  daily_social_media_hours: number;
  daily_ai_tool_usage_hours: number;
  sleep_hours: number;
  physical_activity_hours: number;
};

export type RecordResult = {
  daily_social_media_hours: number;
  daily_ai_tool_usage_hours: number;
  sleep_hours: number;
  physical_activity_hours: number;
  record_date: string;
  mental_health_score: number;
};