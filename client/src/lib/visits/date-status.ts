import type { VisitSummaryResponse } from '$lib/api/models';

/** Capture the browser's calendar date without converting it to UTC. */
export function localDateKey(date: Date = new Date()): string {
	const year = String(date.getFullYear()).padStart(4, '0');
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	return `${year}-${month}-${day}`;
}

/** Format an ISO calendar date without shifting it into the browser's timezone. */
export function formatVisitDate(date: string): string {
	return new Date(`${date}T00:00:00Z`).toLocaleDateString('en-US', {
		timeZone: 'UTC',
		year: 'numeric',
		month: 'long',
		day: 'numeric'
	});
}

export function groupVisits(visits: readonly VisitSummaryResponse[], today: string) {
	const todayAndUpcoming: VisitSummaryResponse[] = [];
	const past: VisitSummaryResponse[] = [];

	for (const visit of visits) {
		(visit.date < today ? past : todayAndUpcoming).push(visit);
	}

	const compareDate = (a: VisitSummaryResponse, b: VisitSummaryResponse) =>
		a.date < b.date ? -1 : a.date > b.date ? 1 : 0;
	todayAndUpcoming.sort((a, b) => compareDate(a, b) || a.id - b.id);
	past.sort((a, b) => compareDate(b, a) || a.id - b.id);

	return { todayAndUpcoming, past };
}
