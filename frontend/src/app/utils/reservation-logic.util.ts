

export class ReservationLogic {

  static toMinutes(timeStr: string): number {
    const [h, m] = timeStr.split(':').map(Number);
    return h * 60 + m;
  }

  // add 0 to left 
  static pad(n: number): string {
    return n < 10 ? '0' + n : '' + n;
  }

  static isOverlap(startStr: string, endStr: string, occupiedSlots: any[]): boolean {
    const s = this.toMinutes(startStr);
    const e = this.toMinutes(endStr);

    return occupiedSlots.some(res => {
      const resStart = new Date(res.startDate);
      const resEnd = new Date(res.endDate);
      
      const startMins = resStart.getHours() * 60 + resStart.getMinutes();
      const endMins = resEnd.getHours() * 60 + resEnd.getMinutes();

      return s < endMins && e > startMins;
    });
  }

  static generateStartTimes(occupiedSlots: any[]): string[] {
    const times: string[] = [];
    let h = 8;
    let m = 0;

    while (h < 21 || (h === 20 && m <= 30)) { 
      const timeStr = `${this.pad(h)}:${this.pad(m)}`;
      
      const timeMins = this.toMinutes(timeStr);
      
      const isOccupied = occupiedSlots.some(res => {
        const start = new Date(res.startDate);
        const end = new Date(res.endDate);
        const sMins = start.getHours() * 60 + start.getMinutes();
        const eMins = end.getHours() * 60 + end.getMinutes();
        return timeMins >= sMins && timeMins < eMins;
      });

      if (!isOccupied) {
        times.push(timeStr);
      }

      m += 30;
      if (m === 60) { h++; m = 0; }
    }
    return times;
  }

  static generateEndTimes(startTime: string, occupiedSlots: any[]): string[] {
    const endTimes: string[] = [];
    if (!startTime) return endTimes;

    const [startH, startM] = startTime.split(':').map(Number);
    const startTotalMins = startH * 60 + startM;

    let h = startH;
    let m = startM + 30;
    if (m === 60) { h++; m = 0; }

    while (h < 21 || (h === 21 && m === 0)) {
      const currentTotalMins = h * 60 + m;
      const duration = currentTotalMins - startTotalMins;

      if (duration > 180) break;

      const timeStr = `${this.pad(h)}:${this.pad(m)}`;

      if (this.isOverlap(startTime, timeStr, occupiedSlots)) break;

      endTimes.push(timeStr);

      m += 30;
      if (m === 60) { h++; m = 0; }
    }
    return endTimes;
  }
}