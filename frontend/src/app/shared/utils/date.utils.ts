/**
 * Formats a Date to YYYY-MM-DD in the local timezone.
 * Avoids UTC issues with toISOString().split('T')[0].
 */
export function formatDate(date: Date): string {
  return date.toLocaleDateString('en-CA');
}

/**
 * Gets Monday of the week for a given date (local timezone).
 */
export function getMonday(date: Date): Date {
  const dayOfWeek = date.getDay();
  const monday = new Date(date);
  monday.setDate(date.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
  return monday;
}

/**
 * Formats a week range as a human-readable string.
 */
export function formatWeekRange(start: Date): string {
  const end = new Date(start);
  end.setDate(start.getDate() + 6);
  const startStr = start.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
  const endStr = end.toLocaleDateString('ru-RU', { day: 'numeric', month: 'short' });
  return `${startStr} — ${endStr}`;
}
