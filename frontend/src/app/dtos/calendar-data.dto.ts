export interface CalendarEventDTO {
    id: number;
    title: string;
    start: string; // ISO String format
    end: string;   // ISO String format
}

export interface DailyOccupancyDTO {
    date: string;  // YYYY-MM-DD
    color: string; // Hex color
    status: string; // High, Medium, Low
}

export interface RoomCalendarDTO {
    events: CalendarEventDTO[];
    dailyOccupancy: DailyOccupancyDTO[];
}