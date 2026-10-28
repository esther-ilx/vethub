import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { formatVisitDate, groupVisits, localDateKey } from './date-status';

const visit = (id: number, date: string) => ({ id, date, description: `Visit ${id}`, vetName: null });
const today = '2026-10-28';

describe('pet visit dates', () => {
	it('partitions and orders unsorted visits without mutating the response', () => {
		const input = [
			visit(8, '2026-10-29'), visit(5, '2026-10-27'), visit(4, today),
			visit(2, '2026-10-27'), visit(3, today), visit(1, '2026-10-26'),
			visit(7, '2026-10-29'), visit(6, '2026-11-03')
		];
		const original = [...input];
		const groups = groupVisits(input, today);
		assert.deepEqual(groups.todayAndUpcoming.map(({ id }) => id), [3, 4, 7, 8, 6]);
		assert.deepEqual(groups.past.map(({ id }) => id), [2, 5, 1]);
		assert.deepEqual(input, original);
	});

	it('handles empty, future-only and past-only visits and a new date boundary', () => {
		assert.deepEqual(groupVisits([], today), { todayAndUpcoming: [], past: [] });
		const future = visit(1, '2026-10-29');
		const past = visit(2, '2026-10-27');
		assert.deepEqual(groupVisits([future], today), { todayAndUpcoming: [future], past: [] });
		assert.deepEqual(groupVisits([past], today), { todayAndUpcoming: [], past: [past] });
		assert.deepEqual(groupVisits([future], '2026-10-30'), { todayAndUpcoming: [], past: [future] });
	});

	it('preserves calendar dates in the configured timezone', () => {
		assert.equal(formatVisitDate('2026-10-28'), 'October 28, 2026');
		assert.equal(formatVisitDate('2026-01-01'), 'January 1, 2026');
	});

	it('uses local calendar components when UTC and local dates differ', () => {
		if (process.env.TZ === 'America/Los_Angeles') {
			const localToday = localDateKey(new Date('2026-10-28T00:30:00Z'));
			assert.equal(localToday, '2026-10-27');
			assert.deepEqual(groupVisits([visit(1, '2026-10-28')], localToday).todayAndUpcoming.map(({ id }) => id), [1]);
		} else if (process.env.TZ === 'Pacific/Auckland') {
			const localToday = localDateKey(new Date('2026-10-27T12:30:00Z'));
			assert.equal(localToday, '2026-10-28');
			assert.deepEqual(groupVisits([visit(1, '2026-10-27')], localToday).past.map(({ id }) => id), [1]);
		} else {
			const local = new Date(2026, 9, 28, 23, 30);
			assert.equal(localDateKey(local), today);
		}
	});
});
